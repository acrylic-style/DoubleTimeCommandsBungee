package xyz.acrylicstyle.doubletimecommandsbungee.commands;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;

import org.json.simple.parser.ParseException;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import xyz.acrylicstyle.doubletimecommandsbungee.providers.ConfigProvider;
import xyz.acrylicstyle.doubletimecommandsbungee.utils.*;

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
		final UUID[] uuid = new UUID[1];
		if (!Utils.run(() -> {
			uuid[0] = PlayerUtils.getByName(args[0]).toUUID();
			if (!ConfigProvider.getBoolean("players." + uuid[0] + ".ban.banned", false, "DoubleTimeCommands")) {
				sender.sendMessage(new TextComponent(ChatColor.RED + "They're not banned in the past!"));
			}
		}, sender, Errors.UNABLE_TO_FETCH_UUID)) return;
		if (uuid[0] == ((ProxiedPlayer)sender).getUniqueId()) {
			sender.sendMessage(new TextComponent(ChatColor.RED + "You can't do that. Illegal."));
			return;
		}
		if (!Utils.run(() -> {
			Utils.unban(uuid[0]);
		}, sender, Errors.IO_ERROR)) return;
		sender.sendMessage(new TextComponent(ChatColor.GREEN + "You've unbanned " + args[0] + "!"));
	}
}
