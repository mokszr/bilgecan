## Bilgecan – AI Tasks
**AI Tasks** are Bilgecan's core execution units for running prompts **asynchronously**. This gives power to **file processing pipelines** as well to apply one AI task to n number of files at the background.

In the **Tasks** page, you can save and manage your AI task templates. An AI task template includes

- Name of the AI task
- User prompt
- System prompt (optional)
- JSON Schema (optional) if you want structured JSON result.
- LLM model to run against

When you save a task, you can use **`Run`** button in the list items, to put the task in queue to be executed asynchronously. Then you can track AI task run in **Runs** page, reachable from sidebar.

### AI Task Runs

You can track the executed or to be executed task runs in this page. A task run can be in one the following states:

- **PENDING**
- **RUNNING**
- **DONE**
- **FAILED**
- **CANCELED**

When you select a run in **DONE** state, you can see result generated and execution details in the right panel of the page.

You can **cancel** or **delete** a run by using buttons under the run items in the list. 

For **workspace** AI task runs, you need to have enough permission to delete or cancel.

---

## Other Features
1. [Chat](/assets/docs/chat.md)
2. [Prompts](/assets/docs/prompts.md)
3. [File Processing Pipelines](/assets/docs/file-processing-pipelines.md)
4. [Feed Knowledge Base (RAG)](/assets/docs/feed-rag.md)
5. [Workspaces](/assets/docs/workspaces.md)
6. [User Management](/assets/docs/user-management.md)
7. [Settings](/assets/docs/settings.md)
8. [Dashboard](/assets/docs/dashboard.md)




