package com.ticketflow.event.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * One row of the daily sales report: how many tickets an event sold on a given
 * day and the revenue it generated. Produced by the Spring Batch job.
 */
@Entity
@Table(name = "daily_event_sales_report")
public class DailyEventSalesReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "report_date", nullable = false)
    private LocalDate reportDate;

    @Column(name = "event_id", nullable = false)
    private Long eventId;

    @Column(name = "event_name", nullable = false)
    private String eventName;

    @Column(name = "tickets_sold", nullable = false)
    private long ticketsSold;

    @Column(nullable = false, precision = 16, scale = 2)
    private BigDecimal revenue;

    @Column(name = "generated_at", nullable = false)
    private Instant generatedAt;

    protected DailyEventSalesReport() {
        // for JPA
    }

    public DailyEventSalesReport(LocalDate reportDate, Long eventId, String eventName,
                                 long ticketsSold, BigDecimal revenue, Instant generatedAt) {
        this.reportDate = reportDate;
        this.eventId = eventId;
        this.eventName = eventName;
        this.ticketsSold = ticketsSold;
        this.revenue = revenue;
        this.generatedAt = generatedAt;
    }

    public Long getId() {
        return id;
    }

    public LocalDate getReportDate() {
        return reportDate;
    }

    public Long getEventId() {
        return eventId;
    }

    public String getEventName() {
        return eventName;
    }

    public long getTicketsSold() {
        return ticketsSold;
    }

    public BigDecimal getRevenue() {
        return revenue;
    }

    public Instant getGeneratedAt() {
        return generatedAt;
    }
}
