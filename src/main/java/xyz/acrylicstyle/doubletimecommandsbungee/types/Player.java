package xyz.acrylicstyle.doubletimecommandsbungee.types;

import util.CollectionList;
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
    public final Ranks getRank() { return this.rank; }
    public final CollectionList<UUID> getFriends() { return this.friends; }
    public final CollectionList<UUID> getFriendRequests() { return this.friendRequests; }
}
