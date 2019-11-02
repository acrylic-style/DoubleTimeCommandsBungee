package xyz.acrylicstyle.doubletimecommandsbungee.types;

import java.util.regex.Pattern;

public class Incident {
    private final int id;
    private final int user_id;
    private final int component_id;
    private final String name;
    private final int status;
    private final int visible;
    private final boolean sticked;
    private final boolean notifications;
    private final String message;
    private final boolean is_resolved;

    public Incident(int id, int user_id, int component_id, String name, int status, int visible, boolean sticked, boolean notifications, String message, boolean is_resolved) {
        this.id = id;
        this.user_id = user_id;
        this.component_id = component_id;
        this.name = name;
        this.status = status;
        this.visible = visible;
        this.sticked = sticked;
        this.notifications = notifications;
        this.message = message.replaceAll(Pattern.quote("\\"), "").replaceAll("\\[.*]\\((.*)\\)", "$1");
        this.is_resolved = is_resolved;
    }

    public final int getId() { return this.id; }
    public final int getUserId() { return this.user_id; }
    public final int getComponentId() { return this.component_id; }
    public final String getName() { return this.name; }
    public final int getStatus() { return this.status; }
    public final int getVisible() { return this.visible; }
    public final boolean isSticked() { return this.sticked; }
    public final boolean isNotifications() { return this.notifications; }
    public final String getMessage() { return this.message; }
    public final boolean isResolved() { return this.is_resolved; }
}
