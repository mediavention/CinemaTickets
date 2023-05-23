package uk.gov.dwp.uc.pairtest.domain;

/**
 * Is an Immutable Object
 */
public class TicketRequest {

    final private int noOfTickets;
    final private Type type;

    public TicketRequest(final Type type, final int noOfTickets) {
        this.type = type;
        this.noOfTickets = noOfTickets;
    }

    public int getNoOfTickets() {
        return noOfTickets;
    }

    public Type getTicketType() {
        return type;
    }

    public enum Type {
        ADULT, CHILD , INFANT
    }

}
