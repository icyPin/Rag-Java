package com.example.Rag_java.service;


import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RelivantChunksService {

    private final VectorStore vectorStore;
    private final int K;

    public RelivantChunksService(VectorStore vectorStore){
        this.vectorStore=vectorStore;
        K = 20;
    }

    public List<Document> searchTopK(String query){

        SearchRequest request = SearchRequest.builder()
                .query(query)
                .topK(K)
                .similarityThreshold(0.6)
                .build();

        List<Document> result = vectorStore.similaritySearch(request);
        System.out.println("retrived top 50 chunk match");

        for(Document docs: result){
            System.out.println("metadata: "+ docs.getMetadata());
            System.out.println("text: "+ docs.getText().strip());
            System.out.println("--------------------------------------------------");
        }
        return result;
    }
}