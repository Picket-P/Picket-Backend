package com.example.picket.domain.show.repository.querydsl;

import com.example.picket.common.enums.Grade;
import com.example.picket.common.enums.SeatStatus;
import com.example.picket.domain.seat.dto.response.SeatSummaryResponse;
import com.example.picket.domain.show.dto.response.ShowDateDetailResponse;
import com.example.picket.domain.show.dto.response.ShowDetailResponse;
import com.example.picket.domain.show.entity.Show;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.example.picket.domain.seat.entity.QSeat.seat;
import static com.example.picket.domain.show.entity.QShow.show;
import static com.example.picket.domain.show.entity.QShowDate.showDate;

@Slf4j
@RequiredArgsConstructor
public class ShowQueryDslImpl implements ShowQueryDslRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<ShowDetailResponse> getShowDetailResponseById(Long showId) {

        List<Tuple> tuples = queryFactory
            .select(
                show,
                showDate.id,
                showDate.date,
                showDate.startTime,
                showDate.endTime,
                showDate.totalSeatCount,
                showDate.reservedSeatCount,
                showDate.availableSeatCount,
                seat.grade,
                seat.seatStatus
            )
            .from(show)
            .leftJoin(showDate).on(showDate.show.eq(show))
            .leftJoin(seat).on(seat.showDate.eq(showDate))
            .where(show.id.eq(showId))
            .fetch();

        if (tuples.isEmpty()) {
            return Optional.empty();
        }

        // Show 정보는 첫 번째 튜플에서 추출
        Show showEntity = tuples.get(0).get(show);
        ShowDetailResponse detailResponse = ShowDetailResponse.toDto(
            showEntity.getId(),
            showEntity.getDirectorId(),
            showEntity.getTitle(),
            showEntity.getPosterUrl(),
            showEntity.getCategory(),
            showEntity.getDescription(),
            showEntity.getLocation(),
            showEntity.getReservationStart(),
            showEntity.getReservationEnd(),
            showEntity.getTicketsLimitPerUser(),
            new ArrayList<>(),
            showEntity.getCreatedAt(),
            showEntity.getModifiedAt()
        );

        // ShowDate별 그룹핑 및 처리
        Map<Long, List<Tuple>> showDateGroups = tuples.stream()
            .filter(tuple -> tuple.get(showDate.id) != null)
            .collect(Collectors.groupingBy(tuple -> tuple.get(showDate.id)));

        List<ShowDateDetailResponse> showDateDetails = showDateGroups.entrySet().stream()
            .map(entry -> {
                Long dateId = entry.getKey();
                List<Tuple> dateTuples = entry.getValue();
                Tuple firstTuple = dateTuples.get(0);

                // ShowDate 정보 추출
                LocalDate date = firstTuple.get(showDate.date);
                LocalTime startTime = firstTuple.get(showDate.startTime);
                LocalTime endTime = firstTuple.get(showDate.endTime);
                Integer totalSeatCount = firstTuple.get(showDate.totalSeatCount);
                Integer reservedSeatCount = firstTuple.get(showDate.reservedSeatCount);
                Integer availableSeatCount = firstTuple.get(showDate.availableSeatCount);

                // Seat 등급별 그룹핑 및 요약
                Map<Grade, Long> seatCountsByGrade = dateTuples.stream()
                    .filter(tuple -> tuple.get(seat.grade) != null)
                    .collect(Collectors.groupingBy(
                        tuple -> tuple.get(seat.grade),
                        Collectors.counting()
                    ));

                Map<Grade, Long> reservedCountsByGrade = dateTuples.stream()
                    .filter(tuple -> tuple.get(seat.grade) != null && tuple.get(seat.seatStatus) == SeatStatus.RESERVED)
                    .collect(Collectors.groupingBy(
                        tuple -> tuple.get(seat.grade),
                        Collectors.counting()
                    ));

                List<SeatSummaryResponse> seatSummaries = seatCountsByGrade.entrySet().stream()
                    .map(gradeEntry -> {
                        Grade grade = gradeEntry.getKey();
                        int total = gradeEntry.getValue().intValue();
                        int reserved = reservedCountsByGrade.getOrDefault(grade, 0L).intValue();
                        int available = total - reserved;
                        return SeatSummaryResponse.toDto(grade, total, reserved, available);
                    })
                    .collect(Collectors.toList());

                // ShowDateDetailResponse 생성
                return ShowDateDetailResponse.toDto(
                    dateId,
                    date,
                    startTime,
                    endTime,
                    totalSeatCount,
                    reservedSeatCount,
                    availableSeatCount,
                    seatSummaries
                );
            })
            .toList();

        detailResponse.getShowDates().addAll(showDateDetails);
        return Optional.of(detailResponse);
    }
}
