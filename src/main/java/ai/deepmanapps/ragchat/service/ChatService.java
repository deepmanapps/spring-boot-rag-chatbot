package ai.deepmanapps.ragchat.service;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.service.AiServices;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@Service
public class ChatService {

    private final ChatLanguageModel chatLanguageModel;
    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore embeddingStore;
    private final ContentRetriever contentRetriever;
    private final DocumentSplitter documentSplitter;

    interface RagAssistant {
        String chat(String userMessage);
    }

    private final RagAssistant ragAssistant;

    public ChatService(ChatLanguageModel chatLanguageModel,
                       EmbeddingModel embeddingModel,
                       EmbeddingStore embeddingStore,
                       ContentRetriever contentRetriever,
                       DocumentSplitter documentSplitter) {
        this.chatLanguageModel = chatLanguageModel;
        this.embeddingModel = embeddingModel;
        this.embeddingStore = embeddingStore;
        this.contentRetriever = contentRetriever;
        this.documentSplitter = documentSplitter;

        this.ragAssistant = AiServices.builder(RagAssistant.class)
                .chatLanguageModel(chatLanguageModel)
                .contentRetriever(contentRetriever)
                .build();
    }

    public String chat(String message) {
        return ragAssistant.chat(message);
    }

    public void processFile(MultipartFile file) throws Exception {
        try (InputStream inputStream = file.getInputStream()) {
            DocumentParser parser = new ApachePdfBoxDocumentParser();
            Document document = parser.parse(inputStream);

            EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                    .documentSplitter(documentSplitter)
                    .embeddingModel(embeddingModel)
                    .embeddingStore(embeddingStore)
                    .build();

            ingestor.ingest(document);
        }
    }
}
