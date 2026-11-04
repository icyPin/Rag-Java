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

    //this is only for local initial testing
    //need to change this to hot directory ingestion
    // will work on this later once  get the time

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
