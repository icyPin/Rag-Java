package com.example.Rag_java.dto;
import java .util.*;

public record PythonRequest(
        String query,
        String context
) {}
