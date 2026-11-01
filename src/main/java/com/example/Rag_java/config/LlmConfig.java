package com.example.Rag_java.config;


import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;

import java.time.Duration;

@Configuration
public class LlmConfig {

    @Bean("primaryChatModel")
    public GoogleAiGeminiChatModel primaryChatMode(@Value("dummy") String key){
        return GoogleAiGeminiChatModel.builder()
                .apiKey(key)
                .modelName("gemini-3.8-flash")
                .timeout(Duration.ofSeconds(10))
                .build();
    }

    @Bean("fallbackChatModel")
    public ChatLanguageModel fallbackChatModel(){
        return OllamaChatModel.builder()
                .baseUrl("http://localhost:11434")
                .modelName("qwen2.5-coder:7b")
                .timeout(Duration.ofMinutes(2))
                .build();
    }
}
