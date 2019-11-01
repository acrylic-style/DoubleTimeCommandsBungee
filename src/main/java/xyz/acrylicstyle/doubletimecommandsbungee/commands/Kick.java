package xyz.acrylicstyle.doubletimecommandsbungee.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import xyz.acrylicstyle.doubletimecommandsbungee.types.Player;
import xyz.acrylicstyle.doubletimecommandsbungee.utils.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Kick extends Command {
    public Kick() {
        super("kick");
    }

    @Override
    public void execute(final CommandSender sender, String[] args) {
        if (!Utils.must(Ranks.HELPER, sender)) return;
        if (args.length < 2) {
            sender.sendMessage(new TextComponent(ChatColor.RED + "You need 2 more argument at least! <player> <reason>"));
            return;
        }
        Player ps;
        try {
            ps = SqlUtils.getPlayer(SqlUtils.getUniqueId(args[0]));
        } catch (SQLException e) {
            sender.sendMessage(new TextComponent(ChatColor.RED + "An error occurred while fetching player!"));
            e.printStackTrace();
            return;
        }
        if (!ps.isConnected()) {
            sender.sendMessage(new TextComponent(ChatColor.RED + "That player is currently offline."));
            return;
        }
        if (ps.getUniqueId() == ((ProxiedPlayer)sender).getUniqueId()) {
            sender.sendMessage(new TextComponent(ChatColor.RED + "Why would you do that? :("));
            return;
        }
        if (Utils.must(PlayerUtils.getRank(((ProxiedPlayer)sender).getUniqueId()), ps.getUniqueId())) {
            sender.sendMessage(new TextComponent(ChatColor.RED + "You can't ban that player!"));
            return;
        }
        final String[] message = {""}; // we could use stringbuilder
        if (!Utils.run((nope) -> {
            List<String> cmdArgsList = new ArrayList<>(Arrays.asList(args));
            cmdArgsList.remove(0);
            for (String a : cmdArgsList) message[0] += a + " ";
        }, sender, Errors.ARGS_ANALYSIS_FAILED)) return;
        Utils.kickPlayer(ps, new TextComponent(ChatColor.RED + "You have been kicked from server!\n" + ChatColor.GRAY + "Reason: " + message[0]));
        sender.sendMessage(new TextComponent(ChatColor.GREEN + "Kicked " + PlayerUtils.getName(ps.getUniqueId()) + " with reason: " + message[0]));
    }
}
