package xyz.acrylicstyle.doubletimecommandsbungee.utils;

import net.md_5.bungee.api.ProxyServer;
import util.CollectionList;
import xyz.acrylicstyle.doubletimecommandsbungee.types.Ban;
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

    public static void sync() throws SQLException {
        sync(false);
    }

    public static void sync(boolean force) throws SQLException {
        if (connection.get() == null) throw new IllegalStateException("Connection haven't made.");
        Statement statement = connection.get().createStatement();
        if (force) statement.executeUpdate("drop table if exists bans;");
        if (force) statement.executeUpdate("drop table if exists players;");
        if (force) statement.executeUpdate("drop table if exists friends;");
        if (force) statement.executeUpdate("drop table if exists friend_requests;");
        statement.executeUpdate("CREATE TABLE if not exists bans (\n" +
                "        id INT NOT NULL AUTO_INCREMENT,\n" +
                "        player VARCHAR(36) NOT NULL,\n" + // uuid
                "        reason VARCHAR(666) default 'None',\n" +
                "        expires NUMERIC(255) NOT NULL,\n" +
                "        executor VARCHAR(36),\n" + // uuid
                "        PRIMARY KEY (id)\n" +
                "    );");
        statement.executeUpdate("CREATE TABLE if not exists players (\n" +
                "        player VARCHAR(36) NOT NULL,\n" + // uuid
                "        rank VARCHAR(100) default 'DEFAULT',\n" +
                "        id VARCHAR(100) NOT NULL,\n" +
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
    }

    public static Player createPlayer(UUID uuid, Ranks rank, String name) throws SQLException {
        Validate.notNull(uuid, rank);
        PreparedStatement preparedStatement = connection.get().prepareStatement("insert into players (player, rank, id)\n" +
                "select * from (select ?, ?, ?) as tmp\n" +
                "where not exists (\n" +
                "    select player from players where player = ?\n" +
                ") limit 1;");
        preparedStatement.setString(1, uuid.toString());
        preparedStatement.setString(2, rank.name());
        preparedStatement.setString(3, name);
        preparedStatement.setString(4, uuid.toString());
        preparedStatement.executeUpdate();
        preparedStatement = connection.get().prepareStatement("update players set id=? where player=?;");
        preparedStatement.setString(1, name);
        preparedStatement.setString(2, uuid.toString());
        preparedStatement.executeUpdate();
        return new Player(uuid, rank, getFriends(uuid), getFriendRequests(uuid));
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

    public static CollectionList<UUID> getFriends(UUID uuid) throws SQLException {
        Validate.notNull(uuid);
        return getUUIDs(uuid, "select player2 from friends where player=", "player2");
    }

    public static CollectionList<UUID> getFriendRequests(UUID uuid) throws SQLException {
        Validate.notNull(uuid);
        return getUUIDs(uuid, "select player2 from friend_requests where player=", "player2");
    }

    public static void addBan(UUID player, String reason, long expires, UUID executor) throws SQLException {
        Validate.notNull(player, expires, executor);
        ProxyServer.getInstance().getLogger().info("debug: expires: " + expires);
        PreparedStatement preparedStatement = connection.get().prepareStatement("insert into bans values (default, ?, ?, ?, ?);");
        preparedStatement.setString(1, player.toString());
        preparedStatement.setString(2, reason);
        preparedStatement.setBigDecimal(3, BigDecimal.valueOf(expires));
        preparedStatement.setString(4, executor.toString());
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

    public static Ranks getRank(UUID uuid) throws SQLException {
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

    public static void setRank(UUID uuid, Ranks rank) throws SQLException {
        connection.get().createStatement().executeUpdate("update players set rank='" + rank.name() + "' where player='" + uuid.toString() + "';");
    }

    public static CollectionList<Ban> getBan(UUID uuid) throws SQLException {
        Statement statement = connection.get().createStatement();
        ResultSet result = statement.executeQuery("select * from bans where player='" + uuid.toString() + "' order by expires;"); // DESC
        CollectionList<Ban> bans = new CollectionList<>();
        while (result.next()) {
            int id = result.getInt("id");
            UUID player = UUID.fromString(result.getString("player"));
            String reason = result.getString("reason");
            BigDecimal expires = result.getBigDecimal("expires");
            UUID executor = UUID.fromString(result.getString("executor"));
            bans.put(new Ban(id, player, reason, expires.toBigInteger().longValueExact(), executor));
        }
        result.close();
        return bans;
    }

    public static Ban getBan(int id) throws SQLException {
        Statement statement = connection.get().createStatement();
        ResultSet result = statement.executeQuery("select * from bans where id=" + id + " limit 1;");
        if (result.next()) return new Ban(result.getInt("id"), UUID.fromString(result.getString("player")), result.getString("reason"), result.getInt("expires"), UUID.fromString(result.getString("executor")));
        return null;
    }

    public static boolean isBanned(UUID uuid) throws SQLException {
        return getBan(uuid).first().getExpires() > System.currentTimeMillis();
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
