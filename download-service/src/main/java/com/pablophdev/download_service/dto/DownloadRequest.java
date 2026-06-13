package com.pablophdev.download_service.dto;

import jakarta.validation.constraints.NotBlank;

public record DownloadRequest(
    @NotBlank(message = "La URL es obligatoria")
    String url,
    @NotBlank(message = "La calidad es obligatoria")
    String quality
) {}
