package xyz.acrylicstyle.doubletimecommandsbungee.utils;

import net.md_5.bungee.api.ChatColor;
import xyz.acrylicstyle.doubletimecommandsbungee.annotations.NonNull;
import xyz.acrylicstyle.doubletimecommandsbungee.annotations.Nullable;

public enum Ranks {
	OWNER("[OWNER]", "OWNER", ChatColor.RED, null, false, true, false),
	ADMIN("[ADMIN]", "ADMIN", ChatColor.RED, null, false, true, false),
	MODERATOR("[MOD]", "MOD", ChatColor.DARK_GREEN, null, false, true, false),
	HELPER("[HELPER]", "HELPER", ChatColor.BLUE, null, false, true, false),
	BUILDTEAM("[BUILD TEAM]", "BUILD TEAM", ChatColor.DARK_AQUA, null, false, true, false),
	PIGP("[PIG+]", "PIG+", ChatColor.LIGHT_PURPLE, ChatColor.GOLD, true, false, false),
	PIG("[PIG]", "PIG", ChatColor.LIGHT_PURPLE, null, false, false, false),
	YOUTUBE("[YOUTUBE]", "YOUTUBE", ChatColor.RED, null, false, false, false),
	MVPPP("[MVP+]", "MVP+", ChatColor.GOLD, ChatColor.RED, true, false, true),
	MVPP("[MVP+]", "MVP+", ChatColor.AQUA, ChatColor.RED, true, false, true),
	MVP("[MVP]", "MVP", ChatColor.AQUA, null, false, false, true),
	VIPP("[VIP+]", "VIP+", ChatColor.GREEN, ChatColor.GOLD, false, false, true),
	VIP("[VIP]", "VIP", ChatColor.GREEN, null, false, false, true),
	SAND("[SAND]", "SAND", ChatColor.YELLOW, null, false, true, false),
	DEFAULT("", "Default", ChatColor.GRAY, null, false, false, false);

	@NonNull
	public final String prefix;
	@NonNull
	public final String name;
	@NonNull
	public final ChatColor defaultColor;
	@Nullable
	public final ChatColor plusColor;
	public final boolean changeablePlusColor;
	public final boolean specialRank;
	public final boolean purchaseable;

	@Nullable
	/**
	 * @return + with color, null if there are no +
	 */
	public String getPlus() {
		switch (this) {
		case PIGP:
		case MVPP:
		case VIPP:
			return this.plusColor + "+";
		case MVPPP:
			return this.plusColor + "++";
		default:
			return null;
		}
	}

	public String replacePlus(String name) {
		return name.replaceFirst("+", this.getPlus());
	}

	@NonNull
	public String getPrefix() {
		return this.defaultColor + replacePlus(this.prefix);
	}

	@NonNull
	public String getName() {
		return this.defaultColor + replacePlus(this.name);
	}

	public String toString() {
		return this.name;
	}

	private Ranks(String prefix, String name, ChatColor defaultColor, ChatColor plusColor, boolean changeablePlusColor, boolean specialRank, boolean purchaseable) {
		this.prefix = prefix;
		this.name = name;
		this.defaultColor = defaultColor;
		this.plusColor = plusColor;
		this.changeablePlusColor = changeablePlusColor;
		this.specialRank = specialRank;
		this.purchaseable = purchaseable;
	}
}
