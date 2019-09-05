package xyz.acrylicstyle.doubletimecommandsbungee.utils;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;

public class Utils {
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
}
