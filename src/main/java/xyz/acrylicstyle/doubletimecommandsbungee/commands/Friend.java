package xyz.acrylicstyle.doubletimecommandsbungee.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import util.CollectionList;
import xyz.acrylicstyle.doubletimecommandsbungee.connection.ProxiedOfflinePlayer;
import xyz.acrylicstyle.doubletimecommandsbungee.utils.PlayerUtils;
import xyz.acrylicstyle.doubletimecommandsbungee.utils.SqlUtils;

import java.sql.SQLException;
import java.util.*;

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
                ChatColor.BLUE + "--------------------------------------------------\n" +
                        ChatColor.GREEN + "Friend Commands:\n" +
                        ChatColor.YELLOW + "/f help §7- §bPrints this help message\n" +
                        ChatColor.YELLOW + "/f add §7- §bAdd a player as a friend\n" +
                        ChatColor.YELLOW + "/f accept §7- §bAccept a friend request\n" +
                        ChatColor.YELLOW + "/f deny §7- §bDecline a friend request\n" +
                        ChatColor.YELLOW + "/f list §7- §bList your friends\n" +
                        ChatColor.YELLOW + "/f remove §7- §bRemove a player from your friends\n" +
                        ChatColor.YELLOW + "/f removeall §7- §bRemove all players from your friends\n" +
                        ChatColor.YELLOW + "/f requests §7- §bView incoming friend requests\n" +
                        ChatColor.BLUE + "--------------------------------------------------";
        if (args.length >= 1) {
            if (args[0].equalsIgnoreCase("help")) {
                sender.sendMessage(new TextComponent(help));
            } else if (args[0].equalsIgnoreCase("list")) {
                new Thread(() -> {
                    CollectionList<UUID> friends = new CollectionList<>();
                    try {
                        friends = SqlUtils.getFriends(((ProxiedPlayer)(sender)).getUniqueId());
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    int i = (args.length >= 2 ? Integer.parseInt(args[1])*8 : 0)-1;
                    UUID f1 = null;
                    UUID f2 = null;
                    UUID f3 = null;
                    UUID f4 = null;
                    UUID f5 = null;
                    UUID f6 = null;
                    UUID f7 = null;
                    UUID f8 = null;
                    try {
                        f1 = friends.get(i+1);
                        f2 = friends.get(i+2);
                        f3 = friends.get(i+3);
                        f4 = friends.get(i+4);
                        f5 = friends.get(i+5);
                        f6 = friends.get(i+6);
                        f7 = friends.get(i+7);
                        f8 = friends.get(i+8);
                    } catch (Exception ignored) {}
                    if (f1 == null) {
                        sender.sendMessage(new TextComponent(ChatColor.RED + "You don't have any friends!"));
                        return;
                    }
                    Collection<TextComponent> stackedMessages = new ArrayList<>();
                    try {
                        stackedMessages.add(new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                        getProxiedOfflinePlayer(f1, stackedMessages);
                        if (isNull(sender, f2, f3, f4, stackedMessages)) return;
                        if (isNull(sender, f5, f6, f7, stackedMessages)) return;
                        if (f8 == null) {
                            stackedMessages.add(new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                            stackedMessages.forEach(sender::sendMessage);
                            return;
                        }
                        getProxiedOfflinePlayer(f8, stackedMessages);
                        stackedMessages.add(new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                        stackedMessages.forEach(sender::sendMessage);
                    } catch (NullPointerException | IllegalArgumentException e) {
                        e.printStackTrace();
                        if (e.getCause() != null) e.getCause().printStackTrace();
                    }
                }).start();
            } else if (args[0].equalsIgnoreCase("remove")) {
                if (args.length == 1) {
                    sender.sendMessage(new TextComponent(ChatColor.RED + "Please specify a player!"));
                    return;
                }
                ProxiedOfflinePlayer player = (ProxiedOfflinePlayer) ProxyServer.getInstance().getPlayer(args[1]);
                try {
                    CollectionList<UUID> friends = SqlUtils.getFriends(playerSender.getUniqueId());
                    if (!friends.contains(player.getUniqueId())) {
                        sender.sendMessage(new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                        sender.sendMessage(new TextComponent(ChatColor.RED + "You aren't friend with " + PlayerUtils.getName(player) + ChatColor.RED + "!"));
                        sender.sendMessage(new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                    }
                    SqlUtils.removeFriend(player.getUniqueId(), playerSender.getUniqueId());
                    SqlUtils.removeFriend(playerSender.getUniqueId(), player.getUniqueId());
                    ProxiedPlayer player2 = ProxyServer.getInstance().getPlayer(player.getUniqueId());
                    if (player2 != null) {
                        player2.sendMessage(new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                        player2.sendMessage(new TextComponent(ChatColor.GRAY + PlayerUtils.getName(playerSender) + ChatColor.YELLOW + " has removed you from their friend list!"));
                        player2.sendMessage(new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                    }
                    sender.sendMessage(new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                    sender.sendMessage(new TextComponent(ChatColor.YELLOW + "You are no longer friend with " + ChatColor.GRAY + PlayerUtils.getName(player) + ChatColor.YELLOW + "."));
                    sender.sendMessage(new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
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
                    CollectionList<UUID> requests = SqlUtils.getFriendRequests(player.getUniqueId());
                    if (!requests.contains(player.getUniqueId())) {
                        sender.sendMessage(new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                        sender.sendMessage(new TextComponent(ChatColor.RED + "They didn't send you friend request!"));
                        sender.sendMessage(new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                        return;
                    }
                    SqlUtils.removeFriendRequest(player.getUniqueId(), playerSender.getUniqueId());
                    SqlUtils.addFriend(player.getUniqueId(), playerSender.getUniqueId());
                    SqlUtils.addFriend(playerSender.getUniqueId(), player.getUniqueId());
                    sender.sendMessage(new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                    sender.sendMessage(new TextComponent(ChatColor.GREEN + "You are now friend with " + PlayerUtils.getName(player)));
                    sender.sendMessage(new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                    player.sendMessage(new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                    player.sendMessage(new TextComponent(ChatColor.GREEN + "You are now friend with " + PlayerUtils.getName((ProxiedPlayer) sender)));
                    player.sendMessage(new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (args[0].equalsIgnoreCase("deny")) {
                if (args.length == 1) {
                    sender.sendMessage(new TextComponent("§cPlease specify a player!"));
                    return;
                }
                ProxiedPlayer player = ProxyServer.getInstance().getPlayer(args[1]);
                try {
                    CollectionList<UUID> requests = SqlUtils.getFriendRequests(player.getUniqueId());
                    if (!requests.contains(player.getUniqueId())) {
                        sender.sendMessage(new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                        sender.sendMessage(new TextComponent(ChatColor.RED + "They didn't send you friend request!"));
                        sender.sendMessage(new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                        return;
                    }
                    SqlUtils.removeFriendRequest(player.getUniqueId(), playerSender.getUniqueId());
                    sender.sendMessage(new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                    sender.sendMessage(new TextComponent(PlayerUtils.getName(player) + ChatColor.YELLOW + " has declined your friend request."));
                    sender.sendMessage(new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                    player.sendMessage(new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                    player.sendMessage(new TextComponent(ChatColor.YELLOW + "You've declined friend request for " + PlayerUtils.getName((ProxiedPlayer)sender) + ChatColor.YELLOW + "."));
                    player.sendMessage(new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                final ProxiedPlayer player = ProxyServer.getInstance().getPlayer(args[0].equalsIgnoreCase("add") && args.length == 2 ? args[1] : args[0]);
                if (player == null) {
                    sender.sendMessage(new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                    sender.sendMessage(new TextComponent(ChatColor.RED + "Unable to find that player!"));
                    sender.sendMessage(new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                    return;
                }
                if (!player.isConnected()) {
                    sender.sendMessage(new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                    sender.sendMessage(new TextComponent(ChatColor.RED + "That player is offline!"));
                    sender.sendMessage(new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                    return;
                }
                if (player.getUniqueId().compareTo(playerSender.getUniqueId()) == 0) {
                    sender.sendMessage(new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                    sender.sendMessage(new TextComponent(ChatColor.YELLOW + "You can't add yourself as a friend!"));
                    sender.sendMessage(new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                    return;
                }
                CollectionList<UUID> friends;
                try {
                    friends = SqlUtils.getFriends(player.getUniqueId());
                } catch (SQLException e) {
                    sender.sendMessage(new TextComponent(ChatColor.RED + "An error occurred while fetching your friends!"));
                    e.printStackTrace();
                    return;
                }
                if (friends.contains(playerSender.getUniqueId())) {
                    sender.sendMessage(new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                    sender.sendMessage(new TextComponent(ChatColor.RED + "You are already friend with that player!"));
                    sender.sendMessage(new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                    return;
                }
                CollectionList<UUID> requests;
                try {
                    requests = SqlUtils.getFriendRequests(player.getUniqueId());
                } catch (SQLException e) {
                    sender.sendMessage(new TextComponent(ChatColor.RED + "An error occurred while fetching your friend requests!"));
                    e.printStackTrace();
                    return;
                }
                if (requests.contains(playerSender.getUniqueId())) {
                    sender.sendMessage(new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                    sender.sendMessage(new TextComponent(ChatColor.YELLOW + "You already sent friend request to " + PlayerUtils.getName(player) + ChatColor.RESET + ChatColor.YELLOW + "! Wait for them to accept!"));
                    sender.sendMessage(new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                    return;
                }
                try {
                    SqlUtils.addFriendRequest(player.getUniqueId(), playerSender.getUniqueId());
                } catch (SQLException e1) {
                    e1.printStackTrace();
                    sender.sendMessage(new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                    sender.sendMessage(new TextComponent(ChatColor.RED + "There was error while saving config! Please try again later!"));
                    sender.sendMessage(new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                    return;
                }
                sender.sendMessage(new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                sender.sendMessage(new TextComponent(ChatColor.YELLOW + "You sent friend request to " + PlayerUtils.getName(player) + ChatColor.RESET + ChatColor.YELLOW + "! They have 5 minutes to accept."));
                sender.sendMessage(new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                player.sendMessage(new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
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
                player.sendMessage(new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                TimerTask task = new TimerTask() {
                    public void run() {
                        try {
                            CollectionList<UUID> friends = SqlUtils.getFriends(player.getUniqueId());
                            CollectionList<UUID> requests = SqlUtils.getFriendRequests(player.getUniqueId());
                            if (friends.contains(playerSender.getUniqueId()) || !requests.contains(playerSender.getUniqueId())) {
                                return; // exit if accepted friend or declined friend req
                            }
                            SqlUtils.removeFriendRequest(player.getUniqueId(), playerSender.getUniqueId());
                            sender.sendMessage(new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                            sender.sendMessage(new TextComponent(ChatColor.YELLOW + "Your friend request to " + PlayerUtils.getName(player) + ChatColor.RESET + ChatColor.YELLOW + " has expired."));
                            sender.sendMessage(new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                            player.sendMessage(new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                            player.sendMessage(new TextComponent(ChatColor.YELLOW + "Your friend request from " + PlayerUtils.getName((ProxiedPlayer)sender) + ChatColor.RESET + ChatColor.YELLOW + " has expired."));
                            player.sendMessage(new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
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
    }

    private boolean isNull(CommandSender sender, UUID f5, UUID f6, UUID f7, Collection<TextComponent> stackedMessages) {
        if (sendMessage(sender, f5, stackedMessages)) return true;
        if (sendMessage(sender, f6, stackedMessages)) return true;
        return sendMessage(sender, f7, stackedMessages);
    }

    private boolean sendMessage(CommandSender sender, UUID f5, Collection<TextComponent> stackedMessages) {
        if (f5 == null) {
            stackedMessages.add(new TextComponent("§9--------------------------------------------------"));
            stackedMessages.forEach(sender::sendMessage);
            return true;
        }
        getProxiedOfflinePlayer(f5, stackedMessages);
        return false;
    }

    private void getProxiedOfflinePlayer(UUID f5, Collection<TextComponent> stackedMessages) {
        ProxiedOfflinePlayer pof5 = new ProxiedOfflinePlayer(f5);
        ProxiedPlayer pf5 = ProxyServer.getInstance().getPlayer(f5);
        assert pf5 != null;
        stackedMessages.add(new TextComponent(PlayerUtils.getName(pof5) + " " + (pf5.isConnected() ? ChatColor.AQUA + "is playing on " + pf5.getServer().getInfo().getName() : ChatColor.RED + "is currently offline")));
    }
}
