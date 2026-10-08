package com.example.Rag_java.controller;

import com.example.Rag_java.dto.ChatRequest;
import com.example.Rag_java.dto.PythonRequest;
import com.example.Rag_java.dto.PythonResponse;
import com.example.Rag_java.service.ModelHandlingService;
import com.example.Rag_java.service.RelivantChunksService;
import org.springframework.ai.document.Document;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RequestMapping
@RestController("/api/get")
public class QueryController {

    private final RelivantChunksService relivantChunksService;
    private final ModelHandlingService modelHandlingService;

    public QueryController(RelivantChunksService relivantChunksService,
                           ModelHandlingService modelHandlingService) {
        this.relivantChunksService = relivantChunksService;
        this.modelHandlingService=modelHandlingService;
    }

    @PostMapping("/ans")
    public ResponseEntity<String> getData(@RequestBody ChatRequest request) {

        String query = request.query();
        List<Document> contextDocs = relivantChunksService.searchTopK(query);
        List<String> context = contextDocs.stream()
                .map(Document::getText)
                .toList();

        PythonRequest pythonRequest = new PythonRequest(query, context);

        PythonResponse response = modelHandlingService.getResponse(pythonRequest);
        return ResponseEntity.ok("fuck");
    }
}
