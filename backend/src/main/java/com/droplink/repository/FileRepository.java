package com.droplink.repository;

import com.droplink.entity.FileRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface FileRepository extends JpaRepository<FileRecord, Long> {

    List<FileRecord> findAllByOrderByUploadTimeDesc();

    Optional<FileRecord> findByFileId(String fileId);

    List<FileRecord> findByExpireTimeBeforeAndExpireTimeIsNotNull(LocalDateTime time);
}
