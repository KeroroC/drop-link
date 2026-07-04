package com.droplink.service;

import com.droplink.entity.FileRecord;
import com.droplink.repository.FileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
        FileRecord record = service.upload(file);

        // Assert
        assertNotNull(record.getFileId());
        assertEquals("test.txt", record.getOriginalName());
        assertEquals(11L, record.getFileSize());
        assertNotNull(record.getUploadTime());
    }

    @Test
    void listAll_shouldReturnRecordsInDescendingOrder() throws IOException {
        // Arrange
        FileRepository repo = new InMemoryFileRepository();
        FileService service = new FileService(repo, tempDir.toString());

        MultipartFile file1 = new MockMultipartFile("file", "a.txt", "text/plain", "A".getBytes());
        MultipartFile file2 = new MockMultipartFile("file", "b.txt", "text/plain", "B".getBytes());

        service.upload(file1);
        service.upload(file2);

        // Act
        List<FileRecord> records = service.listAll();

        // Assert
        assertEquals(2, records.size());
        assertEquals("b.txt", records.get(0).getOriginalName());
        assertEquals("a.txt", records.get(1).getOriginalName());
    }
}
