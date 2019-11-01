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
import xyz.acrylicstyle.doubletimecommandsbungee.utils.*;

import java.sql.SQLException;
import java.util.*;

public class Party extends Command {

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
                ChatColor.BLUE + "--------------------------------------------------\n" +
                        ChatColor.GREEN + "Party Commands:\n" +
                        ChatColor.YELLOW + "/p add §7- §bInvite a player to your party\n" +
                        ChatColor.YELLOW + "/p accept §7- §bAccept a party invite\n" +
                        ChatColor.YELLOW + "/p deny §7- §bDecline a party invite\n" +
                        ChatColor.YELLOW + "/p list §7- §bList all players in your party\n" +
                        ChatColor.YELLOW + "/p remove §7- §bRemove a player from your party\n" +
                        ChatColor.YELLOW + "/p disband §7- §bDisbands your party\n" +
                        ChatColor.YELLOW + "/p leave §7- §bLeave the party\n" +
                        ChatColor.YELLOW + "/p warp §7- §bTeleports your party members into your server\n" +
                        ChatColor.BLUE + "--------------------------------------------------";
        if (args.length >= 1) {
            if (args[0].equalsIgnoreCase("accept")) {
                ProxiedPlayer ps = (ProxiedPlayer) sender;
                if (args.length < 2) {
                    sender.sendMessage(new TextComponent(ChatColor.RED + "Please specify a player!"));
                    return;
                }
                final UUID player;
                try {
                    player = SqlUtils.getUniqueId(args[1]);
                } catch (SQLException e) {
                    sender.sendMessage(new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                    sender.sendMessage(new TextComponent(ChatColor.RED + "Unable to find that player!"));
                    sender.sendMessage(new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                    e.printStackTrace();
                    return;
                }
                CollectionList<UUID> members;
                int party_id;
                try {
                    party_id = SqlUtils.getPartyId(player); // impossible
                    members = SqlUtils.getPartyMembersAsUniqueId(party_id);
                } catch (NullPointerException | SQLException e) {
                    sender.sendMessage(new TextComponent(ChatColor.RED + "That invite is never existed or expired."));
                    e.printStackTrace();
                    return;
                }
                try {
                    SqlUtils.addPartyMember(party_id, ps.getUniqueId());
                } catch (SQLException e) {
                    sender.sendMessage(new TextComponent(ChatColor.RED + "An error occurred while joining party!"));
                    e.printStackTrace();
                    return;
                }
                members.forEach(uuid -> {
                    try {
                        final ProxiedPlayer player2 = ProxyServer.getInstance().getPlayer(uuid);
                        if (player2 != null) {
                            player2.sendMessage(new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                            player2.sendMessage(new TextComponent(ChatColor.GRAY + PlayerUtils.getName(ps) + ChatColor.RESET + ChatColor.GREEN + " joined your party!"));
                            player2.sendMessage(new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        e.getCause().printStackTrace();
                    }
                });
                ps.sendMessage(new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                ps.sendMessage(new TextComponent(ChatColor.GREEN + "You joined the party!"));
                ps.sendMessage(new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
            } else if (args[0].equalsIgnoreCase("disband")) {
                ProxiedPlayer ps = (ProxiedPlayer) sender;
                int party_id;
                try {
                    if (!SqlUtils.inParty(ps.getUniqueId())) {
                        sender.sendMessage(new TextComponent(ChatColor.RED + "You are not in party!"));
                        return;
                    }
                    party_id = SqlUtils.getPartyId(ps.getUniqueId()); // impossible
                    if (!SqlUtils.isPartyLeader(party_id, ps.getUniqueId())) {
                        ps.sendMessage(new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                        ps.sendMessage(new TextComponent(ChatColor.RED + "You are not party leader!"));
                        ps.sendMessage(new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                        return;
                    }
                    ps.sendMessage(new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                    ps.sendMessage(new TextComponent(ChatColor.YELLOW + "You've disbanded the party."));
                    ps.sendMessage(new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                    SqlUtils.getPartyMembersAsUniqueId(party_id).filter(p -> !p.equals(ps.getUniqueId())).forEach(uuid -> {
                        try {
                            final ProxiedPlayer player2 = ProxyServer.getInstance().getPlayer(uuid);
                            if (player2 != null) {
                                player2.sendMessage(new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                                player2.sendMessage(new TextComponent(ChatColor.GRAY + PlayerUtils.getName(ps) + ChatColor.RESET + ChatColor.YELLOW + " has disbanded the party!"));
                                player2.sendMessage(new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            e.getCause().printStackTrace();
                        }
                    });
                    SqlUtils.disbandParty(party_id);
                } catch (SQLException e) {
                    sender.sendMessage(new TextComponent(ChatColor.RED + "Couldn't fetch party status!"));
                    e.printStackTrace();
                }
            } else if (args[0].equalsIgnoreCase("leave")) {
                ProxiedPlayer ps = (ProxiedPlayer) sender;
                try {
                    if (!SqlUtils.inParty(ps.getUniqueId())) {
                        sender.sendMessage(new TextComponent(ChatColor.RED + "You are not in party!"));
                        return;
                    }
                } catch (SQLException e) {
                    sender.sendMessage(new TextComponent(ChatColor.RED + "Couldn't fetch party status!"));
                    e.printStackTrace();
                    return;
                }
                CollectionList<UUID> members;
                int party_id;
                try {
                    party_id = SqlUtils.getPartyId(ps.getUniqueId()); // impossible
                    members = SqlUtils.getPartyMembersAsUniqueId(party_id);
                } catch (SQLException e) {
                    sender.sendMessage(new TextComponent(ChatColor.RED + "An error occurred while fetching party!"));
                    e.printStackTrace();
                    return;
                }
                try {
                    SqlUtils.leaveParty(ps.getUniqueId());
                } catch (SQLException e) {
                    sender.sendMessage(new TextComponent(ChatColor.RED + "Couldn't leave party!"));
                    e.printStackTrace();
                    return;
                }
                ps.sendMessage(new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                ps.sendMessage(new TextComponent(ChatColor.YELLOW + "You left the party"));
                ps.sendMessage(new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                members.forEach(uuid -> {
                    try {
                        final ProxiedPlayer player2 = ProxyServer.getInstance().getPlayer(uuid);
                        if (player2 != null) {
                            player2.sendMessage(new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                            player2.sendMessage(new TextComponent(ChatColor.GRAY + PlayerUtils.getName(ps) + ChatColor.RESET + ChatColor.YELLOW + " left your party."));
                            player2.sendMessage(new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        e.getCause().printStackTrace();
                    }
                });
            } else if (args[0].equalsIgnoreCase("warp")) {
                ProxiedPlayer ps = (ProxiedPlayer) sender;
                int party_id;
                try {
                    if (!SqlUtils.inParty(ps.getUniqueId())) {
                        sender.sendMessage(new TextComponent(ChatColor.RED + "You are not in party!"));
                        return;
                    }
                    party_id = SqlUtils.getPartyId(ps.getUniqueId()); // impossible because already checked with SqlUtils#inParty
                    if (!SqlUtils.isPartyLeader(party_id, ps.getUniqueId())) {
                        ps.sendMessage(new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                        ps.sendMessage(new TextComponent(ChatColor.RED + "You are not a party leader!"));
                        ps.sendMessage(new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                        return;
                    }
                } catch (SQLException e) {
                    sender.sendMessage(new TextComponent(ChatColor.RED + "Couldn't fetch party status!"));
                    e.printStackTrace();
                    return;
                }
                ps.sendMessage(new TextComponent(ChatColor.GRAY + "Warping party members..."));
                CollectionList<UUID> members;
                try {
                    members = SqlUtils.getPartyMembersAsUniqueId(party_id);
                } catch (SQLException e) {
                    Utils.sendError(ps, Errors.COLLECTION_ERROR);
                    e.printStackTrace();
                    return;
                }
                members.remove(ps.getUniqueId()); // remove yourself
                members.forEach(uuid -> {
                    try {
                        final ProxiedPlayer player2 = ProxyServer.getInstance().getPlayer(uuid);
                        if (player2 != null) {
                            player2.sendMessage(new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                            player2.sendMessage(new TextComponent(ChatColor.YELLOW + "Your party leader, " + ChatColor.GRAY + PlayerUtils.getName(ps) + ChatColor.RESET + ChatColor.YELLOW + " has summoned you to their server."));
                            player2.sendMessage(new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                            player2.connect(ps.getServer().getInfo());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        e.getCause().printStackTrace();
                    }
                });
                Collection<TextComponent> stackedMessages = new ArrayList<>();
                stackedMessages.add(new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                TimerTask task = new TimerTask() {
                    public void run() {
                        members.forEach(uuid -> {
                            try {
                                final ProxiedPlayer player2 = ProxyServer.getInstance().getPlayer(uuid);
                                if (!player2.getServer().getInfo().getName().equalsIgnoreCase(ps.getServer().getInfo().getName())) {
                                    player2.sendMessage(new TextComponent(ChatColor.RED + "You didn't warp correctly!"));
                                    stackedMessages.add(new TextComponent(ChatColor.RED + "✖ " + PlayerUtils.getName(player2) + ChatColor.RED + " didn't warp correctly!"));
                                } else {
                                    stackedMessages.add(new TextComponent(ChatColor.GREEN + "✔ Warped " + PlayerUtils.getName(player2) + ChatColor.GREEN + " to your server!"));
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                e.getCause().printStackTrace();
                            }
                        });
                        stackedMessages.add(new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                        stackedMessages.forEach(sender::sendMessage);
                    }
                };
                Timer timer = new Timer();
                timer.schedule(task, 2000);
            }  else if (args[0].equalsIgnoreCase("reset")) {
                if (!Utils.must(Ranks.ADMIN, sender)) return;
                try {
                    SqlUtils.resetParty();
                } catch (SQLException e) {
                    sender.sendMessage(new TextComponent(ChatColor.RED + "Couldn't reset parties!"));
                    e.printStackTrace();
                    return;
                }
                sender.sendMessage(new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                sender.sendMessage(new TextComponent(ChatColor.YELLOW + "Parties has been reset."));
                sender.sendMessage(new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
            } else {
                ProxiedPlayer ps = (ProxiedPlayer) sender;
                final ProxiedPlayer player = ProxyServer.getInstance().getPlayer(args[0].equalsIgnoreCase("add") && args.length >= 2 ? args[1] : args[0]);
                if (player == null) {
                    sender.sendMessage(new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                    sender.sendMessage(new TextComponent(ChatColor.RED + "Unable to find that player!"));
                    sender.sendMessage(new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                    return;
                }
                Integer party_id;
                try {
                    party_id = SqlUtils.getPartyId(player.getUniqueId());
                    if (party_id == null) party_id = SqlUtils.createParty(ps.getUniqueId()).getPartyId();
                } catch (SQLException e) {
                    e.printStackTrace();
                    return;
                }
                try {
                    if (SqlUtils.getPartyInvitesAsUniqueId(party_id).contains(ps.getUniqueId())) {
                        sender.sendMessage(new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                        sender.sendMessage(new TextComponent(ChatColor.YELLOW + "You already sent party request to " + PlayerUtils.getName(player) + ChatColor.RESET + ChatColor.YELLOW + "! Wait for them to accept!"));
                        sender.sendMessage(new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                        return;
                    }
                    SqlUtils.addPartyInvite(party_id, player.getUniqueId());
                } catch (SQLException e) {
                    sender.sendMessage(new TextComponent(ChatColor.RED + "An error occurred while fetching/modifying party status!"));
                    e.printStackTrace();
                    return;
                }
                sender.sendMessage(new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                sender.sendMessage(new TextComponent(ChatColor.YELLOW + "You sent party invite to " + PlayerUtils.getName(player) + ChatColor.RESET + ChatColor.YELLOW + "! They have 1 minute to accept."));
                sender.sendMessage(new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                player.sendMessage(new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
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
                player.sendMessage(new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                TimerTask task = new TimerTask() {
                    public void run() {
                        try {
                            if (SqlUtils.inParty(player.getUniqueId())) return;
                            SqlUtils.removePartyInvite(player.getUniqueId());
                            sender.sendMessage(new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                            sender.sendMessage(new TextComponent(ChatColor.YELLOW + "Your party invite to " + PlayerUtils.getName(player) + ChatColor.RESET + ChatColor.YELLOW + " has expired."));
                            sender.sendMessage(new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                            player.sendMessage(new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                            player.sendMessage(new TextComponent(ChatColor.YELLOW + "Your party invite from " + PlayerUtils.getName(ps) + ChatColor.RESET + ChatColor.YELLOW + " has expired."));
                            player.sendMessage(new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                        } catch(SQLException e) {
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
