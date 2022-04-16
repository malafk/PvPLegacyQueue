package lol.maltest.pvplegacylimbo.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import lol.maltest.pvplegacylimbo.Pvplegacylimbo;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

import java.awt.*;

public class MainListener {

    private final Pvplegacylimbo plugin;

    public MainListener(Pvplegacylimbo plugin) {
        this.plugin = plugin;
    }

    @Subscribe
    public void onConnect(PostLoginEvent e) {
        Player player = e.getPlayer();
        // another plugin should handle deciding if server is full or not
        if(player.getCurrentServer().get().getServerInfo().getName().equals("limbo")) {
            plugin.getQueueManager().queuePlayer(player.getUniqueId());
            player.sendMessage(Component.text("Sorry, PvPLegacy is currently full! You have been placed into the queue!").color(NamedTextColor.RED));
        }
    }

    @Subscribe
    public void onProxyDisconnect(DisconnectEvent e) {
        Player player = e.getPlayer();
        if(plugin.getQueueManager().isQueued(player.getUniqueId())) {
            plugin.getQueueManager().removePlayer(player.getUniqueId());
        }
    }
}
