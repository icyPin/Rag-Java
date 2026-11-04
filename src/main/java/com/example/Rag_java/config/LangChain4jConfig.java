package com.example.Rag_java.config;

import com.example.Rag_java.service.EmbeddingService;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class LangChain4jConfig {

    @Value("${app.dbPassword}")
    private String dbPassword;
    @Bean
    public EmbeddingModel embeddingModel(){
        return OllamaEmbeddingModel.builder()
                .baseUrl("http://localhost:11434")
                .modelName("nomic-embed-text")
                .timeout(Duration.ofMinutes(2))
                .build();
    }

    @Bean
    public EmbeddingStore<TextSegment> embeddingStore(){
        return PgVectorEmbeddingStore.builder()
                .host("localhost")
                .port(5432)
                .database("postgres")
                .user("postgres")
                .password(dbPassword)
                .table("vector_store")
                .dimension(768)
                .build();
    }
}
