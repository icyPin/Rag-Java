package com.example.Rag_java.controller;

import com.example.Rag_java.service.EmbeddingService;
import com.example.Rag_java.service.RelivantChunksService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;



@RestController
@RequestMapping("/api/docs")
public class FileController {

    private final EmbeddingService embeddingService;

    public FileController(EmbeddingService embeddingService) {
        this.embeddingService = embeddingService;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> upload(@RequestParam("file")MultipartFile file){
        try{
            embeddingService.embedFile(file.getResource());
            return ResponseEntity.ok("File: " + file.getOriginalFilename()+"  embedded successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error embedding file: "+ e.getMessage());
        }
    }

}
