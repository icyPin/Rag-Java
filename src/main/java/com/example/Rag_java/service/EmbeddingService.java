package com.example.Rag_java.service;


import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class EmbeddingService  {

    private final VectorStore vectorStore;

    public EmbeddingService(VectorStore vectorStore){
        this.vectorStore=vectorStore;
    }

    public void embedFile(Resource pdf){
        PagePdfDocumentReader pdfReader = new PagePdfDocumentReader(pdf);
        List<Document> rawDocument = pdfReader.get();

        TokenTextSplitter textSplitter = TokenTextSplitter.builder()
                .withChunkSize(100)
                .withMinChunkSizeChars(350)
                .withKeepSeparator(true)
                .build();

        List<Document> vector = textSplitter.transform(rawDocument);

        vectorStore.add(vector);
        System.out.println(vector);
    }

}
