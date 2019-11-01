package xyz.acrylicstyle.doubletimecommandsbungee.types;

import util.CollectionList;

import java.util.UUID;

public class Party {
    private final int id;
    private final UUID leader;
    private final CollectionList<Player> members;

    public Party(int id, UUID leader, CollectionList<Player> members) {
        this.id = id;
        this.leader = leader;
        this.members = members;
    }

    public final int getPartyId() { return this.id; }
    public final UUID getPartyLeader() { return this.leader; }
    public final CollectionList<Player> getMembers() { return this.members; }
}