package xyz.acrylicstyle.doubletimecommandsbungee.connection;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import xyz.acrylicstyle.doubletimecommandsbungee.utils.PlayerUtils;

import java.io.*;
import java.util.UUID;

public class ChannelListener implements Listener {
    @EventHandler
    public void onPluginMessage(PluginMessageEvent e) {
        ProxyServer.getInstance().getLogger().info("Received message from bukkit, tag is: " + e.getTag());
        if (e.getTag().equalsIgnoreCase("dtc:rank")) {
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(e.getData()));
            try {
                String channel = in.readUTF();
                ProxyServer.getInstance().getLogger().info("Channel: " + channel);
                if (channel.equals("rank")) {
                    ServerInfo server = ProxyServer.getInstance().getPlayer(e.getReceiver().toString()).getServer().getInfo();
                    String input = in.readUTF();
                    sendToBukkit(channel, PlayerUtils.getRank(UUID.fromString(input)).name().toUpperCase(), server);
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    public void sendToBukkit(String channel, String message, ServerInfo server) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(stream);
        try {
            out.writeUTF(channel);
            out.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
        server.sendData("dtc:rank", stream.toByteArray());

    }
}