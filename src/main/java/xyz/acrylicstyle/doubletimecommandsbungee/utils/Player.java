package xyz.acrylicstyle.doubletimecommandsbungee.utils;

import java.io.IOException;
import java.net.URL;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

public class Player {
	public String username = null;
	public String uuid = null;

	public Player(String something, boolean uuid) {
		if (uuid) this.uuid = something;
	}

	public Player(UUID uuid) {
		this.uuid = uuid.toString();
	}

	public Player(String username) {
		this.username = username;
	}

	public Player setUUID(UUID uuid) {
		this.uuid = uuid.toString();
		return this;
	}

	public Player setUUID(String uuid) {
		this.uuid = uuid;
		return this;
	}

	public Player setUsername(String username) {
		this.username = username;
		return this;
	}

	public UUID toUUID() throws IllegalArgumentException, IOException, ParseException {
		if (this.username == null) throw new IllegalArgumentException("Username must be set before call this method.");
		String url = "https://api.mojang.com/users/profiles/minecraft/" + this.username;
		String nameJson = IOUtils.toString(new URL(url).openStream());
		JSONObject nameValue = (JSONObject) JSONValue.parseWithException(nameJson);
		return UUID.fromString(nameValue.get("id").toString().replaceFirst("(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)", "$1-$2-$3-$4-$5"));
	}

	public String toUsername() throws IllegalArgumentException, IOException, ParseException {
		if (this.uuid == null) throw new IllegalArgumentException("UUID must be set before call this method.");
		UUID.fromString(this.uuid); // Try parse
		String url = "https://api.mojang.com/user/profiles/" + this.uuid.replaceAll("-", "") + "/names";
		String nameJson = IOUtils.toString(new URL(url).openStream());
		JSONArray nameValue = (JSONArray) JSONValue.parseWithException(nameJson);
		String playerSlot = nameValue.get(nameValue.size()-1).toString();
		JSONObject nameObject = (JSONObject) JSONValue.parseWithException(playerSlot);
		return nameObject.get("name").toString();
	}
}
