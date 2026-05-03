package ai.deepmanapps.ragchat.service;

import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ChatServiceTest {

    @Mock
    private ChatLanguageModel chatLanguageModel;
    @Mock
    private EmbeddingModel embeddingModel;
    @SuppressWarnings("rawtypes")
    @Mock
    private EmbeddingStore embeddingStore;
    @Mock
    private ContentRetriever contentRetriever;
    @Mock
    private DocumentSplitter documentSplitter;

    private ChatService chatService;

    @BeforeEach
    void setUp() {
        when(chatLanguageModel.generate(any(List.class)))
                .thenReturn(Response.from(AiMessage.from("stub")));

        when(embeddingModel.embedAll(any()))
                .thenReturn(Response.from(List.of()));

        chatService = new ChatService(
                chatLanguageModel,
                embeddingModel,
                embeddingStore,
                contentRetriever,
                documentSplitter);
    }

    @Test
    @DisplayName("chat() - returns the reply produced by the RAG assistant")
    void chat_returnsAssistantReply() {
        when(chatLanguageModel.generate(any(List.class)))
                .thenReturn(Response.from(AiMessage.from("Paris is the capital of France.")));

        String reply = chatService.chat("What is the capital of France?");

        assertThat(reply).isEqualTo("Paris is the capital of France.");
    }

    @Test
    @DisplayName("chat() - delegates the message to the language model")
    void chat_delegatesToLanguageModel() {
        chatService.chat("Hello");

        verify(chatLanguageModel, atLeastOnce()).generate(any(List.class));
    }

    @Test
    @DisplayName("chat() - propagates runtime exceptions from the language model")
    void chat_propagatesExceptionFromModel() {
        when(chatLanguageModel.generate(any(List.class)))
                .thenThrow(new RuntimeException("Model unavailable"));

        assertThatThrownBy(() -> chatService.chat("Hello"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Model unavailable");
    }

    @Test
    @DisplayName("processFile() - successfully ingests a valid single-page PDF")
    void processFile_successfulIngestion() throws Exception {
        byte[] pdfBytes = buildMinimalPdf("Hello from test PDF.");
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.pdf", "application/pdf", pdfBytes);

        assertThatCode(() -> chatService.processFile(file))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("processFile() - throws when the multipart file cannot be read")
    void processFile_throwsOnIOException() throws IOException {
        MockMultipartFile brokenFile = mock(MockMultipartFile.class);
        when(brokenFile.getInputStream()).thenThrow(new IOException("Disk read error"));

        assertThatThrownBy(() -> chatService.processFile(brokenFile))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("Disk read error");
    }

    @SuppressWarnings("deprecation")
    private static byte[] buildMinimalPdf(String text) throws IOException {
        try (PDDocument doc = new PDDocument();
                ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            PDPage page = new PDPage();
            doc.addPage(page);

            PDFont font = PDType1Font.HELVETICA;

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                cs.beginText();
                cs.setFont(font, 12);
                cs.newLineAtOffset(50, 700);
                cs.showText(text);
                cs.endText();
            }
            doc.save(out);
            return out.toByteArray();
        }
    }
}
