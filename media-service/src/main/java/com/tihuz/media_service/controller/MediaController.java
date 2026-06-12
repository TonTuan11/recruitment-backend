package com.tihuz.media_service.controller;

import com.tihuz.common.dto.ApiResponse;
import com.tihuz.media_service.service.MediaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/media")
@RequiredArgsConstructor
@Slf4j
public class MediaController {

    private final MediaService mediaService;

    @PostMapping("/upload/avatar")
    public ApiResponse<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        log.info("Upload avatar request received");
        String imageUrl = mediaService.uploadImage(file, "avatars");
        return ApiResponse.<String>builder()
                .result(imageUrl)
                .message("Upload ảnh đại diện thành công")
                .build();
    }

    @PostMapping("/upload/company-logo")
    public ApiResponse<String> uploadCompanyLogo(@RequestParam("file") MultipartFile file) {
        log.info("Upload company logo request received");
        String imageUrl = mediaService.uploadImage(file, "company-logos");
        return ApiResponse.<String>builder()
                .result(imageUrl)
                .message("Upload logo công ty thành công")
                .build();
    }

    // Thêm endpoint upload CV
    @PostMapping("/upload/cv")
    public ApiResponse<String> uploadCV(@RequestParam("file") MultipartFile file) {
        log.info("Upload CV request received");
        String fileUrl = mediaService.uploadCV(file, "cvs");
        return ApiResponse.<String>builder()
                .result(fileUrl)
                .message("Upload CV thành công")
                .build();
    }
}