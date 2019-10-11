package xyz.acrylicstyle.doubletimecommandsbungee.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class Ping extends Command {
    public Ping() {
        super("ping");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ProxiedPlayer)) {
            sender.sendMessage(new TextComponent(ChatColor.RED + "This command must be run from in-game."));
            return;
        }
        ProxiedPlayer ps = (ProxiedPlayer) sender;
        int ping = ps.getPing();
        String message;
        if (ping <= 5) message = "" + ChatColor.LIGHT_PURPLE + ping;
        else if (ping <= 50) message = "" + ChatColor.GREEN + ping;
        else if (ping <= 150) message = "" + ChatColor.YELLOW + ping;
        else if (ping <= 250) message = "" + ChatColor.GOLD + ping;
        else if (ping <= 350) message = "" + ChatColor.RED + ping;
        else message = "" + ChatColor.DARK_RED + ping;
        ps.sendMessage(new TextComponent(ChatColor.GREEN + "Ping: " + message));
    }
}
