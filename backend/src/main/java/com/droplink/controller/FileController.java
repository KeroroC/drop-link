package com.droplink.controller;

import com.droplink.dto.ApiResponse;
import com.droplink.entity.FileRecord;
import com.droplink.service.FileService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @GetMapping
    public ApiResponse<List<FileRecord>> listFiles() {
        List<FileRecord> files = fileService.listAll();
        return ApiResponse.success(files);
    }

    @PostMapping("/upload")
    public ApiResponse<FileRecord> uploadFile(@RequestParam("file") MultipartFile file) throws IOException {
        FileRecord record = fileService.upload(file);
        return ApiResponse.success(record);
    }

    @GetMapping("/{fileId}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileId) {
        FileRecord record = fileService.getFileInfo(fileId);
        Path filePath = fileService.getStoredFile(fileId);

        Resource resource = new FileSystemResource(filePath);

        String encodedName = URLEncoder.encode(record.getOriginalName(), StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename*=UTF-8''" + encodedName)
                .body(resource);
    }
}
