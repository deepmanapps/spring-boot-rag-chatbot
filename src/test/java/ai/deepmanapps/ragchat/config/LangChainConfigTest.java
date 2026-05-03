package ai.deepmanapps.ragchat.config;

import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class LangChainConfigTest {

    private LangChainConfig config;

    @BeforeEach
    void setUp() {
        config = new LangChainConfig();
        // Inject @Value fields without starting the Spring context
        ReflectionTestUtils.setField(config, "geminiApiKey", "test-gemini-key");
        ReflectionTestUtils.setField(config, "pineconeApiKey", "test-pinecone-key");
        ReflectionTestUtils.setField(config, "pineconeIndex", "test-index");
    }

    // =========================================================================
    // embeddingModel()
    // =========================================================================

    @Test
    @DisplayName("embeddingModel() - returns a non-null AllMiniLmL6V2EmbeddingModel instance")
    void embeddingModel_returnsCorrectInstance() {
        EmbeddingModel model = config.embeddingModel();

        assertThat(model)
                .isNotNull()
                .isInstanceOf(AllMiniLmL6V2EmbeddingModel.class);
    }

    // =========================================================================
    // documentSplitter()
    // =========================================================================

    @Test
    @DisplayName("documentSplitter() - returns a non-null DocumentSplitter")
    void documentSplitter_returnsNonNull() {
        DocumentSplitter splitter = config.documentSplitter();

        assertThat(splitter).isNotNull();
    }

    @Test
    @DisplayName("documentSplitter() - splits a short text into at least one segment")
    void documentSplitter_splitsText() {
        DocumentSplitter splitter = config.documentSplitter();

        var segments = splitter.split(
                dev.langchain4j.data.document.Document.from("Hello world. This is a test document."));

        assertThat(segments).isNotEmpty();
    }
}
