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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class DoubleTimeCommands extends Plugin implements Listener {
    public static ConfigProvider config;
    public static Scheduler<ProxiedPlayer> scheduler = new Scheduler<>();

    @Override
    public void onEnable() {
        Locale.setDefault(Locale.ENGLISH);
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
        ProxyServer.getInstance().getPluginManager().registerCommand(this, new SetPrefix());
        ProxyServer.getInstance().getPluginManager().registerCommand(this, new ResetPrefix());
        ProxyServer.getInstance().getPluginManager().registerCommand(this, new Tell());
        ProxyServer.getInstance().getPluginManager().registerCommand(this, new Reply());
        ProxyServer.getInstance().getPluginManager().registerCommand(this, new Rank());
        ProxyServer.getInstance().getPluginManager().registerCommand(this, new Ban());
        ProxyServer.getInstance().getPluginManager().registerCommand(this, new Unban());
        ProxyServer.getInstance().getPluginManager().registerCommand(this, new Kick());
        ProxyServer.getInstance().getPluginManager().registerCommand(this, new Hub());
        ProxyServer.getInstance().getPluginManager().registerCommand(this, new Play());
        ProxyServer.getInstance().getPluginManager().registerCommand(this, new Ping());
        ProxyServer.getInstance().getPluginManager().registerCommand(this, new PartyChat());
        ProxyServer.getInstance().getPluginManager().registerCommand(this, new WhereAmI());
        ProxyServer.getInstance().getPluginManager().registerCommand(this, new Limbo());
        ProxyServer.getInstance().getPluginManager().registerCommand(this, new AfkWarp());
        ProxyServer.getInstance().getPluginManager().registerCommand(this, new Rejoin());
        ProxyServer.getInstance().getPluginManager().registerCommand(this, new AddPoints());
        ProxyServer.getInstance().getPluginManager().registerCommand(this, new SetPoints());
        ProxyServer.getInstance().getPluginManager().registerCommand(this, new AddExperience());
        ProxyServer.getInstance().getPluginManager().registerCommand(this, new SetExperience());
        ProxyServer.getInstance().getPluginManager().registerListener(this, new ChannelListener());
        ProxyServer.getInstance().registerChannel("dtc:rank");
        ProxyServer.getInstance().registerChannel("dtc:playing");
        ProxyServer.getInstance().registerChannel("dtc:availgames");
        ProxyServer.getInstance().registerChannel("dtc:points");
        ProxyServer.getInstance().registerChannel("dtc:experience");
        ProxyServer.getInstance().registerChannel("dtc:addpoints");
        ProxyServer.getInstance().registerChannel("dtc:addexperience");
        ProxyServer.getInstance().registerChannel("commons:transfer");
        ProxyServer.getInstance().registerChannel("commons:transfer2");
        ProxyServer.getInstance().registerChannel("helper:message");
        ProxyServer.getInstance().registerChannel("helper:kick");
        ProxyServer.getInstance().registerChannel("helper:connect");
        ProxyServer.getInstance().registerChannel("helper:sound");
        ProxyServer.getInstance().registerChannel("dtc:getplayer");
    }

    @Override
    public void onDisable() {
        try {
            Collection<ProxiedPlayer> ppls =  ProxyServer.getInstance().getPlayers();
            ProxyServer.getInstance().getLogger().info("Processing " + ppls.size() + " players before disabling plugin");
            ppls.forEach(player -> {
                try {
                    SqlUtils.setConnection(player.getUniqueId(), null);
                    CollectionList<UUID> friends = SqlUtils.getFriends(player.getUniqueId());
                    friends.forEach(uuid -> {
                        try {
                            Utils.sendMessage(SqlUtils.getPlayer(uuid), new TextComponent(ChatColor.YELLOW + player.getName() + " left."));
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    });
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });
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
    public void onProxyPing(ProxyPingEvent e) {
        try {
            e.getResponse().getPlayers().setOnline(SqlUtils.getOnlinePlayers());
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    @EventHandler
    public void onServerConnected(ServerConnectedEvent event) {
        scheduler.schedule(player -> {
            try {
                if (event.getPlayer().getServer().getInfo().getName().startsWith("LOBBY")) {
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
                    File changeLogsFile = new File("./plugins/DoubleTimeCommands/changelogs.txt");
                    if (changeLogsFile.canRead() && changeLogsFile.isFile()) {
                        String changeLogs = new String(Files.readAllBytes(Paths.get(changeLogsFile.getPath())));
                        String[] changeLogsArray = changeLogs.split("=====");
                        String latestChangeLog = changeLogsArray[0];
                        latestChangeLog = latestChangeLog.substring(0, latestChangeLog.length()-1);
                        if (changeLogsArray.length >= 2) { latestChangeLog += changeLogsArray[1]; latestChangeLog = latestChangeLog.substring(0, latestChangeLog.length()-1); }
                        if (changeLogsArray.length >= 3) { latestChangeLog += changeLogsArray[2]; latestChangeLog = latestChangeLog.substring(0, latestChangeLog.length()-1); }
                        if (changeLogsArray.length >= 4) { latestChangeLog += changeLogsArray[3]; latestChangeLog = latestChangeLog.substring(0, latestChangeLog.length()-1); }
                        if (changeLogsArray.length >= 5) { latestChangeLog += changeLogsArray[4]; latestChangeLog = latestChangeLog.substring(0, latestChangeLog.length()-1); }
                        event.getPlayer().sendMessage(new TextComponent(ChatColor.GOLD + "----------- Latest Changes -----------"));
                        event.getPlayer().sendMessage(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', latestChangeLog)));
                        event.getPlayer().sendMessage(new TextComponent(ChatColor.GOLD + "------------------------------------"));
                    }
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
        }, event.getPlayer(), 250);
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
            if (SqlUtils.isPlayerConnected(uuid)) {
                Utils.kickPlayer(uuid, new TextComponent(ChatColor.RED + "You've logged from another location!"));
            }
            int maxPlayers = ProxyServer.getInstance().getConfigurationAdapter().getListeners().iterator().next().getMaxPlayers();
            if (maxPlayers <= SqlUtils.getOnlinePlayers() && SqlUtils.getPlayer(uuid).getRank() == Ranks.DEFAULT) {
                Collection<TextComponent> stackedMessage = new ArrayList<>();
                stackedMessage.add(new TextComponent(ChatColor.YELLOW + "This server is currently full!"));
                stackedMessage.add(new TextComponent(ChatColor.YELLOW + "Get Sand or VIP at least to join this server!"));
                stackedMessage.add(new TextComponent(""));
                stackedMessage.add(new TextComponent(ChatColor.DARK_GRAY + "Don't worry, this isn't ban message."));
                event.getPlayer().disconnect(stackedMessage.toArray(new TextComponent[0]));
                scheduler.schedule(player -> {
                    try {
                        SqlUtils.setConnection(event.getPlayer().getUniqueId(), null);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }, event.getPlayer(), 250);
                return;
            }
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
        if (e.getKickedFrom().equals(e.getPlayer().getServer().getInfo())) return;
        e.getPlayer().sendMessage(TextComponent.fromLegacyText(ChatColor.YELLOW + "You were kicked from game with reason: " + ChatColor.WHITE + e.getKickReasonComponent()[0].toPlainText()));
        e.setKickReasonComponent(TextComponent.fromLegacyText(""));
        e.setCancelled(true);
        if (e.getPlayer().getServer().getInfo().getName().startsWith("LOBBY")) return;
        // e.setCancelServer(ProxyServer.getInstance().getServerInfo("LIMBO"));
        scheduler.schedule(player -> Utils.transferPlayerWithGamePrefix(e.getPlayer(), "LOBBY"), e.getPlayer(), 250);
    }
}
