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

public class Tell extends Command {
    public Tell() {
        super("tell", null, "msg", "w");
    }

    @Override
    public void execute(final CommandSender sender, String[] args) {
        if (!(sender instanceof ProxiedPlayer)) {
            sender.sendMessage(new TextComponent(ChatColor.RED + "" + ChatColor.BOLD + "This command must run from in-game."));
            return;
        }
        if (args.length < 1) {
            sender.sendMessage(new TextComponent(ChatColor.RED + "Please specify a player!"));
            return;
        }
        Player player;
        try {
            player = SqlUtils.getPlayer(SqlUtils.getUniqueId(args[0]));
            if (SqlUtils.isPlayerConnected(player.getUniqueId())) {
                sender.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "This player is currently offline."));
            }
        } catch (SQLException e) {
            sender.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "Couldn't find player!"));
            e.printStackTrace();
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(new TextComponent(ChatColor.RED + "Please specify a message!"));
            return;
        }
        final String[] message = {""};
        if (!Utils.run((nope) -> {
            List<String> cmdArgsList = new ArrayList<>(Arrays.asList(args));
            cmdArgsList.remove(0);
            for (String a : cmdArgsList) message[0] += a + " ";
        }, sender, Errors.ARGS_ANALYSIS_FAILED)) return;
        Utils.run(() -> {
            Utils.sendMessage((ProxiedPlayer) sender, new TextComponent(ChatColor.LIGHT_PURPLE + "To " + PlayerUtils.getName(player.getUniqueId()) + ChatColor.WHITE + ": " + ChatColor.GRAY + message[0]));
            Utils.sendMessage(player, new TextComponent(ChatColor.LIGHT_PURPLE + "From " + PlayerUtils.getName((ProxiedPlayer) sender) + ChatColor.WHITE + ": " + ChatColor.GRAY + message[0]));
            Utils.playSound(SqlUtils.getPlayer(player.getUniqueId()), "ENTITY_EXPERIENCE_ORB_PICKUP");
            SqlUtils.setLastMessageFrom(player.getUniqueId(), ((ProxiedPlayer) sender).getUniqueId());
        }, sender, Errors.COULD_NOT_SEND_MESSAGE);
    }
}
