package com.example.Rag_java.controller;

import com.example.Rag_java.dto.ChatRequest;
import com.example.Rag_java.dto.PythonResponse;
import com.example.Rag_java.service.ModelHandlingService;
import com.example.Rag_java.service.RelivantChunksService;
import dev.langchain4j.data.segment.TextSegment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/get")
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
        List<TextSegment> chunks = relivantChunksService.searchTopK(query);

        String context = chunks.stream()
                .map(TextSegment::text)
                .collect(Collectors.joining("\n---\n"));

        String finalPrompt = String.format("""
            Answer the user's question strictly based on the following context.
            If the answer cannot be found in the context, respond with "I cannot find this in the documents."

            CONTEXT:
            %s

            USER QUESTION:
            %s
            """, context, query);

        PythonResponse response = modelHandlingService.getResponse(finalPrompt);
        return ResponseEntity.ok(response.response());
    }
}
