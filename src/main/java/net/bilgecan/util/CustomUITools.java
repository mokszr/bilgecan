package net.bilgecan.util;

import net.bilgecan.entity.AITaskStatus;

public class CustomUITools {

    public static String getStatusBadge(AITaskStatus status) {
        if(status == null) {
            return "";
        }
        switch (status) {
            case PENDING -> {
                return "contrast";
            }
            case DONE -> {
                return "success";
            }
            case FAILED -> {
                return "error";
            }
            case RUNNING -> {
                return "";
            }
            case CANCELED -> {
                return "canceled";
            }
            default -> {
                return "";
            }
        }
    }
}
