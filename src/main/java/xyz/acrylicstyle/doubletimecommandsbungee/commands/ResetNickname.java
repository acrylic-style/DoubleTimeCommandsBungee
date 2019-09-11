package xyz.acrylicstyle.doubletimecommandsbungee.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import xyz.acrylicstyle.doubletimecommandsbungee.providers.ConfigProvider;
import xyz.acrylicstyle.doubletimecommandsbungee.utils.Errors;
import xyz.acrylicstyle.doubletimecommandsbungee.utils.Utils;

public class ResetNickname extends Command {
	public ResetNickname() {
		super("gunnick", null, "gresetnick");
	}

	@Override
	public void execute(final CommandSender sender, String[] args) {
		if (!(sender instanceof ProxiedPlayer)) {
			sender.sendMessage(new TextComponent(ChatColor.RED + "" + ChatColor.BOLD + "This command must run from in-game."));
			return;
		}
		if (!Utils.run(() -> {
			ConfigProvider.setThenSave("players." + ((ProxiedPlayer)sender).getUniqueId() + ".nick", null, "DoubleTimeCommands");
			final String nick = ConfigProvider.getString("players." + ((ProxiedPlayer)sender).getUniqueId() + ".nick", ((ProxiedPlayer)sender).getName(), "DoubleTimeCommands");
			((ProxiedPlayer)sender).setDisplayName(ChatColor.translateAlternateColorCodes('&', nick));
		}, sender, Errors.COULD_NOT_SAVE_CONFIG)) return;
		sender.sendMessage(new TextComponent(ChatColor.GREEN + "Your nickname has been cleared!"));
	}
}
