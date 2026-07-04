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
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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

    public FileRecord upload(MultipartFile file, Integer expireHours, String password) throws IOException {
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
        if (password != null && !password.isBlank()) {
            record.setPasswordHash(hashPassword(password));
        }
        return fileRepository.save(record);
    }

    public boolean verifyPassword(String fileId, String password) {
        FileRecord record = getFileInfo(fileId);
        if (record.getPasswordHash() == null) {
            return true; // No password set
        }
        if (password == null || password.isBlank()) {
            return false;
        }
        return record.getPasswordHash().equals(hashPassword(password));
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256不可用", e);
        }
    }

    public void deleteFile(String fileId) {
        FileRecord record = getFileInfo(fileId);
        Path filePath = uploadDir.resolve(record.getStoredName()).normalize();
        if (filePath.startsWith(uploadDir) && Files.exists(filePath)) {
            try {
                Files.delete(filePath);
            } catch (IOException e) {
                throw new RuntimeException("删除文件失败", e);
            }
        }
        fileRepository.delete(record);
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
        record.incrementDownloadCount();
        fileRepository.save(record);
        return filePath;
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }
}
