package com.pablophdev.download_service.service;

import java.io.File;
import java.io.IOException;
import org.springframework.stereotype.Service;

@Service
public class DownloadService {

    private String getYtDlpPath() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            return "yt-dlp" + File.separator + "yt-dlp.exe";
        }
        return "/usr/local/bin/yt-dlp";
    }

    // Cambiamos el retorno a String para devolver un mensaje de éxito con la ruta
    public String downloadVideo(String url, String quality) throws IOException, InterruptedException {
        String os = System.getProperty("os.name").toLowerCase();
        boolean isWindows = os.contains("win");
        
        File targetDir;

        if (isWindows) {
            // Obtiene dinámicamente "C:\Users\TuUsuario\Downloads"
            String userHome = System.getProperty("user.home");
            targetDir = new File(userHome + File.separator + "Downloads");
        } else {
            // Fallback por si acaso corres en Linux/producción
            targetDir = new File("downloads");
        }

        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }

        String formatSelection = String.format(
            "bestvideo[height<=%s][vcodec*=avc1]+bestaudio[ext=m4a]",
            quality
        );
                 
        ProcessBuilder processBuilder;
        
        if (isWindows) {    
            String ffmpegLocation = "yt-dlp"; // Carpeta donde está tu ffmpeg.exe relativo
            processBuilder = new ProcessBuilder(
                getYtDlpPath(),
                "-f", formatSelection,
                "--ffmpeg-location", ffmpegLocation,
                "--merge-output-format", "mp4",      
                "--yes-overwrites",                   
                "-o", targetDir.getAbsolutePath() + File.separator + "%(title)s.%(ext)s", // Guardado directo aquí
                url
            );
        } else {
            processBuilder = new ProcessBuilder(
                getYtDlpPath(),
                "-f", formatSelection,
                "--merge-output-format", "mp4",
                "--yes-overwrites",
                "-o", targetDir.getAbsolutePath() + File.separator + "%(title)s.%(ext)s",
                url
            );
        }

        processBuilder.inheritIO(); 

        Process process = processBuilder.start();
        int exitCode = process.waitFor();
        
        if (exitCode != 0) {
            throw new RuntimeException("Error al descargar el video con yt-dlp. Código de salida: " + exitCode);
        }

        // Retornamos la ruta absoluta donde quedó guardado para informar al usuario
        return "Video descargado con éxito en: " + targetDir.getAbsolutePath();
    }

    public String getVideoInfo(String url) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(
            getYtDlpPath(),
            "--get-title", 
            url
        );

        pb.redirectErrorStream(true);
        Process process = pb.start();
        String title = new String(process.getInputStream().readAllBytes());
        process.waitFor();
        return title.trim();
    }
}