package net.blitzcube.peapi.event.engine.listeners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.*;
import com.comphenix.protocol.injector.GamePhase;
import net.blitzcube.peapi.PacketEntityAPI;
import net.blitzcube.peapi.api.event.IPacketEntityEvent;
import net.blitzcube.peapi.event.PacketObjectSpawnEvent;
import net.blitzcube.peapi.event.engine.PacketEventDispatcher;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * Created by iso2013 on 2/23/2018.
 */
public class ObjectListener implements PacketListener {
    private final PacketEntityAPI parent;
    private final PacketEventDispatcher dispatcher;
    private boolean sendForFake;

    public ObjectListener(PacketEntityAPI parent, PacketEventDispatcher
            dispatcher) {
        this.parent = parent;
        this.dispatcher = dispatcher;
        this.sendForFake = false;
    }

    @Override
    public void onPacketSending(PacketEvent packetEvent) {
        PacketType t = packetEvent.getPacketType();
        PacketContainer p = packetEvent.getPacket();
        Player target = packetEvent.getPlayer();

        int entityID = 0;
        if (!PacketType.Play.Server.ENTITY_DESTROY.equals(t)) {
            entityID = p.getIntegers().read(0);
            if (!sendForFake && parent.isFakeID(entityID)) return;
        }

        IPacketEntityEvent e = PacketObjectSpawnEvent.unwrapPacket(entityID, p, target);
        if (e == null) return;
        dispatcher.dispatch(e, true);
        if (e.isCancelled()) {
            packetEvent.setCancelled(true);
        }
    }

    @Override
    public void onPacketReceiving(PacketEvent packetEvent) {

    }

    @Override
    public ListeningWhitelist getSendingWhitelist() {
        return ListeningWhitelist.newBuilder()
                .gamePhase(GamePhase.PLAYING).normal().types(
                        PacketType.Play.Server.SPAWN_ENTITY,
                        PacketType.Play.Server.SPAWN_ENTITY_PAINTING,
                        PacketType.Play.Server.SPAWN_ENTITY_EXPERIENCE_ORB,
                        PacketType.Play.Server.SPAWN_ENTITY_WEATHER
                ).mergeOptions(ListenerOptions.SKIP_PLUGIN_VERIFIER).build();
    }

    @Override
    public ListeningWhitelist getReceivingWhitelist() {
        return ListeningWhitelist.newBuilder().mergeOptions(ListenerOptions.SKIP_PLUGIN_VERIFIER).build();
    }

    @Override
    public Plugin getPlugin() {
        return null;
    }

    public void setSendForFake(boolean sendForFake) {
        this.sendForFake = sendForFake;
    }
}
