package xyz.acrylicstyle.doubletimecommandsbungee;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;
import xyz.acrylicstyle.doubletimecommandsbungee.commands.*;
import xyz.acrylicstyle.doubletimecommandsbungee.providers.ConfigProvider;
import xyz.acrylicstyle.doubletimecommandsbungee.utils.Ranks;
import xyz.acrylicstyle.doubletimecommandsbungee.utils.Utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

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
		ProxyServer.getInstance().getPluginManager().registerCommand(this, new Ban());
		ProxyServer.getInstance().getPluginManager().registerCommand(this, new Unban());
		ProxyServer.getInstance().getPluginManager().registerCommand(this, new Kick());
		ProxyServer.getInstance().getPluginManager().registerCommand(this, new Hub());
		ProxyServer.getInstance().getPluginManager().registerCommand(this, new Play());
	}

	@EventHandler
	public void onCommand(ChatEvent e) {
		if (!e.isCommand()) return;
		if (e.getMessage().startsWith("/server")) {
			if (!(e.getSender() instanceof ProxiedPlayer)) return;
			if (!Utils.must(Ranks.ADMIN, (ProxiedPlayer) e.getSender())) e.setCancelled(true);
		}
	}

	@EventHandler
	public void onLogin(PostLoginEvent event) {
		try {
			ConfigProvider config = new ConfigProvider("./plugins/DoubleTimeCommands/config.yml");
			UUID uuid = event.getPlayer().getUniqueId();
			String path = "players." + uuid + ".ban.";
			boolean banned = config.configuration.getBoolean(path + "banned", false);
			if (!banned) return; // allow login
			String reason = config.configuration.getString(path + "reason", "None");
			long expires = config.configuration.getLong(path + "expires", -1);
			long currentTimestamp = System.currentTimeMillis();
			long days = Math.round(((float) (expires-currentTimestamp)/86400000F)*10L)/10;
			if (expires > 0 && expires <= currentTimestamp) {
				Utils.unban(uuid);
				return;
			}
			boolean perm = expires <= -1;
			Collection<TextComponent> stackedMessage = new ArrayList<>();
			if (perm) stackedMessage.add(new TextComponent(ChatColor.RED + "You are permanently banned from this server!\n\n"));
			if (!perm) stackedMessage.add(new TextComponent(ChatColor.RED + "You are temporarily banned for " + ChatColor.WHITE + days + " days " + ChatColor.RED + "from this server!\n\n"));
			stackedMessage.add(new TextComponent(ChatColor.GRAY + "Reason: " + ChatColor.WHITE + reason + "\n\n"));
			if (reason.equalsIgnoreCase("None")) stackedMessage.add(new TextComponent(ChatColor.YELLOW + "Note: Reason was 'None', please report it to our staff!\n"));
			stackedMessage.add(new TextComponent(ChatColor.GRAY + "Ban ID: " + config.configuration.getString(path + "banId", "#????????")));
			event.getPlayer().disconnect(stackedMessage.toArray(new TextComponent[0]));
		} catch (Exception e) {
			event.getPlayer().disconnect(new TextComponent(ChatColor.RED + "Couldn't read config, please try again later."));
		}
	}
}
