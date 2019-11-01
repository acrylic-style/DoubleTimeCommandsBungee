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
    private final CollectionList<UUID> friends;
    private final CollectionList<UUID> friendRequests;

    public Player(UUID player, Ranks rank, CollectionList<UUID> friends, CollectionList<UUID> friendRequests) {
        this.player = player;
        this.rank = rank;
        this.friends = friends;
        this.friendRequests = friendRequests;
    }

    public final UUID getPlayer() { return this.player; }
    public final UUID getUniqueId() { return this.player; }
    public final Ranks getRank() { return this.rank; }
    public final CollectionList<UUID> getFriends() { return this.friends; }
    public final CollectionList<UUID> getFriendRequests() { return this.friendRequests; }
    public final ProxiedPlayer toProxiedPlayer() { return ProxyServer.getInstance().getPlayer(this.player); }
    public final ProxiedOfflinePlayer toProxiedOfflinePlayer() { return new ProxiedOfflinePlayer(this.player); }
}
