package com.example.Rag_java.service;


import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
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
import org.apache.commons.codec.digest.DigestUtils;

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
    private final ModelHandlingService modelHandlingService;


    //use this for later
    public void embedFile(Resource pdf){

        String fileName = pdf.getFilename()!=null ? pdf.getFilename() : "unknown_pdf";

        log.info("Processing the file or embedding: {}" , fileName);

        try(InputStream hashStream = pdf.getInputStream();
            InputStream stream = pdf.getInputStream()){

            String fileHash = DigestUtils.sha256Hex(hashStream);
            ApachePdfBoxDocumentParser parser = new ApachePdfBoxDocumentParser();
            Document document = parser.parse(stream);

            List<TextSegment> Texts= DocumentSplitters
                    .recursive(800 ,0)
                    .split(document);
            String first800 = Texts.getFirst().text();
            String category = modelHandlingService.getCategory(first800);
            log.info(category);


            List<TextSegment> segments = DocumentSplitters
                    .recursive(500,50)
                    .split(document);

            if(segments.isEmpty()){
                log.warn("empty pdf try another....");
                return;
            }
            List<TextSegment> enrichedEmbeddings = segments.stream()
                    .map(seg -> {
                        Metadata metadata = Metadata.from(Map.of(
                                "file_name",fileName,
                                "file_hash",fileHash,
                                "category",category

                        ));
                        return TextSegment.from(seg.text() , metadata);
                    }).toList();

            Response<List<Embedding>> response = embeddingModel.embedAll(enrichedEmbeddings);
            List<Embedding> embeddings = response.content();

            embeddingStore.addAll(embeddings,enrichedEmbeddings);
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
