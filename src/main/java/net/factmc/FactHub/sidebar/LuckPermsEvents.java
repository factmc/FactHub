package net.factmc.FactHub.sidebar;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.event.EventBus;
import net.luckperms.api.event.user.track.UserDemoteEvent;
import net.luckperms.api.event.user.track.UserPromoteEvent;

public class LuckPermsEvents {
	
	public LuckPermsEvents() {
        EventBus eventBus = LuckPermsProvider.get().getEventBus();
        eventBus.subscribe(UserPromoteEvent.class, this::userPromoted);
        eventBus.subscribe(UserDemoteEvent.class, this::userDemoted);
    }
	
	public void userPromoted(UserPromoteEvent event) {
		rankChange(event.getUser().getUniqueId());
	}
	public void userDemoted(UserDemoteEvent event) {
		rankChange(event.getUser().getUniqueId());
	}
	
	public void rankChange(UUID uuid) {
    	
		Player player = Bukkit.getPlayer(uuid);
		if (player != null)
			FactBukkitConnector.updatePlayer(player);
		
	}
	
}