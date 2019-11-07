package xyz.acrylicstyle.doubletimecommandsbungee;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.*;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;
import util.CollectionList;
import xyz.acrylicstyle.doubletimecommandsbungee.commands.*;
import xyz.acrylicstyle.doubletimecommandsbungee.connection.ChannelListener;
import xyz.acrylicstyle.doubletimecommandsbungee.providers.ConfigProvider;
import xyz.acrylicstyle.doubletimecommandsbungee.types.Incident;
import xyz.acrylicstyle.doubletimecommandsbungee.utils.Ranks;
import xyz.acrylicstyle.doubletimecommandsbungee.utils.Scheduler;
import xyz.acrylicstyle.doubletimecommandsbungee.utils.SqlUtils;
import xyz.acrylicstyle.doubletimecommandsbungee.utils.Utils;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class DoubleTimeCommands extends Plugin implements Listener {
    public static ConfigProvider config;
    private static Scheduler<ProxiedPlayer> scheduler = new Scheduler<>();

    @Override
    public void onEnable() {
        try {
            config = new ConfigProvider("./plugins/DoubleTimeCommands/config.yml");
        } catch (IOException e) {
            e.printStackTrace();
            ProxyServer.getInstance().getLogger().severe("Couldn't load config!");
            return;
        }
        try {
            SqlUtils.loadDriver();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            ProxyServer.getInstance().getLogger().severe("Couldn't load database driver!");
            return;
        }
        boolean forceSync = config.configuration.getBoolean("database.forceSync", false);
        if (forceSync) config.configuration.set("database.forceSync", false);
        String host = config.configuration.getString("database.host", "localhost");
        String database = config.configuration.getString("database.name");
        String user = config.configuration.getString("database.user");
        String password = config.configuration.getString("database.password");
        if (database == null || user == null || password == null) {
            ProxyServer.getInstance().getLogger().severe("Can't connect to the database because name, user, or password is not defined at config file. (database.host, database.name, database.user, and database.password are configurable)");
            return;
        }
        try {
            SqlUtils.connect(host, database, user, password);
            if (forceSync) SqlUtils.sync(true);
        } catch (SQLException e) {
            ProxyServer.getInstance().getLogger().severe("An error occurred while connecting to the database");
            e.printStackTrace();
            return;
        }
        ProxyServer.getInstance().getPluginManager().registerListener(this, this);
        ProxyServer.getInstance().getPluginManager().registerCommand(this, new Party());
        ProxyServer.getInstance().getPluginManager().registerCommand(this, new Friend());
        ProxyServer.getInstance().getPluginManager().registerCommand(this, new SetNickname());
        ProxyServer.getInstance().getPluginManager().registerCommand(this, new ResetNickname());
        ProxyServer.getInstance().getPluginManager().registerCommand(this, new Tell());
        ProxyServer.getInstance().getPluginManager().registerCommand(this, new Rank());
        ProxyServer.getInstance().getPluginManager().registerCommand(this, new Ban());
        ProxyServer.getInstance().getPluginManager().registerCommand(this, new Unban());
        ProxyServer.getInstance().getPluginManager().registerCommand(this, new Kick());
        ProxyServer.getInstance().getPluginManager().registerCommand(this, new Hub());
        ProxyServer.getInstance().getPluginManager().registerCommand(this, new Play());
        ProxyServer.getInstance().getPluginManager().registerCommand(this, new Ping());
        ProxyServer.getInstance().getPluginManager().registerCommand(this, new PartyChat());
        ProxyServer.getInstance().getPluginManager().registerCommand(this, new WhereAmI());
        ProxyServer.getInstance().getPluginManager().registerListener(this, new ChannelListener());
        ProxyServer.getInstance().registerChannel("dtc:rank");
        ProxyServer.getInstance().registerChannel("dtc:playing");
        ProxyServer.getInstance().registerChannel("dtc:availgames");
        ProxyServer.getInstance().registerChannel("commons:transfer");
        ProxyServer.getInstance().registerChannel("commons:transfer2");
        ProxyServer.getInstance().registerChannel("helper:message");
        ProxyServer.getInstance().registerChannel("helper:kick");
        ProxyServer.getInstance().registerChannel("helper:connect");
        ProxyServer.getInstance().registerChannel("helper:sound");
    }

    @Override
    public void onDisable() {
        try {
            SqlUtils.clearFriendRequests();
            SqlUtils.close();
        } catch (SQLException e) {
            ProxyServer.getInstance().getLogger().severe("Couldn't disconnect from the database.");
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onCommand(ChatEvent e) {
        if (!e.isCommand()) return;
        if (e.getMessage().startsWith("/server")) {
            if (!(e.getSender() instanceof ProxiedPlayer)) return;
            if (!Utils.must(Ranks.ADMIN, (ProxiedPlayer) e.getSender())) e.setCancelled(true);
        }
    }

    @EventHandler
    public void onServerConnected(ServerConnectedEvent event) {
        scheduler.schedule(player -> {
            try {
                CollectionList<Incident> unresolvedIncidents = Utils.getUnresolvedIncidents();
                if (unresolvedIncidents.size() == 1) {
                    Incident incident = unresolvedIncidents.first();
                    event.getPlayer().sendMessage(new TextComponent(ChatColor.GOLD + "----------------------------------------"));
                    event.getPlayer().sendMessage(TextComponent.fromLegacyText(ChatColor.YELLOW + "Incident " + ChatColor.RED + "#" + incident.getId() + ChatColor.YELLOW + " is happening right now:"));
                    event.getPlayer().sendMessage(TextComponent.fromLegacyText(ChatColor.YELLOW + "Title: " + ChatColor.AQUA + incident.getName()));
                    event.getPlayer().sendMessage(TextComponent.fromLegacyText(ChatColor.YELLOW + "Summary: " + ChatColor.AQUA + incident.getMessage()));
                    event.getPlayer().sendMessage(TextComponent.fromLegacyText(ChatColor.YELLOW + "Status: " + Utils.getStatus(incident.getStatus())));
                    event.getPlayer().sendMessage(TextComponent.fromLegacyText(ChatColor.YELLOW + "Please see " + ChatColor.AQUA + ChatColor.UNDERLINE + "https://status.acrylicstyle.xyz/incidents/" + incident.getId() + " " + ChatColor.RESET + ChatColor.YELLOW + "for the more information."));
                    event.getPlayer().sendMessage(new TextComponent(ChatColor.GOLD + "----------------------------------------"));
                } else if (unresolvedIncidents.size() >= 2) {
                    event.getPlayer().sendMessage(new TextComponent(ChatColor.GOLD + "----------------------------------------"));
                    event.getPlayer().sendMessage(TextComponent.fromLegacyText(ChatColor.YELLOW + "There are " + unresolvedIncidents.size() + " incidents happening now!"));
                    event.getPlayer().sendMessage(TextComponent.fromLegacyText(ChatColor.YELLOW + "Please see " + ChatColor.AQUA + ChatColor.UNDERLINE + "https://status.acrylicstyle.xyz" + ChatColor.RESET + ChatColor.YELLOW + " for the more information."));
                    event.getPlayer().sendMessage(new TextComponent(ChatColor.GOLD + "----------------------------------------"));
                }
                ArrayList<ServerInfo> servers = new ArrayList<>();
                ProxyServer.getInstance().getServers().forEach((server, info) -> {
                    if ((server.startsWith("LOBBY") || server.startsWith("lobby"))) servers.add(info);
                });
                Collections.shuffle(servers, new Random()); // shuffle all servers
                event.getPlayer().setReconnectServer(new CollectionList<>(servers).first());
                SqlUtils.setConnection(event.getPlayer().getUniqueId(), event.getServer().getInfo().getName());
            } catch (Exception e) {
                ProxyServer.getInstance().getLogger().warning("An error occurred while handling connection event (ServerConnectedEvent)");
                e.printStackTrace();
            }
        }, event.getPlayer(), 0);
    }

    @EventHandler
    public void onLogin(LoginEvent event) {
        try {
            SqlUtils.createPlayer(event.getConnection().getUniqueId(), Ranks.DEFAULT, event.getConnection().getName());
            if (SqlUtils.isBanned(event.getConnection().getUniqueId())) return;
        } catch (SQLException e) {
            event.getConnection().disconnect(new TextComponent(ChatColor.RED + "An error occurred while processing your login, please report it to admins!"));
            ProxyServer.getInstance().getLogger().warning("Couldn't create player!");
            e.printStackTrace();
            return;
        }
        scheduler.schedule(c -> {
            try {
                if (!SqlUtils.isPlayerConnected(event.getConnection().getUniqueId())) return;
                CollectionList<UUID> friends = SqlUtils.getFriends(event.getConnection().getUniqueId());
                friends.forEach(uuid -> {
                    try {
                        if (SqlUtils.isPlayerConnected(uuid))
                            Utils.sendMessage(SqlUtils.getPlayer(uuid), new TextComponent(ChatColor.YELLOW + event.getConnection().getName() + " joined."));
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                });
            } catch (Exception e) {
                ProxyServer.getInstance().getLogger().warning("Couldn't send join message!");
                e.printStackTrace();
            }
        }, null, 0);
    }

    @EventHandler
    public void onLeave(PlayerDisconnectEvent event) {
        try {
            SqlUtils.setConnection(event.getPlayer().getUniqueId(), null);
            if (SqlUtils.isBanned(event.getPlayer().getUniqueId())) return;
            CollectionList<UUID> friends = SqlUtils.getFriends(event.getPlayer().getUniqueId());
            friends.forEach(uuid -> {
                try {
                    if (SqlUtils.isPlayerConnected(uuid)) Utils.sendMessage(SqlUtils.getPlayer(uuid), new TextComponent(ChatColor.YELLOW + event.getPlayer().getName() + " left."));
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            ProxyServer.getInstance().getLogger().warning("Couldn't handle disconnection!");
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onPostLogin(PostLoginEvent event) {
        try {
            UUID uuid = event.getPlayer().getUniqueId();
            AtomicInteger banIndex = new AtomicInteger(-1);
            CollectionList<xyz.acrylicstyle.doubletimecommandsbungee.types.Ban> bans = SqlUtils.getBan(uuid);
            bans.foreach((ban, index) -> {
                if (ban.getExpires() < 0) banIndex.set(index);
            });
            if (bans.size() <= 0) return;
            xyz.acrylicstyle.doubletimecommandsbungee.types.Ban lastBan = banIndex.get() > -1 ? bans.get(banIndex.get()) : bans.first();
            String reason = lastBan.getReason();
            long expires = lastBan.getExpires();
            long currentTimestamp = System.currentTimeMillis();
            long days = Math.round(((float) (expires-currentTimestamp)/86400000F)*10L)/10;
            if (expires >= 0 && expires <= currentTimestamp) return;
            boolean perm = expires <= -1;
            Collection<TextComponent> stackedMessage = new ArrayList<>();
            if (perm) stackedMessage.add(new TextComponent(ChatColor.RED + "You are permanently banned from this server!\n\n"));
            if (!perm) stackedMessage.add(new TextComponent(ChatColor.RED + "You are temporarily banned for " + ChatColor.WHITE + days + " days " + ChatColor.RED + "from this server!\n\n"));
            stackedMessage.add(new TextComponent(ChatColor.GRAY + "Reason: " + ChatColor.WHITE + reason + "\n\n"));
            if (reason.equalsIgnoreCase("None")) stackedMessage.add(new TextComponent(ChatColor.YELLOW + "Note: Reason was 'None', please report it to our staff!\n"));
            stackedMessage.add(new TextComponent(ChatColor.GRAY + "Ban ID: " + lastBan.getBanId()));
            event.getPlayer().disconnect(stackedMessage.toArray(new TextComponent[0]));
        } catch (Exception e) {
            event.getPlayer().disconnect(new TextComponent(ChatColor.RED + "An error occurred while processing your login, please report this to admins!"));
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onServerKick(ServerKickEvent e) {
        Utils.getRandomLobby((result, error) -> {
            e.setCancelled(true);
            e.setCancelServer(result);
        });
    }
}
