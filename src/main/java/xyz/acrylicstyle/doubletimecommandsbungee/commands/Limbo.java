package xyz.acrylicstyle.doubletimecommandsbungee.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class Limbo extends Command {
    public Limbo() {
        super("limbo");
    }

    @Override
    public void execute(CommandSender sender0, String[] args) {
        if (!(sender0 instanceof ProxiedPlayer)) {
            sender0.sendMessage(new TextComponent(ChatColor.RED + "This command must be run from in-game."));
            return;
        }
        final ProxiedPlayer sender = (ProxiedPlayer) sender0;
        if (!sender.getServer().getInfo().getName().startsWith("LIMBO")) return;
        sender.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "The lobby you attempted to join was full or offline."));
        sender.sendMessage(TextComponent.fromLegacyText(ChatColor.YELLOW + "Because of this, you were routed to Limbo, a subset of your own imagination."));
        sender.sendMessage(TextComponent.fromLegacyText(ChatColor.LIGHT_PURPLE + "This place doesn't exist anywhere, and you can stay here as long as you'd like."));
        sender.sendMessage(TextComponent.fromLegacyText(ChatColor.GOLD + "To return to \"reality\", use " + ChatColor.AQUA + "/lobby" + ChatColor.GOLD + "."));
        sender.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "Watch out, though, as there are things that live in Limbo."));
    }
}
