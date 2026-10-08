package com.example.Rag_java.dto;
import java .util.*;

public record PythonRequest(
        String query,
        List<String> context
) {}
