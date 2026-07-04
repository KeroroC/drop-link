package com.droplink.service;

import com.droplink.entity.FileRecord;
import com.droplink.repository.FileRepository;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;

public class InMemoryFileRepository implements FileRepository {

    private final Map<Long, FileRecord> store = new HashMap<>();
    private final AtomicLong idCounter = new AtomicLong(1);

    @Override
    public List<FileRecord> findAllByOrderByUploadTimeDesc() {
        return store.values().stream()
                .sorted(Comparator.comparing(FileRecord::getUploadTime).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public Optional<FileRecord> findByFileId(String fileId) {
        return store.values().stream()
                .filter(r -> r.getFileId().equals(fileId))
                .findFirst();
    }

    @Override
    public <S extends FileRecord> S save(S entity) {
        if (entity.getId() == null) {
            entity.setId(idCounter.getAndIncrement());
        }
        store.put(entity.getId(), entity);
        return entity;
    }

    // Other JpaRepository methods - minimal implementation for testing
    @Override public void flush() {}
    @Override public <S extends FileRecord> S saveAndFlush(S entity) { return save(entity); }
    @Override public <S extends FileRecord> List<S> saveAll(Iterable<S> entities) { return null; }
    @Override public <S extends FileRecord> List<S> saveAllAndFlush(Iterable<S> entities) { return null; }
    @Override public Optional<FileRecord> findById(Long id) { return Optional.ofNullable(store.get(id)); }
    @Override public boolean existsById(Long id) { return store.containsKey(id); }
    @Override public List<FileRecord> findAll() { return new ArrayList<>(store.values()); }
    @Override public List<FileRecord> findAll(Sort sort) { return findAll(); }
    @Override public Page<FileRecord> findAll(Pageable pageable) { return null; }
    @Override public List<FileRecord> findAllById(Iterable<Long> ids) { return null; }
    @Override public long count() { return store.size(); }
    @Override public void deleteById(Long id) { store.remove(id); }
    @Override public void delete(FileRecord entity) { store.remove(entity.getId()); }
    @Override public void deleteAllById(Iterable<? extends Long> ids) {}
    @Override public void deleteAllByIdInBatch(Iterable<Long> ids) {}
    @Override public void deleteAll(Iterable<? extends FileRecord> entities) {}
    @Override public void deleteAll() { store.clear(); }
    @Override public void deleteAllInBatch() {}
    @Override public void deleteAllInBatch(Iterable<FileRecord> entities) {}
    @Override public FileRecord getById(Long id) { return findById(id).orElse(null); }
    @Override public FileRecord getOne(Long id) { return getById(id); }
    @Override public FileRecord getReferenceById(Long id) { return getById(id); }
    @Override public <S extends FileRecord> Optional<S> findOne(Example<S> example) { return Optional.empty(); }
    @Override public <S extends FileRecord> List<S> findAll(Example<S> example) { return null; }
    @Override public <S extends FileRecord> List<S> findAll(Example<S> example, Sort sort) { return null; }
    @Override public <S extends FileRecord> Page<S> findAll(Example<S> example, Pageable pageable) { return null; }
    @Override public <S extends FileRecord> long count(Example<S> example) { return 0; }
    @Override public <S extends FileRecord> boolean exists(Example<S> example) { return false; }
    @Override public <S extends FileRecord, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
}
