package xyz.acrylicstyle.doubletimecommandsbungee.commands;

import java.util.Locale;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import xyz.acrylicstyle.doubletimecommandsbungee.providers.ConfigProvider;
import xyz.acrylicstyle.doubletimecommandsbungee.utils.Ranks;

public class Rank extends Command {
	public Rank() {
		super("rank", "doubletimecommands.rank");
	}

	@Override
	public void execute(final CommandSender sender, String[] args) {
		if (!(sender instanceof ProxiedPlayer)) {
			sender.sendMessage(new TextComponent(ChatColor.RED + "" + ChatColor.BOLD + "This command must run from in-game."));
			return;
		}
		if (args.length <= 1) {
			sender.sendMessage(new TextComponent(ChatColor.RED + "You need 2 more argument at least! <new rank> <player>"));
			return;
		}
		ProxiedPlayer ps = ProxyServer.getInstance().getPlayer(args[1]);
		if (ps == null) {
			sender.sendMessage(new TextComponent("That player is currently offline."));
			return;
		}
		String ucRank = args[0].toUpperCase(Locale.ROOT);
		if (Ranks.valueOf(ucRank) == null) {
			String ranks = "";
			for (Ranks rank : Ranks.values()) ranks += rank.name() + ", ";
			sender.sendMessage(new TextComponent(ChatColor.RED + "Please specify defined ranks! ("+ ranks +")"));
			return;
		}
		try {
			ConfigProvider.setThenSave("players." + ps.getUniqueId() + ".rank", ucRank, "DoubleTimeCommands");
		} catch (Exception e) {
			sender.sendMessage(new TextComponent("There was an unknown error while modifying rank!"));
			e.printStackTrace();
			return;
		}
		sender.sendMessage(new TextComponent(ChatColor.GREEN + ps.getName() + " is now: " + Ranks.valueOf(ucRank).getPrefix() + " " + ps.getName()));
	}
}
