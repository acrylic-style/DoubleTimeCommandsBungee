package xyz.acrylicstyle.doubletimecommandsbungee.utils;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import xyz.acrylicstyle.doubletimecommandsbungee.connection.ProxiedOfflinePlayer;

import java.util.UUID;

public class PlayerUtils {
	public static Player getByName(String username) {
		return new Player(username);
	}

	public static Player getByUUID(UUID uuid) {
		return new Player(uuid);
	}

	public static String getName(ProxiedPlayer player) {
		return getName(player.getUniqueId());
	}

	public static String getName(UUID player) {
		try {
			Ranks rank = SqlUtils.getRank(player);
			return rank.getPrefix() + SqlUtils.getName(player);
		} catch (Exception e1) {
			e1.printStackTrace();
			return ChatColor.GRAY + "UUID=" + player;
		}
	}

	public static String getName(ProxiedOfflinePlayer player) {
		return getName(player.getUniqueId());
	}

	public static Ranks getRank(UUID uuid) {
		try {
			return SqlUtils.getRank(uuid);
		} catch (Exception e) { return Ranks.DEFAULT; }
	}
}
