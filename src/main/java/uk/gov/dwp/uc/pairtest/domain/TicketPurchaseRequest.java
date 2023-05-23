package uk.gov.dwp.uc.pairtest.domain;

/**
 * Is an Immutable Object
 */
public class TicketPurchaseRequest {

    final private long accountId;
    final private TicketRequest[] ticketRequests;

    public TicketPurchaseRequest(final long accountId, final TicketRequest[] ticketRequests) {
        this.accountId = accountId;
        this.ticketRequests = ticketRequests;
    }

    public long getAccountId() {
        return accountId;
    }

    public TicketRequest[] getTicketTypeRequests() {
        return ticketRequests;
    }
}
