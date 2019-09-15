package xyz.acrylicstyle.doubletimecommandsbungee.commands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
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
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import xyz.acrylicstyle.doubletimecommandsbungee.providers.ConfigProvider;
import xyz.acrylicstyle.doubletimecommandsbungee.utils.Errors;
import xyz.acrylicstyle.doubletimecommandsbungee.utils.PlayerUtils;
import xyz.acrylicstyle.doubletimecommandsbungee.utils.Ranks;
import xyz.acrylicstyle.doubletimecommandsbungee.utils.Utils;

public class Party extends Command {
	/**
	 * invites.get(receiver's uuid).contains(sender's uuid)
	 * invites.put(receiver's uuid, [List])
	 **/
	private static HashMap<UUID, List<UUID>> invites = new HashMap<>();
	/**
	 * Party leader, Party members includes party leader.
	 */
	private static HashMap<UUID, List<UUID>> party = new HashMap<>();
	/**
	 * sender's uuid, party leader's uuid
	 */
	private static HashMap<UUID, UUID> memberOf = new HashMap<>();

	public Party() {
		super("party", null, "p");
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if (!(args.length >= 1 && args[0].equalsIgnoreCase("reset"))) {
			if (!(sender instanceof ProxiedPlayer)) {
				sender.sendMessage(new TextComponent(ChatColor.RED + "This command must be run from in-game."));
				return;
			}
		}
		String help =
				ChatColor.BLUE + "------------------------------------------------------------\n" +
				ChatColor.GREEN + "Party Commands:\n" +
				ChatColor.YELLOW + "/p add §7- §bInvite a player to your party\n" +
				ChatColor.YELLOW + "/p accept §7- §bAccept a party invite\n" +
				ChatColor.YELLOW + "/p deny §7- §bDecline a party invite\n" +
				ChatColor.YELLOW + "/p list §7- §bList all players in your party\n" +
				ChatColor.YELLOW + "/p remove §7- §bRemove a player from your party\n" +
				ChatColor.YELLOW + "/p disband §7- §bDisbands your party\n" +
				ChatColor.YELLOW + "/p leave §7- §bLeave the party\n" +
				ChatColor.YELLOW + "/p warp §7- §bTeleports your party members into your server\n" +
				ChatColor.BLUE + "------------------------------------------------------------";
		if (args.length >= 1) {
			if (args[0].equalsIgnoreCase("accept")) {
				ProxiedPlayer ps = (ProxiedPlayer) sender;
				if (args.length < 2) {
					sender.sendMessage(new TextComponent(ChatColor.RED + "Please specify a player!"));
					return;
				}
				final ProxiedPlayer player = ProxyServer.getInstance().getPlayer(args[1]);
				if (player == null) {
					sender.sendMessage(new TextComponent(ChatColor.BLUE + "------------------------------------------------------------"));
					sender.sendMessage(new TextComponent(ChatColor.RED + "Unable to find that player!"));
					sender.sendMessage(new TextComponent(ChatColor.BLUE + "------------------------------------------------------------"));
					return;
				}
				List<UUID> members = party.get(player.getUniqueId());
				members.forEach(uuid -> {
					try {
						final ProxiedPlayer player2 = ProxyServer.getInstance().getPlayer(uuid);
						player2.sendMessage(new TextComponent(ChatColor.BLUE + "------------------------------------------------------------"));
						player2.sendMessage(new TextComponent(ChatColor.GRAY + PlayerUtils.getName(ps) + ChatColor.RESET + ChatColor.GREEN + " joined your party!"));
						player2.sendMessage(new TextComponent(ChatColor.BLUE + "------------------------------------------------------------"));
					} catch (Exception e) {
						e.printStackTrace();
						e.getCause().printStackTrace();
					}
				});
				members.add(ps.getUniqueId());
				ps.sendMessage(new TextComponent(ChatColor.BLUE + "------------------------------------------------------------"));
				ps.sendMessage(new TextComponent(ChatColor.GREEN + "You joined the party!"));
				ps.sendMessage(new TextComponent(ChatColor.BLUE + "------------------------------------------------------------"));
				party.put(player.getUniqueId(), members);
				memberOf.put(ps.getUniqueId(), player.getUniqueId());
			} else if (args[0].equalsIgnoreCase("leave")) {
				ProxiedPlayer ps = (ProxiedPlayer) sender;
				if (memberOf.get(ps.getUniqueId()) == null) {
					sender.sendMessage(new TextComponent(ChatColor.RED + "You are not in party!"));
					return;
				}
				List<UUID> members = party.get(memberOf.get(ps.getUniqueId()));
				members.remove(ps.getUniqueId());
				members.forEach(uuid -> {
					try {
						final ProxiedPlayer player2 = ProxyServer.getInstance().getPlayer(uuid);
						player2.sendMessage(new TextComponent(ChatColor.BLUE + "------------------------------------------------------------"));
						player2.sendMessage(new TextComponent(ChatColor.GRAY + PlayerUtils.getName(ps) + ChatColor.RESET + ChatColor.YELLOW + " left your party."));
						player2.sendMessage(new TextComponent(ChatColor.BLUE + "------------------------------------------------------------"));
					} catch (Exception e) {
						e.printStackTrace();
						e.getCause().printStackTrace();
					}
				});
				ps.sendMessage(new TextComponent(ChatColor.BLUE + "------------------------------------------------------------"));
				ps.sendMessage(new TextComponent(ChatColor.YELLOW + "You left the party"));
				ps.sendMessage(new TextComponent(ChatColor.BLUE + "------------------------------------------------------------"));
				party.put(memberOf.get(ps.getUniqueId()), members);
				memberOf.remove(ps.getUniqueId());
			} else if (args[0].equalsIgnoreCase("warp")) {
				ProxiedPlayer ps = (ProxiedPlayer) sender;
				if (memberOf.get(ps.getUniqueId()) == null) {
					sender.sendMessage(new TextComponent(ChatColor.RED + "You are not in party!"));
					return;
				}
				if (memberOf.get(ps.getUniqueId()) != ps.getUniqueId()) {
					ps.sendMessage(new TextComponent(ChatColor.BLUE + "------------------------------------------------------------"));
					ps.sendMessage(new TextComponent(ChatColor.RED + "You are not a party leader!"));
					ps.sendMessage(new TextComponent(ChatColor.BLUE + "------------------------------------------------------------"));
					return;
				}
				ps.sendMessage(new TextComponent(ChatColor.GRAY + "Warping party members..."));
				ArrayList<UUID> members = new ArrayList<>();
				if (!members.addAll(party.get(ps.getUniqueId()))) {
					Utils.sendError(ps, Errors.ARRAYLIST_ERROR);
					return;
				}
				members.remove(ps.getUniqueId()); // remove yourself
				members.forEach(uuid -> {
					try {
						final ProxiedPlayer player2 = ProxyServer.getInstance().getPlayer(uuid);
						player2.sendMessage(new TextComponent(ChatColor.BLUE + "------------------------------------------------------------"));
						player2.sendMessage(new TextComponent(ChatColor.YELLOW + "Your party leader, " + ChatColor.GRAY + PlayerUtils.getName(ps) + ChatColor.RESET + ChatColor.YELLOW + " has summoned you to their server."));
						player2.sendMessage(new TextComponent(ChatColor.BLUE + "------------------------------------------------------------"));
						player2.connect(ps.getServer().getInfo());
					} catch (Exception e) {
						e.printStackTrace();
						e.getCause().printStackTrace();
					}
				});
				Collection<TextComponent> stackedMessages = new ArrayList<>();
				stackedMessages.add(new TextComponent(ChatColor.BLUE + "------------------------------------------------------------"));
				TimerTask task = new TimerTask() {
					public void run() {
						members.forEach(uuid -> {
							try {
								final ProxiedPlayer player2 = ProxyServer.getInstance().getPlayer(uuid);
								String name = ConfigProvider.getString("players." + player2.getUniqueId() + ".nick", player2.getName(), "DoubleTimeCommands");
								if (!player2.getServer().getInfo().getName().equalsIgnoreCase(ps.getServer().getInfo().getName())) {
									player2.sendMessage(new TextComponent(ChatColor.RED + "You didn't warp correctly!"));
									stackedMessages.add(new TextComponent(ChatColor.RED + "✖ " + ChatColor.GRAY + ChatColor.translateAlternateColorCodes('&', name) + ChatColor.RESET + ChatColor.RED + " didn't warp correctly!"));
								} else {
									stackedMessages.add(new TextComponent(ChatColor.GREEN + "✔ Warped " + ChatColor.GRAY + ChatColor.translateAlternateColorCodes('&', name) + ChatColor.RESET + ChatColor.GREEN + " to your server!"));
								}
							} catch (Exception e) {
								e.printStackTrace();
								e.getCause().printStackTrace();
							}
						});
						members.add(ps.getUniqueId());
						party.put(ps.getUniqueId(), members);
						stackedMessages.add(new TextComponent(ChatColor.BLUE + "------------------------------------------------------------"));
						stackedMessages.forEach(sender::sendMessage);
					}
				};
				Timer timer = new Timer();
				timer.schedule(task, 2000);
			}  else if (args[0].equalsIgnoreCase("reset")) {
				if (!Utils.must(Ranks.ADMIN, sender)) return;
				invites = new HashMap<>();
				party = new HashMap<>();
				memberOf = new HashMap<>();
				sender.sendMessage(new TextComponent(ChatColor.BLUE + "------------------------------------------------------------"));
				sender.sendMessage(new TextComponent(ChatColor.YELLOW + "Parties has been reset."));
				sender.sendMessage(new TextComponent(ChatColor.BLUE + "------------------------------------------------------------"));
			} else {
				ProxiedPlayer ps = (ProxiedPlayer) sender;
				final ProxiedPlayer player = ProxyServer.getInstance().getPlayer(args[0].equalsIgnoreCase("add") && args.length >= 2 ? args[1] : args[0]);
				if (player == null) {
					sender.sendMessage(new TextComponent(ChatColor.BLUE + "------------------------------------------------------------"));
					sender.sendMessage(new TextComponent(ChatColor.RED + "Unable to find that player!"));
					sender.sendMessage(new TextComponent(ChatColor.BLUE + "------------------------------------------------------------"));
					return;
				}
				memberOf.put(ps.getUniqueId(), ps.getUniqueId());
				List<UUID> newMembers = new ArrayList<>();
				newMembers.add(ps.getUniqueId());
				party.putIfAbsent(ps.getUniqueId(), newMembers);
				invites.computeIfAbsent(player.getUniqueId(), k -> new ArrayList<>());
				if (invites.get(player.getUniqueId()).contains(ps.getUniqueId())) {
					sender.sendMessage(new TextComponent(ChatColor.BLUE + "------------------------------------------------------------"));
					sender.sendMessage(new TextComponent(ChatColor.YELLOW + "You already sent friend request to " + PlayerUtils.getName(player) + ChatColor.RESET + ChatColor.YELLOW + "! Wait for them to accept!"));
					sender.sendMessage(new TextComponent(ChatColor.BLUE + "------------------------------------------------------------"));
					return;
				}
				sender.sendMessage(new TextComponent(ChatColor.BLUE + "------------------------------------------------------------"));
				sender.sendMessage(new TextComponent(ChatColor.YELLOW + "You sent party invite to " + PlayerUtils.getName(player) + ChatColor.RESET + ChatColor.YELLOW + "! They have 1 minute to accept."));
				sender.sendMessage(new TextComponent(ChatColor.BLUE + "------------------------------------------------------------"));
				player.sendMessage(new TextComponent(ChatColor.BLUE + "------------------------------------------------------------"));
				player.sendMessage(new TextComponent(ChatColor.YELLOW + "You received party invite from " + PlayerUtils.getName(ps) + ChatColor.RESET + ChatColor.YELLOW + "!"));
				TextComponent dialog = new TextComponent("" + ChatColor.GREEN + ChatColor.BOLD + "[ACCEPT]");
				dialog.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.GREEN + "Accept the party invite and join to their party.").create()));
				dialog.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/p accept " + ps.getName()));
				TextComponent deny = new TextComponent("" + ChatColor.RED + ChatColor.BOLD + "[DENY]");
				deny.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.RED + "Decline the party invite.").create()));
				deny.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/p deny " + ps.getName()));
				dialog.addExtra("" + ChatColor.RESET + ChatColor.GRAY + " - ");
				dialog.addExtra(deny);
				player.sendMessage(dialog);
				player.sendMessage(new TextComponent(ChatColor.BLUE + "------------------------------------------------------------"));
				TimerTask task = new TimerTask() {
					public void run() {
						try {
							if (party.get(ps.getUniqueId()).contains(player.getUniqueId())) {
								return;
							}
							List<UUID> members = new ArrayList<>();
							members.addAll(party.get(ps.getUniqueId()));
							members.remove(player.getUniqueId());
							party.put(player.getUniqueId(), members);
							List<UUID> inv = new ArrayList<>();
							inv.addAll(invites.get(player.getUniqueId()));
							inv.remove(ps.getUniqueId());
							invites.put(player.getUniqueId(), inv);
							sender.sendMessage(new TextComponent(ChatColor.BLUE + "------------------------------------------------------------"));
							sender.sendMessage(new TextComponent(ChatColor.YELLOW + "Your party invite to " + PlayerUtils.getName(player) + ChatColor.RESET + ChatColor.YELLOW + " has expired."));
							sender.sendMessage(new TextComponent(ChatColor.BLUE + "------------------------------------------------------------"));
							player.sendMessage(new TextComponent(ChatColor.BLUE + "------------------------------------------------------------"));
							player.sendMessage(new TextComponent(ChatColor.YELLOW + "Your party invite from " + PlayerUtils.getName(ps) + ChatColor.RESET + ChatColor.YELLOW + " has expired."));
							player.sendMessage(new TextComponent(ChatColor.BLUE + "------------------------------------------------------------"));
						} catch(Exception e) {
							ProxyServer.getInstance().getLogger().severe("Error while handling expired friend request:");
							e.printStackTrace();
							e.getCause().printStackTrace();
						}
					}
				};
				Timer timer = new Timer();
				timer.schedule(task, 1000*60);
			}
		} else {
			sender.sendMessage(new TextComponent(help));
		}
	}
}
