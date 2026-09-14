package com.ticketflow.event.repository;

import com.ticketflow.event.domain.TicketSale;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketSaleRepository extends JpaRepository<TicketSale, Long> {
}
