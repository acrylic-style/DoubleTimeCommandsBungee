package xyz.acrylicstyle.doubletimecommandsbungee.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;

public class Limbo extends Command {
    public Limbo() {
        super("limbo");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        sender.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "The lobby you attempted to join was full or offline."));
        sender.sendMessage(TextComponent.fromLegacyText(ChatColor.YELLOW + "Because of this, you were routed to Limbo, a subset of your own imagination."));
        sender.sendMessage(TextComponent.fromLegacyText(ChatColor.LIGHT_PURPLE + "This place doesn't exist anywhere, and you can stay here as long as you'd like."));
        sender.sendMessage(TextComponent.fromLegacyText(ChatColor.GOLD + "To return to \"reality\", use " + ChatColor.AQUA + "/lobby" + ChatColor.GOLD + "."));
        sender.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "Watch out, though, as there are things that live in Limbo."));
    }
}
