package net.factmc.FactHub.crates;

import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import net.factmc.FactCore.FactSQLConnector;
import net.factmc.FactCore.bukkit.InventoryControl;

public class CratesGUI implements Listener {
	
	public static String prefix = ChatColor.GOLD + "[" + ChatColor.DARK_GREEN +
			ChatColor.BOLD + "FACT Crates" + ChatColor.GOLD + "] ";
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onChestClicked(PlayerInteractEvent event) {
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK
				&& event.getClickedBlock().getType() == Material.ENDER_CHEST) {
			
			event.setCancelled(true);
			Player player = event.getPlayer();
			open(player);
			
		}
	}
	
	public static void open(Player player) {
		Inventory gui = player.getServer().createInventory(player, 27, ChatColor.DARK_GREEN + "FACT Crates");
		
		int points = FactSQLConnector.getPoints(player.getUniqueId());
		
		ItemStack common = InventoryControl.getItemStack(Material.CHEST, "&aCommon Crate",
				"&7Cost: 10 points", getAvailable(10, points));
		
		ItemStack rare = InventoryControl.getItemStack(Material.TRAPPED_CHEST, "&9Rare Crate",
				"&7Cost: 25 points", getAvailable(25, points));
		
		ItemStack legend = InventoryControl.getItemStack(Material.ENDER_CHEST, "&6Legendary Crate",
				"&7Cost: 40 points", getAvailable(40, points));
		
		gui.setItem(10, common);
		gui.setItem(13, rare);
		gui.setItem(16, legend);
		
		player.openInventory(gui);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void itemClicked(InventoryClickEvent event) {
		if (event.getView().getTitle().equalsIgnoreCase(ChatColor.DARK_GREEN + "FACT Crates")) {
			
			Player player = (Player) event.getWhoClicked();
			event.setCancelled(true);
			
			if ((event.getCurrentItem() == null) || (event.getCurrentItem().getType().equals(Material.AIR))) {
				return;
            }
			
			ItemStack item = event.getCurrentItem();
			String name = item.getItemMeta().getDisplayName();
			if (name == null) return;
			
			UUID uuid = player.getUniqueId();
			if (name.equalsIgnoreCase(InventoryControl.convertColors("&aCommon Crate"))) {
				if (FactSQLConnector.getPoints(uuid) < 10) return;
				
				FactSQLConnector.changePoints(uuid, -10);
				OpeningCrateGUI.open(player, 1);
				return;
			}
			else if (name.equalsIgnoreCase(InventoryControl.convertColors("&9Rare Crate"))) {
				if (FactSQLConnector.getPoints(uuid) < 25) return;
				
				FactSQLConnector.changePoints(uuid, -25);
				OpeningCrateGUI.open(player, 2);
				return;
			}
			else if (name.equalsIgnoreCase(InventoryControl.convertColors("&6Legendary Crate"))) {
				if (FactSQLConnector.getPoints(uuid) < 40) return;
				
				FactSQLConnector.changePoints(uuid, -40);
				OpeningCrateGUI.open(player, 3);
				return;
			}
			
		}
	}
	
	private static String getAvailable(int needed, int has) {
		String available = "&cYou need more points";
		if (has >= needed) {
			available = "&aClick to open for " + needed + " points";
		}
		return available;
	}
	
}