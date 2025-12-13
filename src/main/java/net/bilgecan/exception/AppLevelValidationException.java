package net.bilgecan.exception;

import com.vaadin.flow.component.notification.Notification;

public class AppLevelValidationException extends RuntimeException {

    private Notification.Position position = Notification.Position.TOP_END;

    public AppLevelValidationException(String message) {
        super(message);
    }

    public AppLevelValidationException(String message, Notification.Position position) {
        super(message);
        this.position = position;
    }

    public Notification.Position getPosition() {
        return position;
    }
}
