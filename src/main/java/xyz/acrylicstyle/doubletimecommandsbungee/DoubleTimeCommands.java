package xyz.acrylicstyle.doubletimecommandsbungee;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import xyz.acrylicstyle.doubletimecommandsbungee.commands.Friend;
import xyz.acrylicstyle.doubletimecommandsbungee.commands.Party;
import xyz.acrylicstyle.doubletimecommandsbungee.commands.Rank;
import xyz.acrylicstyle.doubletimecommandsbungee.commands.ResetNickname;
import xyz.acrylicstyle.doubletimecommandsbungee.commands.SetNickname;
import xyz.acrylicstyle.doubletimecommandsbungee.commands.Tell;

public class DoubleTimeCommands extends Plugin implements Listener {
	@Override
	public void onEnable() {
		ProxyServer.getInstance().getPluginManager().registerListener(this, this);
		ProxyServer.getInstance().getPluginManager().registerCommand(this, new Party());
		ProxyServer.getInstance().getPluginManager().registerCommand(this, new Friend());
		ProxyServer.getInstance().getPluginManager().registerCommand(this, new SetNickname());
		ProxyServer.getInstance().getPluginManager().registerCommand(this, new ResetNickname());
		ProxyServer.getInstance().getPluginManager().registerCommand(this, new Tell());
		ProxyServer.getInstance().getPluginManager().registerCommand(this, new Rank());
	}

	@Override
	public void onDisable() {
		try {
			//ConfigProvider.setThenSave("players.*.friend.requests", null, "DoubleTimeCommands");
		} catch (Exception e) {
			ProxyServer.getInstance().getLogger().severe("Couldn't remove friend requests!");
			e.printStackTrace();
		}
	}
}
