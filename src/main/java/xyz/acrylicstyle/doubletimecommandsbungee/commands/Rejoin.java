package xyz.acrylicstyle.doubletimecommandsbungee.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import xyz.acrylicstyle.doubletimecommandsbungee.DoubleTimeCommands;

public class Rejoin extends Command {
    public Rejoin() {
        super("rejoin");
    }

    @Override
    public void execute(CommandSender sender0, String[] args) {
        TextComponent text1 = new TextComponent("This command must be used in-game.");
        text1.setColor(ChatColor.RED);
        if (!(sender0 instanceof ProxiedPlayer)) {
            sender0.sendMessage(text1);
            return;
        }
        ProxiedPlayer sender = (ProxiedPlayer) sender0;
        ServerInfo server = sender.getServer().getInfo();
        sender.connect(ProxyServer.getInstance().getServerInfo("LIMBO"));
        DoubleTimeCommands.scheduler.schedule(n -> sender.connect(server), null, 250);
    }
}
