package lol.maltest.pvplegacylimbo.queue;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import com.velocitypowered.api.scheduler.ScheduledTask;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class QueueManager {

    private final ProxyServer server;
    ArrayList<UUID> queuedPlayers = new ArrayList<>();
    ArrayList<UUID> attemptingToConnect = new ArrayList<>();
    ArrayList<RegisteredServer> freeHubs = new ArrayList<>();

    public QueueManager(ProxyServer server) {
        this.server = server;
        server.getScheduler().buildTask(server, () -> {
            // updates free servers every 2 seocnds
            freeHubs.clear();
            for(RegisteredServer registeredServer : server.getAllServers()) {
                if(registeredServer.getServerInfo().getName().startsWith("hub") && registeredServer.getPlayersConnected().size() < 64) {
                    freeHubs.add(registeredServer);
                }
            }
        }).repeat(2, TimeUnit.SECONDS).schedule();
        server.getScheduler()
                .buildTask(server, () -> {
                    if(queuedPlayers.isEmpty()) return;
                    queuedPlayers.forEach(uuid -> {
                        Optional<Player> player = server.getPlayer(uuid);
                        player.get().sendMessage(Component.text("You are position ")
                                .color(NamedTextColor.GRAY)
                                .append(
                                        Component.text(getPosition(uuid))
                                                .color(NamedTextColor.AQUA)
                                )
                                .append(
                                        Component.text(" in the queue!")
                                                .color(NamedTextColor.GRAY)
                                )
                        );
                    });
                })
                .repeat(30L, TimeUnit.SECONDS)
                .schedule();
        // send players every half second..
        server.getScheduler().buildTask(server, () -> {
            UUID attempting = queuedPlayers.get(0);
            if(attemptingToConnect.contains(attempting)) return;
            attemptToSendPlayer(server.getPlayer(attempting).get());
            attemptingToConnect.add(attempting);
        }).repeat(500, TimeUnit.MILLISECONDS);
    }

    public void queuePlayer(UUID uuid) {
        queuedPlayers.add(uuid);
    }

    public void removePlayer(UUID uuid) {
        queuedPlayers.remove(uuid);
    }

    public boolean isQueued(UUID uuid) {
        return queuedPlayers.contains(uuid);
    }
    ScheduledTask loopUntillConnected;
    public void attemptToSendPlayer(Player player) {

        Random random = new Random();
        player.createConnectionRequest(freeHubs.get(random.nextInt(freeHubs.size()))).connect();
        // attempts to connect the player to a server
        // I have never done such thing with velocity so unsure if this automatically runs async or not..
        // this is not tested :)
        server.getScheduler().buildTask(server, () -> {
            // check if the player is still in limbo
            if(player.getCurrentServer().get().getServerInfo().getName().equals("limbo")) {
                // player didnt connect, make a loop until they do!
                loopUntillConnected = server.getScheduler().buildTask(server, () -> {
                            if(player.getCurrentServer().get().getServerInfo().getName().equals("limbo")) {
                                // player didnt connect, make a loop until they do!
                                player.createConnectionRequest(freeHubs.get(random.nextInt(freeHubs.size()))).connect();
                                server.getScheduler().buildTask(server, () -> {
                                        if(!player.getCurrentServer().get().getServerInfo().getName().equals("limbo")) {
                                            // successfully connected.. we can stop now
                                            removePlayer(player.getUniqueId());
                                            loopUntillConnected.cancel();
                                        }
                                }).delay(1, TimeUnit.SECONDS).schedule();
                            }
                        }).repeat(1, TimeUnit.SECONDS).schedule();
            } else {
                removePlayer(player.getUniqueId());
                // wow, player connected first try! remove them gg.
            }
        }).delay(2, TimeUnit.SECONDS).schedule();
        attemptingToConnect.remove(player.getUniqueId());
    }

    public String getPosition(UUID playerUuid) {
        int positionNumber = 0;
        for(UUID uuid : queuedPlayers) {
            positionNumber++;
            if(uuid.equals(playerUuid)) {
                return positionNumber + "/" + queuedPlayers.size();
            }
        }
        return null;
    }

}
