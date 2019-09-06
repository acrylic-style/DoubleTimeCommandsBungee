package xyz.acrylicstyle.doubletimecommandsbungee.commands;

import java.io.IOException;
import java.util.UUID;

import org.json.simple.parser.ParseException;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import xyz.acrylicstyle.doubletimecommandsbungee.providers.ConfigProvider;
import xyz.acrylicstyle.doubletimecommandsbungee.utils.PlayerUtils;
import xyz.acrylicstyle.doubletimecommandsbungee.utils.Ranks;
import xyz.acrylicstyle.doubletimecommandsbungee.utils.Utils;

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
			uuid = PlayerUtils.getByName(args[0]).toUUID();
			if (!ConfigProvider.getBoolean("players." + uuid + ".ban.banned", false, "DoubleTimeCommands")) {
				sender.sendMessage(new TextComponent(ChatColor.RED + "They're not banned in the past!"));
				return;
			}
		} catch (IllegalArgumentException | IOException | ParseException e) {
			sender.sendMessage(new TextComponent(ChatColor.RED + "A unknown error has occurred while fetching UUID!"));
			e.printStackTrace();
			return;
		}
		if (uuid == ((ProxiedPlayer)sender).getUniqueId()) {
			sender.sendMessage(new TextComponent(ChatColor.RED + "You can't do that. Illegal."));
			return;
		}
		Utils.unban(uuid); // destroy their ban info :thinking:
		sender.sendMessage(new TextComponent(ChatColor.GREEN + "You've unbanned " + args[0] + "!"));
	}
}
