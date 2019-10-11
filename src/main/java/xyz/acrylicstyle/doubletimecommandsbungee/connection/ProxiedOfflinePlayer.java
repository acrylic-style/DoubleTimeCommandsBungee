package xyz.acrylicstyle.doubletimecommandsbungee.connection;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.UUID;

import org.json.simple.parser.ParseException;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import xyz.acrylicstyle.doubletimecommandsbungee.utils.PlayerUtils;

public class ProxiedOfflinePlayer implements IProxiedOfflinePlayer {
	private final UUID uuid;

	public ProxiedOfflinePlayer(UUID uuid) {
		this.uuid = uuid;
	}

	public static IProxiedOfflinePlayer getFromProxiedPlayer(ProxiedPlayer player) {
		if (player == null) {
			throw new NullPointerException("Player cannot be null.");
		}
		return new ProxiedOfflinePlayer(player.getUniqueId());
	}

	public String getNameFromUUID(UUID uuid) {
		try {
			return PlayerUtils.getByUUID(uuid).toUsername();
		} catch (IllegalArgumentException | IOException | ParseException ignored) {}
		return null;
	}

	@Override
	public ProxiedPlayer toProxiedPlayer() {
		return (ProxiedPlayer) this;
	}

	public static String getName(ProxiedPlayer player, String uuid) {
		return ProxiedOfflinePlayer.getName(player, UUID.fromString(uuid));
	}

	public static String getName(ProxiedPlayer player, UUID uuid) {
		try {
			return (player == null || player.getName() == null) ? PlayerUtils.getByUUID(uuid).toUsername() : player.getName();
		} catch (Exception ignore) {}
		assert player != null;
		return player.getName();
	}

	@Override
	public String getName() {
		try {
			return PlayerUtils.getByUUID(this.getUniqueId()).toUsername();
		} catch (Exception ignore) {
			return null;
		}
	}

	public UUID getUniqueId() {
		return this.uuid;
	}

	public boolean isConnected() {
		return false;
	}

	@Override
	public String getNameWithException() throws IllegalArgumentException, IOException, ParseException {
		return PlayerUtils.getByUUID(this.getUniqueId()).toUsername();
	}

	@Override
	public InetSocketAddress getAddress() {
		return null;
	}

	@Override
	public void disconnect(String reason) {}

	@Override
	public void disconnect(BaseComponent... reason) {}

	@Override
	public void disconnect(BaseComponent reason) {}

	@Override
	public Unsafe unsafe() {
		return null;
	}
}
