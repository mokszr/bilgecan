package net.bilgecan.util;

import net.bilgecan.entity.Permission;
import net.bilgecan.entity.WorkspaceRole;

public class WorkspacePermissions {

    public static boolean can(WorkspaceRole role, Permission permission) {
        return switch (permission) {
            case VIEW -> role != null; // everyone in workspace
            case USE  -> role != WorkspaceRole.VIEWER;
            case EDIT -> role == WorkspaceRole.EDITOR;
        };
    }
}