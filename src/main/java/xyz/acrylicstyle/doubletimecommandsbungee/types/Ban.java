package xyz.acrylicstyle.doubletimecommandsbungee.types;

import java.util.UUID;

public class Ban {
    private final int id;
    private final UUID player;
    private final String reason;
    private final long expires;
    private final UUID executor;
    private final UUID unbanner;

    public Ban(int id, UUID player, String reason, long expires, UUID executor, String unbanner) {
        this.id = id;
        this.player = player;
        this.reason = reason;
        this.expires = expires;
        this.executor = executor;
        if (unbanner != null) this.unbanner = UUID.fromString(unbanner); else this.unbanner = null;
    }

    public final int getBanId() { return this.id; }
    public final UUID getPlayer() { return this.player; }
    public final String getReason() { return this.reason == null ? "None" : this.reason; }
    public final long getExpires() { return this.expires; }
    public final UUID getExecutor() { return this.executor; }
    public final UUID getUnbanner() { return this.unbanner; }
}
