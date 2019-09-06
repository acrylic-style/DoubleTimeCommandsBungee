package xyz.acrylicstyle.doubletimecommandsbungee.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import xyz.acrylicstyle.doubletimecommandsbungee.utils.PlayerUtils;
import xyz.acrylicstyle.doubletimecommandsbungee.utils.Ranks;
import xyz.acrylicstyle.doubletimecommandsbungee.utils.Utils;

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
		ProxiedPlayer ps = ProxyServer.getInstance().getPlayer(args[0]);
		if (ps == null) {
			sender.sendMessage(new TextComponent(ChatColor.RED + "That player is currently offline."));
			return;
		}
		if (ps.getUniqueId() == ((ProxiedPlayer)sender).getUniqueId()) {
			sender.sendMessage(new TextComponent(ChatColor.RED + "Why would you do that? :("));
			return;
		}
		if (Utils.must(PlayerUtils.getRank(((ProxiedPlayer)sender).getUniqueId()), ps)) {
			sender.sendMessage(new TextComponent(ChatColor.RED + "You can't ban that player!"));
			return;
		}
		String message = "";
		List<String> cmdArgsList = new ArrayList<String>();
		cmdArgsList.addAll(Arrays.asList(args));
		cmdArgsList.remove(0);
		for (String a : cmdArgsList) message += a + " ";
		ps.disconnect(new TextComponent(ChatColor.RED + "You've kicked with reason: " + message));
		sender.sendMessage(new TextComponent(ChatColor.GREEN + "Kicked " + ps.getName() + " with reason: " + message));
	}
}
