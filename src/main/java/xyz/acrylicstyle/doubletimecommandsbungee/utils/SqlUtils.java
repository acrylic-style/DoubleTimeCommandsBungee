package xyz.acrylicstyle.doubletimecommandsbungee.utils;

import util.CollectionList;
import xyz.acrylicstyle.doubletimecommandsbungee.types.Ban;
import xyz.acrylicstyle.doubletimecommandsbungee.types.Player;

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

    public static void sync() throws SQLException {
        sync(false);
    }

    public static void sync(boolean force) throws SQLException {
        if (connection.get() == null) throw new IllegalStateException("Connection haven't made.");
        Statement statement = connection.get().createStatement();
        if (force) statement.executeQuery("drop table bans if exists;");
        if (force) statement.executeQuery("drop table players if exists;");
        if (force) statement.executeQuery("drop table friends if exists;");
        if (force) statement.executeQuery("drop table friend_requests if exists;");
        statement.executeQuery("CREATE TABLE bans (\n" +
                "        id INT NOT NULL AUTO_INCREMENT,\n" +
                "        player VARCHAR(36) NOT NULL,\n" + // uuid
                "        reason VARCHAR(666),\n" +
                "        expires INT(255) NOT NULL,\n" +
                "        PRIMARY KEY (id)\n" +
                "    ) if not exists ;");
        statement.executeQuery("CREATE TABLE players (\n" +
                "        player VARCHAR(36) NOT NULL,\n" + // uuid
                "        rank VARCHAR(100),\n" +
                "        PRIMARY KEY (player)\n" +
                "    ) if not exists ;");
        statement.executeQuery("CREATE TABLE friends (\n" +
                "       player VARCHAR(36) NOT NULL,\n" +
                "       player2 VARCHAR(36) NOT NULL,\n" +
                "       primary key (player)\n" +
                "    ) if not exists ;");
        statement.executeQuery("CREATE TABLE friend_requests (\n" +
                "       player VARCHAR(36) NOT NULL,\n" +
                "       player2 VARCHAR(36) NOT NULL,\n" +
                "       primary key (player)\n" +
                "    ) if not exists ;");
    }

    public static CollectionList<UUID> getFriends(UUID uuid) throws SQLException {
        return getUUIDs(uuid, "select player2 from friends where player=", "player2");
    }

    public static CollectionList<UUID> getFriendRequests(UUID uuid) throws SQLException {
        return getUUIDs(uuid, "select player2 from friend_requests where player=", "player2");
    }

    public static Ranks getRank(UUID uuid) throws SQLException {
        Statement statement = connection.get().createStatement();
        ResultSet result = statement.executeQuery("select rank from players where player=" + uuid.toString() + " limit 1;"); // it's completely safe... i wish.
        Ranks rank = Ranks.valueOf(result.getString("rank") == null ? "DEFAULT" : result.getString("rank"));
        result.close();
        return rank;
    }

    public static CollectionList<Ban> getBan(UUID uuid) throws SQLException {
        Statement statement = connection.get().createStatement();
        ResultSet result = statement.executeQuery("select * from bans where player=" + uuid.toString() + ";");
        CollectionList<Ban> bans = new CollectionList<>();
        while (result.next()) {
            int id = result.getInt("id");
            UUID player = UUID.fromString(result.getString("player"));
            String reason = result.getString("reason");
            int expires = result.getInt("expires");
            result.close();
            bans.put(new Ban(id, player, reason, expires));
        }
        return bans;
    }

    public static Player getPlayer(UUID uuid) throws SQLException {
        return new Player(uuid, getRank(uuid), getFriends(uuid), getFriendRequests(uuid));
    }

    // ----- Private stuff

    private static CollectionList<UUID> getUUIDs(UUID uuid, String query, String s) throws SQLException {
        Statement statement = connection.get().createStatement();
        CollectionList<UUID> uuids = new CollectionList<>();
        ResultSet result = statement.executeQuery(query + uuid.toString() + ";");
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
