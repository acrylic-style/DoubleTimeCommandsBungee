package xyz.acrylicstyle.doubletimecommandsbungee.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import xyz.acrylicstyle.doubletimecommandsbungee.utils.PlayerUtils;
import xyz.acrylicstyle.doubletimecommandsbungee.utils.Ranks;
import xyz.acrylicstyle.doubletimecommandsbungee.utils.SqlUtils;
import xyz.acrylicstyle.doubletimecommandsbungee.utils.Utils;

import java.sql.SQLException;

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
			sender.sendMessage(new TextComponent(ChatColor.RED + "You need 2 more argument at least! <<player> <reason> [<expires> <m/h/d>]> or <info <banID>>"));
			return;
		}
		if (args[0].equalsIgnoreCase("info")) {
		    xyz.acrylicstyle.doubletimecommandsbungee.types.Ban ban;
            try {
                ban = SqlUtils.getBan(Integer.parseInt(args[1]));
                if (ban == null) {
                    sender.sendMessage(new TextComponent(ChatColor.RED + "Couldn't find punishment with specified ID."));
                    return;
                }
                sender.sendMessage(new TextComponent(ChatColor.AQUA + "Information for Ban ID " + ChatColor.RED + args[1] + ChatColor.YELLOW + ":"));
                sender.sendMessage(new TextComponent(ChatColor.GREEN + "Player: " + ChatColor.RED + PlayerUtils.getName(ban.getPlayer())));
                sender.sendMessage(new TextComponent(ChatColor.GREEN + "Reason: " + ChatColor.RED + ban.getReason()));
                sender.sendMessage(new TextComponent(ChatColor.GREEN + "Expires: " + ChatColor.RED + ban.getExpires() + (ban.getExpires() == -1 ? " (Forever)" : "") + (ban.getExpires() == 0 ? " (Unbanned by " + PlayerUtils.getName(ban.getUnbanner()) + ChatColor.RED + ")" : "")));
                sender.sendMessage(new TextComponent(ChatColor.GREEN + "Executor: " + ChatColor.RED + PlayerUtils.getName(ban.getExecutor())));
				sender.sendMessage(new TextComponent(ChatColor.GREEN + "Current time: " + ChatColor.RED + System.currentTimeMillis()));
            } catch (SQLException e) {
                sender.sendMessage(new TextComponent(ChatColor.RED + "Couldn't fetch punishments!"));
                e.printStackTrace();
                return;
            }
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
				Utils.ban(ps.getUniqueId(), args[1], ((ProxiedPlayer)sender).getUniqueId());
				ps.disconnect(new TextComponent(ChatColor.RED + "You've banned from this server!"));
				sender.sendMessage(new TextComponent(ChatColor.GREEN + "Banned " + ps.getName() + " permanently."));
				return;
			}
			try {
				long expires;
				if (args[3].equalsIgnoreCase("d")) {
					expires = Integer.parseInt(args[2]) * Utils.DAY;
				} else if (args[3].equalsIgnoreCase("h")) {
					expires = Integer.parseInt(args[2]) * Utils.HOUR;
				} else if (args[3].equalsIgnoreCase("m")) {
					expires = Integer.parseInt(args[2]) * Utils.MINUTE;
				} else {
					sender.sendMessage(new TextComponent(ChatColor.RED + "Unknown time type: " + args[3]));
					return;
				}
				Utils.ban(ps.getUniqueId(), args[1], expires, ((ProxiedPlayer)sender).getUniqueId());
				ps.disconnect(new TextComponent(ChatColor.RED + "You've banned from this server!"));
				sender.sendMessage(new TextComponent(ChatColor.GREEN + "Banned " + ps.getName() + " temporarily for " + args[2] + args[3] + "."));
			} catch (NumberFormatException e) {
				sender.sendMessage(new TextComponent(ChatColor.RED + "Time must be number."));
			}
		} catch (SQLException e) {
			sender.sendMessage(new TextComponent(ChatColor.RED + "Unable to ban that player!"));
			e.printStackTrace();
		}
	}
}