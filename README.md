# DoubleTimeCommands ![](https://ci.acrylicstyle.xyz/job/DoubleTimeCommandsBungee/badge/icon?style=flat-square)
*BungeeCord plugin for manage player's rank, friends, parties, and something!*

[CI Builds](https://ci.acrylicstyle.xyz/job/DoubleTimeCommandsBungee/)

[Spigot plugin](https://github.com/acrylic-style/DoubleTimeCommands/)

## What can I do with this plugin?
* Database with MySQL (Required)
* Private chat
  * /msg
  * /tell
  * /w
  * /r (/reply)
* Server switcher
  * /play
  * /transfer (if you use spigot plugin)
  * /rejoin for rejoin to the server
* Kick from proxy
  * /kick
* Ban from proxy
  * /ban
  * /unban for unban player
* Friends
  * /f
  * /friend
  * /friends
* Parties
  * /p
  * /party
* Ping command
  * /ping
* Lobby command
  * /lobby
  * /hub
  * /l
  * /zoo
* Where am I (shows current proxy, and server)
  * /whereami
* Ranks
  * Default (enum: DEFAULT)
  * Sand (enum: SAND)
  * VIP (enum: VIP)
  * VIP+ (enum: VIPP)
  * MVP (enum: MVP)
  * MVP+ (enum: MVPP)
  * MVP++ (enum: MVPPP)
  * YouTube (enum: YOUTUBE)
  * PIG (enum: PIG)
  * PIG+ (enum: PIGP)
  * Helper (enum: HELPER)
  * Moderator (enum: MODERATOR)
  * Build Team (enum: BUILDTEAM)
  * Admin (enum: ADMIN)
  * Owner (enum: OWNER)
* Limbo if you use any server for limbo
  * You can specify limbo server at BungeeCord/config.yml
  * `/afkwarp` for warp to the server named as `LIMBO`, does not work if player is already at limbo
  * `/limbo` for herobrine who lives in limbo
  * Warped to the limbo when kicked from server
  * It does not work if not named limbo server as `LIMBO` or does not exist

Note: All commands could be overridden by spigot plugins!
  
## Installation
1. Download this plugin into your BungeeCord/plugins (not spigot)
2. Download [the plugin](https://ci.acrylicstyle.xyz/job/DoubleTimeCommandsBungeeHelper/lastSuccessfulBuild/artifact/target/DoubleTimeCommandsBungeeHelper-0.0.1-SNAPSHOT.jar) into your Spigot/plugins (not bungeecord)
3. Create `plugins/DoubleTimeCommands/config.yml` and write looks like this (you need to change something):
```yaml
proxyName: BUNGEECORD_01_US # change me
commands: # change me if you're using spigot plugin of this
  gamemode: BUILDTEAM
  give: ADMIN
  minecraft:give: ADMIN
  minecraft:ban: ADMIN
  ban: ADMIN
  kick: ADMIN
  minecraft:kick: ADMIN
  # ...
games: # change me
  bedwars: # /play bedwars
  - BEDWARS # server prefix, in this case, find all servers starting with "BEDWARS" and transfers player
  - false # shuffle servers every time or not
  - "Custom message!" # can be omitted and it'll use default message
database: # most important thing
  host: localhost # change me
  name: dtcbungee # change me?
  user: homestead # change me.
  password: owo # please change me
```