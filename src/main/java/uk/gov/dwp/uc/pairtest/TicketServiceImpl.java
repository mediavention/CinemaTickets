package uk.gov.dwp.uc.pairtest;

import thirdparty.discount.Discount;
import thirdparty.discount.DiscountService;
import thirdparty.discount.exception.InvalidDiscountCodeException;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketPurchaseRequest;
import uk.gov.dwp.uc.pairtest.domain.TicketRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

public class TicketServiceImpl implements TicketService {

    // These constants should be moved to a configuration, ideally read from a repository
    public static final int MAX_TICKETS = 20;
    public static final int INFANT_FEE = 0;
    public static final int CHILD_FEE = 10;
    public static final int ADULT_FEE = 20;

    // Could be Autowired
    private final SeatReservationService reservationService;
    private final TicketPaymentService paymentService;
    private final DiscountService discountService;

    /**
     * Constructor
     * @param reservationService reference to the reservation service
     * @param paymentService reference to the payment service
     */
    public TicketServiceImpl(SeatReservationService reservationService, TicketPaymentService paymentService,
                             DiscountService discountService) {
        this.paymentService = paymentService;
        this.reservationService = reservationService;
        this.discountService = discountService;
    }

    /**
     * Should only have private methods other than the one below.
     */
    @Override
    public void purchaseTickets(TicketPurchaseRequest ticketPurchaseRequest) throws InvalidPurchaseException {

        validateAccountNumber(ticketPurchaseRequest);
        validateNumberOfTicket(ticketPurchaseRequest);
        validateChildWithAdult(ticketPurchaseRequest);

        final int totalTickets = calculateTotalTickets(ticketPurchaseRequest);
        final int totalPurchase = calculateTotalPurchase(ticketPurchaseRequest);

        reservationService.reserveSeat(ticketPurchaseRequest.getAccountId(),totalTickets);
        paymentService.makePayment(ticketPurchaseRequest.getAccountId(), totalPurchase);
    }

    /**
     * Calculate the number of tickets purchase for the reservation system
     * @param ticketPurchaseRequest ticket request transaction
     * @return total number of tickets
     */
    int calculateTotalTickets(final TicketPurchaseRequest ticketPurchaseRequest) {
        int totalTickets = 0;
        for (final TicketRequest singleTicket: ticketPurchaseRequest.getTicketTypeRequests()) {
            switch (singleTicket.getTicketType()) {
                case ADULT, CHILD:
                    totalTickets += singleTicket.getNoOfTickets();
                    break;
                case INFANT:
                    // Do not add these because they do not occupy a seat
                    break;
                default:
            }
        }
        return totalTickets;
    }

    /**
     * Calculate the purchase fee for the payment system
     * @param ticketPurchaseRequest ticket request transaction
     * @return total fee
     */
    int calculateTotalPurchase(final TicketPurchaseRequest ticketPurchaseRequest) {
        int totalFee = 0;
        for (final TicketRequest singleTicket: ticketPurchaseRequest.getTicketTypeRequests()) {
            switch (singleTicket.getTicketType()) {
                case ADULT:
                    totalFee += singleTicket.getNoOfTickets() * ADULT_FEE;
                    break;
                case CHILD:
                    totalFee += singleTicket.getNoOfTickets() * CHILD_FEE;
                    break;
                case INFANT:
                    // Infant fee is zero, but in case this changes then the case is handled
                    totalFee += singleTicket.getNoOfTickets() * INFANT_FEE;
                    break;
                default:
            }
        }
        try {
            if (null != discountService) {
                Discount accountDiscount = discountService.getDiscountPercentage(ticketPurchaseRequest.getAccountId(),
                        ticketPurchaseRequest.getDiscountCode());
                // If the discount is null, then it was not found
                if (null != accountDiscount) {
                    totalFee -= (int) ((double) totalFee * (accountDiscount.percentage() / 100.00));
                }
            }
        } catch (InvalidDiscountCodeException ignored) {
            // TODO: log something here or report error
        }
        // Calculate the discounted amount
        return totalFee;
    }

    /**
     * Validate the account id
     * @param ticketPurchaseRequest ticket purchase transaction
     */
    private void validateAccountNumber(final TicketPurchaseRequest ticketPurchaseRequest) {
        if (ticketPurchaseRequest.getAccountId() <= 0) {
            throw new InvalidPurchaseException(String.format("Invalid account id of %d",
                    ticketPurchaseRequest.getAccountId()));
        }
    }

    /**
     * Validate that at least one adult is in the purchase
     * @param ticketPurchaseRequest ticket request transaction
     */
    private void validateChildWithAdult(final TicketPurchaseRequest ticketPurchaseRequest) {
        boolean noAdult = true;
        for (final TicketRequest singleTicket : ticketPurchaseRequest.getTicketTypeRequests()) {
            if (TicketRequest.Type.ADULT == singleTicket.getTicketType()) {
                noAdult = false;
                break;
            }
        }
        // if no adults then it is an invalid purchase
        if (noAdult && ticketPurchaseRequest.getTicketTypeRequests().length != 0) {
            throw new InvalidPurchaseException("At least one adult is required when purchasing tickets");
        }
    }

    /**
     * Validate the number of tickets
     * @param ticketPurchaseRequest ticket request transaction
     */
    private void validateNumberOfTicket(final TicketPurchaseRequest ticketPurchaseRequest) {
        int totalTickets = 0;
        for (final TicketRequest singleTicket: ticketPurchaseRequest.getTicketTypeRequests()) {
            switch (singleTicket.getTicketType()) {
                case ADULT:
                    totalTickets += singleTicket.getNoOfTickets();
                    break;
                case CHILD:
                    totalTickets += singleTicket.getNoOfTickets();
                    break;
                case INFANT:
                    // No seat for infant
                    break;
                default:
            }
            // do check here, so don't waste time looping if the max has been exceeded
            if (totalTickets > MAX_TICKETS) {
                throw new InvalidPurchaseException(String.format("Total tickets exceeded %d", MAX_TICKETS));
            }
        }
    }
}
