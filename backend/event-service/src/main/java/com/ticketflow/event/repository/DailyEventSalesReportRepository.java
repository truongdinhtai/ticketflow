package com.ticketflow.event.repository;

import com.ticketflow.event.domain.DailyEventSalesReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

public interface DailyEventSalesReportRepository extends JpaRepository<DailyEventSalesReport, Long> {

    List<DailyEventSalesReport> findByReportDateOrderByRevenueDesc(LocalDate reportDate);

    /** Used by the batch job to make a re-run for the same day idempotent. */
    @Transactional
    void deleteByReportDate(LocalDate reportDate);
}
