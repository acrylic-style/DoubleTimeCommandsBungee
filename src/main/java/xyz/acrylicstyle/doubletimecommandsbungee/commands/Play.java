package xyz.acrylicstyle.doubletimecommandsbungee.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import util.Collection;
import xyz.acrylicstyle.doubletimecommandsbungee.DoubleTimeCommands;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Play extends Command {
    public Play() {
        super("play");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ProxiedPlayer)) {
            sender.sendMessage(new TextComponent(ChatColor.RED + "This command must be run from in-game."));
            return;
        }
        if (args.length == 0) {
            sender.sendMessage(new TextComponent(ChatColor.RED + "Please specify game!"));
            return;
        }
        String gamePrefix;
        boolean shuffle = false;
        String format = ChatColor.GREEN + "Sending to %s!";
        Collection<String, ArrayList> config = DoubleTimeCommands.config.getConfigSectionValue("games", ArrayList.class);
        if (config.containsKey(args[0])) {
            gamePrefix = (String) config.get(args[0]).get(0);
            if (config.get(args[0]).size() >= 2) shuffle = (Boolean) config.get(args[0]).get(1);
            if (config.get(args[0]).size() >= 3) format = (String) config.get(args[0]).get(2);
        } else {
            sender.sendMessage(new TextComponent(ChatColor.RED + "Please specify valid game!"));
            return;
        }
        ProxiedPlayer player = (ProxiedPlayer) sender;
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
}
