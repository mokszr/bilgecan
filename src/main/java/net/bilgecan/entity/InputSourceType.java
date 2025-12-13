package net.bilgecan.entity;

public enum InputSourceType {
    //only file system is implemented for now, rest is open to extension
    FILE_SYSTEM, CLOUD_STORAGE, MESSAGE_QUEUE, DATABASE
}
