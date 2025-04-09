package com.example.picket.domain.show.repository.querydsl;

import com.example.picket.common.enums.Grade;
import com.example.picket.common.enums.SeatStatus;
import com.example.picket.domain.seat.dto.response.SeatSummaryResponse;
import com.example.picket.domain.show.dto.response.ShowDateDetailResponse;
import com.example.picket.domain.show.entity.ShowDate;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.example.picket.domain.seat.entity.QSeat.seat;
import static com.example.picket.domain.show.entity.QShowDate.showDate;

@RequiredArgsConstructor
public class ShowDateQueryDslImpl implements ShowDateQueryDslRepository {

    private JPAQueryFactory queryFactory;

    @Override
    public List<ShowDateDetailResponse> getShowDateDetailResponseById(Long showId) {

        List<Tuple> tuples = queryFactory
            .select(
                showDate,
                seat.grade,
                seat.seatStatus
            )
            .from(showDate)
            .leftJoin(seat).on(seat.showDate.eq(showDate))
            .where(showDate.show.id.eq(showId))
            .fetch();

        if (tuples.isEmpty()) {
            return List.of();
        }

        Map<Long, List<Tuple>> showDateGroups = tuples.stream()
            .collect(Collectors.groupingBy(tuple -> tuple.get(showDate).getId()));

        return showDateGroups.entrySet().stream()
            .map(entry -> {
                Long dateId = entry.getKey();
                List<Tuple> dateTuples = entry.getValue();
                ShowDate showDateEntity = dateTuples.get(0).get(showDate);

                Map<Grade, Long> seatCountsByGrade = dateTuples.stream()
                    .filter(t -> t.get(seat.grade) != null)
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

                List<SeatSummaryResponse> seatSummaryResponses = seatCountsByGrade.entrySet().stream()
                    .map(gradeEntry -> {
                        Grade grade = gradeEntry.getKey();
                        int total = gradeEntry.getValue().intValue();
                        int reserved = reservedCountsByGrade.getOrDefault(grade, 0L).intValue();
                        int available = total - reserved;
                        return SeatSummaryResponse.toDto(grade, total, reserved, available);
                    })
                    .toList();

                return ShowDateDetailResponse.toDto(
                    showDateEntity.getId(),
                    showDateEntity.getDate(),
                    showDateEntity.getStartTime(),
                    showDateEntity.getEndTime(),
                    showDateEntity.getTotalSeatCount(),
                    showDateEntity.getReservedSeatCount(),
                    showDateEntity.getAvailableSeatCount(),
                    seatSummaryResponses
                );
            })
            .toList();
    }
}
