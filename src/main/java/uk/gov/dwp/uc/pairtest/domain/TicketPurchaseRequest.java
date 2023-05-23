package uk.gov.dwp.uc.pairtest.domain;

/**
 * Is an Immutable Object
 */
public class TicketPurchaseRequest {

    final private long accountId;
    final private TicketRequest[] ticketRequests;
    final private String discountCode;

    public TicketPurchaseRequest(final long accountId, final TicketRequest[] ticketRequests, final String discountCode) {
        this.accountId = accountId;
        this.ticketRequests = ticketRequests;
        this.discountCode = discountCode;
    }

    public long getAccountId() {
        return accountId;
    }

    public TicketRequest[] getTicketTypeRequests() {
        return ticketRequests;
    }

    public String getDiscountCode() {
        return discountCode;
    }
}
