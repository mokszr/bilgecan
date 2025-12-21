<p align="center">
  <img src="/assets/images/bilgecan_logo.png" alt="Bilgecan logo" width="200">
</p>

# Bilgecan – Open Source Local AI Platform

**Bilgecan** is a secure, flexible, and open-source AI platform that small and medium-sized companies and individual AI enthusiasts can run **on their own servers (self-hosted)**. It works with **local LLM** models through **Ollama**, without sending data to external services.

---
## What can you do with Bilgecan?

- Use **local LLM models via Ollama** to run privacy-friendly AI prompts and chat without sending your data to third parties.

- With **RAG (Retrieval-Augmented Generation)**, you can feed your own files into a knowledge base and enrich AI outputs with **your private data**.

- Define **asynchronous AI tasks** to run long operations (document analysis, report generation, large text processing, image analysis, etc.) in the background.

- Use the **file processing pipeline** to run asynchronous AI tasks over many files automatically.

- With the **Workspace** structure, you can share AI prompts and tasks with your team in a collaborative environment.

---
<p align="center">
  <img src="/assets/images/bilgecan_screenshot.jpg" alt="Bilgecan logo" width="500">
</p>

---

## Demo of Bilgecan Usage

[![Demo of Bilgecan usage](https://img.youtube.com/vi/n3wb7089NeE/0.jpg)](https://www.youtube.com/watch?v=n3wb7089NeE)

---

## Architecture Overview

Bilgecan is built using:

- **Spring Boot and Spring AI Backend** for API, worker execution, RAG, queues
- **Vaadin Frontend** (100% Java UI)
- **Ollama** for LLM inference
- **PostgreSQL + pgvector** for database and embeddings

---
## Installation
Bilgecan is a self-hosted, open-source Local AI Platform built with Spring Boot + Vaadin, designed to work with PostgreSQL + pgvector extension, Ollama, and local LLMs.
Follow the steps below to install and run Bilgecan on your machine or server.

### 1. Requirements
Before installing, ensure you have the following tools installed:
- **Java 24** or newer version 
  - You can use https://sdkman.io/ to install different versions of Java and easily switch between versions.
- [PostgreSQL](https://www.postgresql.org/download/) and [pgvector](https://github.com/pgvector/pgvector) extension
  - You can use [example docker-compose](assets/docker-compose-example.yaml) content here to start up PostgreSQL + pgvector easily.
    - Install [Docker](https://www.docker.com/) first if you don't have already
    - Copy the example docker-compose content into a docker-compose.yaml file and replace **/path/to/valid/bilgecan_postgre** with a valid directory path from your machine that DB data will be kept.
    - run `docker-compose up -d` to start the db
    - Connect to the db and make sure you enabled pgvector by executing 
        ```
        CREATE EXTENSION IF NOT EXISTS vector;
        ```
- Install [Ollama](https://ollama.com/download)
  - Default embedding model **mxbai-embed-large** is required. Install **mxbai-embed-large**
    ```bash
    ollama pull mxbai-embed-large
    ```
  - Install at least one model. You can install a vision model as well to process images.
    ```bash
    ollama pull llama3.1:8b
    ollama pull llama3.2-vision:11b
    ```
  - Be aware that which models can be run is strictly related with your hardware specs. See more on [Ollama community discussions.](https://www.reddit.com/r/ollama/)
### 2. Download Bilgecan 
#### Option A — Download Release JAR (recommended)

Go to:
👉 https://github.com/mokszr/bilgecan/releases

Download **bilgecan-\<version\>.jar**

#### Option B — Build from Source

```bash
git clone https://github.com/mokszr/bilgecan.git
cd bilgecan
./mvnw clean package -Pproduction 
```
The JAR will be generated under target:
```bash
target/bilgecan-<version>.jar
```
### 3.Configuration
Bilgecan uses Spring Boot configuration files. Copy [example application properties here](assets/application-example.properties) and put them in a **application-prod.properties** file in **config** directory aside the jar file.
```
bilgecan-<version>.jar
config/
    application-prod.properties 
```

- Edit server port value you would like to run bilgecan on
    ```properties
    server.port=8087
    ``` 
- Edit database connectivity details
    ```properties
    spring.datasource.url=jdbc:postgresql://localhost:5432/yourdb?currentSchema=public
    spring.datasource.username=youruser
    spring.datasource.password=yourpassword
    ``` 
- Edit Ollama base url. Ollama can be running on the same machine or a remote one. Put correct URL here
    ```properties
    spring.ai.ollama.base-url=http://localhost:11434
    ``` 
- Edit Spring AI related properties. You need to define a default chat model and define vectorstore schema and table names.
    ```properties
    # The model name you want to use (must be installed in your Ollama)
    # Examples: llama2, mistral, codellama, gemma, etc.
    spring.ai.ollama.chat.model=llama3.1:8b

    spring.ai.vectorstore.pgvector.initialize-schema=true
    spring.ai.vectorstore.pgvector.schema-name=public
    spring.ai.vectorstore.pgvector.table-name=vector_store
    ``` 
- Optionally, you can uncomment and edit max concurrent AI task execution (thread pool max pool size). But default value 1 would be good for most of the cases. It is **not recommended** to increase this thread pool size **unless you have greater than 32 GB of ram and a strong GPU.**
  ```properties
    # Tune these for your box (CPU/GPU concurrency) Default value is 1
    # It is not recommended to give a value greater than 1 if you don't have greater than 32GB of ram and a strong GPU.
    #bilgecan.maxConcurrentAITaskExecution=1
    ``` 
- Optionally you can edit AI task run date format. You can uncomment it and customize, or leave it commented if you would like to go with default format.
    ```properties
    #optioal run date format, if you want to customize it
    #bilgecan.runDateFormat=yyyy-MM-dd HH:mm:ss
    ``` 
- Edit root directory paths for file processing pipelines. You will be able to select subdirectories under these root directories in the app.
    ```properties
    bilgecan.rootInputFileDirectoryPath=/path/to/root/input/directory
    bilgecan.rootOutputFileDirectoryPath=/path/to/root/output/directory
    bilgecan.rootArchiveFileDirectoryPath=/path/to/root/archive/directory
    bilgecan.rootUploadFileDirectoryPath=/path/to/root/upload/directory
    ``` 
- Optionally edit logging options or leave them as is in the example properties file
    ```properties
    # Spring Boot default log file
    logging.file.name=log/bilgecan-prod.log

    ############################################
    # LOGBACK ROLLOVER PROPERTIES
    ############################################
    # Enable log archiving
    logging.logback.rollingpolicy.file-name-pattern=log/bilgecan-prod.%d{yyyy-MM-dd}.%i.gz
    
    # Max size of each log file before rollover
    logging.logback.rollingpolicy.max-file-size=20MB
    
    # Keep logs for 30 days
    logging.logback.rollingpolicy.max-history=30
    
    # Total size cap for all archived logs
    logging.logback.rollingpolicy.total-size-cap=1GB
    
    logging.level.root=ERROR
    logging.level.net.bilgecan=ERROR
    ``` 
#### What About Environment Variables?
You may want to use environment variables instead of application-prod.properties file, because of privacy concerns or for running bilgecan on cloud environments / dockerized. No worries, it is supported. 

To convert a property name in the canonical-form to an environment variable name you can follow these rules:

- Replace dots (.) with underscores (_).
- Remove any dashes (-).
- Convert to uppercase.

For example, the configuration property spring.main.log-startup-info would be an environment variable named **SPRING_MAIN_LOGSTARTUPINFO**.
spring.datasource.url would be **SPRING_DATASOURCE_URL**
bilgecan.rootInputFileDirectoryPath would be **BILGECAN_ROOTINPUTFILEDIRECTORYPATH**
etc...


### 4. 🚀 Run Bilgecan
- After preparing jar file,
- config directory and application-prod.properties file in the config directory,
- and making sure PostgreSQL DB up and running and enabling pgvector extension

go to the installation directory. Check java version
```bash
    java -version
```
It should be java 24 or a newer version.

Then run bilgecan:
```bash
    java -Dspring.profiles.active=prod  -jar bilgecan-<version>.jar
```

Now check http://localhost:8087 (change port with your configured value)

Default username and password is **root / root**

**It is strongly recommended to change password immediately by going Settings page and use Reset Password button**

### 5. Setting HTTPS with a self signed certificate

If you would like to serve bilgecan on https (recommended for security reasons) follow following steps to generate a self-signed certificate and configre in application-prod.properties file.

Create a PKCS12 keystore file that Spring Boot can use directly.
```bash
    keytool -genkeypair \
      -alias bilgecan \
      -keyalg RSA \
      -keysize 4096 \
      -storetype PKCS12 \
      -keystore bilgecan-keystore.p12 \
      -validity 3650
```
During the prompts:

- Keystore password → choose a strong password (e.g. changeit for local dev).

- First and last name → use the hostname you’ll use in the browser, e.g.: 
  - localhost for local development 
  - bilgecan.local or your real domain for more realistic tests

Other fields (organizational unit, etc.) can be anything for local use.  

You should now have a file:
```bash
bilgecan-keystore.p12
```
Move it somewhere appropriate, e.g.:
```bash
mkdir -p config/ssl
mv bilgecan-keystore.p12 config/ssl/
```
#### Configure Spring Boot for HTTPS

Uncomment and edit SSL realted configs in **application-prod.properties**
```properties
server.ssl.enabled=true

server.ssl.key-store=/absolute/path/to/config/ssl/bilgecan-keystore.p12

server.ssl.key-store-password=changeit
server.ssl.key-store-type=PKCS12
server.ssl.key-alias=bilgecan
```

Then run bilgecan:
```bash
    java -Dspring.profiles.active=prod  -jar bilgecan-<version>.jar
```
Now check https://localhost:8087 (change port with your configured value)

---

### 6. Dockerizing Bilgecan

After building the project you will have a jar file under target/ directory. You can use Dockerfile in the root of this repo to build docker image.

```bash
     docker build -t bilgecan:locallatest .
```
You can change `locallatest` with valid current version name.

#### Docker run command

Here is example docker run command. You need to **edit it and change values according to your machine installation**.

```bash
docker run --rm \
    --name bilgecan \
    -p 8087:8087 \
    -e SPRING_PROFILES_ACTIVE=prod \
    -e SPRING_DATASOURCE_URL="jdbc:postgresql://host.docker.internal:5433/bilgecandb?currentSchema=public" \
    -e SPRING_DATASOURCE_USERNAME=myuser \
    -e SPRING_DATASOURCE_PASSWORD=secret \
    -e SERVER_PORT=8087 \
    -e BILGECAN_ROOTINPUTFILEDIRECTORYPATH=/opt/bilgecan/rootDirs/input \
    -e BILGECAN_ROOTOUTPUTFILEDIRECTORYPATH=/opt/bilgecan/rootDirs/output \
    -e BILGECAN_ROOTARCHIVEFILEDIRECTORYPATH=/opt/bilgecan/rootDirs/archive \
    -e BILGECAN_ROOTUPLOADFILEDIRECTORYPATH=/opt/bilgecan/rootDirs/upload \
    -e SPRING_AI_OLLAMA_BASEURL=http://host.docker.internal:11434 \
    -e SPRING_AI_OLLAMA_CHAT_MODEL=llama3.1:8b \
    -e SPRING_AI_VECTORSTORE_PGVECTOR_INITIALIZESCHEMA=true \
    -e SPRING_AI_VECTORSTORE_PGVECTOR_SCHEMANAME=public \
    -e SPRING_AI_VECTORSTORE_PGVECTOR_TABLENAME=vector_store \
    -e LOGGING_FILE_NAME=/opt/bilgecan/log/bilgecan-prod.log \
    -e LOGGING_LEVEL_ROOT=ERROR \
    -e LOGGING_LEVEL_NET_BILGECAN=ERROR \
    -e LOGGING_LOGBACK_ROLLINGPOLICY_FILENAMEPATTERN=/opt/bilgecan/log/bilgecan-prod.%d{yyyy-MM-dd}.%i.gz \
    -e LOGGING_LOGBACK_ROLLINGPOLICY_MAXFILESIZE=20MB \
    -e LOGGING_LOGBACK_ROLLINGPOLICY_MAXHISTORY=30 \
    -e LOGGING_LOGBACK_ROLLINGPOLICY_TOTALSIZECAP=1GB \
    -e SERVER_SSL_ENABLED=true \
    -e SERVER_SSL_KEYSTORE=/opt/bilgecan/config/ssl/bilgecan-keystore.p12 \
    -e SERVER_SSL_KEYSTOREPASSWORD=changeit \
    -e SERVER_SSL_KEYSTORETYPE=PKCS12 \
    -e SERVER_SSL_KEYALIAS=bilgecan \
    -v "/absolute/path/to/config/ssl:/opt/bilgecan/config/ssl:ro" \
    -v "/absolute/path/to/bilgecan/log:/opt/bilgecan/log" \
    -v "/path/to/root/input/directory:/opt/bilgecan/rootDirs/input" \
    -v "/path/to/root/output/directory:/opt/bilgecan/rootDirs/output" \
    -v "/path/to/root/archive/directory:/opt/bilgecan/rootDirs/archive" \
    -v "/path/to/root/upload/directory:/opt/bilgecan/rootDirs/upload" \
    --add-host=host.docker.internal:host-gateway \
    bilgecan:locallatest
```

#### Using docker-compose

You can use this example [docker-compose-example-with-bilgecan.yaml](assets/docker-compose-example-with-bilgecan.yaml)  file, **edit values and paths according to your machine installation** and start PostgreSQL with pgvector and bilgecan docker container at the same time. 

```bash
    docker-compose up -d
```


---
## Feature Documentations
1. [Chat](/assets/docs/chat.md)
2. [Prompts](/assets/docs/prompts.md)
3. [AI Tasks](/assets/docs/tasks.md)
4. [File Processing Pipelines](/assets/docs/file-processing-pipelines.md)
5. [Feed Knowledge Base (RAG)](/assets/docs/feed-rag.md)
6. [Workspaces](/assets/docs/workspaces.md)
7. [User Management](/assets/docs/user-management.md)
8. [Settings](/assets/docs/settings.md)
9. [Dashboard](/assets/docs/dashboard.md)

---

## Open-source & Independent Developer Project

Bilgecan is fully open source and developed independently by **Murat Öksüzer**.

If Bilgecan has been helpful and you’d like to support the project, even a small donation provides great motivation.  
Every coffee means: new features, better documentation, and more educational videos.

---
## Support the Project ☕

If you’d like to support, you can buy me a coffee:

<a href="https://www.buymeacoffee.com/muratoksuzer" target="_blank">
  <img src="https://cdn.buymeacoffee.com/buttons/v2/default-yellow.png" alt="Buy Me A Coffee" height="60" width="217">
</a>

If you can’t donate, no worries — starring the GitHub repo, sharing the project, or giving feedback also helps a lot. ❤️

---

## Contact Me

I’d love to hear your thoughts, suggestions, and feedback about Bilgecan. You can reach me or follow the project through the links below:

- 📧 **Email:** [muratoksuzer01@gmail.com](mailto:muratoksuzer01@gmail.com)
- ⭐ **GitHub:** [Bilgecan Repository](https://github.com/mokszr/bilgecan)
- ▶️ **YouTube:** [@muratoksuzer](https://www.youtube.com/@muratoksuzer)
- 💼 **LinkedIn:** [My Profile](https://www.linkedin.com/in/murat-%C3%B6ks%C3%BCzer-bb856644/)
- 🕊️ **X (Twitter):** [@murat_dev01](https://x.com/murat_dev01)
- 🌐 **Website:** [www.muratoksuzer.com](https://www.muratoksuzer.com/)

---

## License

Bilgecan is licensed under the **Apache License 2.0**. See the full license text in the [LICENSE](LICENSE) file.

---

