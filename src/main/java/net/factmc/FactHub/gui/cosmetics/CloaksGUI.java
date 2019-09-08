package net.factmc.FactHub.gui.cosmetics;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import net.factmc.FactHub.Data;
import net.factmc.FactCore.FactSQLConnector;
import net.factmc.FactCore.bukkit.InventoryControl;
import net.factmc.FactHub.crates.Util;
import net.factmc.FactHub.gui.ConfirmGUI;
import net.factmc.FactHub.gui.CosmeticsGUI;

public class CloaksGUI implements Listener {
	
	private static boolean loaded = false;
	
	public static void open(Player player, String name) {
		FileConfiguration cloakData = Data.getCloakData();

		double count = (double) cloakData.getConfigurationSection("cloaks").getKeys(false).size() / (double) 7;
		int i = 0;
		while (i < count) {
			i++;
		}
		int length = (4 + i) * 9;
		
		Inventory gui = player.getServer().createInventory(player, length, ChatColor.DARK_RED + "Cloaks");
		
		UUID uuid = player.getUniqueId();
		if (!player.getName().equals(name)) {
			uuid = FactSQLConnector.getUUID(name);
			
			ItemStack skull = new ItemStack(Material.PLAYER_HEAD, 1);
			SkullMeta meta = (SkullMeta) skull.getItemMeta();
			meta.setOwningPlayer(Bukkit.getOfflinePlayer(uuid));
			skull.setItemMeta(meta);
			
			ItemStack otherPlayer = InventoryControl.getItemStack(skull, "&aModifing Other Player", "&7Player: " + name);
			gui.setItem(0, otherPlayer);
		}
		
		i = 10;
		String savedCloak = Data.getSelected(uuid, "CLOAK");
		List<String> availableTypes = Data.getAccess(uuid, "CLOAKS");
		for (String cloak : cloakData.getConfigurationSection("cloaks").getKeys(false)) {
			String path = "cloaks." + cloak;
			
			String available = "&cNot Available";
			ItemStack stack = new ItemStack(Material.GUNPOWDER, 1);
			
			if (availableTypes.contains(cloak.toUpperCase()) || player.hasPermission("facthub.cosmetics.access-all")) {
				available = "&bAvailable";
				String item = cloakData.getString(path + ".item");
				stack = new ItemStack(Material.valueOf(item), 1);
			}
			if (savedCloak.equalsIgnoreCase(cloak.toUpperCase())) {
				available = "&a&oActive";
				ItemMeta meta = stack.getItemMeta();
				meta.addEnchant(Enchantment.ARROW_INFINITE, -1, true);
				meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
				stack.setItemMeta(meta);
			}
			
			String cloakName = cloakData.getString(path + ".name");
			
			List<String> lore = cloakData.getStringList(path + ".description");
			if (lore != null && lore.size() == 0) {
				lore.add("&7No Description");
				lore.add("&7Tell an admin if");
				lore.add("&7you are seeing this");
			}
			
			FileConfiguration valueData = Data.getValueData();
			int value = valueData.getInt("values.cloaks." + cloak.toUpperCase());
			String valueName = Util.getNameByValue(value, valueData);
			int cost = valueData.getInt("value." + value + ".cost");
			
			lore.add("");
			lore.add(valueName);
			lore.add("");
			lore.add(available);
			if (available.equalsIgnoreCase("&cNot Available") && cost > -1) {
				lore.add("&7Click to buy for " + cost + " points");
			}
		
			ItemStack pickStack = InventoryControl.getItemStack(stack, cloakName, lore);
			gui.setItem(i, pickStack);
			
			if ((i-7) % 9 == 0) i += 3;
			else i++;
		}
		
		ItemStack reset = InventoryControl.getItemStack(Material.BARRIER, "&cReset Cloak", "&7Turn off your cloak");
		gui.setItem(length - 14, reset);
		ItemStack back = InventoryControl.getItemStack(Material.ARROW, "&6Back", "&7Return to parent menu");
		gui.setItem(length - 15, back);
		
		loaded = true;
		player.openInventory(gui);
	}
	
	@EventHandler
	public void itemClicked(InventoryClickEvent event) {
		if (!loaded) return;
		final Player player = (Player) event.getWhoClicked();
		if (event.getView().getTitle().equalsIgnoreCase(ChatColor.DARK_RED + "Cloaks")) {
			
			if ((event.getCurrentItem() == null) || (event.getCurrentItem().getType().equals(Material.AIR))) {
                return;
            }
			
			ItemStack otherPlayer = event.getInventory().getItem(0);
			String playerName = player.getName();
			UUID uuid = player.getUniqueId();
			if (otherPlayer != null) {
				SkullMeta meta = (SkullMeta) otherPlayer.getItemMeta();
				uuid = meta.getOwningPlayer().getUniqueId();
				playerName = meta.getOwningPlayer().getName();
			}
			
			event.setCancelled(true);
			ItemStack item = event.getCurrentItem();
			String name = item.getItemMeta().getDisplayName();
			if (name == null) return;
			
			if (name.equalsIgnoreCase(InventoryControl.convertColors("&6Back"))) {
				CosmeticsGUI.open(player, playerName);
			}
			
			else if (name.equalsIgnoreCase(InventoryControl.convertColors("&cReset Cloak"))) {
				Data.setSelected(uuid, "CLOAK", "NONE");
				open(player, playerName);
			}
			
			else {
				FileConfiguration cloakData = Data.getCloakData();
				
				if (item.getType() == Material.GUNPOWDER) {
					FileConfiguration valueData = Data.getValueData();
					for (String cloak : cloakData.getConfigurationSection("cloaks").getKeys(false)) {
						String cloakName = ChatColor.translateAlternateColorCodes('&',
								cloakData.getString("cloaks." + cloak + ".name"));
						if (ChatColor.stripColor(cloakName).equalsIgnoreCase(ChatColor.stripColor(name))) {
							int value = valueData.getInt("values.cloaks." + cloak.toUpperCase());
							int cost = valueData.getInt("value." + value + ".cost");
							
							if (FactSQLConnector.getPoints(uuid) >= cost) {
								String materialName = cloakData.getString("cloaks." + cloak + ".item");
								ItemStack stack = InventoryControl.getItemStack(Material.valueOf(materialName),
										cloakData.getString("cloaks." + cloak + ".name"));
								ConfirmGUI.open(player, playerName, stack, cost, "CLOAKS", cloak.toUpperCase());
							}
							break;
						}
					}
				}
				
				else {
					for (String cloak : cloakData.getConfigurationSection("cloaks").getKeys(false)) {
						String cloakName = cloakData.getString("cloaks." + cloak + ".name");
						
						if (ChatColor.stripColor(name).equalsIgnoreCase(ChatColor.stripColor(InventoryControl.convertColors(cloakName)))) {
							Data.setSelected(uuid, "CLOAK", cloak.toUpperCase());
							open(player, playerName);
						}
					}
				}
				
			}
			
		}
	}
	
}