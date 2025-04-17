package com.example.picket.domain.comment.service;

import com.example.picket.common.enums.*;
import com.example.picket.domain.comment.entity.Comment;
import com.example.picket.domain.comment.repository.CommentRepository;
import com.example.picket.domain.seat.entity.Seat;
import com.example.picket.domain.show.entity.Show;
import com.example.picket.domain.show.entity.ShowDate;
import com.example.picket.domain.show.repository.ShowRepository;
import com.example.picket.domain.user.entity.User;
import com.example.picket.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
class CommentQueryServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private ShowRepository showRepository;

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private CommentQueryService commentQueryService;

    @Test
    void 댓글_다건_조회_성공(){
        // given
        Long userId1 = 1L;
        User user1 = createUser(userId1);

        Long userId2 = 2L;
        User user2 = createUser(userId2);

        Long showId = 1L;
        Show show = createShow(user1, showId);

        Comment comment1 = Comment.toEntity("댓글내용1", show, user1);
        ReflectionTestUtils.setField(comment1, "id", 1L);

        Comment comment2 = Comment.toEntity("댓글내용2", show, user1);
        ReflectionTestUtils.setField(comment2, "id", 2L);
        List<Comment> commentList = Arrays.asList(comment1, comment2);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Comment> page = new PageImpl<>(commentList, pageable, commentList.size());

        given(commentRepository.findByShowIdAndDeletedAtIsNull(showId, pageable)).willReturn(page);

        // when
        Page<Comment> result = commentQueryService.getComments(showId, pageable);

        // then
        assertEquals(2, result.getTotalElements());
        assertEquals("댓글내용1", result.getContent().get(0).getContent());
        assertEquals("댓글내용2", result.getContent().get(1).getContent());
        verify(commentRepository).findByShowIdAndDeletedAtIsNull(showId, pageable);
    }

    private User createUser(Long userId) {
        User user = User.toEntity("user@example.com"
                ,"test123!"
                , UserRole.USER
                ,null
                ,"닉네임"
                , LocalDate.parse("1990-06-25")
                , Gender.FEMALE);

        ReflectionTestUtils.setField(user, "id", userId);

        return user;
    }

    private Show createShow(User user, Long showId) {
        Show show = Show.toEntity(user.getId()
                ,"제목"
                , "포스터url"
                , Category.CLASSIC
                , "description"
                , "서울"
                , LocalDateTime.now().plusMinutes(1L)
                , LocalDateTime.now().plusDays(1)
                , 1
        );

        ReflectionTestUtils.setField(show, "id", showId);

        createShowDate(show, 1L);
        return show;
    }

    private ShowDate createShowDate(Show show, Long showDateId) {
        ShowDate showDate = ShowDate.toEntity(LocalDate.now()
                , LocalTime.now()
                , LocalTime.now().plusHours(1)
                , 10
                , 10
                , show);

        ReflectionTestUtils.setField(showDate, "id", showDateId);
        createSeat(showDate, 1L);
        return showDate;
    }

    private Seat createSeat(ShowDate showDate, Long seatId) {
        Seat seat = Seat.toEntity(Grade.A
                , 1
                , new BigDecimal(100000)
                , showDate);

        ReflectionTestUtils.setField(seat, "id", seatId);
        return seat;
    }
}