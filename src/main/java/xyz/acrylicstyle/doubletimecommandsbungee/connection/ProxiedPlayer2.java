package xyz.acrylicstyle.doubletimecommandsbungee.connection;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public abstract class ProxiedPlayer2 implements ProxiedPlayer {
    @Override
    public void sendMessage(BaseComponent message) {
        ChannelListener.sendToBukkit("helper:message", "", message.toPlainText(), this.getServer().getInfo());
    }
}
