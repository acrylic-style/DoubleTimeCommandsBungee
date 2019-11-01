package xyz.acrylicstyle.doubletimecommandsbungee.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import org.json.simple.parser.ParseException;
import xyz.acrylicstyle.doubletimecommandsbungee.utils.PlayerUtils;
import xyz.acrylicstyle.doubletimecommandsbungee.utils.Ranks;
import xyz.acrylicstyle.doubletimecommandsbungee.utils.SqlUtils;
import xyz.acrylicstyle.doubletimecommandsbungee.utils.Utils;

import java.io.IOException;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

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
		final AtomicReference<UUID> uuid = new AtomicReference<>();
		try {
			uuid.set(PlayerUtils.getByName(args[0]).toUUID());
			if (!SqlUtils.isBanned(uuid.get())) {
				sender.sendMessage(new TextComponent(ChatColor.RED + "They're not banned in the past!"));
			}
		} catch (IOException | ParseException | SQLException e) {
			e.printStackTrace();
		}
		if (uuid.get() == ((ProxiedPlayer)sender).getUniqueId()) {
			sender.sendMessage(new TextComponent(ChatColor.RED + "You can't do that. Illegal."));
			return;
		}
		try {
			SqlUtils.unban(uuid.get(), ((ProxiedPlayer) sender).getUniqueId());
		} catch (SQLException e) {
			sender.sendMessage(new TextComponent(ChatColor.RED + "Couldn't unban that player!"));
			e.printStackTrace();
		}
	}
}
