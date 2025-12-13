package net.bilgecan.entity;

public enum OutputTargetType {
    //only file system is implemented for now, rest is open to extension
    FILE_SYSTEM, CLOUD_STORAGE, MESSAGE_QUEUE, DATABASE
}
