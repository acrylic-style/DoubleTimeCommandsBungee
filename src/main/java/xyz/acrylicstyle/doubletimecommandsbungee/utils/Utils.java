package xyz.acrylicstyle.doubletimecommandsbungee.utils;

import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import util.Collection;
import util.CollectionList;
import xyz.acrylicstyle.doubletimecommandsbungee.DoubleTimeCommands;
import xyz.acrylicstyle.doubletimecommandsbungee.connection.ChannelListener;
import xyz.acrylicstyle.doubletimecommandsbungee.types.Incident;
import xyz.acrylicstyle.doubletimecommandsbungee.types.Player;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class Utils {
    public final static int DAY = 86400000;
    public final static int HOUR = 3600000;
    public final static int MINUTE = 60000;

    /**
     * @param consumer Consumer without any args. <b>It won't pass any arguments.</b>
     */
    public static void run(Consumer<?> consumer) {
        ProxyServer.getInstance().getScheduler().schedule(Utils.getPlugin(), () -> consumer.accept(null), 1, TimeUnit.MICROSECONDS);
    }

    /**
     * @param consumer Consumer without any args. <b>It won't pass any arguments.</b>
     */
    public static <T extends CommandSender> boolean run(Consumer<?> consumer, T player, Errors error) {
        final boolean[] type = {true};
        try {
            ProxyServer.getInstance().getScheduler().schedule(Utils.getPlugin(), () -> {
                try {
                    consumer.accept(null);
                } catch (Throwable e) {
                    e.printStackTrace();
                    Utils.sendError(player, error);
                    type[0] = false;
                }
            }, 1, TimeUnit.NANOSECONDS);
        } catch (Throwable e) {
            e.printStackTrace();
            Utils.sendError(player, Errors.SCHEDULER_ERROR);
            type[0] = false;
        }
        return type[0];
    }

    public static <T extends CommandSender> boolean run(ThrowableRunnable consumer, T player, Errors error) {
        AtomicBoolean status = new AtomicBoolean(true);
        try {
            ProxyServer.getInstance().getScheduler().schedule(Utils.getPlugin(), () -> {
                try {
                    consumer.run();
                } catch (Throwable e) {
                    e.printStackTrace();
                    Utils.sendError(player, error);
                    status.set(false);
                }
            }, 1, TimeUnit.NANOSECONDS);
        } catch (Throwable e) {
            e.printStackTrace();
            Utils.sendError(player, Errors.SCHEDULER_ERROR);
            status.set(false);
        }
        return status.get();
    }


    public static Plugin getPlugin() {
        return ProxyServer.getInstance().getPluginManager().getPlugin("DoubleTimeCommands");
    }

    /**
     * Example: <pre>if (!Utils.must(Ranks.ADMIN, PlayerUtils.getRank(player.getUniqueId))) return; // it sends message automatically, so do only return</pre>
     * @param required Required rank for do something
     * @param sender anything extends CommandSender for check if they have Admin rank
     * @return True if the required rank equals actual rank but console always returns true
     */
    public static boolean must(Ranks required, CommandSender sender) {
        if (!(sender instanceof ProxiedPlayer)) return true;
        ProxiedPlayer player = (ProxiedPlayer) sender;
        Ranks actual = PlayerUtils.getRank(player.getUniqueId());
        if (required.ordinal() < actual.ordinal()) {
            Utils.sendMessage(player, new TextComponent(ChatColor.RED + "You must be " + required.name().toLowerCase(Locale.ROOT) + " or higher to use this command!"));
            return false;
        }
        return true;
    }

    public static boolean must(Ranks required, UUID player) {
        Ranks actual = PlayerUtils.getRank(player);
        return required.ordinal() >= actual.ordinal();
    }

    public static void ban(UUID uuid, String reason, UUID executor) throws SQLException {
        Utils.ban(uuid, reason, -1, executor); // -1 means never
    }

    public static void ban(UUID uuid, String reason, int expires, UUID executor) throws SQLException {
        SqlUtils.addBan(uuid, reason, expires, executor);
    }

    public static <T extends CommandSender> void sendError(T player, Errors error) {
        player.sendMessage(new TextComponent(ChatColor.RED + "Couldn't run this command! Please try again later. (" + error.toString() + ")"));
    }

    public static void transferPlayer(ProxiedPlayer player, String game) {
        String gamePrefix;
        boolean shuffle = false;
        String format = ChatColor.GREEN + "Sending to %s!";
        Collection<String, ArrayList> config = DoubleTimeCommands.config.getConfigSectionValue("games", ArrayList.class);
        if (config.containsKey(game)) {
            gamePrefix = (String) config.get(game).get(0);
            if (config.get(game).size() >= 2) shuffle = (Boolean) config.get(game).get(1);
            if (config.get(game).size() >= 3) format = (String) config.get(game).get(2);
        } else {
            player.sendMessage(new TextComponent(ChatColor.RED + "Please specify valid game!"));
            return;
        }
        ArrayList<ServerInfo> servers = new ArrayList<>();
        String finalGamePrefix = gamePrefix;
        ProxyServer.getInstance().getServers().forEach((server, info) -> {
            if ((server.startsWith(finalGamePrefix.toUpperCase(Locale.ROOT)))) servers.add(info);
        });
        AtomicBoolean connected = new AtomicBoolean(false);
        if (shuffle) Collections.shuffle(servers, new Random()); // shuffle all servers
        String finalFormat = format;
        String before = player.getServer().getInfo().getName();
        AtomicInteger checked = new AtomicInteger();
        servers.forEach(info -> info.ping((result, error) -> {
            checked.getAndIncrement();
            if (error == null && !connected.get() && result.getPlayers().getMax() > result.getPlayers().getOnline()) {
                connected.set(true);
                player.sendMessage(new TextComponent(String.format(finalFormat, info.getName())));
                player.connect(info); // connect to *random* *online* *lobby* server
            }
            if (!connected.get() && before.equalsIgnoreCase(player.getServer().getInfo().getName()) && checked.get() >= servers.size())
                player.sendMessage(new TextComponent(ChatColor.RED + "We couldn't find available server!"));
        }));
    }

    public static void getRandomLobby(Callback<ServerInfo> callback) {
        ArrayList<ServerInfo> servers = new ArrayList<>();
        ProxyServer.getInstance().getServers().forEach((server, info) -> {
            if ((server.startsWith("LOBBY"))) servers.add(info);
        });
        AtomicBoolean connected = new AtomicBoolean(false);
        AtomicInteger checked = new AtomicInteger();
        servers.forEach(info -> info.ping((result, error) -> {
            checked.getAndIncrement();
            if (error == null && !connected.get() && result.getPlayers().getMax() > result.getPlayers().getOnline()) {
                connected.set(true);
                callback.done(info, null);
            }
            if (!connected.get() && checked.get() >= servers.size()) callback.done(ProxyServer.getInstance().getServerInfo("LIMBO"), null);
        }));
    }

    public static void transferPlayerWithGamePrefix(ProxiedPlayer player, String gamePrefix) {
        ArrayList<ServerInfo> servers = new ArrayList<>();
        ProxyServer.getInstance().getServers().forEach((server, info) -> {
            if ((server.startsWith(gamePrefix.toUpperCase(Locale.ROOT)))) servers.add(info);
        });
        AtomicBoolean connected = new AtomicBoolean(false);
        String before = player.getServer().getInfo().getName();
        AtomicInteger checked = new AtomicInteger();
        servers.forEach(info -> info.ping((result, error) -> {
            checked.getAndIncrement();
            if (error == null && !connected.get() && result.getPlayers().getMax() > result.getPlayers().getOnline()) {
                connected.set(true);
                player.sendMessage(new TextComponent(String.format(ChatColor.GREEN + "Sending to %s!", info.getName())));
                player.connect(info);
            }
            if (!connected.get() && before.equalsIgnoreCase(player.getServer().getInfo().getName()) && checked.get() >= servers.size())
                player.sendMessage(new TextComponent(ChatColor.RED + "We couldn't find available server!"));
        }));
    }

    public static void getPlayers(String gamePrefix, Callback<Integer> callback) {
        ArrayList<ServerInfo> servers = new ArrayList<>();
        ProxyServer.getInstance().getServers().forEach((server, info) -> {
            if ((server.startsWith(gamePrefix.toUpperCase(Locale.ROOT)))) servers.add(info);
        });
        AtomicInteger checked = new AtomicInteger();
        AtomicBoolean successful = new AtomicBoolean();
        if (servers.size() <= 0) callback.done(-1, null);
        servers.forEach(info -> info.ping((result, error) -> {
            if (successful.get()) return;
            checked.getAndIncrement();
            if (error == null) {
                successful.set(true);
                try {
                    callback.done(SqlUtils.countPlayersInServer(gamePrefix), null);
                } catch (SQLException e) {
                    e.printStackTrace();
                    callback.done(-1, e);
                }
            }
            if (checked.get() >= servers.size()) callback.done(-1, null);
        }));
    }

    public static void getAvailableGames(String gamePrefix, Callback<Integer> callback) {
        ArrayList<ServerInfo> servers = new ArrayList<>();
        ProxyServer.getInstance().getServers().forEach((server, info) -> {
            if ((server.startsWith(gamePrefix.toUpperCase(Locale.ROOT)))) servers.add(info);
        });
        AtomicInteger checked = new AtomicInteger();
        AtomicInteger working = new AtomicInteger();
        if (servers.size() <= 0) callback.done(0, null);
        servers.forEach(info -> info.ping((result, error) -> {
            checked.getAndIncrement();
            if (error == null) working.getAndIncrement();
            if (checked.get() >= servers.size()) callback.done(working.get(), null);
        }));
    }

    public static void sendMessage(ProxiedPlayer player, BaseComponent message) {
        sendMessage(player, "", message);
    }

    public static void sendMessage(ProxiedPlayer player, String subchannel, BaseComponent message) {
        ChannelListener.sendToBukkit("helper:message", player.getUniqueId().toString() + "," + subchannel, message.toPlainText(), player.getServer().getInfo());
    }

    public static void sendMessage(Player player, BaseComponent message) {
        sendMessage(player, "", message);
    }

    public static void sendMessage(Player player, String subchannel, BaseComponent message) {
        try {
            if (SqlUtils.isPlayerConnected(player.getUniqueId()))
                ChannelListener.sendToBukkit("helper:message", player.getUniqueId().toString() + "," + subchannel, message.toPlainText(), ProxyServer.getInstance().getServerInfo(player.getConnectedServer()));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void kickPlayer(Player player, BaseComponent message) {
        ChannelListener.sendToBukkit("helper:kick", player.getUniqueId().toString() + ",", message.toPlainText(), ProxyServer.getInstance().getServerInfo(player.getConnectedServer()));
    }

    public static void kickPlayer(UUID uuid, BaseComponent message) {
        ProxyServer.getInstance().getServers().values().forEach(server -> {
            ChannelListener.sendToBukkit("helper:kick", uuid.toString() + ",", message.toPlainText(), server);
        });
    }

    public static void connect(Player player, String server) {
        ChannelListener.sendToBukkit("helper:connect", player.getUniqueId().toString(), server, ProxyServer.getInstance().getServerInfo(player.getConnectedServer()));
    }

    public static void playSound(Player player, String sound) {
        try {
            if (SqlUtils.isPlayerConnected(player.getUniqueId()))
                ChannelListener.sendToBukkit("helper:sound", player.getUniqueId().toString(), sound, ProxyServer.getInstance().getServerInfo(player.getConnectedServer()));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void partyChat(ProxiedPlayer player, String[] args, int i) {
        try {
            if (!SqlUtils.inParty(player.getUniqueId())) {
                Utils.sendMessage(player, new TextComponent(ChatColor.RED + "You are not in party!"));
                return;
            }
            int party_id = SqlUtils.getPartyId(player.getUniqueId());
            final String[] message = {""};
            new CollectionList<>(Arrays.asList(args)).foreach((arg, index) -> {
                if (index >= i) message[0] += arg + " ";
            });
            if (message[0].length() <= 0) {
                Utils.sendMessage(player, new TextComponent(ChatColor.RED + "Please specify message!"));
                return;
            }
            final String name = PlayerUtils.getName(player.getUniqueId());
            SqlUtils.getPartyMembersAsUniqueId(party_id).forEach(uuid -> {
                try {
                    Player player2 = SqlUtils.getPlayer(uuid);
                    Utils.sendMessage(player2, new TextComponent(ChatColor.BLUE + "Party > " + name + ChatColor.WHITE + ": " + message[0]));
                    Utils.playSound(player2, "ENTITY_EXPERIENCE_ORB_PICKUP");
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            Utils.sendMessage(player, new TextComponent(ChatColor.RED + "Couldn't send message! Please try again later!"));
            e.printStackTrace();
        }
    }

    private static JSONObject callAPI(URL url) throws IOException, ParseException {
        BufferedReader br;
        String line;
        String text = "";
        try (InputStream is = url.openStream()) {
            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                text += line;
            }
        } catch (IOException mue) {
            mue.printStackTrace();
        }
        return getJSON(text);
    }

    private static JSONObject getJSON(String s) throws ParseException {
        return (JSONObject) new JSONParser().parse(s);
    }

    @SuppressWarnings("unchecked")
    private static CollectionList<Incident> getIncidents() throws IOException, ParseException {
        try {
            JSONObject object = callAPI(new URL("https://status.acrylicstyle.xyz/api/v1/incidents"));
            CollectionList<Incident> incidents = new CollectionList<>();
            ((JSONArray)object.get("data")).forEach(obj -> {
                JSONObject jsonObj = (JSONObject) obj;
                int id = (int) (long) jsonObj.get("id");
                int user_id = (int) (long) jsonObj.get("user_id");
                int component_id = (int) (long) jsonObj.get("component_id");
                String name = (String) jsonObj.get("name");
                int status = (int) (long) jsonObj.get("status");
                int visible = (int) (long) jsonObj.get("visible");
                boolean stickied = (boolean) jsonObj.get("stickied");
                boolean notifications = (boolean) jsonObj.get("notifications");
                String message = (String) jsonObj.get("message");
                boolean is_resolved = (boolean) jsonObj.get("is_resolved");
                incidents.add(new Incident(id, user_id, component_id, name, status, visible, stickied, notifications, message, is_resolved));
            });
            return incidents;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public static CollectionList<Incident> getUnresolvedIncidents() throws IOException, ParseException {
        return getIncidents().filter(incident -> !incident.isResolved()).clone();
    }

    public static String getStatus(int status) {
        switch (status) {
            case 0: return ChatColor.DARK_PURPLE + "Scheduled";
            case 1: return ChatColor.BLUE + "Investigating";
            case 2: return ChatColor.GOLD + "Identified";
            case 3: return ChatColor.DARK_GREEN + "Watching";
            case 4: return ChatColor.GREEN + "Fixed";
            default: return ChatColor.GRAY + "<Undefined>";
        }
    }
}
