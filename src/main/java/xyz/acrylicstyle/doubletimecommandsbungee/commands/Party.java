package xyz.acrylicstyle.doubletimecommandsbungee.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import util.CollectionList;
import xyz.acrylicstyle.doubletimecommandsbungee.types.Player;
import xyz.acrylicstyle.doubletimecommandsbungee.utils.*;

import java.sql.SQLException;
import java.util.*;

public class Party extends Command {

    public Party() {
        super("party", null, "p");
    }

    @Override
    public void execute(CommandSender sender0, String[] args) {
        if (!(args.length >= 1 && args[0].equalsIgnoreCase("reset"))) {
            if (!(sender0 instanceof ProxiedPlayer)) {
                sender0.sendMessage(new TextComponent(ChatColor.RED + "This command must be run from in-game."));
                return;
            }
        }
        final ProxiedPlayer sender = (ProxiedPlayer) sender0; 
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
                if (args.length < 2) {
                    Utils.sendMessage(sender, new TextComponent(ChatColor.RED + "Please specify a player!"));
                    return;
                }
                final UUID player;
                try {
                    player = SqlUtils.getUniqueId(args[1]);
                } catch (SQLException e) {
                    Utils.sendMessage(sender, new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                    Utils.sendMessage(sender, new TextComponent(ChatColor.RED + "Unable to find that player!"));
                    Utils.sendMessage(sender, new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                    e.printStackTrace();
                    return;
                }
                CollectionList<UUID> members;
                int party_id;
                try {
                    party_id = SqlUtils.getPartyId(player); // impossible
                    members = SqlUtils.getPartyMembersAsUniqueId(party_id);
                } catch (NullPointerException | SQLException e) {
                    Utils.sendMessage(sender, new TextComponent(ChatColor.RED + "That invite is never existed or expired."));
                    e.printStackTrace();
                    return;
                }
                try {
                    SqlUtils.addPartyMember(party_id, sender.getUniqueId());
                    SqlUtils.removePartyInvite(party_id, player);
                } catch (SQLException e) {
                    Utils.sendMessage(sender, new TextComponent(ChatColor.RED + "An error occurred while joining party!"));
                    e.printStackTrace();
                    return;
                }
                members.forEach(uuid -> {
                    try {
                        final Player player2 = SqlUtils.getPlayer(uuid);
                        Utils.sendMessage(player2, new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                        Utils.sendMessage(player2, new TextComponent(ChatColor.GRAY + PlayerUtils.getName(sender) + ChatColor.RESET + ChatColor.GREEN + " joined your party!"));
                        Utils.sendMessage(player2, new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                    } catch (Exception e) {
                        e.printStackTrace();
                        e.getCause().printStackTrace();
                    }
                });
                Utils.sendMessage(sender, new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                Utils.sendMessage(sender, new TextComponent(ChatColor.GREEN + "You joined the party!"));
                Utils.sendMessage(sender, new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
            } else if (args[0].equalsIgnoreCase("deny")) {
                if (args.length < 2) {
                    Utils.sendMessage(sender, new TextComponent(ChatColor.RED + "Please specify a player!"));
                    return;
                }
                final UUID player;
                try {
                    player = SqlUtils.getUniqueId(args[1]);
                } catch (SQLException e) {
                    Utils.sendMessage(sender, new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                    Utils.sendMessage(sender, new TextComponent(ChatColor.RED + "Unable to find that player!"));
                    Utils.sendMessage(sender, new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                    e.printStackTrace();
                    return;
                }
                CollectionList<UUID> members;
                int party_id;
                try {
                    party_id = SqlUtils.getPartyId(player); // impossible
                    members = SqlUtils.getPartyMembersAsUniqueId(party_id);
                } catch (NullPointerException | SQLException e) {
                    Utils.sendMessage(sender, new TextComponent(ChatColor.RED + "That invite is never existed or expired."));
                    e.printStackTrace();
                    return;
                }
                try {
                    SqlUtils.removePartyInvite(party_id, player);
                } catch (SQLException e) {
                    Utils.sendMessage(sender, new TextComponent(ChatColor.RED + "An error occurred while declining party!"));
                    e.printStackTrace();
                    return;
                }
                members.forEach(uuid -> {
                    try {
                        final Player player2 = SqlUtils.getPlayer(uuid);
                        Utils.sendMessage(player2, new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                        Utils.sendMessage(player2, new TextComponent(ChatColor.GRAY + PlayerUtils.getName(sender) + ChatColor.RESET + ChatColor.YELLOW + " has declined the party invite."));
                        Utils.sendMessage(player2, new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                    } catch (Exception e) {
                        e.printStackTrace();
                        e.getCause().printStackTrace();
                    }
                });
                Utils.sendMessage(sender, new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                Utils.sendMessage(sender, new TextComponent(ChatColor.YELLOW + "You declined the party invite."));
                Utils.sendMessage(sender, new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                try {
                    emptyPartyCheck(SqlUtils.getPartyLeader(party_id).toProxiedPlayer());
                } catch (Exception ignored) {}
            } else if (args[0].equalsIgnoreCase("disband")) {
                int party_id;
                try {
                    if (!SqlUtils.inParty(sender.getUniqueId())) {
                        Utils.sendMessage(sender, new TextComponent(ChatColor.RED + "You are not in party!"));
                        return;
                    }
                    party_id = SqlUtils.getPartyId(sender.getUniqueId()); // impossible
                    if (!SqlUtils.isPartyLeader(party_id, sender.getUniqueId())) {
                        Utils.sendMessage(sender, new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                        Utils.sendMessage(sender, new TextComponent(ChatColor.RED + "You are not party leader!"));
                        Utils.sendMessage(sender, new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                        return;
                    }
                    Utils.sendMessage(sender, new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                    Utils.sendMessage(sender, new TextComponent(ChatColor.YELLOW + "You disbanded the party!"));
                    Utils.sendMessage(sender, new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                    SqlUtils.getPartyMembersAsUniqueId(party_id).filter(p -> !p.equals(sender.getUniqueId())).forEach(uuid -> {
                        try {
                            final Player player2 = SqlUtils.getPlayer(uuid);
                            Utils.sendMessage(player2, new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                            Utils.sendMessage(player2, new TextComponent(ChatColor.GRAY + PlayerUtils.getName(sender) + ChatColor.RESET + ChatColor.YELLOW + " has disbanded the party!"));
                            Utils.sendMessage(player2, new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                        } catch (Exception e) {
                            e.printStackTrace();
                            e.getCause().printStackTrace();
                        }
                    });
                    SqlUtils.disbandParty(party_id);
                } catch (SQLException e) {
                    Utils.sendMessage(sender, new TextComponent(ChatColor.RED + "Couldn't fetch party status!"));
                    e.printStackTrace();
                }
            } else if (args[0].equalsIgnoreCase("leave")) {
                try {
                    if (!SqlUtils.inParty(sender.getUniqueId())) {
                        Utils.sendMessage(sender, new TextComponent(ChatColor.RED + "You are not in party!"));
                        return;
                    }
                } catch (SQLException e) {
                    Utils.sendMessage(sender, new TextComponent(ChatColor.RED + "Couldn't fetch party status!"));
                    e.printStackTrace();
                    return;
                }
                CollectionList<UUID> members;
                int party_id;
                try {
                    party_id = SqlUtils.getPartyId(sender.getUniqueId()); // impossible
                    members = SqlUtils.getPartyMembersAsUniqueId(party_id);
                } catch (SQLException e) {
                    Utils.sendMessage(sender, new TextComponent(ChatColor.RED + "An error occurred while fetching party!"));
                    e.printStackTrace();
                    return;
                }
                try {
                    SqlUtils.leaveParty(sender.getUniqueId());
                } catch (SQLException e) {
                    Utils.sendMessage(sender, new TextComponent(ChatColor.RED + "Couldn't leave party!"));
                    e.printStackTrace();
                    return;
                }
                Utils.sendMessage(sender, new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                Utils.sendMessage(sender, new TextComponent(ChatColor.YELLOW + "You left the party"));
                Utils.sendMessage(sender, new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                members.forEach(uuid -> {
                    try {
                        final Player player2 = SqlUtils.getPlayer(uuid);
                        Utils.sendMessage(player2, new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                        Utils.sendMessage(player2, new TextComponent(ChatColor.GRAY + PlayerUtils.getName(sender) + ChatColor.RESET + ChatColor.YELLOW + " left your party."));
                        Utils.sendMessage(player2, new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                    } catch (Exception e) {
                        e.printStackTrace();
                        e.getCause().printStackTrace();
                    }
                });
                try {
                    emptyPartyCheck(ProxyServer.getInstance().getPlayer(members.first()));
                } catch (Exception ignored) {}
            } else if (args[0].equalsIgnoreCase("warp")) {
                int party_id;
                try {
                    if (!SqlUtils.inParty(sender.getUniqueId())) {
                        Utils.sendMessage(sender, new TextComponent(ChatColor.RED + "You are not in party!"));
                        return;
                    }
                    party_id = SqlUtils.getPartyId(sender.getUniqueId()); // impossible because already checked with SqlUtils#inParty
                    if (!SqlUtils.isPartyLeader(party_id, sender.getUniqueId())) {
                        Utils.sendMessage(sender, new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                        Utils.sendMessage(sender, new TextComponent(ChatColor.RED + "You are not a party leader!"));
                        Utils.sendMessage(sender, new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                        return;
                    }
                } catch (SQLException e) {
                    Utils.sendMessage(sender, new TextComponent(ChatColor.RED + "Couldn't fetch party status!"));
                    e.printStackTrace();
                    return;
                }
                Utils.sendMessage(sender, new TextComponent(ChatColor.GRAY + "Warping party members..."));
                CollectionList<UUID> members;
                try {
                    members = SqlUtils.getPartyMembersAsUniqueId(party_id);
                } catch (SQLException e) {
                    Utils.sendError(sender, Errors.COLLECTION_ERROR);
                    e.printStackTrace();
                    return;
                }
                members.remove(sender.getUniqueId()); // remove yourself
                members.forEach(uuid -> {
                    try {
                        final Player player2 = SqlUtils.getPlayer(uuid);
                        Utils.sendMessage(player2, new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                        Utils.sendMessage(player2, new TextComponent(ChatColor.YELLOW + "Your party leader, " + ChatColor.GRAY + PlayerUtils.getName(sender) + ChatColor.RESET + ChatColor.YELLOW + " has summoned you to their server."));
                        Utils.sendMessage(player2, new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                        Utils.connect(player2, sender.getServer().getInfo().getName());
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
                                final Player player2 = SqlUtils.getPlayer(uuid);
                                if (!player2.getConnectedServer().equalsIgnoreCase(sender.getServer().getInfo().getName())) {
                                    Utils.sendMessage(player2, new TextComponent(ChatColor.RED + "You didn't warp correctly!"));
                                    stackedMessages.add(new TextComponent(ChatColor.RED + "✖ " + PlayerUtils.getName(player2.getUniqueId()) + ChatColor.RED + " didn't warp correctly!"));
                                } else {
                                    stackedMessages.add(new TextComponent(ChatColor.GREEN + "✔ Warped " + PlayerUtils.getName(player2.getUniqueId()) + ChatColor.GREEN + " to your server!"));
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
                        stackedMessages.add(new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                        stackedMessages.forEach(msg -> Utils.sendMessage(sender, msg));
                    }
                };
                Timer timer = new Timer();
                timer.schedule(task, 2000);
            }  else if (args[0].equalsIgnoreCase("reset")) {
                if (!Utils.must(Ranks.ADMIN, sender)) return;
                try {
                    SqlUtils.resetParty();
                } catch (SQLException e) {
                    Utils.sendMessage(sender, new TextComponent(ChatColor.RED + "Couldn't reset parties!"));
                    e.printStackTrace();
                    return;
                }
                Utils.sendMessage(sender, new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                Utils.sendMessage(sender, new TextComponent(ChatColor.YELLOW + "Parties has been reset."));
                Utils.sendMessage(sender, new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
            } else {
                final Player player;
                try {
                    player = SqlUtils.getPlayer(SqlUtils.getUniqueId(args[0].equalsIgnoreCase("add") && args.length >= 2 ? args[1] : args[0]));
                } catch (SQLException e) {
                    Utils.sendMessage(sender, new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                    Utils.sendMessage(sender, new TextComponent(ChatColor.RED + "Unable to find that player!"));
                    Utils.sendMessage(sender, new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                    e.printStackTrace();
                    return;
                }
                if (sender.getUniqueId() == player.getUniqueId()) {
                    Utils.sendMessage(sender, new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                    Utils.sendMessage(sender, new TextComponent(ChatColor.RED + "You can't send party invite to yourself!"));
                    Utils.sendMessage(sender, new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                    return;
                }
                Integer party_id;
                try {
                    party_id = SqlUtils.getPartyId(player.getUniqueId());
                    if (party_id == null) party_id = SqlUtils.createParty(sender.getUniqueId()).getPartyId();
                } catch (SQLException e) {
                    e.printStackTrace();
                    return;
                }
                try {
                    if (SqlUtils.getPartyInvitesAsUniqueId(party_id).contains(sender.getUniqueId())) {
                        Utils.sendMessage(sender, new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                        Utils.sendMessage(sender, new TextComponent(ChatColor.YELLOW + "You already sent party request to " + PlayerUtils.getName(player.getUniqueId()) + ChatColor.RESET + ChatColor.YELLOW + "! Wait for them to accept!"));
                        Utils.sendMessage(sender, new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                        return;
                    }
                    SqlUtils.addPartyInvite(party_id, player.getUniqueId());
                } catch (SQLException e) {
                    Utils.sendMessage(sender, new TextComponent(ChatColor.RED + "An error occurred while fetching/modifying party status!"));
                    e.printStackTrace();
                    return;
                }
                CollectionList<UUID> members;
                try {
                    members = SqlUtils.getPartyMembersAsUniqueId(party_id);
                } catch (SQLException e) {
                    Utils.sendError(sender, Errors.COLLECTION_ERROR);
                    e.printStackTrace();
                    return;
                }
                members.remove(sender.getUniqueId()); // remove yourself
                members.forEach(uuid -> {
                    try {
                        final Player player2 = SqlUtils.getPlayer(uuid);
                        Utils.sendMessage(player2, new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                        Utils.sendMessage(player2, new TextComponent(PlayerUtils.getName(sender.getUniqueId()) + ChatColor.YELLOW + " sent party invite to " + PlayerUtils.getName(player.getUniqueId()) + ChatColor.RESET + ChatColor.YELLOW + "! They have 1 minute to accept."));
                        Utils.sendMessage(player2, new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                        Utils.connect(player2, sender.getServer().getInfo().getName());
                    } catch (Exception e) {
                        e.printStackTrace();
                        e.getCause().printStackTrace();
                    }
                });
                Utils.sendMessage(sender, new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                Utils.sendMessage(sender, new TextComponent(ChatColor.YELLOW + "You sent party invite to " + PlayerUtils.getName(player.getUniqueId()) + ChatColor.RESET + ChatColor.YELLOW + "! They have 1 minute to accept."));
                Utils.sendMessage(sender, new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                Utils.sendMessage(player, new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                Utils.sendMessage(player, new TextComponent(ChatColor.YELLOW + "You received party invite from " + PlayerUtils.getName(sender.getUniqueId()) + ChatColor.RESET + ChatColor.YELLOW + "!"));
                Utils.sendMessage(player, "/p," + sender.getName(), new TextComponent(""));
                Utils.sendMessage(player, new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                Integer finalParty_id = party_id;
                TimerTask task = new TimerTask() {
                    public void run() {
                        try {
                            if (!SqlUtils.inPartyInvite(finalParty_id, player.getUniqueId())) return;
                            SqlUtils.removePartyInvite(finalParty_id, player.getUniqueId());
                            CollectionList<UUID> members = SqlUtils.getPartyMembersAsUniqueId(finalParty_id);
                            members.remove(sender.getUniqueId()); // remove yourself
                            members.forEach(uuid -> {
                                try {
                                    final Player player2 = SqlUtils.getPlayer(uuid);
                                    Utils.sendMessage(player2, new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                                    Utils.sendMessage(player2, new TextComponent(ChatColor.YELLOW + "Party invite to " + PlayerUtils.getName(player.getUniqueId()) + ChatColor.RESET + ChatColor.YELLOW + " has expired."));
                                    Utils.sendMessage(player2, new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                                    Utils.connect(player2, sender.getServer().getInfo().getName());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    e.getCause().printStackTrace();
                                }
                            });
                            Utils.sendMessage(sender, new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                            Utils.sendMessage(sender, new TextComponent(ChatColor.YELLOW + "Your party invite to " + PlayerUtils.getName(player.getUniqueId()) + ChatColor.RESET + ChatColor.YELLOW + " has expired."));
                            Utils.sendMessage(sender, new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                            Utils.sendMessage(player, new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                            Utils.sendMessage(player, new TextComponent(ChatColor.YELLOW + "Your party invite from " + PlayerUtils.getName(sender) + ChatColor.RESET + ChatColor.YELLOW + " has expired."));
                            Utils.sendMessage(player, new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                            emptyPartyCheck(sender);
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
            Utils.sendMessage(sender, new TextComponent(help));
        }
    }

    private void emptyPartyCheck(ProxiedPlayer sender) {
        try {
            Integer party_id = SqlUtils.getPartyId(sender.getUniqueId());
            if (party_id == null) return;
            if (SqlUtils.getPartyMembers(party_id).size() <= 1) {
                SqlUtils.disbandParty(party_id);
                Utils.sendMessage(sender, new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
                Utils.sendMessage(sender, new TextComponent(ChatColor.YELLOW + "Your party has 1 player (only you) and party has been disbanded."));
                Utils.sendMessage(sender, new TextComponent(ChatColor.BLUE + "--------------------------------------------------"));
            }
        } catch (SQLException e) {
            ProxyServer.getInstance().getLogger().warning("An error occurred while checking for empty party!");
            e.printStackTrace();
        }
    }
}
