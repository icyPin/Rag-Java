package com.example.Rag_java.service;


import com.example.Rag_java.dto.PythonRequest;
import com.example.Rag_java.dto.PythonResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.sql.SQLOutput;

@Service
public class ModelHandlingService {

    private final RestClient client;

    public ModelHandlingService(RestClient.Builder builder){
        this.client=builder.baseUrl("http://127.0.0.1:8000").build();
    }

    @CircuitBreaker(name = "primaryService" , fallbackMethod = "fallBack")
    public PythonResponse getResponse(PythonRequest request){

        System.out.println("Primary service invoked...");
    }

    public PythonResponse fallBack(PythonRequest request , Throwable throwable){

        System.out.println("Fallback option bcs gemini sucks...");

    }
}
