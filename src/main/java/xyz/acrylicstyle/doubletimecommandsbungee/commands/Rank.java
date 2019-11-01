package xyz.acrylicstyle.doubletimecommandsbungee.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import xyz.acrylicstyle.doubletimecommandsbungee.utils.PlayerUtils;
import xyz.acrylicstyle.doubletimecommandsbungee.utils.Ranks;
import xyz.acrylicstyle.doubletimecommandsbungee.utils.SqlUtils;
import xyz.acrylicstyle.doubletimecommandsbungee.utils.Utils;

import java.sql.SQLException;
import java.util.Locale;
import java.util.UUID;

public class Rank extends Command {
	public Rank() {
		super("rank");
	}

	@Override
	public void execute(final CommandSender sender, String[] args) {
		if (!Utils.must(Ranks.ADMIN, sender)) return;
		if (args.length <= 1) {
			sender.sendMessage(new TextComponent(ChatColor.RED + "You need 2 more argument at least! <new rank> <player>"));
			return;
		}
		UUID player;
		try {
			player = SqlUtils.getUniqueId(args[1]);
		} catch (SQLException e) {
			sender.sendMessage(new TextComponent(ChatColor.RED + "Couldn't find player!"));
			e.printStackTrace();
			return;
		}
		if (sender instanceof ProxiedPlayer) if (player == ((ProxiedPlayer)sender).getUniqueId() && PlayerUtils.getRank(((ProxiedPlayer)sender).getUniqueId()) != Ranks.OWNER) {
			sender.sendMessage(new TextComponent(ChatColor.RED + "You can't change the rank yourself!"));
			return;
		}
		String ucRank = args[0].toUpperCase(Locale.ROOT);
		if (ucRank.equalsIgnoreCase("OWNER") && sender instanceof ProxiedPlayer) {
			sender.sendMessage(new TextComponent(ChatColor.RED + "This command must run from console."));
			return;
		}
		try {
			Ranks.valueOf(ucRank);
		} catch (IllegalArgumentException e) {
			StringBuilder ranks = new StringBuilder();
			for (Ranks rank : Ranks.values()) ranks.append(rank.name()).append(", ");
			sender.sendMessage(new TextComponent(ChatColor.RED + "Please specify defined ranks! ("+ ranks.toString() +")"));
			return;
		}
		String before = PlayerUtils.getName(player);
		try {
			SqlUtils.setRank(player, Ranks.valueOf(ucRank));
		} catch (Exception e) {
			sender.sendMessage(new TextComponent(ChatColor.RED + "There was an unknown error while modifying rank!"));
			e.printStackTrace();
			return;
		}
		sender.sendMessage(new TextComponent(before + ChatColor.GREEN + " is now: " + PlayerUtils.getName(player)));
	}
}
