package com.pablophdev.download_service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Video {

    private String title;
    private String url;
    private String format;
    private String thumbnail;
    private String duration;
}
