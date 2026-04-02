package com.example.RAG.RAG.service;

import jakarta.annotation.PostConstruct;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.w3c.dom.Text;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RagService {
    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    public RagService(ChatClient.Builder builder, EmbeddingModel embeddingModel) {
        this.vectorStore = SimpleVectorStore.builder(embeddingModel).build();

        this.chatClient = builder.defaultSystem("You are a helpful assistant.\n" +
                "                        Answer ONLY using the context provided to you.\n" +
                "                        If the answer is not in the context, say\n" +
                "                        \"I don't have that information in my documents.\"\n" +
                "                        Do not make up answers.").build();
    }

    @PostConstruct
    public void ingestDocument(){
        System.out.println("In ingesting document");


        //step 1 : extracting text from the document
        TextReader txt = new TextReader(new ClassPathResource("context.txt"));
        List<Document> doc = txt.get();
        System.out.println("Doc size "+doc.size());

        //step 2 : transform into chunks
        TokenTextSplitter splitter = new TokenTextSplitter();
        List<Document> chunks = splitter.split(doc);
        System.out.println("Chunks size "+chunks.size());


        //step 3 : Load the chunks in vector store
        vectorStore.add(chunks);
        System.out.println("Ingestion completed");
    }

    public String askDocument(String question){
        List<Document> relevantChunks = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(question).topK(3)
                        .build()
        );

        String context = relevantChunks.stream().map(Document :: getText)
                .collect(Collectors.joining("\n\n"));

        String promptClient = """
                Use the following context to answer the question. Context : """ + context + """
                """ + question;

        return chatClient.prompt(promptClient).call().content();
    }
}
