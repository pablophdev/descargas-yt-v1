package com.pablophdev.download_service.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.springframework.stereotype.Service;

@Service
public class DownloadService {

    /**
     * Detecta el sistema operativo y retorna la ruta correspondiente para yt-dlp.
     * En Windows usa tu ruta local; en Linux (Render) usa la ruta absoluta donde 
     * el Dockerfile instala el binario globalmente.
     */
    private String getYtDlpPath() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            return "C:\\Users\\Pablo\\Downloads\\yt-dlp\\yt-dlp.exe";
        }
        // Ruta absoluta estándar dentro del contenedor Linux de Render
        return "/usr/local/bin/yt-dlp";
    }

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
                    
        String os = System.getProperty("os.name").toLowerCase();
        boolean isWindows = os.contains("win");

        // 2. Configurar el ProcessBuilder adaptando los argumentos según el entorno
        ProcessBuilder processBuilder;
        
        if (isWindows) {
            // Configuración para tu PC Local (Windows)
            String ffmpegLocation = "C:\\Users\\Pablo\\Downloads\\yt-dlp";
            processBuilder = new ProcessBuilder(
                getYtDlpPath(),
                "-f", formatSelection,
                "--ffmpeg-location", ffmpegLocation, // Fuerza a yt-dlp a encontrar FFmpeg en tu PC
                "--merge-output-format", "mp4",       // Vincula las pistas en un MP4 limpio
                "--yes-overwrites",                   // Evita bloqueos por preguntas de sobreescritura
                "-o", tempDir.getAbsolutePath() + File.separator + "%(title)s.%(ext)s",
                url
            );
        } else {
            // Configuración para el servidor de producción (Render / Linux)
            // En Linux, FFmpeg se instala de forma global a nivel de sistema,
            // por lo que yt-dlp lo detecta automáticamente sin necesidad del parámetro '--ffmpeg-location'.
            processBuilder = new ProcessBuilder(
                getYtDlpPath(),
                "-f", formatSelection,
                "--merge-output-format", "mp4",
                "--yes-overwrites",
                "-o", tempDir.getAbsolutePath() + File.separator + "%(title)s.%(ext)s",
                url
            );
        }

        // 3. Redirigir la salida del proceso para ver el progreso directamente en la consola
        processBuilder.inheritIO(); 

        // 4. Iniciar y esperar que el proceso termine por completo
        Process process = processBuilder.start();
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            deleteDirectory(tempDir);
            throw new RuntimeException("Error al descargar el video con yt-dlp. Código de salida: " + exitCode);
        }

        // 5. Leer la carpeta temporal de forma segura
        File[] files = tempDir.listFiles();
        if (files == null || files.length == 0) {
            deleteDirectory(tempDir);
            throw new RuntimeException("No se encontró ningún archivo descargado en la carpeta temporal");
        }
                    
        // 6. Buscar específicamente el archivo fusionado definitivo (.mp4)
        File downloadedFile = null;
        for (File file : files) {
            if (file.getName().endsWith(".mp4")) {
                downloadedFile = file;
                break;
            }
        }

        // Si por alguna razón externa no se generó el .mp4, tomamos el primero como respaldo
        if (downloadedFile == null) {
            downloadedFile = files[0];
        }
                    
        // 7. Convertir el archivo final en un arreglo de bytes para transferirlo
        byte[] fileContent = Files.readAllBytes(downloadedFile.toPath());

        // 8. Limpieza absoluta de la carpeta temporal para evitar agotar el almacenamiento de Render
        deleteDirectory(tempDir);
        return fileContent;
    }

    public String getVideoInfo(String url) throws Exception {
        // Método auxiliar adaptado para recuperar el título usando la ruta dinámica del sistema
        ProcessBuilder pb = new ProcessBuilder(
            getYtDlpPath(),
            "--get-title", 
            url
        );

        // CORRECCIÓN CRÍTICA PARA LINUX: Fusiona el flujo de error con la salida estándar.
        // Esto evita que el buffer de Linux se llene con advertencias y congele el hilo.
        pb.redirectErrorStream(true);

        Process process = pb.start();
        
        // Se lee el stream completo (salida estándar + errores integrados)
        String output = new String(process.getInputStream().readAllBytes());
        int exitCode = process.waitFor();
        
        if (exitCode != 0) {
            // Si falla, imprimimos exactamente qué dijo yt-dlp (muy útil si YouTube te tira un bloqueo 429)
            System.err.println("Error al obtener info del video. Salida de yt-dlp: " + output);
            throw new RuntimeException("yt-dlp falló al obtener el título. Código de salida: " + exitCode);
        }

        return output.trim();
    }

    private void deleteDirectory(File directory) {
        // Eliminar archivos internos recursivamente antes de borrar el directorio
        File[] allContents = directory.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                file.delete();
            }
        }
        directory.delete();
    }
}