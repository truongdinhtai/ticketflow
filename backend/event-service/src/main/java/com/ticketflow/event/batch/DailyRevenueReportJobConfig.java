package com.ticketflow.event.batch;

import com.ticketflow.event.domain.DailyEventSalesReport;
import com.ticketflow.event.repository.DailyEventSalesReportRepository;
import jakarta.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

/**
 * Spring Batch job that rolls up a day's ticket sales into the
 * {@code daily_event_sales_report} table.
 *
 * <p>The job has two steps:
 * <ol>
 *   <li><b>purgeStep</b> — deletes any existing rows for the target day, so the
 *       job is idempotent and safe to re-run.</li>
 *   <li><b>aggregateStep</b> — a chunk-oriented step that reads per-event
 *       aggregates from {@code ticket_sales}, maps each to a report row, and
 *       writes them via JPA.</li>
 * </ol>
 *
 * <p>The target day comes from the {@code reportDate} job parameter (ISO date),
 * which makes every step {@link StepScope}-bound and late-bound at run time.
 */
@Configuration
public class DailyRevenueReportJobConfig {

    private static final Logger log = LoggerFactory.getLogger(DailyRevenueReportJobConfig.class);

    public static final String JOB_NAME = "dailyRevenueReportJob";

    private static final String AGGREGATE_SQL = """
            SELECT ts.event_id      AS event_id,
                   e.name           AS event_name,
                   SUM(ts.quantity) AS tickets_sold,
                   SUM(ts.total_amount) AS revenue
              FROM ticket_sales ts
              JOIN events e ON e.id = ts.event_id
             WHERE ts.sold_at >= ? AND ts.sold_at < ?
             GROUP BY ts.event_id, e.name
            """;

    @Bean
    public Job dailyRevenueReportJob(JobRepository jobRepository, Step purgeStep, Step aggregateStep) {
        return new JobBuilder(JOB_NAME, jobRepository)
                .start(purgeStep)
                .next(aggregateStep)
                .build();
    }

    // ----- Step 1: purge (idempotency) -----

    @Bean
    public Step purgeStep(JobRepository jobRepository, PlatformTransactionManager txManager,
                          Tasklet purgeTasklet) {
        return new StepBuilder("purgeStep", jobRepository)
                .tasklet(purgeTasklet, txManager)
                .build();
    }

    @Bean
    @StepScope
    public Tasklet purgeTasklet(@Value("#{jobParameters['reportDate']}") String reportDate,
                                DailyEventSalesReportRepository reportRepository) {
        return (contribution, chunkContext) -> {
            LocalDate day = LocalDate.parse(reportDate);
            reportRepository.deleteByReportDate(day);
            log.info("Purged existing daily report rows for {}", day);
            return RepeatStatus.FINISHED;
        };
    }

    // ----- Step 2: aggregate (chunk: reader -> processor -> writer) -----

    @Bean
    public Step aggregateStep(JobRepository jobRepository, PlatformTransactionManager txManager,
                              JdbcCursorItemReader<DailyEventAggregate> reader,
                              DailyReportProcessor processor,
                              JpaItemWriter<DailyEventSalesReport> writer) {
        return new StepBuilder("aggregateStep", jobRepository)
                .<DailyEventAggregate, DailyEventSalesReport>chunk(100, txManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    @Bean
    @StepScope
    public JdbcCursorItemReader<DailyEventAggregate> reader(DataSource dataSource,
                                                            @Value("#{jobParameters['reportDate']}") String reportDate) {
        LocalDate day = LocalDate.parse(reportDate);
        Instant startOfDay = day.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant endOfDay = day.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();

        return new JdbcCursorItemReaderBuilder<DailyEventAggregate>()
                .name("dailySalesReader")
                .dataSource(dataSource)
                .sql(AGGREGATE_SQL)
                .preparedStatementSetter(ps -> {
                    ps.setTimestamp(1, Timestamp.from(startOfDay));
                    ps.setTimestamp(2, Timestamp.from(endOfDay));
                })
                .rowMapper((rs, rowNum) -> new DailyEventAggregate(
                        rs.getLong("event_id"),
                        rs.getString("event_name"),
                        rs.getLong("tickets_sold"),
                        rs.getBigDecimal("revenue")))
                .build();
    }

    @Bean
    @StepScope
    public DailyReportProcessor processor(@Value("#{jobParameters['reportDate']}") String reportDate) {
        return new DailyReportProcessor(LocalDate.parse(reportDate));
    }

    @Bean
    public JpaItemWriter<DailyEventSalesReport> writer(EntityManagerFactory entityManagerFactory) {
        return new JpaItemWriterBuilder<DailyEventSalesReport>()
                .entityManagerFactory(entityManagerFactory)
                .build();
    }
}
