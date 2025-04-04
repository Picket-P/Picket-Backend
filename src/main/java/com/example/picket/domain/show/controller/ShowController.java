package com.example.picket.domain.show.controller;

import com.example.picket.common.annotation.Auth;
import com.example.picket.common.annotation.AuthPermission;
import com.example.picket.common.dto.AuthUser;
import com.example.picket.common.enums.UserRole;
import com.example.picket.domain.show.dto.ShowCreateRequest;
import com.example.picket.domain.show.dto.ShowResponse;
import com.example.picket.domain.show.service.ShowCommandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ShowController {

    private final ShowCommandService showCommandService;

    @PostMapping("/admin/shows")
    @AuthPermission(role = UserRole.ADMIN)
    public ResponseEntity<ShowResponse> createShow(
            @Auth AuthUser authUser,
            @RequestBody @Valid ShowCreateRequest request
    ) {
        ShowResponse response = showCommandService.createShow(authUser, request);
        return ResponseEntity.ok(response);
    }
}