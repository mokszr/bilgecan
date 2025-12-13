## Bilgecan – Feed Knowledge Base (RAG)
Bilgecan allows you to **ingest** documents into your knowledge base and use them in Retrieval-Augmented Generation (RAG).
This feature lets you enrich your AI generations **with context** from **PDFs, text files, DOCs,** or other supported formats—all processed locally and securely.

### How It Works

1. Upload or drop files into the Feed Knowledge Base UI.
   2. Optionally you can insert plain text manually by selecting **Feed Method** as **User Text.**
2. Bilgecan extracts text depending on the file type using **Apache Tika**.
3. Extracted contents are converted into embeddings using your locally installed Ollama model:
```
mxbai-embed-large
```
This model is a **must-have.** You need to install if it doesn't exist in your Ollama installation.
```bash
ollama pull mxbai-embed-large
```
4. Embeddings are stored in your **vector store (PostgreSQL + pgvector).**
5. AI prompt executions can now retrieve relevant chunks from vector store when you enable **use RAG**.
6. By this way your AI generations will be enriched with your own private data.

### Supported File Types

Bilgecan currently supports:

- PDF documents
- Plain text files
- HTML files
- DOC/DOCX files
- PowerPoint files (PPT/PPTX)
- Or you can put text manually with **user text** feed method.

### Feed RAG History

Once you submit your feed, a feed history item is created and listed in the page.

You can also delete that feed RAG history, also **cleaning/removing all embeddings related with that feed submit.**

### Best Practices

- Keep documents **fact-dense** and **well-structured** for best embedding quality.
- Prefer PDFs over screenshots when possible.
- Use consistent formatting across documents for predictable chunking.
- Periodically clean old or unused entries to maintain vector store performance.

---

## Other Features
1. [Chat](/assets/docs/chat.md)
2. [Prompts](/assets/docs/prompts.md)
3. [AI Tasks](/assets/docs/tasks.md)
4. [File Processing Pipelines](/assets/docs/file-processing-pipelines.md)
5. [Workspaces](/assets/docs/workspaces.md)
6. [User Management](/assets/docs/user-management.md)
7. [Settings](/assets/docs/settings.md)
8. [Dashboard](/assets/docs/dashboard.md)