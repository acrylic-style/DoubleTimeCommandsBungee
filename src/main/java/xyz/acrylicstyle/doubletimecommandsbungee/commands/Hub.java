package xyz.acrylicstyle.doubletimecommandsbungee.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

public class Hub extends Command {
    public Hub() {
        super("hub", null, "l", "lobby", "zoo");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ProxiedPlayer)) {
            sender.sendMessage(new TextComponent(ChatColor.RED + "This command must be run from in-game."));
            return;
        }
        ProxiedPlayer player = (ProxiedPlayer) sender;
        ArrayList<ServerInfo> servers = new ArrayList<>();
        ProxyServer.getInstance().getServers().forEach((server, info) -> {
            if ((server.startsWith("LOBBY") || server.startsWith("lobby"))) servers.add(info);
        });
        AtomicBoolean connected = new AtomicBoolean(false);
        Collections.shuffle(servers, new Random()); // shuffle all servers
        servers.forEach(info -> info.ping((result, error) -> {
            if (error == null && !connected.get()) {
                connected.set(true);
                player.connect(info); // connect to *random* *online* *lobby* server
            }
        }));
    }
}
