package com.example.picket.domain.comment.service;

import com.example.picket.common.enums.Category;
import com.example.picket.common.enums.Gender;
import com.example.picket.common.enums.Grade;
import com.example.picket.common.enums.UserRole;
import com.example.picket.common.exception.CustomException;
import com.example.picket.common.exception.ErrorCode;
import com.example.picket.domain.comment.dto.request.CommentRequest;
import com.example.picket.domain.comment.entity.Comment;
import com.example.picket.domain.comment.repository.CommentRepository;
import com.example.picket.domain.seat.entity.Seat;
import com.example.picket.domain.show.entity.Show;
import com.example.picket.domain.show.entity.ShowDate;
import com.example.picket.domain.show.service.ShowQueryService;
import com.example.picket.domain.user.entity.User;
import com.example.picket.domain.user.service.UserQueryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CommentCommandServiceTest {

    @Mock
    private UserQueryService userQueryService;

    @Mock
    private ShowQueryService showQueryService;

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private CommentCommandService commentCommandService;


    @Test
    void 댓글생성시_공연이_존재하지_않을_경우_오류_발생(){
        // given
        Long userId = 1L;
        User user = createUser(userId);

        Long showId = 1L;
        Show show = createShow(user, showId);
        given(showQueryService.getShow(anyLong())).willThrow(new CustomException(ErrorCode.SHOW_NOT_FOUND));

        CommentRequest commentRequest = new CommentRequest("댓글내용");

        // when
        CustomException exception = assertThrows(CustomException.class, () ->
                commentCommandService.createComment(userId, showId, commentRequest)
        );

        // then
        assertEquals("해당 공연을 찾을 수 없습니다.", exception.getMessage());
    }

    @Test
    void 댓글_생성_완료(){
        // given
        Long userId = 1L;
        User user = createUser(userId);
        given(userQueryService.findById(anyLong())).willReturn(user);

        Long showId = 1L;
        Show show = createShow(user, showId);
        given(showQueryService.getShow(anyLong())).willReturn(show);

        CommentRequest commentRequest = new CommentRequest("댓글내용");

        given(commentRepository.save(any(Comment.class))).willAnswer(invocation -> {
            Comment c = invocation.getArgument(0);
            ReflectionTestUtils.setField(c, "id", 1L);
            return c;
        });

        // when
        Comment result = commentCommandService.createComment(userId, showId, commentRequest);

        // then
        assertEquals(1L, result.getId());
        assertEquals(commentRequest.getContent(), result.getContent());
    }

    @Test
    void 댓글_수정시_유저정보가_일치하지_않을경우_오류발생(){
        // given
        Long writeId = 1L;
        User WriteUser = createUser(writeId);

        Long modifiedUserId = 2L;
        User modifiedUser = createUser(modifiedUserId);

        Long showId = 1L;
        Show show = createShow(WriteUser, showId);

        Long commentId = 1L;
        CommentRequest commentRequest = new CommentRequest("댓글내용");
        Comment comment = Comment.toEntity(commentRequest.getContent(), show, WriteUser);
        ReflectionTestUtils.setField(comment, "id", commentId);

        // when
        CustomException exception = assertThrows(CustomException.class, () ->
                commentCommandService.updateComment(modifiedUserId, showId, commentId, commentRequest)
        );

        // then
        assertEquals("해당 공연에 해당 사용자의 댓글이 없습니다.", exception.getMessage());
    }

    @Test
    void 댓글_수정_완료(){
        // given
        Long userId = 1L;
        User user = createUser(userId);

        Long showId = 1L;
        Show show = createShow(user, showId);

        Long commentId = 1L;
        CommentRequest commentRequest = new CommentRequest("댓글내용");
        Comment comment = Comment.toEntity(commentRequest.getContent(), show, user);
        ReflectionTestUtils.setField(comment, "id", commentId);

        given(commentRepository.findByIdAndShowIdAndUserId(anyLong(), anyLong(), anyLong())).willReturn(Optional.of(comment));

        // when
        Comment result = commentCommandService.updateComment(userId, showId, commentId, commentRequest);

        // then
        assertEquals(comment.getId(), result.getId());
        assertEquals(comment.getContent(), result.getContent());
    }

    @Test
    void 댓글_삭제_권한이_없는_경우_오류_발생(){
        // given
        Long writeId = 1L;
        User WriteUser = createUser(writeId);

        Long deletedUserId = 2L;
        User deletedUser = createUser(deletedUserId);

        Long showId = 1L;
        Show show = createShow(WriteUser, showId);

        Long commentId = 1L;
        CommentRequest commentRequest = new CommentRequest("댓글내용");
        Comment comment = Comment.toEntity(commentRequest.getContent(), show, WriteUser);
        ReflectionTestUtils.setField(comment, "id", commentId);

        given(commentRepository.findByIdAndShowId(anyLong(), anyLong())).willReturn(Optional.of(comment));

        // when
        CustomException exception = assertThrows(CustomException.class, () ->
                commentCommandService.deleteComment(deletedUserId, showId, commentId)
        );

        // then
        assertEquals("해당 댓글의 삭제 권한이 없습니다.", exception.getMessage());
    }

    @Test
    void 댓글_작성자가_삭제_성공(){
        // given
        Long userId = 1L;
        User user = createUser(userId);

        Long showId = 1L;
        Show show = createShow(user, showId);

        Long commentId = 1L;
        CommentRequest commentRequest = new CommentRequest("댓글내용");
        Comment comment = Comment.toEntity(commentRequest.getContent(), show, user);
        ReflectionTestUtils.setField(comment, "id", commentId);

        given(commentRepository.findByIdAndShowId(anyLong(), anyLong())).willReturn(Optional.of(comment));

        // when
        commentCommandService.deleteComment(userId, showId, commentId);

        // then
        assertNotNull(comment.getDeletedAt());
    }

    @Test
    void 공연_생성자가_삭제_성공(){
        // given
        Long userId = 1L;
        User user = createUser(userId);

        Long directorId = 2L;
        User director = createDirectorUser(directorId);

        Long showId = 1L;
        Show show = createShow(director, showId);

        Long commentId = 1L;
        CommentRequest commentRequest = new CommentRequest("댓글내용");
        Comment comment = Comment.toEntity(commentRequest.getContent(), show, user);
        ReflectionTestUtils.setField(comment, "id", commentId);

        given(commentRepository.findByIdAndShowId(anyLong(), anyLong())).willReturn(Optional.of(comment));
        // when
        commentCommandService.deleteComment(directorId, showId, commentId);

        // then
        assertNotNull(comment.getDeletedAt());
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

    private User createDirectorUser(Long userId) {
        User user = User.toEntity("director@example.com"
                ,"test123!"
                , UserRole.DIRECTOR
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
                , 1);

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