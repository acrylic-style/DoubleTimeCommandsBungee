package xyz.acrylicstyle.doubletimecommandsbungee.utils;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import xyz.acrylicstyle.doubletimecommandsbungee.connection.ProxiedOfflinePlayer;

import java.util.UUID;

public class PlayerUtils {
	/**
	 * @param something UUID or username.
	 * @param uuid Is "something" uuid or not
	 */
	public static Player getBySomething(String something, boolean uuid) {
		return new Player(something, uuid);
	}

	public static Player getByName(String username) {
		return new Player(username);
	}

	public static Player getByUUID(UUID uuid) {
		return new Player(uuid);
	}

	public static String getName(ProxiedPlayer player) {
		try {
			Ranks rank = SqlUtils.getRank(player.getUniqueId());
			return rank.getPrefix() + player.getName();
		} catch (Exception e1) {
			e1.printStackTrace();
			return ChatColor.GRAY + player.getName();
		}
	}

	public static String getName(ProxiedOfflinePlayer player) {
		try {
			Ranks rank = SqlUtils.getRank(player.getUniqueId());
			return rank.getPrefix() + player.getName();
		} catch (Exception e1) {
			e1.printStackTrace();
			return ChatColor.GRAY + player.getName();
		}
	}

	public static Ranks getRank(UUID uuid) {
		try {
			return SqlUtils.getRank(uuid);
		} catch (Exception e) { return Ranks.DEFAULT; }
	}
}
