package com.example.Rag_java.service;


import com.example.Rag_java.dto.ChatRequest;
import com.example.Rag_java.dto.PythonRequest;
import com.example.Rag_java.dto.PythonResponse;
import dev.langchain4j.model.chat.ChatModel;
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

    private final ChatModel primary;
    private final ChatModel fallback;

    public ModelHandlingService(@Qualifier("primaryChatModel") ChatModel primary ,
                                @Qualifier("fallbackChatModel") ChatModel fallback){
        this.primary=primary;
        this.fallback=fallback;
    }

    @CircuitBreaker(name = "primaryService" , fallbackMethod = "fallBack")
    public PythonResponse getResponse(String prompt){

        log.info("gemini invoked...");
        String response = primary.chat(prompt);
        return new PythonResponse(response);
    }

    public PythonResponse fallBack(String prompt , Throwable throwable) {

        log.warn("fallback option bcs gemini sucks...");
        String response = fallback.chat(prompt);
        return new PythonResponse(response);
    }

}
