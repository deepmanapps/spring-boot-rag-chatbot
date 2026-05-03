package ai.deepmanapps.ragchat.controller;

import ai.deepmanapps.ragchat.service.ChatService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ChatController.class)
class ChatControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private ChatService chatService;

        // =========================================================================
        // GET /
        // =========================================================================

        @Test
        @DisplayName("GET / - returns HTTP 200 and renders the 'index' view")
        void getIndex_returns200AndIndexView() throws Exception {
                mockMvc.perform(get("/"))
                                .andExpect(status().isOk())
                                .andExpect(view().name("index"));
        }

        // =========================================================================
        // POST /chat
        // =========================================================================

        @Test
        @DisplayName("POST /chat - adds message and AI response to model, returns fragment")
        void postChat_happyPath() throws Exception {
                // Arrange
                when(chatService.chat("Hello")).thenReturn("Hi there!");

                // Act & Assert
                mockMvc.perform(post("/chat").param("message", "Hello"))
                                .andExpect(status().isOk())
                                .andExpect(view().name("index :: chat-message"))
                                .andExpect(model().attribute("message", "Hello"))
                                .andExpect(model().attribute("response", "Hi there!"));
        }

        @Test
        @DisplayName("POST /chat - when service throws, response attribute contains error text")
        void postChat_serviceException_returnsErrorMessage() throws Exception {
                // Arrange
                when(chatService.chat(eq("boom")))
                                .thenThrow(new RuntimeException("Model timeout"));

                // Act & Assert
                mockMvc.perform(post("/chat").param("message", "boom"))
                                .andExpect(status().isOk())
                                .andExpect(view().name("index :: chat-message"))
                                .andExpect(model().attribute("message", "boom"))
                                .andExpect(model().attributeExists("response"));
        }

        @Test
        @DisplayName("POST /chat - empty message is still forwarded to service")
        void postChat_emptyMessage_forwardsToService() throws Exception {
                when(chatService.chat("")).thenReturn("Please ask a question.");

                mockMvc.perform(post("/chat").param("message", ""))
                                .andExpect(status().isOk())
                                .andExpect(model().attribute("response", "Please ask a question."));
        }

        // =========================================================================
        // POST /upload
        // =========================================================================

        @Test
        @DisplayName("POST /upload - empty file returns error HTML snippet")
        void postUpload_emptyFile_returnsErrorSnippet() throws Exception {
                MockMultipartFile emptyFile = new MockMultipartFile(
                                "file", "empty.pdf", "application/pdf", new byte[0]);

                mockMvc.perform(multipart("/upload").file(emptyFile))
                                .andExpect(status().isOk())
                                .andExpect(content().string(containsString("Please select a file")));

                // Service must NOT be called for an empty file
                verify(chatService, never()).processFile(any());
        }

        @Test
        @DisplayName("POST /upload - valid file returns success HTML snippet")
        void postUpload_validFile_returnsSuccessSnippet() throws Exception {
                MockMultipartFile validFile = new MockMultipartFile(
                                "file", "doc.pdf", "application/pdf", "PDF content".getBytes());

                // processFile() returns void – no stubbing needed (Mockito default: do nothing)

                mockMvc.perform(multipart("/upload").file(validFile))
                                .andExpect(status().isOk())
                                .andExpect(content().string(containsString("successfully")));

                verify(chatService).processFile(any());
        }

        @Test
        @DisplayName("POST /upload - service exception returns failure HTML snippet")
        void postUpload_serviceException_returnsFailureSnippet() throws Exception {
                MockMultipartFile validFile = new MockMultipartFile(
                                "file", "bad.pdf", "application/pdf", "content".getBytes());
                doThrow(new RuntimeException("Parse error")).when(chatService).processFile(any());

                mockMvc.perform(multipart("/upload").file(validFile))
                                .andExpect(status().isOk())
                                .andExpect(content().string(containsString("Failed to process")));
        }
}
