package com.example.Rag_java.service;


import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.filter.Filter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

import static dev.langchain4j.store.embedding.filter.MetadataFilterBuilder.metadataKey;

@Slf4j
@Service
public class RelivantChunksService {

    private final EmbeddingService embeddingService;
    private final EmbeddingStore<TextSegment> embeddingStore;
    private final int k = 20;

    public RelivantChunksService(EmbeddingStore<TextSegment> embeddingStore,
                                 EmbeddingService embeddingService){
        this.embeddingStore=embeddingStore;
        this.embeddingService=embeddingService;
    }

    public List<TextSegment> searchTopK(String query , String category) {

        Embedding embQuery = embeddingService.embedQuery(query);

        Filter Filter = (category != null && !category.equalsIgnoreCase("ALL"))
                ? metadataKey("category").isEqualTo(category.toUpperCase())
                : null;

        EmbeddingSearchRequest request = EmbeddingSearchRequest.builder()
                .queryEmbedding(embQuery)
                .maxResults(k)
                .minScore(0.6)
                .filter(Filter)
                .build();


        List<EmbeddingMatch<TextSegment>> matches = embeddingStore.search(request).matches();

        return matches.stream()
                .map(EmbeddingMatch::embedded)
                .toList();

    }
}