package xyz.acrylicstyle.doubletimecommandsbungee.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import xyz.acrylicstyle.doubletimecommandsbungee.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

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
        String format = ChatColor.GREEN + "Sending to %s!";
        if (args[0].equalsIgnoreCase("bw") || args[0].equalsIgnoreCase("bedwars") || args[0].equalsIgnoreCase("bed")) {
            gamePrefix = "BEDWARS";
        } else if (args[0].equalsIgnoreCase("hp") || args[0].equalsIgnoreCase("hotpotato") || args[0].equalsIgnoreCase("potato")) {
            gamePrefix = "HOTPOTATO";
            format = ChatColor.GOLD + "Sending to %s!";
        } else if (args[0].equalsIgnoreCase("ze") || args[0].equalsIgnoreCase("zombieescape")) {
            gamePrefix = "ZOMBIEESCAPE";
            format = ChatColor.DARK_GREEN + "Sending to %s!";
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
        Collections.shuffle(servers, new Random()); // shuffle all servers
        String finalFormat = format;
        String before = player.getServer().getInfo().getName();
        servers.forEach(info -> info.ping((result, error) -> {
            if (error == null && !connected.get() && result.getPlayers().getMax() > result.getPlayers().getOnline()) {
                connected.set(true);
                player.sendMessage(new TextComponent(String.format(finalFormat, info.getName())));
                player.connect(info); // connect to *random* *online* *lobby* server
            }
        }));
        ProxyServer.getInstance().getScheduler().schedule(Utils.getPlugin(), () -> {
            if (!connected.get() && before.equalsIgnoreCase(player.getServer().getInfo().getName()))
                player.sendMessage(new TextComponent(ChatColor.RED + "We couldn't find available server!"));
        }, 5, TimeUnit.SECONDS);
    }
}
