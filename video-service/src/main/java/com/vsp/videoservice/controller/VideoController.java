package com.vsp.videoservice.controller;

import com.vsp.videoservice.service.VideoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("api/v1/videos")
@Slf4j
@RequiredArgsConstructor
public class VideoController {

    private final VideoService videoService;

    @PostMapping("/upload/{movieId}")
    // upload vide file for a movie , accept multipart file upload
    public ResponseEntity uploadVideo (
            @PathVariable String movieId,
            @RequestParam("file") MultipartFile file
            ) throws IOException {
            log.info("Video upload request for movie : {} , file size MB : {}", movieId, file.getSize() / (1024*1024));

            if(file.isEmpty()){
                return ResponseEntity.badRequest().body("File is empty");
            }

            String videoKey = videoService.uploadVideo(movieId,file);

            return ResponseEntity.ok("Video uploaded successfully" + videoKey +
                    " - Encoding started automatically by kafka");

    }
}
