package com.pablophdev.download_service.controller;

import com.pablophdev.download_service.dto.DownloadRequest;
import com.pablophdev.download_service.service.DownloadService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/download")
@CrossOrigin(origins = "*", exposedHeaders = "Content-Disposition") // <-- AGREGA ESTA LÍNEA
public class DownloadController {

    private final DownloadService downloadService;

    @Autowired
    public DownloadController(DownloadService downloadService) {
        this.downloadService = downloadService;
    }

    @PostMapping("/stream")
    public ResponseEntity<byte[]> downloadVideoAsFile(@Valid @RequestBody DownloadRequest request) {
        try {
            // 1. Obtener el título original para nombrar el archivo de descarga
            String title = downloadService.getVideoInfo(request.url());
            // Reemplazar caracteres raros para evitar errores en el nombre de archivo
            String safeTitle = title.replaceAll("[^a-zA-Z0-9.-]", "_");

            // 2. Descargar el video y obtener sus bytes
            byte[] videoBytes = downloadService.downloadVideo(request.url(), request.quality());

            // 3. Configurar cabeceras de descarga directa para el navegador del usuario
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", safeTitle + ".mp4");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(videoBytes);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}