package xyz.acrylicstyle.doubletimecommandsbungee.utils;

import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;

public class Utils {
	/**
	 * @param consumer Consumer without any args. <b>It won't pass any arguments.</b>
	 */
	public static void runAsync(Consumer<?> consumer) {
		ProxyServer.getInstance().getScheduler().runAsync(Utils.getPlugin(), new Runnable() {
			public void run() {
				consumer.accept(null);
			}
		});
	}

	/**
	 * @param consumer Consumer without any args. <b>It won't pass any arguments.</b>
	 */
	public static void run(Consumer<?> consumer) {
		ProxyServer.getInstance().getScheduler().schedule(Utils.getPlugin(), new Runnable() {
			public void run() {
				consumer.accept(null);
			}
		}, 1, TimeUnit.NANOSECONDS);
	}

	public static Plugin getPlugin() {
		return ProxyServer.getInstance().getPluginManager().getPlugin("DoubleTimeCommands");
	}

	/**
	 * @param required Required rank for do something
	 * @param sender anything extends CommandSender for check if they have Admin rank
	 * @return True if the required rank equals actual rank but console always returns true
	 * @example
	 * if (!Utils.must(Ranks.ADMIN, PlayerUtils.getRank(player.getUniqueId))) return; // it sends message automatically, so do only return
	 */
	public static boolean must(Ranks required, CommandSender sender) {
		if (!(sender instanceof ProxiedPlayer)) return true;
		ProxiedPlayer player = (ProxiedPlayer) sender;
		Ranks actual = PlayerUtils.getRank(player.getUniqueId());
		if (required.ordinal() < actual.ordinal()) {
			player.sendMessage(new TextComponent(ChatColor.RED + "You must be " + required.name.toLowerCase(Locale.ROOT) + " or higher to use this command!"));
			return false;
		}
		return true;
	}
}
