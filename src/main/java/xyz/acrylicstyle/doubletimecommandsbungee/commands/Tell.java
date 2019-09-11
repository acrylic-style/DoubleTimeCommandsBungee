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
import xyz.acrylicstyle.doubletimecommandsbungee.utils.Errors;
import xyz.acrylicstyle.doubletimecommandsbungee.utils.PlayerUtils;
import xyz.acrylicstyle.doubletimecommandsbungee.utils.Utils;

public class Tell extends Command {
	public Tell() {
		super("tell", null, "msg", "w");
	}

	@Override
	public void execute(final CommandSender sender, String[] args) {
		if (!(sender instanceof ProxiedPlayer)) {
			sender.sendMessage(new TextComponent(ChatColor.RED + "" + ChatColor.BOLD + "This command must run from in-game."));
			return;
		}
		if (args.length < 1) {
			sender.sendMessage(new TextComponent(ChatColor.RED + "Please specify a player!"));
			return;
		}
		ProxiedPlayer player = ProxyServer.getInstance().getPlayer(args[0]);
		if (player == null) {
			sender.sendMessage(new TextComponent(ChatColor.RED + "That player is currently offline."));
			return;
		}
		if (args.length < 2) {
			sender.sendMessage(new TextComponent(ChatColor.RED + "Please specify a message!"));
			return;
		}
		final String[] message = {""};
		if (!Utils.run((nope) -> {
			List<String> cmdArgsList = new ArrayList<String>();
			cmdArgsList.addAll(Arrays.asList(args));
			cmdArgsList.remove(0);
			for (String a : cmdArgsList) message[0] += a + " ";
		}, sender, Errors.ARGS_ANALYSIS_FAILED)) return;
		Utils.run((nope) -> {
			sender.sendMessage(new TextComponent(ChatColor.LIGHT_PURPLE + "To " + PlayerUtils.getName(player) + ChatColor.WHITE + ": " + ChatColor.GRAY + message[0]));
			player.sendMessage(new TextComponent(ChatColor.LIGHT_PURPLE + "From " + PlayerUtils.getName((ProxiedPlayer) sender) + ChatColor.WHITE + ": " + ChatColor.GRAY + message[0]));
		}, sender, Errors.COULD_NOT_SEND_MESSAGE);
	}
}
