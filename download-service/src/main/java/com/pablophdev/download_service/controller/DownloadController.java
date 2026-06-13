package com.pablophdev.download_service.controller;

import com.pablophdev.download_service.dto.DownloadRequest;
import com.pablophdev.download_service.service.DownloadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/download")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Permite que tu frontend local se conecte sin problemas de CORS
public class DownloadController {

    private final DownloadService downloadService;

    @PostMapping("/stream")
    public ResponseEntity<String> downloadStream(@RequestBody DownloadRequest request) {
        try {
            String resultMessage = downloadService.downloadVideo(request.url(), request.quality());
            return ResponseEntity.ok(resultMessage);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error en la descarga: " + e.getMessage());
        }
    }
}