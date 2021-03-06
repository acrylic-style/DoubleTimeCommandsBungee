package xyz.acrylicstyle.doubletimecommandsbungee.utils;

import net.md_5.bungee.api.ChatColor;
import xyz.acrylicstyle.doubletimecommandsbungee.annotations.NonNull;
import xyz.acrylicstyle.doubletimecommandsbungee.annotations.Nullable;

import java.util.regex.Pattern;

public enum Ranks {
    OWNER("[OWNER]", "OWNER", ChatColor.RED, null, false, true, false),
    ADMIN("[ADMIN]", "ADMIN", ChatColor.RED, null, false, true, false),
    BUILDTEAM("[BUILD TEAM]", "BUILD TEAM", ChatColor.DARK_AQUA, null, false, true, false),
    MODERATOR("[MOD]", "MOD", ChatColor.DARK_GREEN, null, false, true, false),
    HELPER("[HELPER]", "HELPER", ChatColor.BLUE, null, false, true, false),
    PIGP("[PIG+]", "PIG+", ChatColor.LIGHT_PURPLE, ChatColor.GOLD, true, false, false),
    PIG("[PIG]", "PIG", ChatColor.LIGHT_PURPLE, null, false, false, false),
    YOUTUBE("[YOUTUBE]", "YOUTUBE", ChatColor.RED, null, false, false, false),
    MVPPP("[MVP++]", "MVP++", ChatColor.GOLD, ChatColor.RED, true, false, true),
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
    /**
     * Staff rank or not
     */
    public final boolean specialRank;
    public final boolean purchaseable;

    /**
     * @return + with color, null if there are no +
     */
    @Nullable
    public String getPlus() {
        switch (this) {
            case PIGP:
            case MVPP:
            case VIPP:
                return this.plusColor + "+" + this.defaultColor;
            case MVPPP:
                return this.plusColor + "++" + this.defaultColor;
            default:
                return "";
        }
    }

    public String replacePlus(String name) {
        return name.replaceFirst(Pattern.quote("+"), this.getPlus());
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

    Ranks(String prefix, String name, ChatColor defaultColor, ChatColor plusColor, boolean changeablePlusColor, boolean specialRank, boolean purchaseable) {
        this.prefix = prefix;
        this.name = name;
        this.defaultColor = defaultColor;
        this.plusColor = plusColor;
        this.changeablePlusColor = changeablePlusColor;
        this.specialRank = specialRank;
        this.purchaseable = purchaseable;
    }
}
