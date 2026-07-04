package com.droplink.service;

import com.droplink.entity.FileRecord;
import com.droplink.exception.FileNotFoundException;
import com.droplink.repository.FileRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class FileService {

    private final FileRepository fileRepository;
    private final Path uploadDir;

    public FileService(FileRepository fileRepository,
                       @Value("${app.upload.dir:./uploads}") String uploadDir) {
        this.fileRepository = fileRepository;
        this.uploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    @PostConstruct
    public void init() throws IOException {
        Files.createDirectories(uploadDir);
    }

    public FileRecord upload(MultipartFile file) throws IOException {
        String fileId = UUID.randomUUID().toString();
        String originalName = file.getOriginalFilename();
        String storedName = fileId + getFileExtension(originalName);
        Long fileSize = file.getSize();

        Path targetPath = uploadDir.resolve(storedName);
        file.transferTo(targetPath.toFile());

        FileRecord record = new FileRecord(fileId, originalName, storedName, fileSize, LocalDateTime.now());
        return fileRepository.save(record);
    }

    public List<FileRecord> listAll() {
        return fileRepository.findAllByOrderByUploadTimeDesc();
    }

    public FileRecord getFileInfo(String fileId) {
        return fileRepository.findByFileId(fileId)
                .orElseThrow(() -> new FileNotFoundException("文件不存在: " + fileId));
    }

    public Path getStoredFile(String fileId) {
        FileRecord record = getFileInfo(fileId);
        Path filePath = uploadDir.resolve(record.getStoredName());
        if (!Files.exists(filePath)) {
            throw new FileNotFoundException("物理文件不存在: " + fileId);
        }
        return filePath;
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }
}
