package xyz.acrylicstyle.doubletimecommandsbungee.connection;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import util.DataSerializer;
import xyz.acrylicstyle.doubletimecommandsbungee.types.Player;
import xyz.acrylicstyle.doubletimecommandsbungee.utils.PlayerUtils;
import xyz.acrylicstyle.doubletimecommandsbungee.utils.SqlUtils;
import xyz.acrylicstyle.doubletimecommandsbungee.utils.Utils;

import java.io.*;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.UUID;

public class ChannelListener implements Listener {
    @EventHandler
    public void onPluginMessage(PluginMessageEvent e) {
        //ProxyServer.getInstance().getLogger().info("Received message from bukkit, tag is: " + e.getTag());
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(e.getData()));
        if (e.getTag().equalsIgnoreCase("dtc:rank")) {
            try {
                String subchannel = in.readUTF(); // it'll be player's uuid see PluginChannelListener#sendToBungeeCord
                ServerInfo server = ProxyServer.getInstance().getPlayer(UUID.fromString(subchannel)).getServer().getInfo();
                in.readUTF();
                sendToBukkit(e.getTag(), subchannel, PlayerUtils.getRank(UUID.fromString(subchannel)).name().toUpperCase(), server);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } else if (e.getTag().equalsIgnoreCase("dtc:playing")) {
            try {
                String subchannel = in.readUTF();
                ServerInfo server = ProxyServer.getInstance().getPlayer(UUID.fromString(subchannel.split(",")[0])).getServer().getInfo();
                String message = in.readUTF().toUpperCase();
                Utils.getPlayers(message, (result, error) -> sendToBukkit(e.getTag(), subchannel, NumberFormat.getInstance().format(result), server));
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } else if (e.getTag().equalsIgnoreCase("dtc:availgames")) {
            try {
                String subchannel = in.readUTF();
                ServerInfo server = ProxyServer.getInstance().getPlayer(UUID.fromString(subchannel.split(",")[0])).getServer().getInfo();
                String message = in.readUTF().toUpperCase();
                Utils.getAvailableGames(message, (result, error) -> sendToBukkit(e.getTag(), subchannel, NumberFormat.getInstance().format(result), server));
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } else if (e.getTag().equalsIgnoreCase("dtc:points")) {
            try {
                String subchannel = in.readUTF();
                ServerInfo server = ProxyServer.getInstance().getPlayer(UUID.fromString(subchannel)).getServer().getInfo();
                in.readUTF();
                sendToBukkit(e.getTag(), subchannel, Long.toString(SqlUtils.getPoints(UUID.fromString(subchannel))), server);
            } catch (IOException | SQLException e1) {
                e1.printStackTrace();
            }
        } else if (e.getTag().equalsIgnoreCase("dtc:experience")) {
            try {
                String subchannel = in.readUTF();
                ServerInfo server = ProxyServer.getInstance().getPlayer(UUID.fromString(subchannel)).getServer().getInfo();
                in.readUTF();
                sendToBukkit(e.getTag(), subchannel, Long.toString(SqlUtils.getExperience(UUID.fromString(subchannel))), server);
            } catch (IOException | SQLException e1) {
                e1.printStackTrace();
            }
        } else if (e.getTag().equalsIgnoreCase("dtc:addpoints")) {
            try {
                String subchannel = in.readUTF();
                ServerInfo server = ProxyServer.getInstance().getPlayer(UUID.fromString(subchannel)).getServer().getInfo();
                String message = in.readUTF();
                SqlUtils.addPoints(UUID.fromString(subchannel), Long.parseLong(message));
                sendToBukkit(e.getTag(), subchannel, "", server);
            } catch (IOException | SQLException e1) {
                e1.printStackTrace();
            }
        } else if (e.getTag().equalsIgnoreCase("dtc:addexperience")) {
            try {
                String subchannel = in.readUTF();
                ServerInfo server = ProxyServer.getInstance().getPlayer(UUID.fromString(subchannel)).getServer().getInfo();
                String message = in.readUTF();
                SqlUtils.addExperience(UUID.fromString(subchannel), Long.parseLong(message));
                sendToBukkit(e.getTag(), subchannel, "", server);
            } catch (IOException | SQLException e1) {
                e1.printStackTrace();
            }
        } else if (e.getTag().equalsIgnoreCase("commons:transfer")) {
            try {
                String subchannel = in.readUTF();
                ProxyServer.getInstance().getLogger().info("Subchannel: " + subchannel);
                ServerInfo server = ProxyServer.getInstance().getPlayer(UUID.fromString(subchannel)).getServer().getInfo();
                String input = in.readUTF();
                Utils.transferPlayer(ProxyServer.getInstance().getPlayer(UUID.fromString(subchannel)), input);
                sendToBukkit(e.getTag(), subchannel, "", server);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } else if (e.getTag().equalsIgnoreCase("commons:transfer2")) {
            try {
                String subchannel = in.readUTF();
                ProxyServer.getInstance().getLogger().info("Subchannel: " + subchannel);
                ServerInfo server = ProxyServer.getInstance().getPlayer(UUID.fromString(subchannel)).getServer().getInfo();
                String input = in.readUTF();
                Utils.transferPlayerWithGamePrefix(ProxyServer.getInstance().getPlayer(UUID.fromString(subchannel)), input);
                sendToBukkit(e.getTag(), subchannel, "", server);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } else if (e.getTag().equalsIgnoreCase("helper:connect")) {
            try {
                String subchannel = in.readUTF();
                String input = in.readUTF();
                ProxyServer.getInstance().getPlayer(UUID.fromString(subchannel)).connect(ProxyServer.getInstance().getServerInfo(input));
                sendToBukkit(e.getTag(), subchannel, "", ProxyServer.getInstance().getPlayer(UUID.fromString(subchannel)).getServer().getInfo()); // just for clear callback
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } else if (e.getTag().equalsIgnoreCase("dtc:getPlayer")) {
            try {
                String subchannel = in.readUTF();
                in.readUTF();
                Player player = SqlUtils.getPlayer(UUID.fromString(subchannel));
                DataSerializer dataSerializer = DataSerializer.newInstance();
                dataSerializer.set("points", player.getPoints());
                dataSerializer.set("experience", player.getExperience());
                dataSerializer.set("uuid", player.getUniqueId());
                dataSerializer.set("customPrefix", player.getCustomPrefix());
                dataSerializer.set("rank", player.getRank().name());
                sendToBukkit(e.getTag(), subchannel, dataSerializer.serialize(), ProxyServer.getInstance().getPlayer(UUID.fromString(subchannel)).getServer().getInfo()); // just for clear callback
            } catch (SQLException | IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    public static void sendToBukkit(String tag, String subchannel, String message, ServerInfo server) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(stream);
        try {
            out.writeUTF(subchannel);
            out.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
        server.sendData(tag, stream.toByteArray()); // channel = tag
    }
}