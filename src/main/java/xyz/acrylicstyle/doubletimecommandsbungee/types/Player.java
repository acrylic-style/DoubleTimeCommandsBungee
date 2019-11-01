package xyz.acrylicstyle.doubletimecommandsbungee.types;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import util.CollectionList;
import xyz.acrylicstyle.doubletimecommandsbungee.connection.ProxiedOfflinePlayer;
import xyz.acrylicstyle.doubletimecommandsbungee.utils.Ranks;

import java.util.UUID;

public class Player {
    private final UUID player;
    private final Ranks rank;
    private final long experience;
    private final long points;
    private final CollectionList<UUID> friends;
    private final CollectionList<UUID> friendRequests;
    private final boolean connected;
    private final String connectedServer;

    public Player(UUID player, Ranks rank, long experience, long points, CollectionList<UUID> friends, CollectionList<UUID> friendRequests, boolean connected, String connectedServer) {
        this.player = player;
        this.rank = rank;
        this.experience = experience;
        this.points = points;
        this.friends = friends;
        this.friendRequests = friendRequests;
        this.connected = connected;
        this.connectedServer = connectedServer;
    }

    public final UUID getPlayer() { return this.player; }
    public final UUID getUniqueId() { return this.player; }
    public final Ranks getRank() { return this.rank; }
    public final long getExperience() { return this.experience; }
    public final long getPoints() { return this.points; }
    public final CollectionList<UUID> getFriends() { return this.friends; }
    public final CollectionList<UUID> getFriendRequests() { return this.friendRequests; }
    public final ProxiedPlayer toProxiedPlayer() { return ProxyServer.getInstance().getPlayer(this.player); }
    public final ProxiedOfflinePlayer toProxiedOfflinePlayer() { return new ProxiedOfflinePlayer(this.player); }
    public final boolean isConnected() { return this.connected; }
    public final String getConnectedServer() { return this.connectedServer; }
}
