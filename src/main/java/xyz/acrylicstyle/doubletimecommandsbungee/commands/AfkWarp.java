package xyz.acrylicstyle.doubletimecommandsbungee.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class AfkWarp extends Command {
    public AfkWarp() {
        super("afkwarp");
    }

    @Override
    public void execute(CommandSender sender0, String[] args) {
        if (!(sender0 instanceof ProxiedPlayer)) {
            sender0.sendMessage(new TextComponent(ChatColor.RED + "This command must be run from in-game."));
            return;
        }
        final ProxiedPlayer sender = (ProxiedPlayer) sender0;
        if (!sender.getServer().getInfo().getName().startsWith("LIMBO")) return;
        sender.connect(ProxyServer.getInstance().getServerInfo("LIMBO"));
    }
}
