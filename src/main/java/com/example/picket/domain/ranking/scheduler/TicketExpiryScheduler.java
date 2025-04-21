package com.example.picket.domain.ranking.scheduler;

import com.example.picket.common.enums.ShowStatus;
import com.example.picket.common.enums.TicketStatus;
import com.example.picket.domain.show.entity.Show;
import com.example.picket.domain.show.repository.ShowRepository;
import com.example.picket.domain.ticket.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketExpiryScheduler {

    private final ShowRepository showRepository;
    private final TicketRepository ticketRepository;

    @Scheduled(cron = "0 0 1 * * ?") // 매일 새벽 1시에 실행
    @SchedulerLock(name = "ticket_expiry", lockAtMostFor = "PT1H", lockAtLeastFor = "PT10S")
    @Transactional
    public void expireTicketsForFinishedShows() {
        try {
            long startTime = System.currentTimeMillis();
            log.info("티켓 만료 처리 작업 시작");

            // 종료된 공연 목록 조회
            List<Show> finishedShows = showRepository.findByStatus(ShowStatus.FINISHED);
            log.info("종료된 공연 {}개 조회", finishedShows.size());

            int totalUpdatedTickets = processShows(finishedShows);

            long duration = System.currentTimeMillis() - startTime;
            log.info("티켓 만료 처리 작업 완료: 총 {}개 공연, {}개 티켓 처리됨, 소요 시간: {}ms",
                    finishedShows.size(), totalUpdatedTickets, duration);
        } catch (Exception e) {
            log.error("티켓 만료 처리 작업 실패", e);
        }
    }

    @Transactional
    public void expireTicketsForShows(List<Show> shows) {
        try {
            long startTime = System.currentTimeMillis();
            log.info("지정된 공연에 대한 티켓 만료 처리 시작: {}개 공연", shows.size());

            int totalUpdatedTickets = processShows(shows);

            long duration = System.currentTimeMillis() - startTime;
            log.info("지정된 공연 티켓 만료 처리 완료: 총 {}개 공연, {}개 티켓 처리됨, 소요 시간: {}ms",
                    shows.size(), totalUpdatedTickets, duration);
        } catch (Exception e) {
            log.error("지정된 공연 티켓 만료 처리 실패", e);
        }
    }

    private int processShows(List<Show> shows) {
        int totalUpdatedTickets = 0;
        for (Show show : shows) {
            int updatedTickets = ticketRepository.updateTicketStatusByShowId(
                    show.getId(), TicketStatus.TICKET_CREATED, TicketStatus.TICKET_EXPIRED);

            if (updatedTickets > 0) {
                log.info("공연 ID: {}, 제목: {}, 총 {}개 티켓 만료 처리 완료",
                        show.getId(), show.getTitle(), updatedTickets);
            }
            totalUpdatedTickets += updatedTickets;
        }
        return totalUpdatedTickets;
    }
}