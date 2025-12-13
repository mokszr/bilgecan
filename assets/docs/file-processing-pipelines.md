## Bilgecan – File Processing Pipelines

File Processing Pipelines allow Bilgecan to automatically process images (or any other type of files that Ollama model supports)
using AI Tasks. Basically, you can apply an AI task to **n number of files** and generate response for each one **asynchronously.** In order to understand this feature, please check [AI Tasks docs](/assets/docs/tasks.md) first.

File processing pipelines are ideal for repetitive workflows such as extracting structured data from files.

You can create a file processing pipeline as follows:

- Give a name for pipeline
- Select **input source type** (Currently, only file system input source is implemented.)
- Define file name regex pattern to filter files in selected input directory
- Remember root directory paths configured in **application-prod.properties**
```properties
bilgecan.rootInputFileDirectoryPath=/path/to/root/input/directory
bilgecan.rootOutputFileDirectoryPath=/path/to/root/output/directory
bilgecan.rootArchiveFileDirectoryPath=/path/to/root/archive/directory
``` 
- You need to create **subdirectories** under root input, output and archive directories.
- Put your files under an input subdirectory and select it in pipeline creation form.
- Select mime type of the files subject to processing. (i.e image/jpeg, image/png)
- Choose **AI task** to be applied to all files from the tasks you created beforehand.
- Select **output target type** (Currently, only file system output target is implemented)
- Select an output directory that you created as subdirectory under root output path.
- If you want to move input file to an archive directory select **"Move File To Archive"** option.
- Select an archive directory that you created as subdirectory under the archive root path.
- Save the form and the pipeline will be listed in the page.
- Click the **Run Pipeline** button on the listed item. One AI task per input file will be created and queued for asynchronous execution.
- Each file will be processed within one AI task run execution and response will be persisted as separate file under the selected output directory.
- If requested, input file is moved to archive directory selected, after ai task run is done.
- You can track progress of AI task runs from **Runs** page reached from sidebar menu.

---

## Other Features
1. [Chat](/assets/docs/chat.md)
2. [Prompts](/assets/docs/prompts.md)
3. [AI Tasks](/assets/docs/tasks.md)
4. [Feed Knowledge Base (RAG)](/assets/docs/feed-rag.md)
5. [Workspaces](/assets/docs/workspaces.md)
6. [User Management](/assets/docs/user-management.md)
7. [Settings](/assets/docs/settings.md)
8. [Dashboard](/assets/docs/dashboard.md)