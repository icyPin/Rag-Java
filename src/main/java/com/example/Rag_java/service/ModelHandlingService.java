package com.example.Rag_java.service;


import com.example.Rag_java.dto.ChatRequest;
import com.example.Rag_java.dto.PythonRequest;
import com.example.Rag_java.dto.PythonResponse;
import dev.langchain4j.model.chat.ChatLanguageModel;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.sql.SQLOutput;

@Slf4j
@Service
public class ModelHandlingService {

    private final ChatLanguageModel primary;
    private final ChatLanguageModel fallback;

    public ModelHandlingService(@Qualifier("primaryChatModel") ChatLanguageModel primary ,
                                @Qualifier("fallbackChatModel") ChatLanguageModel fallback){
        this.primary=primary;
        this.fallback=fallback;
    }

    @CircuitBreaker(name = "primaryService" , fallbackMethod = "fallBack")
    public PythonResponse getResponse(String prompt){

        log.info("gemini invoked...");
        String response = primary.generate(prompt);
        return new PythonResponse(response);
    }

    public PythonResponse fallBack(String prompt , Throwable throwable) {

        log.warn("fallback option bcs gemini sucks...");
        String response = fallback.generate(prompt);
        return new PythonResponse(response);
    }

}
