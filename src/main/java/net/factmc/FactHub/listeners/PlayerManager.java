package net.factmc.FactHub.listeners;

import org.bukkit.event.Listener;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.inventory.InventoryClickEvent;

import net.factmc.FactCore.FactSQLConnector;
import net.factmc.FactCore.bukkit.InventoryControl;
import net.factmc.FactHub.Main;
import net.factmc.FactHub.gui.ServerGUI;

import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerManager implements Listener {
	
	public static void load() {
		for (Player player : Bukkit.getOnlinePlayers()) {
			loadPlayer(player);
		}
		
		new BukkitRunnable() {
			
			@Override
			public void run() {
				if (Bukkit.getPluginManager().getPlugin("FactCosmetics") != null) {
					for (Player player : Bukkit.getOnlinePlayers()) {
						
						Location loc = player.getLocation();
			    		if (loc.getBlock().getBiome() == Biome.BADLANDS
			    				|| getBlockAbove(loc) == Material.AIR
			    				|| loc.add(0,-1,0).getBlock().getType() == Material.RED_SANDSTONE) {
			    			
			    			if (!net.factmc.FactCosmetics.Util.isSealed(player)) {
			    				lackOxygen(player);
			    			}
			    			
			    		}
			    		
					}
				}
			}
		}.runTaskTimer(Main.getPlugin(), 0L, 20L);
	}
	
	private static Material getBlockAbove(Location loc) {
    	int x = loc.getBlockX();
    	int y = loc.getBlockY() + 1;
    	int z = loc.getBlockZ();
    	
    	for (int i = y; i < 256; i++) {
    		Block block = loc.getWorld().getBlockAt(x, i, z);
    		if (block.getType() != Material.AIR) {
        		return block.getType();
        	}
    	}
    	
    	return Material.AIR;
    }
	
	
	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		loadPlayer(event.getPlayer());
	}
	
	public static void loadPlayer(Player player) {
		player.getInventory().clear();
		player.setTotalExperience(0);
		
		ItemStack profile = InventoryControl.getHead(player,
				"&a" + player.getName() + "'s Stats",
				"&7Click for your stats");
		
		ItemStack selector = InventoryControl.getItemStack(Material.NETHER_STAR,
				"&dServer Selector",
				"&7Click to join other servers");
		
		ItemStack cosmetics = InventoryControl.getItemStack(Material.ENDER_CHEST,
				"&6Cosmetics",
				"&7Click to see your cosmetics");
		
		ItemStack hidden = new ItemStack(Material.LIME_DYE, 1);
		if (FactSQLConnector.getBooleanValue(FactSQLConnector.getOptionsTable(), player.getUniqueId(), "HIDEPLAYERS")) {
			hidden = new ItemStack(Material.GRAY_DYE, 1);
		}
		ItemStack players = InventoryControl.getItemStack(hidden, "&bPlayer Visibility", "&7Toggle seeing other players");
		
		player.getInventory().setItem(0, selector);
		player.getInventory().setItem(4, cosmetics);
		player.getInventory().setItem(8, profile);
		player.getInventory().setItem(7, players);
	}
	
	@EventHandler
	public void heldItemClicked(PlayerInteractEvent event) {
		final Player player = event.getPlayer();
			
		if ((event.getItem() == null) || (event.getItem().getType().equals(Material.AIR))) {
            return;
        }
		
		boolean check = checkItem(player, event.getItem());
		if (check) event.setCancelled(true);
			
	}
	
	@EventHandler
	public void inventoryItemClicked(InventoryClickEvent event) {
		final Player player = (Player) event.getWhoClicked();
		
		if ((event.getCurrentItem() == null) || (event.getCurrentItem().getType().equals(Material.AIR))) {
            return;
        }
		event.setCancelled(!player.hasPermission("facthub.use"));
		
		boolean check = checkItem(player, event.getCurrentItem());
		if (check) event.setCancelled(true);
		
	}
	
	public boolean checkItem(Player player, ItemStack item) {
		
		//String name = event.getCurrentItem().getItemMeta().getDisplayName();
		List<String> loreList = item.getItemMeta().getLore();
		if (loreList == null) return false;
		String lore = loreList.get(0);
		boolean tr = false;
		
		if (lore.equalsIgnoreCase(InventoryControl.convertColors("&7Click for your stats"))) {
			tr = true;
			player.performCommand("stats");
		}
		else if (lore.equalsIgnoreCase(InventoryControl.convertColors("&7Click to join other servers"))) {
			tr = true;
			ServerGUI.open(player);
		}
		else if (lore.equalsIgnoreCase(InventoryControl.convertColors("&7Click to see your cosmetics"))) {
			tr = true;
			player.performCommand("cosmetics");
		}
		
		else if (lore.equalsIgnoreCase(InventoryControl.convertColors("&7Toggle seeing other players"))) {
			tr = true;
			UUID uuid = player.getUniqueId();
			boolean hidePlayers = !FactSQLConnector.getBooleanValue(FactSQLConnector.getOptionsTable(), uuid, "HIDEPLAYERS");
			FactSQLConnector.setValue(FactSQLConnector.getOptionsTable(), uuid, "HIDEPLAYERS", hidePlayers);
			PlayerManager.updateHiddenPlayers();
			
			ItemStack hidden = new ItemStack(Material.LIME_DYE, 1);
			if (hidePlayers) {
				hidden = new ItemStack(Material.GRAY_DYE, 1);
			}
			ItemStack players = InventoryControl.getItemStack(hidden, "&bPlayer Visibility", "&7Toggle seeing other players");
			player.getInventory().setItem(7, players);
		}
		
		return tr;
		
	}
	
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void areaControl(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		Location loc = event.getTo();
		if (loc.getX() < -199.5 || loc.getX() > 200.5
				|| loc.getZ() < -199.5 || loc.getZ() > 200.5) {
			if (!player.hasPermission("facthub.escape")) {
				event.setCancelled(true);
				player.sendMessage(ChatColor.RED + "You cannot leave this area");
			}
		}
		
		if (loc.getY() < -50) {
            player.teleport(Main.getSpawn());
		}
		
		if (loc.getBlock().getType() == Material.END_PORTAL) {
			
			for (Object[] portal : WorldProtection.portals) {
				Location portalLoc = (Location) portal[1];
				if (loc.distance(portalLoc) < 2.5) {
					ServerGUI.connect(player, (String) portal[0]);
				}
			}
			
		}
		
	}
	
	@Deprecated
	public static boolean isBetween(Location location, Location corner1, Location corner2) {
		if (location.getBlockX() >= corner1.getBlockX() && location.getBlockX() <= corner2.getBlockX()
				&& location.getBlockY() >= corner1.getBlockY() && location.getBlockY() <= corner2.getBlockY()
				&& location.getBlockZ() >= corner1.getBlockZ() && location.getBlockZ() <= corner2.getBlockZ())
			return true;
		else return false;
	}
	
	
	public static void lackOxygen(Player player) {
		if (!player.hasPermission("facthub.oxygen")) {
			player.sendTitle(ChatColor.RED + "/!\\ WARNING /!\\", "You need a space suit to survive here!", 5, 10, 5);
			
			PotionEffect effect = new PotionEffect(PotionEffectType.WITHER, 30, 10, true, false);
			player.removePotionEffect(PotionEffectType.WITHER);
			
			player.addPotionEffect(effect, false);
		}
	}
	
	@EventHandler
	public void onPlayerDamaged(EntityDamageEvent event) {
		Entity entity = event.getEntity();
		if (entity instanceof Player) {
			Player player = (Player) entity;
			if (event.getCause() != DamageCause.VOID && event.getCause() != DamageCause.WITHER) {
				event.setCancelled(true);
				player.setHealth(20);
			}
			
			if (player.getHealth() < 1) {
				event.setCancelled(true);
	            player.teleport(Main.getSpawn());
				for (PotionEffect effect : player.getActivePotionEffects()) {
					player.removePotionEffect(effect.getType());
				}
				
				player.setHealth(20);
				player.setFoodLevel(20);
			}
			
		}
	}
	
	@EventHandler
	public void onPlayerHungry(FoodLevelChangeEvent event) {
		HumanEntity entity = event.getEntity();
		if (entity instanceof Player) {
			Player player = (Player) entity;
			event.setCancelled(true);
			player.setFoodLevel(20);
		}
	}
	
	public static void updateHiddenPlayers() {
		for (Player player : Bukkit.getServer().getOnlinePlayers()) {
			boolean hide = FactSQLConnector.getBooleanValue(FactSQLConnector.getOptionsTable(), player.getUniqueId(), "HIDEPLAYERS");
			
			for (Player nextPlayer : Bukkit.getServer().getOnlinePlayers()) {
				
				if (hide) {
					player.hidePlayer(Main.getPlugin(), nextPlayer);
				}
				else {
					player.showPlayer(Main.getPlugin(), nextPlayer);
				}
				
			}
			
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerJoin(PlayerJoinEvent event) {
		updateHiddenPlayers();
	}
	
}