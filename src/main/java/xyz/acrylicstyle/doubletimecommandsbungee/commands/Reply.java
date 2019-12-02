package xyz.acrylicstyle.doubletimecommandsbungee.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import xyz.acrylicstyle.doubletimecommandsbungee.types.Player;
import xyz.acrylicstyle.doubletimecommandsbungee.utils.Errors;
import xyz.acrylicstyle.doubletimecommandsbungee.utils.PlayerUtils;
import xyz.acrylicstyle.doubletimecommandsbungee.utils.SqlUtils;
import xyz.acrylicstyle.doubletimecommandsbungee.utils.Utils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class Reply extends Command {
    public Reply() {
        super("reply", null, "r");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ProxiedPlayer)) {
            sender.sendMessage(new TextComponent(ChatColor.RED + "" + ChatColor.BOLD + "This command must run from in-game."));
            return;
        }
        if (args.length < 1) {
            sender.sendMessage(new TextComponent(ChatColor.RED + "Please specify message!"));
            return;
        }
        Player player;
        try {
            UUID p = SqlUtils.getLastMessageFrom(((ProxiedPlayer) sender).getUniqueId());
            if (p == null) {
                sender.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "Nobody has messaged you in the past!"));
                return;
            }
            player = SqlUtils.getPlayer(p);
            if (SqlUtils.isPlayerConnected(player.getUniqueId())) {
                sender.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "This player is currently offline."));
                return;
            }
        } catch (SQLException e) {
            sender.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "Couldn't find player! " + ChatColor.GRAY + "(wait, it's weird. if you see this, please report it to the admins!)"));
            e.printStackTrace();
            return;
        }
        final String[] message = {""};
        if (!Utils.run((nope) -> {
            List<String> cmdArgsList = new ArrayList<>(Arrays.asList(args));
            for (String a : cmdArgsList) message[0] += a + " ";
        }, sender, Errors.ARGS_ANALYSIS_FAILED)) return;
        Utils.run(() -> {
            Utils.sendMessage((ProxiedPlayer) sender, new TextComponent(ChatColor.LIGHT_PURPLE + "To " + PlayerUtils.getName(player.getUniqueId()) + ChatColor.WHITE + ": " + ChatColor.GRAY + message[0]));
            Utils.sendMessage(player, new TextComponent(ChatColor.LIGHT_PURPLE + "From " + PlayerUtils.getName((ProxiedPlayer) sender) + ChatColor.WHITE + ": " + ChatColor.GRAY + message[0]));
            Utils.playSound(SqlUtils.getPlayer(player.getUniqueId()), "ENTITY_EXPERIENCE_ORB_PICKUP");
        }, sender, Errors.COULD_NOT_SEND_MESSAGE);
    }
}
