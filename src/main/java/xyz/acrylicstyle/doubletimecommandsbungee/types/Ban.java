package xyz.acrylicstyle.doubletimecommandsbungee.types;

import java.util.UUID;

public class Ban {
    private final int id;
    private final UUID player;
    private final String reason;
    private final int expires;

    public Ban(int id, UUID player, String reason, int expires) {
        this.id = id;
        this.player = player;
        this.reason = reason;
        this.expires = expires;
    }

    public final int getBanId() { return this.id; }
    public final UUID getPlayer() { return this.player; }
    public final String getReason() { return this.reason == null ? "None" : this.reason; }
    public final int getExpires() { return this.expires; }
}
