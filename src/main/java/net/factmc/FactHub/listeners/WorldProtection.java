package net.factmc.FactHub.listeners;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import net.factmc.FactHub.Main;

import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingBreakEvent.RemoveCause;

public class WorldProtection implements Listener {
	
	public static List<Object[]> portals = new ArrayList<Object[]>();
	
	public static final List<Material> ALLOWED_BLOCKS = new ArrayList<Material>();
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onExplosion(EntityExplodeEvent event) {
		event.setCancelled(true);
	}
	@EventHandler(priority = EventPriority.HIGH)
	public void onExplosion(BlockExplodeEvent event) {
		event.setCancelled(true);
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onFireSpread(BlockPlaceEvent event) {
		if (event.getBlockPlaced().getType() == Material.FIRE) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onBlockBurn(BlockBurnEvent event) {
		event.setCancelled(true);
	}
	@EventHandler(priority = EventPriority.NORMAL)
	public void onBlockBurn(BlockIgniteEvent event) {
		if (event.getPlayer() == null || !event.getPlayer().hasPermission("facthub.use")) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onEndermanSteal(EntityChangeBlockEvent event) {
		if (event.getEntityType() == EntityType.ENDERMAN) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onIceMelt(BlockFromToEvent event) {
		if (event.getBlock().getType() == Material.ICE) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onHangingDestroyed(HangingBreakByEntityEvent event) {
		if (event.getRemover() instanceof Player) {
			Player player = (Player) event.getRemover();
			//Bukkit.broadcastMessage("Broke by player " + player.getName() + ". Cancel: " + !player.hasPermission("facthub.use"));
			event.setCancelled(!player.hasPermission("facthub.use"));
		}
		else {
			//Bukkit.broadcastMessage("Broke by entity. Cancel: true");
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onHangingBreak(HangingBreakEvent event) {
		//Bukkit.broadcastMessage("HangingBreakEvent");
		if (event.getCause() != RemoveCause.ENTITY) {
			//Bukkit.broadcastMessage("Broke. Cancel: true");
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK
				&& event.getClickedBlock().getType() == Material.ENDER_CHEST) {
			
			event.setCancelled(true);
			player.performCommand("crates");
			return;
			
		}
		
		if (event.getAction() == Action.LEFT_CLICK_BLOCK
				|| event.getAction() == Action.RIGHT_CLICK_BLOCK
				|| event.getAction() == Action.PHYSICAL) {
			
			if (!ALLOWED_BLOCKS.contains(event.getClickedBlock().getType())) {
				event.setCancelled(!player.hasPermission("facthub.use"));
			}
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onItemDrop(PlayerDropItemEvent event) {
		Player player = event.getPlayer();
		event.setCancelled(!player.hasPermission("facthub.use"));
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onItemPickup(EntityPickupItemEvent event) {
		Entity entity = event.getEntity();
		if (entity instanceof Player) {
			Player player = (Player) entity;
			event.setCancelled(!player.hasPermission("facthub.use"));
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onSwapHands(PlayerSwapHandItemsEvent event) {
		Player player = event.getPlayer();
		event.setCancelled(!player.hasPermission("facthub.use"));
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onEntityInteract(PlayerInteractAtEntityEvent event) {
		Player player = event.getPlayer();
		event.setCancelled(!player.hasPermission("facthub.use"));
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onEntityAttack(EntityDamageByEntityEvent event) {
		Entity attacker = event.getDamager();
		if (attacker instanceof Player) {
			Player player = (Player) attacker;
			//Bukkit.broadcastMessage(player.getName() + " damaged " + event.getEntity().getName() + " Cancelled: " + !player.hasPermission("facthub.use"));
			event.setCancelled(!player.hasPermission("facthub.use"));
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onSleepAttempt(PlayerBedEnterEvent event) {
		event.setCancelled(true);
	}
	
	
	
	public static void load() {
		
		ALLOWED_BLOCKS.clear();
		for (String block : Main.getConfigStringList("allow-interact")) {
			
			ALLOWED_BLOCKS.add(Material.valueOf(block));
			
		}
		
		// Load Portals
		portals.clear();
		for (String server : Main.getPlugin().getConfig().getConfigurationSection("portals").getKeys(false)) {
			
			String raw = Main.getPlugin().getConfig().getString("portals." + server);
			String[] portal = raw.split(",");
			
			int[] location = new int[3];
			for (int i = 0; i < 3; i++) {
				location[i] = Integer.parseInt(portal[i]);
			}
			
			World world = Bukkit.getWorlds().get(0);
			Location a = new Location(world, location[0], location[1], location[2]);
			portals.add(new Object[] {server, a, Double.parseDouble(portal[3])});
			
		}
		
	}
	
}