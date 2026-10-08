package com.example.Rag_java.controller;

import com.example.Rag_java.service.EmbeddingService;
import com.example.Rag_java.service.RelivantChunksService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@RequestMapping
@RestController("/api/docs")
public class FileController {

    private final EmbeddingService embeddingService;
    private final RelivantChunksService relivantChunksService;

    public FileController(EmbeddingService embeddingService, RelivantChunksService relivantChunksService) {
        this.embeddingService = embeddingService;
        this.relivantChunksService = relivantChunksService;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> upload(@RequestParam("file")MultipartFile file){
        try{
            embeddingService.embedFile(file.getResource());
            return ResponseEntity.ok("File: " + file.getOriginalFilename()+" embedded successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error embedding file: "+ e.getMessage());
        }
    }

}
