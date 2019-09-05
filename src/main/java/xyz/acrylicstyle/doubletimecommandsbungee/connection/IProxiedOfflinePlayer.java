package xyz.acrylicstyle.doubletimecommandsbungee.connection;

import java.io.IOException;
import java.util.UUID;

import org.json.simple.parser.ParseException;

import net.md_5.bungee.api.connection.Connection;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public interface IProxiedOfflinePlayer extends Connection {
	public ProxiedPlayer toProxiedPlayer();
	public String getName();
	public UUID getUniqueId();
	public default boolean isConnected() { return false; } // always returns false because they're not connected to this proxy
	public String getNameWithException() throws IllegalArgumentException, IOException, ParseException;
}
