package com.example.Rag_java.service;


import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import javax.swing.text.Segment;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class EmbeddingService  {

    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;

    public void embedFile(Resource pdf){

        log.info("Processing the file or embedding: {}" , pdf.getFilename());

        try(InputStream stream = pdf.getInputStream()){
            ApachePdfBoxDocumentParser parser = new ApachePdfBoxDocumentParser();
            Document document = parser.parse(stream);

            List<TextSegment> segments = DocumentSplitters
                    .recursive(500,50)
                    .split(document);

            if(segments.isEmpty()){
                log.warn("empty pdf try another....");
                return;
            }

            Response<List<Embedding>> response = embeddingModel.embedAll(segments);
            List<Embedding> embeddings = response.content();

            embeddingStore.addAll(embeddings,segments);

            log.info("saved embedding of file {} with {} chunks" , pdf.getFilename() , segments.size());

        }
        catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    public Embedding  embedQuery(String query){
        return embeddingModel.embed(query).content();
    }
}
