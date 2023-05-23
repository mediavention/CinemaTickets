package uk.gov.dwp.uc.pairtest;

import uk.gov.dwp.uc.pairtest.domain.TicketPurchaseRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

public interface TicketService {

    void purchaseTickets(TicketPurchaseRequest ticketPurchaseRequest) throws InvalidPurchaseException;

}
