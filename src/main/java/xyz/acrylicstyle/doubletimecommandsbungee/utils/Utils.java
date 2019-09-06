package xyz.acrylicstyle.doubletimecommandsbungee.utils;

import java.io.IOException;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import xyz.acrylicstyle.doubletimecommandsbungee.providers.ConfigProvider;

public class Utils {
	public final static int DAY = 86400000;
	public final static int HOUR = 3600000;
	public final static int MINUTE = 60000;

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

	public static void ban(UUID uuid, UUID executor, String reason) throws IOException {
		Utils.ban(uuid, executor, reason, -1); // -1 means never
	}

	public static void ban(UUID uuid, UUID executor, String reason, long expires) throws IOException {
		String id = Utils.generateBanID();
		ConfigProvider.setThenSave("players." + uuid + ".ban.banned", true, "DoubleTimeCommands");
		ConfigProvider.setThenSave("players." + uuid + ".ban.executor", executor.toString(), "DoubleTimeCommands");
		ConfigProvider.setThenSave("players." + uuid + ".ban.reason", reason, "DoubleTimeCommands");
		ConfigProvider.setThenSave("players." + uuid + ".ban.expires", expires, "DoubleTimeCommands");
		ConfigProvider.setThenSave("players." + uuid + ".ban.banId", id, "DoubleTimeCommands");
	}

	public static void unban(UUID uuid) {
		try {
			ConfigProvider.setThenSave("players." + uuid + ".ban", null, "DoubleTimeCommands");
		} catch (Exception ignore) {}
	}

	public static String generateAlphaNumericString(int length) {
		String alphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
		StringBuilder sb = new StringBuilder(length);
		for (int i = 0; i < length; i++) {
			int index = (int) (alphaNumericString.length() * Math.random());
			sb.append(alphaNumericString.charAt(index));
		}
		return sb.toString();
	}

	public static String generateBanID() {
		return "#" + Utils.generateAlphaNumericString(8);
	}
}
