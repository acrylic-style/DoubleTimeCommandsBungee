package xyz.acrylicstyle.doubletimecommandsbungee.commands;

import java.io.IOException;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import xyz.acrylicstyle.doubletimecommandsbungee.utils.PlayerUtils;
import xyz.acrylicstyle.doubletimecommandsbungee.utils.Ranks;
import xyz.acrylicstyle.doubletimecommandsbungee.utils.Utils;

public class Ban extends Command {
	public Ban() {
		super("ban");
	}

	@Override
	public void execute(final CommandSender sender, String[] args) {
		if (!(sender instanceof ProxiedPlayer)) {
			sender.sendMessage(new TextComponent(ChatColor.RED + "" + ChatColor.BOLD + "This command must run from in-game."));
			return;
		}
		if (!Utils.must(Ranks.MODERATOR, sender)) return;
		if (args.length < 2) {
			sender.sendMessage(new TextComponent(ChatColor.RED + "You need 2 more argument at least! <player> <reason> [<expires> <d/h/m>]"));
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
		try {
			if (args.length <= 3) {
				Utils.ban(ps.getUniqueId(), ((ProxiedPlayer)sender).getUniqueId(), args[1]);
				ps.disconnect(new TextComponent(ChatColor.RED + "You've banned from this server!"));
				sender.sendMessage(new TextComponent(ChatColor.GREEN + "Banned " + ps.getName() + " permanently."));
				return;
			}
			try {
				long expires = System.currentTimeMillis();
				if (args[3].equalsIgnoreCase("d")) {
					expires = expires + (Integer.parseInt(args[2]) * Utils.DAY);
				} else if (args[3].equalsIgnoreCase("h")) {
					expires = expires + (Integer.parseInt(args[2]) * Utils.HOUR);
				} else if (args[3].equalsIgnoreCase("m")) {
					expires = expires + (Integer.parseInt(args[2]) * Utils.MINUTE);
				} else {
					sender.sendMessage(new TextComponent(ChatColor.RED + "Unknown time type: " + args[3]));
					return;
				}
				Utils.ban(ps.getUniqueId(), ((ProxiedPlayer)sender).getUniqueId(), args[1], expires);
				ps.disconnect(new TextComponent(ChatColor.RED + "You've banned from this server!"));
				sender.sendMessage(new TextComponent(ChatColor.GREEN + "Banned " + ps.getName() + " temporarily for " + args[2] + args[3] + "."));
			} catch (NumberFormatException e) {
				sender.sendMessage(new TextComponent(ChatColor.RED + "Time must be number."));
			}
		} catch (IOException e) {
			sender.sendMessage(new TextComponent(ChatColor.RED + "Unable to ban that player!"));
			e.printStackTrace();
		}
	}
}