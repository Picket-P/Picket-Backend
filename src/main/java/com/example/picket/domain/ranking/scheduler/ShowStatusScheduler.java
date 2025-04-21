package com.example.picket.domain.ranking.scheduler;

import com.example.picket.common.enums.ShowStatus;
import com.example.picket.domain.show.entity.Show;
import com.example.picket.domain.show.entity.ShowDate;
import com.example.picket.domain.show.repository.ShowDateRepository;
import com.example.picket.domain.show.repository.ShowRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShowStatusScheduler {

    private final ShowRepository showRepository;
    private final ShowDateRepository showDateRepository;
    private final HotShowRankingScheduler hotShowRankingScheduler;
    private final LikeRankingScheduler likeRankingScheduler;
    private final TicketExpiryScheduler ticketExpiryScheduler;

    @Scheduled(cron = "0 */5 * * * ?") // 5분마다
    @SchedulerLock(name = "show_status", lockAtMostFor = "PT5M", lockAtLeastFor = "PT10S")
    @Transactional
    public void updateShowStatuses() {
        try {
            log.info("공연 상태 업데이트 시작");
            LocalDateTime now = LocalDateTime.now();
            List<ShowDate> showDates = showDateRepository.findAllActiveShowDates(ShowStatus.FINISHED);
            log.info("활성 공연 날짜 {}개 조회", showDates.size());

            // Show ID별 ShowDate 그룹핑
            Map<Long, List<ShowDate>> showDateMap = showDates.stream()
                    .collect(Collectors.groupingBy(showDate -> showDate.getShow().getId()));

            List<Show> shows = showDateMap.keySet().stream()
                    .map(showRepository::findById)
                    .filter(show -> show.isPresent())
                    .map(show -> show.get())
                    .toList();

            List<Show> newlyFinishedShows = new ArrayList<>();
            shows.forEach(show -> {
                if (updateShowStatus(show, showDateMap.getOrDefault(show.getId(), List.of()), now)) {
                    newlyFinishedShows.add(show);
                }
            });

            log.info("공연 상태 업데이트 완료: {}개 공연이 FINISHED로 변경됨", newlyFinishedShows.size());
            hotShowRankingScheduler.updateHotShowRanking();
            likeRankingScheduler.updateLikeRanking();
            if (!newlyFinishedShows.isEmpty()) {
                ticketExpiryScheduler.expireTicketsForShows(newlyFinishedShows);
            }
            log.info("랭킹 캐시 및 티켓 만료 처리 완료");
        } catch (Exception e) {
            log.error("공연 상태 업데이트 실패", e);
        }
    }

    private boolean updateShowStatus(Show show, List<ShowDate> showDates, LocalDateTime now) {
        ShowStatus oldStatus = show.getStatus();
        ShowStatus newStatus = calculateShowStatus(show, showDates, now);

        if (oldStatus != newStatus) {
            show.updateStatus(newStatus);
            log.info("공연 ID: {}, 제목: {}, 상태 변경: {} -> {}",
                    show.getId(), show.getTitle(), oldStatus, newStatus);
            return newStatus == ShowStatus.FINISHED;
        }
        return false;
    }

    private ShowStatus calculateShowStatus(Show show, List<ShowDate> showDates, LocalDateTime now) {
        if (now.isBefore(show.getReservationStart())) {
            log.debug("공연 ID: {}, 예매 시작 전: {}", show.getId(), show.getReservationStart());
            return ShowStatus.RESERVATION_PENDING;
        }
        if (now.isEqual(show.getReservationStart()) ||
                (now.isAfter(show.getReservationStart()) && now.isBefore(show.getReservationEnd()))) {
            log.debug("공연 ID: {}, 예매 진행 중: {} ~ {}", show.getId(), show.getReservationStart(), show.getReservationEnd());
            return ShowStatus.RESERVATION_ONGOING;
        }
        if (showDates.isEmpty()) {
            log.debug("공연 ID: {}, 공연 날짜 없음", show.getId());
            return ShowStatus.RESERVATION_CLOSED;
        }
        boolean isPerformanceOngoing = showDates.stream().anyMatch(showDate -> {
            LocalDateTime startDateTime = showDate.getDate().atTime(showDate.getStartTime());
            LocalDateTime endDateTime = showDate.getDate().atTime(showDate.getEndTime());
            boolean isOngoing = (now.isEqual(startDateTime) || now.isAfter(startDateTime)) &&
                    (now.isBefore(endDateTime) || now.isEqual(endDateTime));
            log.debug("공연 ID: {}, 날짜: {}, 시작: {}, 종료: {}, 진행 중: {}",
                    show.getId(), showDate.getDate(), startDateTime, endDateTime, isOngoing);
            return isOngoing;
        });
        if (isPerformanceOngoing) {
            return ShowStatus.PERFORMANCE_ONGOING;
        }
        boolean isAllFinished = showDates.stream().allMatch(showDate -> {
            LocalDateTime endDateTime = showDate.getDate().atTime(showDate.getEndTime());
            boolean isFinished = now.isAfter(endDateTime) || now.isEqual(endDateTime);
            log.debug("공연 ID: {}, 날짜: {}, 종료: {}, 종료 여부: {}",
                    show.getId(), showDate.getDate(), endDateTime, isFinished);
            return isFinished;
        });
        if (isAllFinished) {
            return ShowStatus.FINISHED;
        }
        log.debug("공연 ID: {}, 예매 마감", show.getId());
        return ShowStatus.RESERVATION_CLOSED;
    }
}