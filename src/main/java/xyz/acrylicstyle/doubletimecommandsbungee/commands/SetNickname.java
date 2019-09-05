package xyz.acrylicstyle.doubletimecommandsbungee.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import xyz.acrylicstyle.doubletimecommandsbungee.providers.ConfigProvider;

public class SetNickname extends Command {
	public SetNickname() {
		super("gnick", null, "gsetnick");
	}

	@Override
	public void execute(final CommandSender sender, String[] args) {
		if (!(sender instanceof ProxiedPlayer)) {
			sender.sendMessage(new TextComponent(ChatColor.RED + "" + ChatColor.BOLD + "This command must run from in-game."));
			return;
		}
		if (args.length == 0) {
			sender.sendMessage(new TextComponent(ChatColor.RED + "You need 1 more argument! (new nick)"));
			return;
		}
		try {
			ConfigProvider.setThenSave("players." + ((ProxiedPlayer)sender).getUniqueId() + ".nick", args[0], "DoubleTimeCommands");
		} catch (Exception e) {
			e.printStackTrace();
			e.getCause().printStackTrace();
			sender.sendMessage(new TextComponent(ChatColor.RED + "Error while saving prefix! Please try again later."));
			return;
		}
		try {
			final String nick = ConfigProvider.getString("players." + ((ProxiedPlayer)sender).getUniqueId() + ".nick", ((ProxiedPlayer)sender).getName(), "DoubleTimeCommands");
			((ProxiedPlayer)sender).setDisplayName(ChatColor.translateAlternateColorCodes('&', nick));
		} catch(Exception e) {
			e.printStackTrace();
			e.getCause().printStackTrace();
			sender.sendMessage(new TextComponent(ChatColor.RED + "Error while setting your name! Please try again later."));
		}
		sender.sendMessage(new TextComponent(ChatColor.GREEN + "Your nick has been set to: \"" + ChatColor.GRAY + ChatColor.translateAlternateColorCodes('&', args[0]) + ChatColor.RESET + ChatColor.GREEN + "\""));
		sender.sendMessage(new TextComponent(ChatColor.GRAY + "To reset your nick, type /gunnick."));
		return;
	}
}
