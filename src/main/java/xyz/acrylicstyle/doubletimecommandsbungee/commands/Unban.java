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

public class Unban extends Command {
	public Unban() {
		super("unban");
	}

	@Override
	public void execute(final CommandSender sender, String[] args) {
		if (!Utils.must(Ranks.MODERATOR, sender)) return;
		if (args.length < 1) {
			sender.sendMessage(new TextComponent(ChatColor.RED + "You need 1 more argument! <player>"));
			return;
		}
		UUID uuid;
		try {
			uuid = SqlUtils.getUniqueId(args[0]);
			if (!SqlUtils.isBanned(uuid)) {
				sender.sendMessage(new TextComponent(ChatColor.RED + "They're not banned in the past!"));
				return;
			}
		} catch (SQLException e) {
			sender.sendMessage(new TextComponent(ChatColor.RED + "Couldn't get player!"));
			e.printStackTrace();
			return;
		}
		if (uuid == ((ProxiedPlayer)sender).getUniqueId()) {
			sender.sendMessage(new TextComponent(ChatColor.RED + "You can't do that. Illegal."));
			return;
		}
		try {
			SqlUtils.unban(uuid, ((ProxiedPlayer) sender).getUniqueId());
		} catch (SQLException e) {
			sender.sendMessage(new TextComponent(ChatColor.RED + "Couldn't unban that player!"));
			e.printStackTrace();
		}
	}
}
