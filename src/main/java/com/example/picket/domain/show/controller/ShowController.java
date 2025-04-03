package com.example.picket.domain.show.controller;

import com.example.picket.domain.show.dto.ShowCreateRequest;
import com.example.picket.domain.show.dto.ShowResponse;
import com.example.picket.domain.show.service.ShowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ShowController {
    private final ShowService showService;

    @PostMapping("/api/v1/admin/shows")
    public ResponseEntity<ShowResponse> createShow(@Valid @RequestBody ShowCreateRequest request) {
        ShowResponse response = showService.createShow(request);
        return ResponseEntity.ok(response);
    }
}