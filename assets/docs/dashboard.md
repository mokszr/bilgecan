## Bilgecan – Dashboard

The Dashboard provides a real-time overview of Bilgecan’s system health, AI execution capacity, and available AI models.  
It is designed to give users and administrators quick visibility into whether the system is ready to execute AI tasks.

## Dashboard Sections

### **Worker Information**
Displays the unique **Worker ID** of the running Bilgecan instance.

**Purpose:**
- Identify the active worker in multi-instance setups
- Useful for debugging and logs

---

### **System Metrics**
Shows JVM and system-level resource usage.

**Metrics include:**
- JVM used memory (MB)
- JVM maximum memory (MB)
- Total system RAM (MB)
- Free system RAM (MB)
- Process CPU usage (%)
- System CPU usage (%)
- Available processors

**Use cases:**
- Monitor memory pressure
- Detect CPU bottlenecks
- Validate JVM sizing

---

### **Available LLMs**
Lists AI models currently available to Bilgecan (via Ollama).

**Details:**
- Shows installed and discoverable models
- Refresh button rechecks the model provider
  - Use this **Refresh** button when you install a new model in Ollama to make it visible to **Bilgecan**

---

### **AI Task Executor Pool Details**
Displays the status of the internal AI task execution pool.

**Metrics include:**
- **Active Count** – currently running AI tasks
- **Remaining Capacity** – how many tasks can still be executed concurrently

**Use cases:**
- Detect queue saturation
- Understand execution throughput
- Debug stuck or slow tasks

---

## Refresh Actions
Each dashboard card includes a **Refresh** button to fetch the latest values without reloading the page.

---

## Other Features
1. [Chat](/assets/docs/chat.md)
2. [Prompts](/assets/docs/prompts.md)
3. [AI Tasks](/assets/docs/tasks.md)
4. [File Processing Pipelines](/assets/docs/file-processing-pipelines.md)
5. [Feed Knowledge Base (RAG)](/assets/docs/feed-rag.md)
6. [Workspaces](/assets/docs/workspaces.md)
7. [User Management](/assets/docs/user-management.md)
8. [Settings](/assets/docs/settings.md)
