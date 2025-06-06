package com.example.picket.domain.user.service;

import com.example.picket.common.dto.AuthUser;
import com.example.picket.common.exception.CustomException;
import com.example.picket.common.service.S3Service;
import com.example.picket.config.PasswordEncoder;
import com.example.picket.domain.images.dto.response.ImageResponse;
import com.example.picket.domain.images.entity.UserImage;
import com.example.picket.domain.images.repository.UserImageRepository;
import com.example.picket.domain.user.dto.request.UpdatePasswordRequest;
import com.example.picket.domain.user.dto.request.UpdateUserRequest;
import com.example.picket.domain.user.dto.request.WithdrawUserRequest;
import com.example.picket.domain.user.entity.User;
import com.example.picket.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Service
@RequiredArgsConstructor
@Transactional
public class UserCommandService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final S3Service s3Service;
    private final UserImageRepository userImageRepository;

    public User updateUser(AuthUser authUser, UpdateUserRequest request) {
        User user = userRepository.findById(authUser.getId()).orElseThrow(
                () -> new CustomException(NOT_FOUND, "해당 유저를 찾을 수 없습니다.")
        );

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new CustomException(UNAUTHORIZED, "비밀번호가 일치하지 않습니다.");
        }

        UserImage userImage = userImageRepository.findByImageUrl(request.getProfileUrl())
                .orElseThrow(() -> new CustomException(NOT_FOUND, "해당 이미지를 찾을 수 없습니다. 다시 업로드 시도를 해주세요."));
        userImage.updateUser(user.getId());

        if (user.getProfileUrl() != null) {
            deleteUserImage(user.getProfileUrl());
        }

        user.update(request.getNickname(), request.getProfileUrl());

        return user;
    }

    public void updatePassword(AuthUser authUser, UpdatePasswordRequest request) {
        User user = userRepository.findById(authUser.getId()).orElseThrow(
                () -> new CustomException(NOT_FOUND, "해당 유저를 찾을 수 없습니다.")
        );

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new CustomException(UNAUTHORIZED, "비밀번호가 일치하지 않습니다.");
        }

        String encodePassword = passwordEncoder.encode(request.getNewPassword());

        user.passwordUpdate(encodePassword);
    }

    public void withdrawUserRequest(AuthUser authUser, WithdrawUserRequest request) {
        User user = userRepository.findById(authUser.getId()).orElseThrow(
                () -> new CustomException(NOT_FOUND, "해당 유저를 찾을 수 없습니다.")
        );

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new CustomException(UNAUTHORIZED, "비밀번호가 일치하지 않습니다.");
        }

        deleteUserImage(user.getProfileUrl());
        userRepository.delete(user);
    }

    public String uploadImage(MultipartFile multipartFile) {
        ImageResponse imageResponse = s3Service.upload(multipartFile);
        UserImage userImage = UserImage.create(imageResponse, null);
        userImageRepository.save(userImage);
        return imageResponse.getImageUrl();
    }

    public void deleteUserImage(String imageUrl) {
        UserImage userImage = userImageRepository.findByImageUrl(imageUrl)
                .orElseThrow(() -> new CustomException(NOT_FOUND, "해당 이미지 파일을 찾을 수 없습니다."));
        userImage.updateDeletedAt(LocalDateTime.now());
    }
}
