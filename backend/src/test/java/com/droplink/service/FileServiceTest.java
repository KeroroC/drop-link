package com.droplink.service;

import com.droplink.entity.FileRecord;
import com.droplink.exception.FileNotFoundException;
import com.droplink.repository.FileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void upload_shouldSaveFileAndReturnRecord() throws IOException {
        // Arrange
        FileRepository repo = new InMemoryFileRepository();
        FileService service = new FileService(repo, tempDir.toString());

        MultipartFile file = new MockMultipartFile(
                "file", "test.txt", "text/plain", "Hello World".getBytes()
        );

        // Act
        FileRecord record = service.upload(file, 24, null);

        // Assert
        assertNotNull(record.getFileId());
        assertEquals("test.txt", record.getOriginalName());
        assertEquals(11L, record.getFileSize());
        assertNotNull(record.getUploadTime());
        assertNotNull(record.getExpireTime());
    }

    @Test
    void listAll_shouldReturnRecordsInDescendingOrder() throws IOException {
        // Arrange
        FileRepository repo = new InMemoryFileRepository();
        FileService service = new FileService(repo, tempDir.toString());

        MultipartFile file1 = new MockMultipartFile("file", "a.txt", "text/plain", "A".getBytes());
        MultipartFile file2 = new MockMultipartFile("file", "b.txt", "text/plain", "B".getBytes());

        service.upload(file1, 24, null);
        service.upload(file2, 24, null);

        // Act
        List<FileRecord> records = service.listAll();

        // Assert
        assertEquals(2, records.size());
        assertEquals("b.txt", records.get(0).getOriginalName());
        assertEquals("a.txt", records.get(1).getOriginalName());
    }

    @Test
    void upload_shouldWriteFileToDisk() throws IOException {
        // Arrange
        FileRepository repo = new InMemoryFileRepository();
        FileService service = new FileService(repo, tempDir.toString());

        MultipartFile file = new MockMultipartFile(
                "file", "disk-test.txt", "text/plain", "Disk Content".getBytes()
        );

        // Act
        FileRecord record = service.upload(file, 24, null);

        // Assert
        Path storedPath = tempDir.resolve(record.getStoredName());
        assertTrue(Files.exists(storedPath), "File should exist on disk after upload");
        assertEquals("Disk Content", Files.readString(storedPath));
    }

    @Test
    void getFileInfo_shouldReturnRecordForExistingFileId() throws IOException {
        // Arrange
        FileRepository repo = new InMemoryFileRepository();
        FileService service = new FileService(repo, tempDir.toString());

        MultipartFile file = new MockMultipartFile(
                "file", "info-test.txt", "text/plain", "Info Content".getBytes()
        );
        FileRecord uploaded = service.upload(file, 24, null);

        // Act
        FileRecord result = service.getFileInfo(uploaded.getFileId());

        // Assert
        assertEquals(uploaded.getFileId(), result.getFileId());
        assertEquals("info-test.txt", result.getOriginalName());
        assertEquals(uploaded.getStoredName(), result.getStoredName());
        assertEquals(12L, result.getFileSize());
        assertNotNull(result.getUploadTime());
    }

    @Test
    void getFileInfo_shouldThrowForNonexistentFileId() {
        // Arrange
        FileRepository repo = new InMemoryFileRepository();
        FileService service = new FileService(repo, tempDir.toString());

        // Act & Assert
        assertThrows(FileNotFoundException.class,
                () -> service.getFileInfo("nonexistent-id"));
    }

    @Test
    void getStoredFile_shouldReturnPathThatExistsOnDisk() throws IOException {
        // Arrange
        FileRepository repo = new InMemoryFileRepository();
        FileService service = new FileService(repo, tempDir.toString());

        MultipartFile file = new MockMultipartFile(
                "file", "stored-test.txt", "text/plain", "Stored Content".getBytes()
        );
        FileRecord uploaded = service.upload(file, 24, null);

        // Act
        Path result = service.getStoredFile(uploaded.getFileId());

        // Assert
        assertTrue(Files.exists(result), "Returned path should exist on disk");
        assertEquals(tempDir.resolve(uploaded.getStoredName()), result);
    }

    @Test
    void getStoredFile_shouldThrowWhenPhysicalFileMissing() throws IOException {
        // Arrange
        FileRepository repo = new InMemoryFileRepository();
        FileService service = new FileService(repo, tempDir.toString());

        MultipartFile file = new MockMultipartFile(
                "file", "missing-test.txt", "text/plain", "Will be deleted".getBytes()
        );
        FileRecord uploaded = service.upload(file, 24, null);

        // Delete the physical file from disk
        Path storedPath = tempDir.resolve(uploaded.getStoredName());
        Files.delete(storedPath);
        assertFalse(Files.exists(storedPath));

        // Act & Assert
        assertThrows(FileNotFoundException.class,
                () -> service.getStoredFile(uploaded.getFileId()));
    }
}
