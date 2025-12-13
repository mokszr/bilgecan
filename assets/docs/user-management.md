## Bilgecan – User Management

User Management in Bilgecan handles authentication, authorization, and basic user identity.

Bilgecan currently supports **two system-level roles only**.

### User Roles


#### **ROLE_ADMIN**
System administrator role.

**Permissions:**
- Full access to all system features
- Feed knowledge base (RAG)
- Manage users (view, create, disable...)
- Create and manage workspaces
- Manage workspace memberships

---

#### **ROLE_USER**
Default role for regular users.

**Permissions:**
- Access personal AI features (Chat, Prompts, AI Tasks, Pipelines)
- Use workspace-shared resources if workspace role is applicable
- Use knowledge base in AI generations (RAG)

---

### User Creation
A **ROLE_ADMIN** user can create a new user account with the following required fields:
- **Email** (unique)
- **Username** (unique)
- **Password** (securely hashed)
- **Roles** selected new user will have

---

### Authentication
- Username or email-based login
- Passwords are securely hashed and never stored in plain text
- Integrated with **Spring Security**

---

### Authorization Model
- System-level roles control global permissions
- Workspace permissions are handled separately via workspace roles
- ROLE_ADMIN does **not** automatically override workspace roles. That means ROLE_ADMIN users cannot view AI resources of workspaces that they are not member of.

---

### Design Principles
- Minimal role model for clarity
- Strong separation between system roles and workspace roles
- Secure by default

---

## Other Features
1. [Chat](/assets/docs/chat.md)
2. [Prompts](/assets/docs/prompts.md)
3. [AI Tasks](/assets/docs/tasks.md)
4. [File Processing Pipelines](/assets/docs/file-processing-pipelines.md)
5. [Feed Knowledge Base (RAG)](/assets/docs/feed-rag.md)
6. [Workspaces](/assets/docs/workspaces.md)
7. [Settings](/assets/docs/settings.md)
8. [Dashboard](/assets/docs/dashboard.md)