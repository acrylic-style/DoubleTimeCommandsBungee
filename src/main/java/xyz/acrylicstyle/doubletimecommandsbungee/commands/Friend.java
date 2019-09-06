package xyz.acrylicstyle.doubletimecommandsbungee.commands;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.Connection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import xyz.acrylicstyle.doubletimecommandsbungee.connection.ProxiedOfflinePlayer;
import xyz.acrylicstyle.doubletimecommandsbungee.providers.ConfigProvider;
import xyz.acrylicstyle.doubletimecommandsbungee.utils.PlayerUtils;

public class Friend extends Command {
	public Friend() {
		super("friend", null, "f", "friends");
	}

	@Override
	public void execute(final CommandSender sender, String[] args) {
		TextComponent text1 = new TextComponent("This command must be used in-game.");
		text1.setColor(ChatColor.RED);
		if (!(sender instanceof ProxiedPlayer)) {
			sender.sendMessage(text1);
			return;
		}
		final ProxiedPlayer playerSender = (ProxiedPlayer) sender;
		String help =
				"§9------------------------------------------------------------\n" +
				"§aFriend Commands:\n" +
				"§e/f help §7- §bPrints this help message\n" +
				"§e/f add §7- §bAdd a player as a friend\n" +
				"§e/f accept §7- §bAccept a friend request\n" +
				"§e/f deny §7- §bDecline a friend request\n" +
				"§e/f list §7- §bList your friends\n" +
				"§e/f remove §7- §bRemove a player from your friends\n" +
				"§e/f removeall §7- §bRemove all players from your friends\n" +
				"§e/f requests §7- §bView incoming friend requests\n" +
				"§9------------------------------------------------------------";
		if (args.length >= 1) {
			if (args[0].equalsIgnoreCase("help")) {
				sender.sendMessage(new TextComponent(help));
			} else if (args[0].equalsIgnoreCase("list")) {
				new Thread(() -> {
					ConfigProvider config = null;
					try {
						config = new ConfigProvider("./plugins/DoubleTimeCommands/config.yml");
					} catch (Exception e) {
						e.printStackTrace();
						e.getCause().printStackTrace();
						sender.sendMessage(new TextComponent(ChatColor.RED + "Couldn't read config! Please try again later."));
						return;
					}
					List<String> friends = Arrays.asList(config.getList("players." + playerSender.getUniqueId() + ".friend.friends", new ArrayList<Object>()).toArray(new String[0]));
					int i = (args.length >= 2 ? Integer.parseInt(args[1])*8 : 0)-1;
					String f1 = null;
					String f2 = null;
					String f3 = null;
					String f4 = null;
					String f5 = null;
					String f6 = null;
					String f7 = null;
					String f8 = null;
					try {
						f1 = friends.get(i+1);
						f2 = friends.get(i+2);
						f3 = friends.get(i+3);
						f4 = friends.get(i+4);
						f5 = friends.get(i+5);
						f6 = friends.get(i+6);
						f7 = friends.get(i+7);
						f8 = friends.get(i+8);
					} catch (Exception justIgnore) {}
					if (f1 == null) {
						sender.sendMessage(new TextComponent("§cYou don't have any friends!"));
						return;
					}
					Collection<TextComponent> stackedMessages = new ArrayList<TextComponent>();
					try {
						stackedMessages.add(new TextComponent("§9------------------------------------------------------------"));
						ProxiedOfflinePlayer pof1 = new ProxiedOfflinePlayer(UUID.fromString(f1));
						ProxiedPlayer pf1 = ProxyServer.getInstance().getPlayer(UUID.fromString(f1));
						stackedMessages.add(new TextComponent(PlayerUtils.getName(pof1) + " " + (((Connection) (pf1 == null ? pof1 : pf1)).isConnected() ? ChatColor.AQUA + "is playing on " + pf1.getServer().getInfo().getName() : ChatColor.RED + "is currently offline")));
						if (f2 == null) {
							stackedMessages.add(new TextComponent("§9------------------------------------------------------------"));
							stackedMessages.forEach(text -> sender.sendMessage(text));
							return;
						}
						ProxiedOfflinePlayer pof2 = new ProxiedOfflinePlayer(UUID.fromString(f2));
						ProxiedPlayer pf2 = ProxyServer.getInstance().getPlayer(UUID.fromString(f2));
						stackedMessages.add(new TextComponent(PlayerUtils.getName(pof2) + " " + (((Connection) (pf2 == null ? pof2 : pf2)).isConnected() ? ChatColor.AQUA + "is playing on " + pf2.getServer().getInfo().getName() : ChatColor.RED + "is currently offline")));
						if (f3 == null) {
							stackedMessages.add(new TextComponent("§9------------------------------------------------------------"));
							stackedMessages.forEach(text -> sender.sendMessage(text));
							return;
						}
						ProxiedOfflinePlayer pof3 = new ProxiedOfflinePlayer(UUID.fromString(f3));
						ProxiedPlayer pf3 = ProxyServer.getInstance().getPlayer(UUID.fromString(f3));
						stackedMessages.add(new TextComponent(PlayerUtils.getName(pof3) + " " + (((Connection) (pf3 == null ? pof3 : pf3)).isConnected() ? ChatColor.AQUA + "is playing on " + pf3.getServer().getInfo().getName() : ChatColor.RED + "is currently offline")));
						if (f4 == null) {
							stackedMessages.add(new TextComponent("§9------------------------------------------------------------"));
							stackedMessages.forEach(text -> sender.sendMessage(text));
							return;
						}
						ProxiedOfflinePlayer pof4 = new ProxiedOfflinePlayer(UUID.fromString(f4));
						ProxiedPlayer pf4 = ProxyServer.getInstance().getPlayer(UUID.fromString(f4));
						stackedMessages.add(new TextComponent(PlayerUtils.getName(pof4) + " " + (((Connection) (pf4 == null ? pof4 : pf4)).isConnected() ? ChatColor.AQUA + "is playing on " + pf4.getServer().getInfo().getName() : ChatColor.RED + "is currently offline")));
						if (f5 == null) {
							sender.sendMessage(new TextComponent("§9------------------------------------------------------------"));
							stackedMessages.forEach(text -> sender.sendMessage(text));
							return;
						}
						ProxiedOfflinePlayer pof5 = new ProxiedOfflinePlayer(UUID.fromString(f5));
						ProxiedPlayer pf5 = ProxyServer.getInstance().getPlayer(UUID.fromString(f5));
						stackedMessages.add(new TextComponent(PlayerUtils.getName(pof5) + " " + (((Connection) (pf5 == null ? pof5 : pf5)).isConnected() ? ChatColor.AQUA + "is playing on " + pf5.getServer().getInfo().getName() : ChatColor.RED + "is currently offline")));
						if (f6 == null) {
							stackedMessages.add(new TextComponent("§9------------------------------------------------------------"));
							stackedMessages.forEach(text -> sender.sendMessage(text));
							return;
						}
						ProxiedOfflinePlayer pof6 = new ProxiedOfflinePlayer(UUID.fromString(f6));
						ProxiedPlayer pf6 = ProxyServer.getInstance().getPlayer(UUID.fromString(f6));
						stackedMessages.add(new TextComponent(PlayerUtils.getName(pof6) + " " + (((Connection) (pf6 == null ? pof6 : pf6)).isConnected() ? ChatColor.AQUA + "is playing on " + pf6.getServer().getInfo().getName() : ChatColor.RED + "is currently offline")));
						if (f7 == null) {
							stackedMessages.add(new TextComponent("§9------------------------------------------------------------"));
							stackedMessages.forEach(text -> sender.sendMessage(text));
							return;
						}
						ProxiedOfflinePlayer pof7 = new ProxiedOfflinePlayer(UUID.fromString(f7));
						ProxiedPlayer pf7 = ProxyServer.getInstance().getPlayer(UUID.fromString(f7));
						stackedMessages.add(new TextComponent(PlayerUtils.getName(pof7) + " " + (((Connection) (pf7 == null ? pof7 : pf7)).isConnected() ? ChatColor.AQUA + "is playing on " + pf7.getServer().getInfo().getName() : ChatColor.RED + "is currently offline")));
						if (f8 == null) {
							stackedMessages.add(new TextComponent("§9------------------------------------------------------------"));
							stackedMessages.forEach(text -> sender.sendMessage(text));
							return;
						}
						ProxiedOfflinePlayer pof8 = new ProxiedOfflinePlayer(UUID.fromString(f8));
						ProxiedPlayer pf8 = ProxyServer.getInstance().getPlayer(UUID.fromString(f8));
						stackedMessages.add(new TextComponent(PlayerUtils.getName(pof8) + " " + (((Connection) (pf8 == null ? pof8 : pf8)).isConnected() ? ChatColor.AQUA + "is playing on " + pf8.getServer().getInfo().getName() : ChatColor.RED + "is currently offline")));
						stackedMessages.add(new TextComponent("§9------------------------------------------------------------"));
						stackedMessages.forEach(text -> sender.sendMessage(text));
					} catch (NullPointerException | IllegalArgumentException e) {
						e.printStackTrace();
						if (e.getCause() != null) e.getCause().printStackTrace();
					}
				}).start();
			} else if (args[0].equalsIgnoreCase("remove")) {
				if (args.length == 1) {
					sender.sendMessage(new TextComponent("§cPlease specify a player!"));
					return;
				}
				ProxiedOfflinePlayer player = (ProxiedOfflinePlayer) ProxyServer.getInstance().getPlayer(args[1]);
				try {
					ConfigProvider config = new ConfigProvider("./plugins/DoubleTimeCommands/config.yml");
					List<String> friends = Arrays.asList(config.getList("players." + playerSender.getUniqueId() + ".friend.friends", new ArrayList<Object>()).toArray(new String[0]));
					List<String> theirFriends = Arrays.asList(config.getList("players." + player.getUniqueId() + ".friend.friends", new ArrayList<Object>()).toArray(new String[0]));
					if (!friends.contains(player.getUniqueId().toString())) {
						sender.sendMessage(new TextComponent("§9------------------------------------------------------------"));
						sender.sendMessage(new TextComponent("§cYou aren't friend with " + PlayerUtils.getName(player) + "§r§c!"));
						sender.sendMessage(new TextComponent("§9------------------------------------------------------------"));
						return;
					}
				} catch (Exception e) {
					e.printStackTrace();
					e.getCause().printStackTrace();
				}
			} else if (args[0].equalsIgnoreCase("accept")) {
				if (args.length == 1) {
					sender.sendMessage(new TextComponent("§cPlease specify a player!"));
					return;
				}
				ProxiedPlayer player = ProxyServer.getInstance().getPlayer(args[1]);
				try {
					ConfigProvider config = new ConfigProvider("./plugins/DoubleTimeCommands/config.yml");
					List<String> requests = new ArrayList<String>();
					for (Object rawData : config.getList("players." + playerSender.getUniqueId() + ".friend.requests", new ArrayList<String>()))
						requests.add(rawData.toString());
					if (!requests.contains(player.getUniqueId().toString())) {
						sender.sendMessage(new TextComponent("§9------------------------------------------------------------"));
						sender.sendMessage(new TextComponent("§cThey didn't send you friend request!"));
						sender.sendMessage(new TextComponent("§9------------------------------------------------------------"));
						return;
					}
					requests.remove(player.getUniqueId().toString());
					config.set("players." + playerSender.getUniqueId() + ".friend.requests", requests);
					List<String> followers = new ArrayList<String>();
					List<String> following = new ArrayList<String>();
					followers.addAll(Arrays.asList(config.getList("players." + playerSender.getUniqueId() + ".friend.friends", new ArrayList<String>()).toArray(new String[0])));
					following.addAll(Arrays.asList(config.getList("players." + player.getUniqueId() + ".friend.friends", new ArrayList<String>()).toArray(new String[0])));
					followers.add(player.getUniqueId().toString());
					following.add(playerSender.getUniqueId().toString());
					config.set("players." + playerSender.getUniqueId() + ".friend.friends", followers);
					config.set("players." + player.getUniqueId() + ".friend.friends", following);
					sender.sendMessage(new TextComponent("§9------------------------------------------------------------"));
					sender.sendMessage(new TextComponent("§aYou are now friend with " + PlayerUtils.getName(player)));
					sender.sendMessage(new TextComponent("§9------------------------------------------------------------"));
					player.sendMessage(new TextComponent(ChatColor.BLUE + "------------------------------------------------------------"));
					player.sendMessage(new TextComponent(ChatColor.GREEN + "You are now friend with " + PlayerUtils.getName((ProxiedPlayer)sender)));
					player.sendMessage(new TextComponent(ChatColor.BLUE + "------------------------------------------------------------"));
					config.save();
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				final ProxiedPlayer player = ProxyServer.getInstance().getPlayer(args[0].equalsIgnoreCase("add") && args.length == 2 ? args[1] : args[0]);
				if (player == null) {
					sender.sendMessage(new TextComponent(ChatColor.BLUE + "------------------------------------------------------------"));
					sender.sendMessage(new TextComponent(ChatColor.RED + "Unable to find that player!"));
					sender.sendMessage(new TextComponent(ChatColor.BLUE + "------------------------------------------------------------"));
					return;
				}
				if (!player.isConnected()) {
					sender.sendMessage(new TextComponent(ChatColor.BLUE + "------------------------------------------------------------"));
					sender.sendMessage(new TextComponent(ChatColor.RED + "That player is offline!"));
					sender.sendMessage(new TextComponent(ChatColor.BLUE + "------------------------------------------------------------"));
					return;
				}
				if (player.getUniqueId().compareTo(playerSender.getUniqueId()) == 0) {
					sender.sendMessage(new TextComponent(ChatColor.BLUE + "------------------------------------------------------------"));
					sender.sendMessage(new TextComponent(ChatColor.YELLOW + "You can't add yourself as a friend!"));
					sender.sendMessage(new TextComponent(ChatColor.BLUE + "------------------------------------------------------------"));
					return;
				}
				ConfigProvider config = null;
				try {
					config = new ConfigProvider("./plugins/DoubleTimeCommands/config.yml");
				} catch (Exception e) {
					e.printStackTrace();
					e.getCause().printStackTrace();
					sender.sendMessage(new TextComponent(ChatColor.RED + "Couldn't read config! Please try again later."));
					return;
				}
				List<String> friends = Arrays.asList(config.getList("players." + player.getUniqueId() + ".friend.friends", new ArrayList<Object>()).toArray(new String[0]));
				if (friends.contains(playerSender.getUniqueId().toString())) {
					sender.sendMessage(new TextComponent(ChatColor.BLUE + "------------------------------------------------------------"));
					sender.sendMessage(new TextComponent(ChatColor.RED + "You are already friend with that player!"));
					sender.sendMessage(new TextComponent(ChatColor.BLUE + "------------------------------------------------------------"));
					return;
				}
				List<?> rawRequests = config.getList("players." + player.getUniqueId() + ".friend.requests", new ArrayList<Object>());
				List<String> requests = new ArrayList<String>();
				for (Object rawData : rawRequests) requests.add(rawData.toString());
				if (requests.contains(playerSender.getUniqueId().toString())) {
					sender.sendMessage(new TextComponent(ChatColor.BLUE + "------------------------------------------------------------"));
					sender.sendMessage(new TextComponent(ChatColor.YELLOW + "You already sent friend request to " + PlayerUtils.getName(player) + ChatColor.RESET + ChatColor.YELLOW + "! Wait for them to accept!"));
					sender.sendMessage(new TextComponent(ChatColor.BLUE + "------------------------------------------------------------"));
					return;
				}
				requests.add(playerSender.getUniqueId().toString());
				try {
					config.set("players." + player.getUniqueId() + ".friend.requests", requests);
					config.save();
				} catch (IOException e1) {
					e1.printStackTrace();
					sender.sendMessage(new TextComponent(ChatColor.BLUE + "------------------------------------------------------------"));
					sender.sendMessage(new TextComponent(ChatColor.RED + "There was error while saving config! Please try again later!"));
					sender.sendMessage(new TextComponent(ChatColor.BLUE + "------------------------------------------------------------"));
					return;
				}
				sender.sendMessage(new TextComponent(ChatColor.BLUE + "------------------------------------------------------------"));
				sender.sendMessage(new TextComponent(ChatColor.YELLOW + "You sent friend request to " + PlayerUtils.getName(player) + ChatColor.RESET + ChatColor.YELLOW + "! They have 5 minutes to accept."));
				sender.sendMessage(new TextComponent(ChatColor.BLUE + "------------------------------------------------------------"));
				player.sendMessage(new TextComponent(ChatColor.BLUE + "------------------------------------------------------------"));
				player.sendMessage(new TextComponent(ChatColor.YELLOW + "You received friend request from " + PlayerUtils.getName((ProxiedPlayer)sender) + ChatColor.RESET + ChatColor.YELLOW + "!"));
				TextComponent dialog = new TextComponent("" + ChatColor.GREEN + ChatColor.BOLD + "[ACCEPT]");
				dialog.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.GREEN + "Accept the friend request and add to your/their friend list.").create()));
				dialog.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/f accept " + playerSender.getName()));
				TextComponent deny = new TextComponent("" + ChatColor.RED + ChatColor.BOLD + "[DENY]");
				deny.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.RED + "Decline the friend request.").create()));
				deny.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/f deny " + playerSender.getName()));
				dialog.addExtra("" + ChatColor.RESET + ChatColor.GRAY + " - ");
				dialog.addExtra(deny);
				player.sendMessage(dialog);
				player.sendMessage(new TextComponent(ChatColor.BLUE + "------------------------------------------------------------"));
				TimerTask task = new TimerTask() {
					public void run() {
						try {
							ConfigProvider config = new ConfigProvider("./plugins/DoubleTimeCommands/config.yml");
							List<String> friends = Arrays.asList(config.getList("players." + player.getUniqueId() + ".friend.friends", new ArrayList<Object>()).toArray(new String[0]));
							if (friends.contains(playerSender.getUniqueId().toString())) {
								return; // Just end this task by this hand
							}
							List<String> requests = Arrays.asList(config.getList("players." + player.getUniqueId() + ".friend.requests", new ArrayList<Object>()).toArray(new String[0]));
							requests.remove(playerSender.getUniqueId().toString());
							config.set("players." + player.getUniqueId() + ".friend.requests", requests);
							config.save();
							sender.sendMessage(new TextComponent(ChatColor.BLUE + "------------------------------------------------------------"));
							sender.sendMessage(new TextComponent(ChatColor.YELLOW + "Your friend request to " + PlayerUtils.getName(player) + ChatColor.RESET + ChatColor.YELLOW + " has expired."));
							sender.sendMessage(new TextComponent(ChatColor.BLUE + "------------------------------------------------------------"));
							player.sendMessage(new TextComponent(ChatColor.BLUE + "------------------------------------------------------------"));
							player.sendMessage(new TextComponent(ChatColor.YELLOW + "Your friend request from " + PlayerUtils.getName((ProxiedPlayer)sender) + ChatColor.RESET + ChatColor.YELLOW + " has expired."));
							player.sendMessage(new TextComponent(ChatColor.BLUE + "------------------------------------------------------------"));
						} catch(Exception e) {
							ProxyServer.getInstance().getLogger().severe("Error while handling expired friend request:");
							e.printStackTrace();
							e.getCause().printStackTrace();
						}
					}
				};
				Timer timer = new Timer();
				timer.schedule(task, 1000*60*5); // 5 minutes
			}
		} else {
			sender.sendMessage(new TextComponent(help));
		}
		return;
	}
}