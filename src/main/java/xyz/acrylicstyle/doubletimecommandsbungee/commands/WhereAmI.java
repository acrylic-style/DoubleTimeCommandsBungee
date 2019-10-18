package xyz.acrylicstyle.doubletimecommandsbungee.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class WhereAmI extends Command {
    public WhereAmI() {
        super("whereami", null, "wtfserver");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ProxiedPlayer)) {
            sender.sendMessage(new TextComponent(ChatColor.RED + "This command must run from in-game."));
            return;
        }
        ProxiedPlayer player = (ProxiedPlayer) sender;
        player.sendMessage(new TextComponent(ChatColor.GREEN + "You are currently playing at " + ChatColor.AQUA + player.getServer().getInfo().getName() + ChatColor.GREEN + "!"));
    }
}
