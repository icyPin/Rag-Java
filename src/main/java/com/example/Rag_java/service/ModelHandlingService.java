package com.example.Rag_java.service;


import com.example.Rag_java.dto.PythonRequest;
import com.example.Rag_java.dto.PythonResponse;
import org.springframework.stereotype.Service;

@Service
public class ModelHandlingService {

    public PythonResponse call(PythonRequest request){
        return new PythonResponse("hhhh");
    }
}
