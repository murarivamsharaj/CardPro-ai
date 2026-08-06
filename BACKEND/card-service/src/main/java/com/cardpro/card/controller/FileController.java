package com.cardpro.card.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/files")
public class FileController {

    private final String UPLOAD_DIR = "uploads/";

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "File is empty"));
            }

            // Ensure upload directory exists
            File dir = new File(UPLOAD_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // Generate unique filename to prevent collisions
            String originalFileName = file.getOriginalFilename();
            String extension = originalFileName != null ? originalFileName.substring(originalFileName.lastIndexOf(".")) : ".jpg";
            String uniqueFileName = UUID.randomUUID().toString() + extension;

            Path path = Paths.get(UPLOAD_DIR + uniqueFileName);
            Files.write(path, file.getBytes());

            // Return accessible file URL path
            String fileUrl = "/api/v1/files/view/" + uniqueFileName;
            return ResponseEntity.ok(Map.of("url", fileUrl));

        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to upload file"));
        }
    }
}