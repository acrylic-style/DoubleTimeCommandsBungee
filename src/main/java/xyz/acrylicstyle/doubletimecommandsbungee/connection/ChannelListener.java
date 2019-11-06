package xyz.acrylicstyle.doubletimecommandsbungee.connection;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import xyz.acrylicstyle.doubletimecommandsbungee.utils.PlayerUtils;
import xyz.acrylicstyle.doubletimecommandsbungee.utils.Utils;

import java.io.*;
import java.util.UUID;

public class ChannelListener implements Listener {
    @EventHandler
    public void onPluginMessage(PluginMessageEvent e) {
        ProxyServer.getInstance().getLogger().info("Received message from bukkit, tag is: " + e.getTag());
        if (e.getTag().equalsIgnoreCase("dtc:rank")) {
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(e.getData()));
            try {
                String subchannel = in.readUTF(); // it'll be player's uuid see PluginChannelListener#sendToBungeeCord
                ProxyServer.getInstance().getLogger().info("Subchannel: " + subchannel);
                ServerInfo server = ProxyServer.getInstance().getPlayer(UUID.fromString(subchannel)).getServer().getInfo();
                in.readUTF();
                sendToBukkit(e.getTag(), subchannel, PlayerUtils.getRank(UUID.fromString(subchannel)).name().toUpperCase(), server);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } else if (e.getTag().equalsIgnoreCase("commons:transfer")) {
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(e.getData()));
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
        } else if (e.getTag().equalsIgnoreCase("helper:connect")) {
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(e.getData()));
            try {
                String subchannel = in.readUTF();
                String input = in.readUTF();
                ProxyServer.getInstance().getPlayer(UUID.fromString(subchannel)).connect(ProxyServer.getInstance().getServerInfo(input));
            } catch (IOException e1) {
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