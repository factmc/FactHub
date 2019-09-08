package net.factmc.FactHub.sidebar;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import de.myzelyam.api.vanish.PlayerHideEvent;
import de.myzelyam.api.vanish.PlayerShowEvent;

public class FactBukkitConnector implements Listener {
	
	@EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
    	updatePlayer(event.getPlayer());
    }
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void vanishChange(PlayerHideEvent event) {
		updatePlayer(event.getPlayer());
	}
	@EventHandler(priority = EventPriority.MONITOR)
	public void vanishChange(PlayerShowEvent event) {
		updatePlayer(event.getPlayer());
	}
	
	@Deprecated
	public static void updateAllPlayers() {
		for (Player player : Bukkit.getOnlinePlayers()) {
    		for (Player p : Bukkit.getOnlinePlayers()) {
    			net.factmc.FactBukkit.JoinEvents.updateTeam(player, p.getScoreboard());
    		}
		}
	}
	
	public static void updatePlayer(Player player) {
		for (Player p : Bukkit.getOnlinePlayers()) {
			net.factmc.FactBukkit.JoinEvents.updateTeam(player, p.getScoreboard());
		}
	}
	
}