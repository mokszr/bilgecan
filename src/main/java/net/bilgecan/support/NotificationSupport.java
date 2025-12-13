package net.bilgecan.support;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;

public class NotificationSupport {

    public static final int DEFAULT_SUCCESS_DURATION = 1500;
    public static final int DEFAULT_ERROR_DURATION = 2000;
    public static final int DEFAULT_INFO_DURATION = 1500;

    public static void showInfo(String message) {

        Notification notification = Notification.show(message);
        notification.addThemeVariants(NotificationVariant.LUMO_PRIMARY);
        notification.setPosition(Notification.Position.TOP_END);
        notification.setDuration(DEFAULT_INFO_DURATION);
    }

    public static void showSuccess(String message) {

        Notification notification = Notification.show(message);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        notification.setPosition(Notification.Position.TOP_END);
        notification.setDuration(DEFAULT_SUCCESS_DURATION);
    }

    public static void showSuccess(String message, int duration) {

        Notification notification = Notification.show(message);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        notification.setPosition(Notification.Position.TOP_END);
        notification.setDuration(duration);
    }

    public static void showError(String message, int duration) {

        Notification notification = Notification.show(message);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        notification.setPosition(Notification.Position.TOP_END);
        notification.setDuration(duration);
    }

    public static void showError(String message, int duration, Notification.Position position) {

        Notification notification = Notification.show(message);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        notification.setPosition(position);
        notification.setDuration(duration);
    }

    public static void showError(String message) {

        Notification notification = Notification.show(message);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        notification.setPosition(Notification.Position.TOP_END);
        notification.setDuration(DEFAULT_ERROR_DURATION);
    }
}
