## Bilgecan – Chat

You can generate AI responses to your prompts here interactively. Chat page keeps chat memory up to **50** messages. This helps AI to remember previous chat context.

### Select LLM Model

In the LLM Model selection combobox, you can select which model to use for your prompts.

### Use Your Knowledge Base (RAG) Option

You can enrich your AI responses with your own private data with **RAG (Retrieval-Augmented Generation)** technology. 
- First, you feed your own data, with plain text or PDF, DOCX etc files, into the knowledge base in **Feed Knowledge Base (RAG)** page.
  - Only users having `ROLE_ADMIN` role can feed knowledge base
- bilgecan converts your data into **embeddings** in vectorstore table. (pgvector)
- Then, you can enable your knowledge base to be used in prompt executions by checking this **Use Your Knowledge Base** checkbox.

### Add Photo / Image file with your prompt

There are some LLM models that support image file processing such as **llama3.2-vision:11b**. When you select such LLM model, you can use **Add photos & files** button to attach an image file.

Example prompt with an image attached and a vision model is selected:
```
What is in the picture?
```

## Other Features
1. [Prompts](/assets/docs/prompts.md)
2. [AI Tasks](/assets/docs/tasks.md)
3. [File Processing Pipelines](/assets/docs/file-processing-pipelines.md)
4. [Feed Knowledge Base (RAG)](/assets/docs/feed-rag.md)
5. [Workspaces](/assets/docs/workspaces.md)
6. [User Management](/assets/docs/user-management.md)
7. [Settings](/assets/docs/settings.md)
8. [Dashboard](/assets/docs/dashboard.md)