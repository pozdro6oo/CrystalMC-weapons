package eu.crystalmc.weapons.resourcepack;

import net.kyori.adventure.resource.ResourcePackRequest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class Send implements Listener {
    ResourcePackRequest request;

    Send(ResourcePackRequest request) {
        this.request = request;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onJoin(PlayerJoinEvent event) {
        event.getPlayer().sendResourcePacks(request);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onQuit(PlayerQuitEvent event) {
        event.getPlayer().removeResourcePacks(request);
    }
}