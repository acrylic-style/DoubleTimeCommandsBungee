package xyz.acrylicstyle.doubletimecommandsbungee.types;

import java.util.UUID;

public class Ban {
    private final int id;
    private final UUID player;
    private final String reason;
    private final long expires;
    private final UUID executor;

    public Ban(int id, UUID player, String reason, long expires, UUID executor) {
        this.id = id;
        this.player = player;
        this.reason = reason;
        this.expires = expires;
        this.executor = executor;
    }

    public final int getBanId() { return this.id; }
    public final UUID getPlayer() { return this.player; }
    public final String getReason() { return this.reason == null ? "None" : this.reason; }
    public final long getExpires() { return this.expires; }
    public final UUID getExecutor() { return this.executor; }
}
