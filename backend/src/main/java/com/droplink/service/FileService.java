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
import java.util.Set;
import java.util.UUID;

@Service
public class FileService {

    private static final Set<String> BLOCKED_EXTENSIONS = Set.of(
            ".exe", ".bat", ".cmd", ".sh", ".ps1", ".vbs", ".js", ".msi", ".com", ".scr", ".pif"
    );

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

    public FileRecord upload(MultipartFile file, Integer expireHours) throws IOException {
        String fileId = UUID.randomUUID().toString();
        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.isBlank()) {
            originalName = "unnamed";
        }

        String ext = getFileExtension(originalName).toLowerCase();
        if (BLOCKED_EXTENSIONS.contains(ext)) {
            throw new IllegalArgumentException("不允许上传此类文件: " + ext);
        }

        String storedName = fileId + ext;
        Long fileSize = file.getSize();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expireTime = (expireHours != null && expireHours > 0)
                ? now.plusHours(expireHours)
                : null;

        Path targetPath = uploadDir.resolve(storedName).normalize();
        if (!targetPath.startsWith(uploadDir)) {
            throw new SecurityException("非法文件路径");
        }
        file.transferTo(targetPath.toFile());

        FileRecord record = new FileRecord(fileId, originalName, storedName, fileSize, now, expireTime);
        return fileRepository.save(record);
    }

    public void cleanExpiredFiles() {
        List<FileRecord> expiredFiles = fileRepository.findByExpireTimeBeforeAndExpireTimeIsNotNull(LocalDateTime.now());
        for (FileRecord record : expiredFiles) {
            try {
                Path filePath = uploadDir.resolve(record.getStoredName()).normalize();
                if (filePath.startsWith(uploadDir) && Files.exists(filePath)) {
                    Files.delete(filePath);
                }
            } catch (IOException e) {
                // Log error but continue cleaning other files
            }
            fileRepository.delete(record);
        }
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
        Path filePath = uploadDir.resolve(record.getStoredName()).normalize();
        if (!filePath.startsWith(uploadDir)) {
            throw new SecurityException("非法文件路径");
        }
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
