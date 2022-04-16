package lol.maltest.pvplegacylimbo;

import com.google.inject.Inject;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import lol.maltest.pvplegacylimbo.queue.QueueManager;
import org.slf4j.Logger;

import java.util.Queue;

@Plugin(id = "pvplegacylimbo", name = "PvPLegacyLimbo", version = "1.0-SNAPSHOT", description = "A limbo system for pvplegacy when its full", url = "https://mal.rocks", authors = {"maltest"})
public class Pvplegacylimbo {

    private Pvplegacylimbo plugin;
    private ProxyServer server;
    private final Logger logger;
    private QueueManager queueManager;

    @Inject
    public Pvplegacylimbo(ProxyServer server, Logger logger) {
        this.plugin = this;
        this.server = server;
        this.logger = logger;

        logger.info("The limbo plugin has started");
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        this.queueManager = new QueueManager(server);
        System.out.println("Queue Manager has been registered");
    }

    public QueueManager getQueueManager() {
        return queueManager;
    }
}
