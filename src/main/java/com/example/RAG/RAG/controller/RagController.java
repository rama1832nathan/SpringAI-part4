package com.example.RAG.RAG.controller;


import com.example.RAG.RAG.dto.ChatRequest;
import com.example.RAG.RAG.dto.ChatResponse;
import com.example.RAG.RAG.service.RagService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class RagController {
    private final RagService ragService;
    public RagController(RagService ragService) {
        this.ragService = ragService;
    }

    @PostMapping("/rag")
    public ChatResponse askDoc(@RequestBody ChatRequest chatRequest){
        String reply = ragService.askDocument(chatRequest.msg());
        return  new ChatResponse(reply,null);
    }
}
