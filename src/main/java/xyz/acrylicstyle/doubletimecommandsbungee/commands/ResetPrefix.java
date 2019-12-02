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

public class ResetPrefix extends Command {
	public ResetPrefix() { super("resetprefix", null); }

	@Override
	public void execute(final CommandSender sender, String[] args) {
		if (!Utils.must(Ranks.MVPPP, sender)) return;
		UUID uuid;
		try {
			if (args.length >= 1) {
				if (!Utils.must(Ranks.ADMIN, sender)) return;
				uuid = SqlUtils.getUniqueId(args[1]);
				if (uuid == null) {
					sender.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "Couldn't find player!"));
					return;
				}
			} else {
				if (!(sender instanceof ProxiedPlayer)) {
					sender.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "You must specify player."));
					return;
				}
				uuid = ((ProxiedPlayer) sender).getUniqueId();
			}
			SqlUtils.setCustomPrefix(null, uuid);
		} catch (SQLException e) {
			sender.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "An error occurred while saving prefix!"));
			e.printStackTrace();
			return;
		}
		sender.sendMessage(TextComponent.fromLegacyText(ChatColor.GREEN + "Prefix has been cleared!"));
	}
}
