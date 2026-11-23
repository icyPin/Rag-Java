package com.example.Rag_java.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class FolderReadingService {
    private final EmbeddingService embeddingService;
    private final JdbcClient jdbcClient;
    private final Path base = Paths.get("./context-base");
    private record TrackedFile(long fileSize, long lastModified, String fileHash) {}

    @Scheduled(fixedDelay = 30000)
    public void syncFolder(){

       try{
           if(!Files.exists(base)){
               Files.createDirectories(base);
               log.info("new package created for base context");
               return;
           }

           Set<String> files = new HashSet<>();
           try(Stream<Path> paths = Files.walk(base)){

               List<Path> pdfs = paths
                       .filter(Files::isRegularFile)
                       .filter(p-> p.toString().toLowerCase().endsWith(".pdf"))
                       .toList();

               for(Path file : pdfs){
                   files.add(file.getFileName().toString());
                   processesFile(file);
               }
           }
           cleanup(files);
       } catch (IOException e) {
           log.error("error scanning directory: {}" , base , e);
       }
    }

    private void processesFile(Path file){

        String fileName = file.getFileName().toString();

        try{
            if(!Files.isReadable(file)) return;

            long size = Files.size(file);
            //for its still being copied
            if (size==0) return;

            long currentModified = Files.getLastModifiedTime(file).toMillis();
            Optional<TrackedFile> existing = jdbcClient.sql(
                            "SELECT file_size, last_modified, file_hash FROM ingested_files WHERE file_name = ?")
                    .param(fileName)
                    .query((rs, rowNum) -> new TrackedFile(
                            rs.getLong("file_size"),
                            rs.getLong("last_modified"),
                            rs.getString("file_hash")
                    ))
                    .optional();

            if(existing.isPresent()
                    && existing.get().fileSize == size
                    && existing.get().lastModified==currentModified)
                return;

            String currentHash;
            try (InputStream is = Files.newInputStream(file)) {
                currentHash = DigestUtils.sha256Hex(is);
            }
            if (existing.isEmpty()) {
                log.info("New PDF detected: {}. Embedding...", fileName);
                embeddingService.embedFile(new FileSystemResource(file));

                jdbcClient.sql("""
                        INSERT INTO ingested_files (file_name, file_size, last_modified, file_hash)
                        VALUES (?, ?, ?, ?)
                        """)
                        .params(fileName, size, currentModified, currentHash)
                        .update();

            } else if (!existing.get().fileHash().equals(currentHash)) {
                log.info("Modified PDF detected: {}. Replacing old embeddings...", fileName);

                jdbcClient.sql("DELETE FROM vector_store WHERE metadata->>'file_name' = ?")
                        .param(fileName)
                        .update();

                embeddingService.embedFile(new FileSystemResource(file));

                jdbcClient.sql("""
                        UPDATE ingested_files
                        SET file_size = ?, last_modified = ?, file_hash = ?, updated_at = CURRENT_TIMESTAMP
                        WHERE file_name = ?
                        """)
                        .params(size, currentModified, currentHash, fileName)
                        .update();

            } else {
                // file was copied/touched without edits
                jdbcClient.sql("""
                        UPDATE ingested_files
                        SET file_size = ?, last_modified = ?
                        WHERE file_name = ?
                        """)
                        .params(size, currentModified, fileName)
                        .update();
            }

        } catch (IOException e) {
            log.error("failed processing file : {}" , fileName, e);
        }
    }

    public void cleanup(Set<String> files){
        List<String> trackedNames = jdbcClient.sql("SELECT file_name FROM ingested_files")
                .query(String.class)
                .list();

        for (String trackedName : trackedNames) {
            if (!files.contains(trackedName)) {
                log.info("Deleted PDF detected: {}. Removing vectors...", trackedName);
                jdbcClient.sql("DELETE FROM vector_store WHERE metadata->>'file_name' = ?")
                        .param(trackedName)
                        .update();
                jdbcClient.sql("DELETE FROM ingested_files WHERE file_name = ?")
                        .param(trackedName)
                        .update();
            }
        }
    }
}
