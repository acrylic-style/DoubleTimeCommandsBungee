package xyz.acrylicstyle.doubletimecommandsbungee.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import xyz.acrylicstyle.doubletimecommandsbungee.utils.Ranks;
import xyz.acrylicstyle.doubletimecommandsbungee.utils.SqlUtils;
import xyz.acrylicstyle.doubletimecommandsbungee.utils.Utils;

import java.sql.SQLException;
import java.util.UUID;

public class SetPoints extends Command {
    public SetPoints() {
        super("setpoints");
    }

    @Override
    public void execute(CommandSender sender0, String[] args) {
        if (!(sender0 instanceof ProxiedPlayer)) {
            sender0.sendMessage(new TextComponent(ChatColor.RED + "This command must be run from in-game."));
            return;
        }
        ProxiedPlayer sender = (ProxiedPlayer) sender0;
        if (!Utils.must(Ranks.ADMIN, sender)) return;
        if (args.length < 1) {
            sender.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "You need 1 argument at least! <amount of points> [player]"));
            return;
        }
        long points;
        try {
            points = Long.parseLong(args[0]);
        } catch (NumberFormatException e) {
            sender.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "Please specify a valid number!"));
            return;
        }
        UUID uuid;
        if (args.length >= 2) {
            try {
                uuid = SqlUtils.getUniqueId(args[1]);
                if (uuid == null) {
                    sender.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "Unable to find player!"));
                    return;
                }
            } catch (SQLException e) {
                sender.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "An unknown error occurred while fetching player!"));
                e.printStackTrace();
                return;
            }
        } else {
            uuid = sender.getUniqueId();
        }
        try {
            SqlUtils.setPoints(uuid, points);
        } catch (SQLException e) {
            sender.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "An error occurred while adding points!"));
            e.printStackTrace();
        }
    }
}
