package uk.gov.dwp.uc.pairtest;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketPurchaseRequest;
import uk.gov.dwp.uc.pairtest.domain.TicketRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit Test, for TicketService
 */
public class TestTicketServiceImpl {
    private final SeatReservationService reservationService = Mockito.mock(SeatReservationService.class);
    private final TicketPaymentService paymentService = Mockito.mock(TicketPaymentService.class);
    private final TicketServiceImpl ticketService = new TicketServiceImpl(reservationService, paymentService);

    /**
     * Unit Test, Valid purchase
     * If a valid purchase failed then check exception for reason why
     */
    @Test
    public void testValidPurchase () {
        List<TicketRequest> ticketRequestList = new ArrayList<>();
        ticketRequestList.add(new TicketRequest(TicketRequest.Type.ADULT, 10));
        ticketRequestList.add(new TicketRequest(TicketRequest.Type.CHILD, 10));
        ticketRequestList.add(new TicketRequest(TicketRequest.Type.INFANT, 10));
        TicketPurchaseRequest ticketPurchase =
                new TicketPurchaseRequest(111, ticketRequestList.toArray(new TicketRequest[0]));
        try {
            ticketService.purchaseTickets(ticketPurchase);
        } catch (Exception ex) {
            fail(String.format("Should be OK, error %s", ex.getMessage()));
        }
    }

    /**
     * Unit Test, confirm maximum of 20 tickets
     * If fails then the maximum number of tickets calculate is in error, or the validation of the total number
     */
    @Test
    public void testOverMaxTickets () {
        List<TicketRequest> ticketRequestList = new ArrayList<>();
        ticketRequestList.add(new TicketRequest(TicketRequest.Type.ADULT, 10));
        ticketRequestList.add(new TicketRequest(TicketRequest.Type.CHILD, 11));
        ticketRequestList.add(new TicketRequest(TicketRequest.Type.INFANT, 10));
        TicketPurchaseRequest ticketPurchase =
                new TicketPurchaseRequest(111, ticketRequestList.toArray(new TicketRequest[0]));
        Throwable exception = assertThrows(InvalidPurchaseException.class, () -> ticketService.purchaseTickets(ticketPurchase));
        assertEquals(String.format("Total tickets exceeded %d", TicketServiceImpl.MAX_TICKETS), exception.getMessage());
    }

    /**
     * Unit Test, confirm one adult in the purchase
     * If fails then the rule validation that at least one adult is required is not handled correctly
     */
    @Test
    public void testNoAdults () {
        List<TicketRequest> ticketRequestList = new ArrayList<>();
        ticketRequestList.add(new TicketRequest(TicketRequest.Type.CHILD, 11));
        ticketRequestList.add(new TicketRequest(TicketRequest.Type.INFANT, 10));
        TicketPurchaseRequest ticketPurchase =
                new TicketPurchaseRequest(111, ticketRequestList.toArray(new TicketRequest[0]));
        Throwable exception = assertThrows(InvalidPurchaseException.class, () -> ticketService.purchaseTickets(ticketPurchase));
        assertEquals("At least one adult is required when purchasing tickets", exception.getMessage());
    }

    /**
     * Unit Test, confirm account number
     * if fails then account number validation is not handling the business rule correctly
     */
    @Test
    public void testAccountNumber () {
        List<TicketRequest> ticketRequestList = new ArrayList<>();
        ticketRequestList.add(new TicketRequest(TicketRequest.Type.ADULT, 10));
        ticketRequestList.add(new TicketRequest(TicketRequest.Type.INFANT, 10));
        TicketPurchaseRequest ticketPurchase =
                new TicketPurchaseRequest(0, ticketRequestList.toArray(new TicketRequest[0]));
        Throwable exception = assertThrows(InvalidPurchaseException.class, () -> ticketService.purchaseTickets(ticketPurchase));
        assertEquals("Invalid account id of 0", exception.getMessage());
    }

    /**
     * Unit Test, confirm total seat count
     * If fails then the total seat count calculation is not working correctly
     */
    @Test
    public void testTotalSeatCount () {
        List<TicketRequest> ticketRequestList = new ArrayList<>();
        ticketRequestList.add(new TicketRequest(TicketRequest.Type.ADULT, 1));
        ticketRequestList.add(new TicketRequest(TicketRequest.Type.CHILD, 5));
        ticketRequestList.add(new TicketRequest(TicketRequest.Type.INFANT, 10));
        TicketPurchaseRequest ticketPurchase =
                new TicketPurchaseRequest(0, ticketRequestList.toArray(new TicketRequest[0]));
        ticketService.calculateTotalTickets(ticketPurchase);
        assertEquals((1 + 5),ticketService.calculateTotalTickets(ticketPurchase));
    }

    /**
     * Unit Test, confirm total purchase fee
     * If fails that the purchase fee calculation is not computing the correct amount
     */
    @Test
    public void testTotalFee () {
        List<TicketRequest> ticketRequestList = new ArrayList<>();
        ticketRequestList.add(new TicketRequest(TicketRequest.Type.ADULT, 10));
        ticketRequestList.add(new TicketRequest(TicketRequest.Type.CHILD, 5));
        ticketRequestList.add(new TicketRequest(TicketRequest.Type.INFANT, 2));
        TicketPurchaseRequest ticketPurchase =
                new TicketPurchaseRequest(0, ticketRequestList.toArray(new TicketRequest[0]));
        assertEquals((10 * TicketServiceImpl.ADULT_FEE + 5 * TicketServiceImpl.CHILD_FEE),
                ticketService.calculateTotalPurchase(ticketPurchase));
    }
}
