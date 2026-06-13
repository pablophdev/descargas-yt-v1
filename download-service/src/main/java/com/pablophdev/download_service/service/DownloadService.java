package com.pablophdev.download_service.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.springframework.stereotype.Service;

@Service
public class DownloadService {

    // Ruta absoluta al ejecutable de yt-dlp
    private static final String YT_DLP_PATH = "C:\\Users\\Pablo\\Downloads\\yt-dlp\\yt-dlp.exe";

    public byte[] downloadVideo(String url, String quality) throws IOException, InterruptedException {
        // 1. Crear el directorio principal y la subcarpeta temporal basado en el timestamp
        File downloadsDir = new File("downloads");
        if (!downloadsDir.exists()) {
            downloadsDir.mkdirs();
        }
        
        File tempDir = new File(downloadsDir, String.valueOf(System.currentTimeMillis()));
        if (!tempDir.exists()) {
            tempDir.mkdirs();
        }

        // Forzar H264 + AAC para máxima compatibilidad
        String formatSelection = String.format(
        "bestvideo[height<=%s][vcodec*=avc1]+bestaudio[ext=m4a]",
        quality
        );
        
        // Ruta de la carpeta donde tienes guardado tanto ffmpeg.exe como ffprobe.exe
        String ffmpegLocation = "C:\\Users\\Pablo\\Downloads\\yt-dlp";

        // 3. Configurar el ProcessBuilder con los argumentos adecuados para Windows
        ProcessBuilder processBuilder = new ProcessBuilder(
            YT_DLP_PATH,
            "-f", formatSelection,
            "--ffmpeg-location", ffmpegLocation, // Fuerza a yt-dlp a encontrar FFmpeg para la fusión
            "--merge-output-format", "mp4",       // Vincula los webm/mkv temporales en un contenedor MP4 limpio
            "--yes-overwrites",                   // Evita bloqueos por preguntas de sobreescritura en la terminal
            "-o", tempDir.getAbsolutePath() + File.separator + "%(title)s.%(ext)s",
            url
        );

        // 4. Redirigir la salida del proceso para ver el progreso (%) directamente en la consola de VS Code
        processBuilder.inheritIO(); 

        // 5. Iniciar y esperar que el proceso termine por completo
        Process process = processBuilder.start();
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            deleteDirectory(tempDir);
            throw new RuntimeException("Error al descargar el video con yt-dlp. Código de salida: " + exitCode);
        }

        // 6. Leer la carpeta temporal de forma segura
        File[] files = tempDir.listFiles();
        if (files == null || files.length == 0) {
            deleteDirectory(tempDir);
            throw new RuntimeException("No se encontró ningún archivo descargado en la carpeta temporal");
        }
        
        // 7. Buscar específicamente el archivo fusionado definitivo (.mp4)
        File downloadedFile = null;
        for (File file : files) {
            if (file.getName().endsWith(".mp4")) {
                downloadedFile = file;
                break;
            }
        }

        // Si por alguna razón externa no se generó el .mp4, tomamos el primero como respaldo para evitar caídas
        if (downloadedFile == null) {
            downloadedFile = files[0];
        }
        
        // 8. Convertir el archivo final en un arreglo de bytes para transferirlo
        byte[] fileContent = Files.readAllBytes(downloadedFile.toPath());

        // 9. Limpieza absoluta de la carpeta temporal
        //deleteDirectory(tempDir);

        return fileContent;
    }

    public String getVideoInfo(String url) throws Exception {
        // Método auxiliar para recuperar el título del video en formato plano
        ProcessBuilder pb = new ProcessBuilder(
            YT_DLP_PATH,
            "--get-title", 
            url
        );
        Process process = pb.start();
        String title = new String(process.getInputStream().readAllBytes());
        process.waitFor();
        return title.trim();
    }

    private void deleteDirectory(File directory) {
        // Eliminar archivos internos recursivamente antes de tumbar el directorio
        File[] allContents = directory.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                file.delete();
            }
        }
        directory.delete();
    }
}