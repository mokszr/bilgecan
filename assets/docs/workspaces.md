## Bilgecan – Workspaces
Workspaces allow multiple users to **collaborate** inside Bilgecan by sharing AI resources. They are designed for **teams, companies, and groups** that want to **reuse prompts and AI tasks** without duplicating work.

Users are able to create prompts and AI tasks in workspaces they belong to. All users in same workspace can view, use or edit them **depending on their workspace role.**

Workspace management operations; 
- creating workspace 
- adding or removing users to workspaces 
- changing workspace role of users 

can be done only by users that have **ROLE_ADMIN** role.

### Key Concepts

### Workspace
A logical container that groups:
- Members
- Shared prompts
- Shared AI tasks

### Workspace Member
A user belongs to a workspace.  
Membership is required before accessing any workspace-owned resources.

### Workspace Roles
- **EDITOR**: Can view, create, edit and run AI resources.
- **MEMBER**: Can view and run AI resources.
- **VIEWER**: Can only view AI resources.

### How It Works
1. A user having `ROLE_ADMIN` creates a workspace and add users to it.
2. Editors can create prompts or AI tasks in workspace. 
4. Editors or members can run prompts or AI tasks in workspace.


| Action               | Editor | Member | Viewer |
|----------------------|--------|--------|-----|
| Create/Edit prompts  | ✅      | ❌      | ❌ |
| Create/Edit AI tasks | ✅      | ❌      | ❌ |
| Run prompts          | ✅      | ✅      | ❌ |
| Run AI Tasks         | ✅      | ✅      | ❌ |
| View prompts         | ✅      | ✅      | ✅ |
| View AI tasks        | ✅      | ✅      | ✅ |

### UI
Workspace prompts and AI tasks are reachable from Workspace Tools menu in sidebar.

---

## Other Features
1. [Chat](/assets/docs/chat.md)
2. [Prompts](/assets/docs/prompts.md)
3. [AI Tasks](/assets/docs/tasks.md)
4. [File Processing Pipelines](/assets/docs/file-processing-pipelines.md)
5. [Feed Knowledge Base (RAG)](/assets/docs/feed-rag.md)
6. [User Management](/assets/docs/user-management.md)
7. [Settings](/assets/docs/settings.md)
8. [Dashboard](/assets/docs/dashboard.md)
