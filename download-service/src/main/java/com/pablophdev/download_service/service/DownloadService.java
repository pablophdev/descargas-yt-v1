package com.pablophdev.download_service.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.springframework.stereotype.Service;

@Service
public class DownloadService {

    // Ya no es una constante fija de Windows
    private String getYtDlpPath() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            // Tu ruta local en Windows
            return "C:\\Users\\Pablo\\Downloads\\yt-dlp\\yt-dlp.exe";
        }
        // En Render (Linux), al estar instalado globalmente, basta con invocar el comando
        return "yt-dlp";
    }

    public byte[] downloadVideo(String url, String quality) throws IOException, InterruptedException {
        File downloadsDir = new File("downloads");
        if (!downloadsDir.exists()) {
            downloadsDir.mkdirs();
        }
        
        File tempDir = new File(downloadsDir, String.valueOf(System.currentTimeMillis()));
        if (!tempDir.exists()) {
            tempDir.mkdirs();
        }

        String formatSelection = String.format(
            "bestvideo[height<=%s][vcodec*=avc1]+bestaudio[ext=m4a]",
            quality
        );
        
        String os = System.getProperty("os.name").toLowerCase();
        boolean isWindows = os.contains("win");

        // 3. Configurar el ProcessBuilder dinámicamente
        ProcessBuilder processBuilder;
        
        if (isWindows) {
            String ffmpegLocation = "C:\\Users\\Pablo\\Downloads\\yt-dlp";
            processBuilder = new ProcessBuilder(
                getYtDlpPath(),
                "-f", formatSelection,
                "--ffmpeg-location", ffmpegLocation,
                "--merge-output-format", "mp4",
                "--yes-overwrites",
                "-o", tempDir.getAbsolutePath() + File.separator + "%(title)s.%(ext)s",
                url
            );
        } else {
            // En Render (Linux), FFmpeg se instaló globalmente vía apt-get.
            // No necesitas pasarle el parámetro '--ffmpeg-location' porque el sistema lo encuentra solo.
            processBuilder = new ProcessBuilder(
                getYtDlpPath(),
                "-f", formatSelection,
                "--merge-output-format", "mp4",
                "--yes-overwrites",
                "-o", tempDir.getAbsolutePath() + File.separator + "%(title)s.%(ext)s",
                url
            );
        }

        processBuilder.inheritIO(); 

        Process process = processBuilder.start();
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            deleteDirectory(tempDir);
            throw new RuntimeException("Error al descargar el video con yt-dlp. Código de salida: " + exitCode);
        }

        File[] files = tempDir.listFiles();
        if (files == null || files.length == 0) {
            deleteDirectory(tempDir);
            throw new RuntimeException("No se encontró ningún archivo descargado en la carpeta temporal");
        }
        
        File downloadedFile = null;
        for (File file : files) {
            if (file.getName().endsWith(".mp4")) {
                downloadedFile = file;
                break;
            }
        }

        if (downloadedFile == null) {
            downloadedFile = files[0];
        }
        
        byte[] fileContent = Files.readAllBytes(downloadedFile.toPath());

        // Opcional: Descomenta esto en Render para que no se te llene el almacenamiento del contenedor
        deleteDirectory(tempDir); 

        return fileContent;
    }

    public String getVideoInfo(String url) throws Exception {
        // También adaptamos la obtención del título
        ProcessBuilder pb = new ProcessBuilder(
            getYtDlpPath(),
            "--get-title", 
            url
        );
        Process process = pb.start();
        String title = new String(process.getInputStream().readAllBytes());
        process.waitFor();
        return title.trim();
    }

    private void deleteDirectory(File directory) {
        File[] allContents = directory.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                file.delete();
            }
        }
        directory.delete();
    }
}