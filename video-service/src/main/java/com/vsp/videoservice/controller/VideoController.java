package com.vsp.videoservice.controller;

import com.vsp.videoservice.service.VideoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/videos")
@Slf4j
@RequiredArgsConstructor
public class VideoController {

    private final VideoService videoService;

    @PostMapping("/upload/{movieId}")
    public ResponseEntity<?> uploadVideo(
            @PathVariable String movieId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "thumbnail", required = false) MultipartFile thumbnailFile
    ) throws IOException {

        log.info("Video upload request for movie : {} , file size MB : {}", movieId, file.getSize() / (1024 * 1024));

        if (thumbnailFile != null && !thumbnailFile.isEmpty()) {
            log.info("Custom thumbnail provided for movie : {}, size KB : {}", movieId, thumbnailFile.getSize() / 1024);
        } else {
            log.info("No custom thumbnail provided for movie : {}. Will auto-generate from video.", movieId);
        }

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Video file is empty");
        }

        // Passes both files to the updated VideoService we created earlier
        String videoKey = videoService.uploadVideo(movieId, file, thumbnailFile);

        return ResponseEntity.ok("Video uploaded successfully. Key: " + videoKey +
                " - Encoding started automatically by Kafka");
    }
}