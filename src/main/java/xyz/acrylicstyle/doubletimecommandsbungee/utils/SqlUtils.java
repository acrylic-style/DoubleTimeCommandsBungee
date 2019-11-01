package xyz.acrylicstyle.doubletimecommandsbungee.utils;

import net.md_5.bungee.api.ProxyServer;
import util.CollectionList;
import xyz.acrylicstyle.doubletimecommandsbungee.annotations.NonNull;
import xyz.acrylicstyle.doubletimecommandsbungee.annotations.Nullable;
import xyz.acrylicstyle.doubletimecommandsbungee.types.Ban;
import xyz.acrylicstyle.doubletimecommandsbungee.types.Party;
import xyz.acrylicstyle.doubletimecommandsbungee.types.Player;

import java.math.BigDecimal;
import java.sql.*;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public final class SqlUtils {
    public static void loadDriver() throws ClassNotFoundException {
        if (init.get()) throw new IllegalStateException("Driver is already loaded!");
        Class.forName("com.mysql.jdbc.Driver");
        init.set(true);
    }

    public static Connection connect(String host, String database, String user, String password) throws SQLException {
        if (!init.get()) throw new IllegalStateException("Driver isn't loaded! (Did you call SqlUtils#loadDriver?)");
        String url =  "jdbc:mysql://" + host + "/" + database;
        connection.set(DriverManager.getConnection(url, user, password));
        sync();
        return connection.get();
    }

    public static Connection getConnection() {
        return connection.get();
    }

    public static void close() throws SQLException {
        if (connection.get() == null) throw new IllegalStateException("Connection haven't made.");
        connection.get().close();
        connection.set(null);
    }

    private static void sync() throws SQLException {
        sync(false);
    }

    private static void sync(boolean force) throws SQLException {
        if (connection.get() == null) throw new IllegalStateException("Connection haven't made.");
        Statement statement = connection.get().createStatement();
        if (force) statement.executeUpdate("drop table if exists bans;");
        if (force) statement.executeUpdate("drop table if exists players;");
        if (force) statement.executeUpdate("drop table if exists friends;");
        if (force) statement.executeUpdate("drop table if exists friend_requests;");
        if (force) statement.executeUpdate("drop table if exists party_invites;");
        if (force) statement.executeUpdate("drop table if exists parties;");
        if (force) statement.executeUpdate("drop table if exists party_members;");
        statement.executeUpdate("CREATE TABLE if not exists bans (\n" +
                "        id INT NOT NULL AUTO_INCREMENT,\n" +
                "        player VARCHAR(36) NOT NULL,\n" + // uuid
                "        reason VARCHAR(666) not null default 'None',\n" +
                "        bannedAt BIGINT(255) NOT NULL,\n" +
                "        expires BIGINT(255) NOT NULL,\n" +
                "        executor VARCHAR(36),\n" + // uuid
                "        unbanner VARCHAR(36) default null,\n" + // uuid
                "        PRIMARY KEY (id)\n" +
                "    );");
        statement.executeUpdate("CREATE TABLE if not exists players (\n" +
                "        player VARCHAR(36) NOT NULL,\n" + // uuid
                "        rank VARCHAR(100) default 'DEFAULT',\n" +
                "        id VARCHAR(100) NOT NULL,\n" +
                "        experience BIGINT(255) NOT NULL default 0,\n" +
                "        points BIGINT(255) NOT NULL default 0,\n" +
                "        PRIMARY KEY (player)\n" +
                "    );");
        statement.executeUpdate("CREATE TABLE if not exists friends (\n" +
                "       player VARCHAR(36) NOT NULL,\n" +
                "       player2 VARCHAR(36) NOT NULL,\n" +
                "       primary key (player)\n" +
                "    );");
        statement.executeUpdate("CREATE TABLE if not exists friend_requests (\n" +
                "       player VARCHAR(36) NOT NULL,\n" +
                "       player2 VARCHAR(36) NOT NULL,\n" +
                "       primary key (player)\n" +
                "    );");
        statement.executeUpdate("create table if not exists party_invites (\n" +
                "       party_id int not null,\n" +
                "       member varchar(36) not null,\n" + // uuid
                "       primary key (party_id)" +
                ");");
        statement.executeUpdate("create table if not exists parties (\n" +
                "       party_id int not null auto_increment,\n" +
                "       leader varchar(36) not null,\n" + // uuid
                "       primary key (party_id)\n" +
                ");");
        statement.executeUpdate("create table if not exists party_members (\n" +
                "       party_id int not null,\n" +
                "       member varchar(36) not null,\n" + // uuid
                "       primary key(party_key)\n" +
                ");");
    }

    public static boolean isPartyLeader(UUID player) throws SQLException {
        Validate.notNull(player);
        return getPartyLeader(getPartyId(player)).getUniqueId().equals(player);
    }

    public static boolean isPartyLeader(int party_id, UUID player) throws SQLException {
        Validate.notNull(party_id, player);
        return getPartyLeader(party_id).getUniqueId().equals(player);
    }

    public static boolean inParty(UUID player) throws SQLException {
        Validate.notNull(player);
        PreparedStatement preparedStatement = connection.get().prepareStatement("select * from party_members where member=?;");
        preparedStatement.setString(1, player.toString());
        ResultSet result = preparedStatement.executeQuery();
        boolean isInParty = result.next();
        result.close();
        return isInParty;
    }

    public static Player getPartyLeader(int party_id) throws SQLException {
        Validate.notNull(party_id);
        PreparedStatement preparedStatement = connection.get().prepareStatement("select leader from parties where party_id=?;");
        preparedStatement.setInt(1, party_id);
        ResultSet result = preparedStatement.executeQuery();
        result.next();
        UUID player = UUID.fromString(result.getString("leader"));
        result.close();
        return getPlayer(player);
    }

    public static void disbandParty(int party_id) throws SQLException {
        Validate.notNull(party_id);
        PreparedStatement preparedStatement = connection.get().prepareStatement("delete from parties where party_id=?;");
        preparedStatement.setInt(1, party_id);
        preparedStatement.executeUpdate();
        preparedStatement = connection.get().prepareStatement("delete from party_invites where party_id=?;");
        preparedStatement.setInt(1, party_id);
        preparedStatement.executeUpdate();
        preparedStatement = connection.get().prepareStatement("delete from party_members where party_id=?;");
        preparedStatement.setInt(1, party_id);
        preparedStatement.executeUpdate();
    }

    public static void resetParty() throws SQLException {
        PreparedStatement preparedStatement = connection.get().prepareStatement("delete from parties where true;");
        preparedStatement.executeUpdate();
        preparedStatement = connection.get().prepareStatement("delete from party_invites where true;");
        preparedStatement.executeUpdate();
        preparedStatement = connection.get().prepareStatement("delete from party_members where true;");
        preparedStatement.executeUpdate();
    }

    public static void leaveParty(UUID member) throws SQLException {
        Validate.notNull(member);
        PreparedStatement preparedStatement = connection.get().prepareStatement("delete from party_members where member=?;");
        preparedStatement.setString(1, member.toString());
        preparedStatement.executeUpdate();
    }

    public static Party createParty(UUID leader) throws SQLException {
        Validate.notNull(leader);
        PreparedStatement preparedStatement = connection.get().prepareStatement("insert into parties values (default, ?);");
        preparedStatement.setString(1, leader.toString());
        preparedStatement.executeUpdate();
        Party party = getParty(leader);
        addPartyMember(party.getPartyId(), leader);
        return getParty(party.getPartyId());
    }

    public static CollectionList<Player> getPartyInvites(int party_id) throws SQLException {
        Validate.notNull(party_id);
        PreparedStatement preparedStatement = connection.get().prepareStatement("select member from party_invites where party_id=?;");
        preparedStatement.setInt(1, party_id);
        ResultSet result = preparedStatement.executeQuery();
        CollectionList<Player> players = new CollectionList<>();
        while (result.next()) {
            players.add(getPlayer(UUID.fromString(result.getString("member"))));
        }
        return players;
    }

    public static CollectionList<UUID> getPartyInvitesAsUniqueId(int party_id) throws SQLException {
        Validate.notNull(party_id);
        PreparedStatement preparedStatement = connection.get().prepareStatement("select member from party_invites where party_id=?;");
        preparedStatement.setInt(1, party_id);
        ResultSet result = preparedStatement.executeQuery();
        CollectionList<UUID> players = new CollectionList<>();
        while (result.next()) {
            players.add(UUID.fromString(result.getString("member")));
        }
        return players;
    }

    public static void removePartyInvite(UUID member) throws SQLException {
        Validate.notNull(member);
        PreparedStatement preparedStatement = connection.get().prepareStatement("delete from party_invites where member=?;");
        preparedStatement.setString(1, member.toString());
        preparedStatement.executeUpdate();
    }

    public static void addPartyInvite(int party_id, UUID member) throws SQLException {
        Validate.notNull(party_id, member);
        PreparedStatement preparedStatement = connection.get().prepareStatement("insert into party_invites values (?, ?);");
        preparedStatement.setInt(1, party_id);
        preparedStatement.setString(2, member.toString());
        preparedStatement.executeUpdate();
    }

    public static void addPartyMember(int party_id, UUID member) throws SQLException {
        Validate.notNull(member);
        PreparedStatement preparedStatement = connection.get().prepareStatement("insert into party_members values (?, ?);");
        preparedStatement.setInt(1, party_id);
        preparedStatement.setString(2, member.toString());
        preparedStatement.executeUpdate();
    }

    public static Party getParty(int party_id) throws SQLException {
        Validate.notNull(party_id);
        UUID leader = getPartyLeader(party_id).getUniqueId();
        CollectionList<Player> partyMembers = getPartyMembers(party_id);
        return new Party(party_id, leader, partyMembers);
    }

    public static Party getParty(UUID member) throws SQLException {
        Validate.notNull(member);
        int partyId = getPartyId(member);
        UUID leader = getPartyLeader(partyId).getUniqueId();
        CollectionList<Player> partyMembers = getPartyMembers(partyId);
        return new Party(partyId, leader, partyMembers);
    }

    public static int getPartyId(UUID member) throws SQLException {
        Validate.notNull(member);
        PreparedStatement preparedStatement = connection.get().prepareStatement("select party_id from party_members where member=?;");
        preparedStatement.setString(1, member.toString());
        ResultSet result = preparedStatement.executeQuery();
        result.next();
        int partyId = result.getInt("party_id");
        result.close();
        return partyId;
    }

    public static CollectionList<Player> getPartyMembers(int party_id) throws SQLException {
        Validate.notNull(party_id);
        PreparedStatement preparedStatement = connection.get().prepareStatement("select member from party_members where party_id=?;");
        preparedStatement.setInt(1, party_id);
        ResultSet result = preparedStatement.executeQuery();
        CollectionList<Player> members = new CollectionList<>();
        while (result.next()) {
            members.add(getPlayer(UUID.fromString(result.getString("member"))));
        }
        return members;
    }

    public static CollectionList<UUID> getPartyMembersAsUniqueId(int party_id) throws SQLException {
        Validate.notNull(party_id);
        PreparedStatement preparedStatement = connection.get().prepareStatement("select member from party_members where party_id=?;");
        preparedStatement.setInt(1, party_id);
        ResultSet result = preparedStatement.executeQuery();
        CollectionList<UUID> members = new CollectionList<>();
        while (result.next()) {
            members.add(UUID.fromString(result.getString("member")));
        }
        return members;
    }

    public static Player createPlayer(UUID uuid, Ranks rank, String name) throws SQLException {
        Validate.notNull(uuid, rank);
        PreparedStatement preparedStatement = connection.get().prepareStatement("insert into players (player, rank, id, experience, points)\n" +
                "select * from (select ?, ?, ?, ?, ?) as tmp\n" +
                "where not exists (\n" +
                "    select player from players where player = ?\n" +
                ") limit 1;");
        preparedStatement.setString(1, uuid.toString());
        preparedStatement.setString(2, rank.name());
        preparedStatement.setString(3, name);
        preparedStatement.setBigDecimal(4, BigDecimal.ZERO);
        preparedStatement.setBigDecimal(5, BigDecimal.ZERO);
        preparedStatement.setString(6, uuid.toString());
        preparedStatement.executeUpdate();
        preparedStatement = connection.get().prepareStatement("update players set id=? where player=?;");
        preparedStatement.setString(1, name);
        preparedStatement.setString(2, uuid.toString());
        preparedStatement.executeUpdate();
        return new Player(uuid, rank, getFriends(uuid), getFriendRequests(uuid));
    }

    public static void setPoints(UUID uuid, long points) throws SQLException {
        Validate.notNull(uuid, points);
        PreparedStatement preparedStatement = connection.get().prepareStatement("update players set points=? where player=?;");
        preparedStatement.setBigDecimal(1, BigDecimal.valueOf(points));
        preparedStatement.setString(2, uuid.toString());
    }

    public static void addPoints(UUID uuid, long points) throws SQLException {
        Validate.notNull(uuid, points);
        PreparedStatement preparedStatement = connection.get().prepareStatement("select points from players where player=?;");
        preparedStatement.setString(1, uuid.toString());
        ResultSet result = preparedStatement.executeQuery();
        result.next();
        long points1 = result.getBigDecimal("points").longValueExact();
        result.close();
        preparedStatement = connection.get().prepareStatement("update players set points=? where player=?;");
        preparedStatement.setBigDecimal(1, BigDecimal.valueOf(points1 + points));
        preparedStatement.setString(2, uuid.toString());
    }

    public static long getPoints(UUID uuid) throws SQLException {
        Validate.notNull(uuid);
        PreparedStatement preparedStatement = connection.get().prepareStatement("select points from players where player=?;");
        preparedStatement.setString(1, uuid.toString());
        ResultSet result = preparedStatement.executeQuery();
        result.next();
        long value = result.getBigDecimal("points").longValueExact();
        result.close();
        return value;
    }

    public static void setExperience(UUID uuid, long experience) throws SQLException {
        Validate.notNull(uuid, experience);
        PreparedStatement preparedStatement = connection.get().prepareStatement("update players set experience=? where player=?;");
        preparedStatement.setBigDecimal(1, BigDecimal.valueOf(experience));
        preparedStatement.setString(2, uuid.toString());
    }

    public static void addExperience(UUID uuid, long experience) throws SQLException {
        Validate.notNull(uuid, experience);
        PreparedStatement preparedStatement = connection.get().prepareStatement("select experience from players where player=?;");
        preparedStatement.setString(1, uuid.toString());
        ResultSet result = preparedStatement.executeQuery();
        result.next();
        long experience1 = result.getBigDecimal("experience").longValueExact();
        result.close();
        preparedStatement = connection.get().prepareStatement("update players set experience=? where player=?;");
        preparedStatement.setBigDecimal(1, BigDecimal.valueOf(experience1 + experience));
        preparedStatement.setString(2, uuid.toString());
    }

    public static long getExperience(UUID uuid) throws SQLException {
        Validate.notNull(uuid);
        PreparedStatement preparedStatement = connection.get().prepareStatement("select experience from players where player=?;");
        preparedStatement.setString(1, uuid.toString());
        ResultSet result = preparedStatement.executeQuery();
        result.next();
        long value = result.getBigDecimal("experience").longValueExact();
        result.close();
        return value;
    }

    public static String getName(UUID uuid) throws SQLException {
        Validate.notNull(uuid);
        PreparedStatement preparedStatement = connection.get().prepareStatement("select id from players where player=? limit 1;");
        preparedStatement.setString(1, uuid.toString());
        ResultSet result = preparedStatement.executeQuery();
        result.next();
        String name = result.getString("id");
        result.close();
        return name;
    }

    @Nullable
    public static CollectionList<UUID> getFriends(@NonNull UUID uuid) throws SQLException {
        Validate.notNull(uuid);
        return getUUIDs(uuid, "select player2 from friends where player=", "player2");
    }

    @Nullable
    public static CollectionList<UUID> getFriendRequests(@NonNull UUID uuid) throws SQLException {
        Validate.notNull(uuid);
        return getUUIDs(uuid, "select player2 from friend_requests where player=", "player2");
    }

    static void addBan(@NonNull UUID player, @Nullable String reason, @NonNull long expires, @NonNull UUID executor) throws SQLException {
        Validate.notNull(player, expires, executor);
        ProxyServer.getInstance().getLogger().info("debug: expires: " + (expires != -1 ? System.currentTimeMillis() +expires : -1));
        PreparedStatement preparedStatement = connection.get().prepareStatement("insert into bans values (default, ?, ?, ?, ?, ?, default);");
        preparedStatement.setString(1, player.toString());
        preparedStatement.setString(2, reason);
        preparedStatement.setBigDecimal(3, BigDecimal.valueOf(System.currentTimeMillis()));
        preparedStatement.setBigDecimal(4, BigDecimal.valueOf(expires != -1 ? System.currentTimeMillis() + expires : -1));
        preparedStatement.setString(5, executor.toString());
        preparedStatement.executeUpdate();
    }

    public static void unban(@NonNull UUID player, @NonNull UUID unbanner) throws SQLException {
        Validate.notNull(player);
        PreparedStatement preparedStatement = connection.get().prepareStatement("update bans set expires=0, unbanner=? where player=?;");
        preparedStatement.setString(1, unbanner.toString());
        preparedStatement.setString(2, player.toString());
        preparedStatement.executeUpdate();
    }

    public static void updateBan(@NonNull int id, @Nullable UUID player, @Nullable String reason, @Nullable Long expires, @Nullable UUID executor, @Nullable UUID unbanner) throws SQLException {
        Validate.notNull(id);
        PreparedStatement preparedStatement = connection.get().prepareStatement("update bans set player=?, reason=?, expires=?, executor=?, unbanner=? where id=?;");
        if (player != null) preparedStatement.setString(1, player.toString()); else preparedStatement.setString(1, null);
        if (reason != null) preparedStatement.setString(2, reason); else preparedStatement.setString(2, null);
        if (expires != null) preparedStatement.setBigDecimal(3, BigDecimal.valueOf(expires)); else preparedStatement.setBigDecimal(3, null);
        if (executor != null) preparedStatement.setString(4, executor.toString()); else preparedStatement.setString(4, null);
        if (unbanner != null) preparedStatement.setString(5, unbanner.toString()); else preparedStatement.setString(5, null);
        preparedStatement.setInt(6, id);
        preparedStatement.executeUpdate();
    }

    public static void addFriend(UUID player1, UUID player2) throws SQLException {
        Validate.notNull(player1, player2);
        PreparedStatement preparedStatement = connection.get().prepareStatement("insert into friends values (?, ?);");
        preparedStatement.setString(1, player1.toString());
        preparedStatement.setString(2, player2.toString());
        preparedStatement.executeUpdate();
    }

    public static void removeFriend(UUID player1, UUID player2) throws SQLException {
        Validate.notNull(player1, player2);
        PreparedStatement preparedStatement = connection.get().prepareStatement("delete from friends where player=? and player2=?;");
        preparedStatement.setString(1, player1.toString());
        preparedStatement.setString(2, player2.toString());
        preparedStatement.executeUpdate();
    }

    public static void addFriendRequest(UUID player1, UUID player2) throws SQLException {
        Validate.notNull(player1, player2);
        PreparedStatement preparedStatement = connection.get().prepareStatement("insert into friend_requests values (?, ?);");
        preparedStatement.setString(1, player1.toString());
        preparedStatement.setString(2, player2.toString());
        preparedStatement.executeUpdate();
    }

    public static void removeFriendRequest(UUID player1, UUID player2) throws SQLException {
        Validate.notNull(player1, player2);
        PreparedStatement preparedStatement = connection.get().prepareStatement("delete from friend_requests where player=? and player2=?;");
        preparedStatement.setString(1, player1.toString());
        preparedStatement.setString(2, player2.toString());
        preparedStatement.executeUpdate();
    }

    public static void clearFriendRequests() throws SQLException {
        connection.get().createStatement().executeUpdate("delete from friend_requests where true;");
    }

    @NonNull
    static Ranks getRank(@NonNull UUID uuid) throws SQLException {
        Validate.notNull(uuid);
        Statement statement = connection.get().createStatement();
        ResultSet result = statement.executeQuery("select rank from players where player='" + uuid.toString() + "' limit 1;");
        result.next();
        String rank1;
        try {
            rank1 = result.getString("rank");
        } catch (SQLException e) {
            e.printStackTrace();
            setRank(uuid, Ranks.DEFAULT);
            rank1 = "DEFAULT";
        } finally {
            result.close();
        }
        return Ranks.valueOf(rank1);
    }

    public static void setRank(@NonNull UUID uuid, @NonNull Ranks rank) throws SQLException {
        connection.get().createStatement().executeUpdate("update players set rank='" + rank.name() + "' where player='" + uuid.toString() + "';");
    }

    @NonNull
    public static CollectionList<Ban> getBan(@NonNull UUID uuid) throws SQLException {
        Validate.notNull(uuid);
        Statement statement = connection.get().createStatement();
        ResultSet result = statement.executeQuery("select * from bans where player='" + uuid.toString() + "' order by expires DESC;"); // DESC
        CollectionList<Ban> bans = new CollectionList<>();
        while (result.next()) {
            int id = result.getInt("id");
            UUID player = UUID.fromString(result.getString("player"));
            String reason = result.getString("reason");
            BigDecimal bannedAt = result.getBigDecimal("bannedAt");
            BigDecimal expires = result.getBigDecimal("expires");
            UUID executor = UUID.fromString(result.getString("executor"));
            String unbanner = result.getString("unbanner");
            bans.put(new Ban(id, player, reason, bannedAt.longValueExact(), expires.longValueExact(), executor, unbanner));
        }
        result.close();
        return bans;
    }

    public static Ban getBan(int id) throws SQLException {
        Statement statement = connection.get().createStatement();
        ResultSet result = statement.executeQuery("select * from bans where id=" + id + " limit 1;");
        if (result.next()) return new Ban(result.getInt("id"), UUID.fromString(result.getString("player")), result.getString("reason"), result.getBigDecimal("bannedAt").longValueExact(), result.getBigDecimal("expires").longValueExact(), UUID.fromString(result.getString("executor")), result.getString("unbanner"));
        return null;
    }

    public static boolean isBanned(UUID uuid) throws SQLException {
        CollectionList<Ban> bans = getBan(uuid);
        return bans.first().getExpires() > System.currentTimeMillis() || bans.valuesArray()[bans.size()].getExpires() == -1;
    }

    public static UUID getUniqueId(String name) throws SQLException {
        Statement statement = connection.get().createStatement();
        ResultSet result = statement.executeQuery("select player from players where id='" + name + "';");
        result.next();
        UUID uuid = UUID.fromString(result.getString("player"));
        result.close();
        return uuid;
    }

    public static Player getPlayer(UUID uuid) throws SQLException {
        return new Player(uuid, getRank(uuid), getFriends(uuid), getFriendRequests(uuid));
    }

    // ----- Private stuff

    private static CollectionList<UUID> getUUIDs(UUID uuid, String query, String s) throws SQLException {
        Statement statement = connection.get().createStatement();
        CollectionList<UUID> uuids = new CollectionList<>();
        ResultSet result = statement.executeQuery(query + "'" + uuid.toString() + "';");
        while (result.next()) {
            String player = result.getString(s);
            uuids.put(UUID.fromString(player));
        }
        return uuids;
    }

    private SqlUtils() {}
    private static final AtomicReference<Connection> connection = new AtomicReference<>();
    private static final AtomicBoolean init = new AtomicBoolean(false);
}
