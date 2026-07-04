package com.droplink.repository;

import com.droplink.entity.FileRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface FileRepository extends JpaRepository<FileRecord, Long> {

    List<FileRecord> findAllByOrderByUploadTimeDesc();

    Optional<FileRecord> findByFileId(String fileId);

    List<FileRecord> findByExpireTimeBeforeAndExpireTimeIsNotNull(LocalDateTime time);

    @Modifying
    @Transactional
    @Query("UPDATE FileRecord f SET f.downloadCount = f.downloadCount + 1 WHERE f.fileId = :fileId")
    int incrementDownloadCount(@Param("fileId") String fileId);
}
