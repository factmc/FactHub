package net.factmc.FactHub.listeners;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import net.factmc.FactHub.Main;

public class LaunchPads implements Listener {
	
	private static List<Player> delay = new ArrayList<Player>();
	
	@EventHandler
	public void onLaunchPad(PlayerInteractEvent event) {
		
		Player player = event.getPlayer();
		if (delay.contains(player)) return;
		
		if (event.getAction() == Action.PHYSICAL && event.getClickedBlock().getType() == Material.LIGHT_WEIGHTED_PRESSURE_PLATE) {
			event.setCancelled(true);
			startDelay(player);
			
			Location location = player.getLocation().clone();
			location.setPitch(Float.parseFloat(Main.getPlugin().getConfig().getString("launchpad-angle")));
			Vector vec = location.getDirection().multiply(Main.getPlugin().getConfig().getInt("launchpad-power"));
			
			player.playSound(player.getLocation(), Sound.ENTITY_CHICKEN_EGG, 1f, 1.5f);
			player.setVelocity(vec);
			
		}
		
	}
	
	private static void startDelay(Player player) {
		
		delay.add(player);
		Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), new Runnable() {

			@Override
			public void run() {
				delay.remove(player);
			}
			
		}, 10L);
		
	}
	
}