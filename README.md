# 🤖 Spring Boot RAG Chatbot

A **Retrieval-Augmented Generation (RAG)** chatbot built with Spring Boot. Upload PDF documents, index them into Pinecone vector database, and ask questions about their content — powered by Google Gemini AI.

---

## ✨ Features

- 📄 **PDF Upload & Indexing** — Upload PDF files and automatically chunk, embed, and store them in Pinecone
- 💬 **Contextual Chat** — Ask questions grounded in your uploaded documents using RAG
- ⚡ **Reactive UI** — Built with HTMX + Alpine.js for a smooth, SPA-like experience without a JavaScript framework
- 🔒 **Environment-based Config** — API keys loaded securely from `.env` via `spring-dotenv`

---

## 🏗️ Architecture

```
User (Browser)
    │
    ▼
ChatController  ──────────────────────────────────────────┐
    │                                                      │
    ▼                                                      ▼
ChatService                                         POST /upload
    │                                                      │
    ├─► RagAssistant (LangChain4j AiServices proxy)        ▼
    │       │                                     EmbeddingStoreIngestor
    │       ├─► Google Gemini (ChatLanguageModel)      │
    │       └─► EmbeddingStoreContentRetriever         ├─► ApachePdfBoxDocumentParser
    │                   │                              ├─► DocumentSplitter (recursive 500/50)
    │                   ▼                              ├─► AllMiniLmL6V2EmbeddingModel
    │           Pinecone Vector DB ◄───────────────────┘
    │
    ▼
Thymeleaf (index.html) — HTMX fragments swap into page
```

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Backend | Spring Boot 3.2, Java 17 |
| AI Orchestration | LangChain4j 0.36.2 |
| LLM | Google Gemini 2.5 Pro |
| Embedding Model | AllMiniLM-L6-v2 (local, no API cost) |
| Vector Store | Pinecone (serverless) |
| PDF Parsing | Apache PDFBox (via LangChain4j) |
| Frontend | Thymeleaf + HTMX + Alpine.js + Tailwind CSS CDN |
| Config | `spring-dotenv` (`.env` file support) |
| Testing | JUnit 5, Mockito, Spring MockMvc |

---

## 🚀 Getting Started

### Prerequisites

- Java 17+
- Maven 3.8+
- A [Google AI Studio](https://aistudio.google.com/) API key (Gemini)
- A [Pinecone](https://www.pinecone.io/) account with a serverless index (dimension: **384**, metric: **cosine**)

### 1. Clone the repository

```bash
git clone https://github.com/deepmanapps/spring-boot-rag-chatbot.git
cd spring-boot-rag-chatbot
```

### 2. Create your Pinecone index

Log in to Pinecone and create a **serverless** index with:
- **Dimensions:** `384` (matches AllMiniLM-L6-v2 output)
- **Metric:** `cosine`

### 3. Configure environment variables

Create a `.env` file in the project root:

```env
GEMINI_API_KEY=your-google-gemini-api-key
PINECONE_API_KEY=your-pinecone-api-key
PINECONE_INDEX=your-index-name
```

> **Note:** `.env` is automatically loaded at startup by `spring-dotenv`. Never commit this file — it is already in `.gitignore`.

### 4. Run the application

```bash
mvn spring-boot:run
```

The app starts on **http://localhost:8080**.

---

## 💡 How to Use

1. Open **http://localhost:8080** in your browser
2. Use the **sidebar** to upload a PDF — it will be parsed, chunked, embedded, and indexed into Pinecone
3. Type a question in the chat box and press **Enter** (or click the send button)
4. The AI answers based on the content of your uploaded documents

---

## 🧪 Running Tests

```bash
mvn test
```

Tests are written following the **TDD** approach:

| Test Class | Tests | Scope |
|---|---|---|
| `ChatServiceTest` | 5 | Unit — mocks all LangChain4j collaborators |
| `ChatControllerTest` | 7 | Web slice — `@WebMvcTest` with MockMvc |
| `LangChainConfigTest` | 3 | Unit — verifies `@Bean` factory methods |

**Total: 15 tests**

---

## 📁 Project Structure

```
src/
├── main/
│   ├── java/ai/deepmanapps/ragchat/
│   │   ├── RagChatApplication.java       # Spring Boot entry point
│   │   ├── config/
│   │   │   └── LangChainConfig.java      # All LangChain4j @Bean definitions
│   │   ├── controller/
│   │   │   └── ChatController.java       # HTTP endpoints: GET /, POST /chat, POST /upload
│   │   └── service/
│   │       └── ChatService.java          # RAG logic: chat & PDF ingestion
│   └── resources/
│       ├── application.properties        # App config (references .env vars)
│       └── templates/
│           └── index.html                # Thymeleaf + HTMX + Alpine.js UI
└── test/
    └── java/ai/deepmanapps/ragchat/
        ├── config/LangChainConfigTest.java
        ├── controller/ChatControllerTest.java
        └── service/ChatServiceTest.java
```

---

## ⚙️ Configuration Reference

| Property | Env Variable | Description |
|---|---|---|
| `gemini.api.key` | `GEMINI_API_KEY` | Google Gemini API key |
| `pinecone.api.key` | `PINECONE_API_KEY` | Pinecone API key |
| `pinecone.index` | `PINECONE_INDEX` | Pinecone index name |
| `spring.servlet.multipart.max-file-size` | — | Max upload size (default: 10MB) |

