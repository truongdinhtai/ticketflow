package com.ticketflow.event.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * A record of tickets sold for an event, written whenever Booking Service
 * successfully reserves tickets through the REST API.
 *
 * <p>Keeping sales here (rather than querying Booking Service's database) lets
 * the daily revenue batch job aggregate purely from this service's own data,
 * respecting the database-per-service principle.
 */
@Entity
@Table(name = "ticket_sales")
public class TicketSale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false)
    private Long eventId;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "total_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal totalAmount;

    @CreationTimestamp
    @Column(name = "sold_at", nullable = false, updatable = false)
    private Instant soldAt;

    protected TicketSale() {
        // for JPA
    }

    public TicketSale(Long eventId, int quantity, BigDecimal unitPrice, BigDecimal totalAmount) {
        this.eventId = eventId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalAmount = totalAmount;
    }

    public Long getId() {
        return id;
    }

    public Long getEventId() {
        return eventId;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public Instant getSoldAt() {
        return soldAt;
    }
}
