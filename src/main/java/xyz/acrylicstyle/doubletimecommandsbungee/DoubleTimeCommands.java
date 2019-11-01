package xyz.acrylicstyle.doubletimecommandsbungee;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;
import util.CollectionList;
import xyz.acrylicstyle.doubletimecommandsbungee.commands.*;
import xyz.acrylicstyle.doubletimecommandsbungee.connection.ChannelListener;
import xyz.acrylicstyle.doubletimecommandsbungee.providers.ConfigProvider;
import xyz.acrylicstyle.doubletimecommandsbungee.utils.Ranks;
import xyz.acrylicstyle.doubletimecommandsbungee.utils.SqlUtils;
import xyz.acrylicstyle.doubletimecommandsbungee.utils.Utils;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class DoubleTimeCommands extends Plugin implements Listener {
    public static ConfigProvider config;

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
        ProxyServer.getInstance().getPluginManager().registerCommand(this, new WhereAmI());
        ProxyServer.getInstance().getPluginManager().registerListener(this, new ChannelListener());
        ProxyServer.getInstance().registerChannel("dtc:rank");
        ProxyServer.getInstance().registerChannel("commons:transfer");
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
    public void onLogin(LoginEvent event) {
        try {
            SqlUtils.createPlayer(event.getConnection().getUniqueId(), Ranks.DEFAULT, event.getConnection().getName());
        } catch (SQLException e) {
            ProxyServer.getInstance().getLogger().warning("Couldn't create player!");
            e.printStackTrace();
        }
        try {
            CollectionList<UUID> friends = SqlUtils.getFriends(event.getConnection().getUniqueId());
            friends.forEach(uuid -> {
                ProxiedPlayer player = ProxyServer.getInstance().getPlayer(uuid);
                if (player != null) player.sendMessage(new TextComponent(ChatColor.YELLOW + event.getConnection().getName() + " joined."));
            });
        } catch (Exception e) {
            ProxyServer.getInstance().getLogger().warning("Couldn't send join message!");
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onLeave(PlayerDisconnectEvent event) {
        try {
            ArrayList<ServerInfo> servers = new ArrayList<>();
            ProxyServer.getInstance().getServers().forEach((server, info) -> {
                if ((server.startsWith("LOBBY") || server.startsWith("lobby"))) servers.add(info);
            });
            Collections.shuffle(servers, new Random()); // shuffle all servers
            event.getPlayer().setReconnectServer(new CollectionList<>(servers).first());
            CollectionList<UUID> friends = SqlUtils.getFriends(event.getPlayer().getUniqueId());
            friends.forEach(uuid -> {
                ProxiedPlayer player = ProxyServer.getInstance().getPlayer(uuid);
                if (player != null) player.sendMessage(new TextComponent(ChatColor.YELLOW + event.getPlayer().getName() + " left."));
            });
        } catch (Exception e) {
            ProxyServer.getInstance().getLogger().warning("Couldn't send disconnect message!");
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
            if (expires > 0 && expires <= currentTimestamp) return;
            boolean perm = expires <= -1;
            Collection<TextComponent> stackedMessage = new ArrayList<>();
            if (perm) stackedMessage.add(new TextComponent(ChatColor.RED + "You are permanently banned from this server!\n\n"));
            if (!perm) stackedMessage.add(new TextComponent(ChatColor.RED + "You are temporarily banned for " + ChatColor.WHITE + days + " days " + ChatColor.RED + "from this server!\n\n"));
            stackedMessage.add(new TextComponent(ChatColor.GRAY + "Reason: " + ChatColor.WHITE + reason + "\n\n"));
            if (reason.equalsIgnoreCase("None")) stackedMessage.add(new TextComponent(ChatColor.YELLOW + "Note: Reason was 'None', please report it to our staff!\n"));
            stackedMessage.add(new TextComponent(ChatColor.GRAY + "Ban ID: " + lastBan.getBanId()));
            event.getPlayer().disconnect(stackedMessage.toArray(new TextComponent[0]));
        } catch (Exception e) {
            event.getPlayer().disconnect(new TextComponent(ChatColor.RED + "Couldn't read config, please report this to admins!"));
            e.printStackTrace();
        }
    }
}
