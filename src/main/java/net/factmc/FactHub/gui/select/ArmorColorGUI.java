package net.factmc.FactHub.gui.select;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
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
import net.factmc.FactCore.CoreUtils;
import net.factmc.FactHub.cosmetics.UpdateSuits;
import net.factmc.FactHub.crates.Util;
import net.factmc.FactHub.gui.ConfirmGUI;
import net.factmc.FactHub.gui.cosmetics.SuitsGUI;

public class ArmorColorGUI implements Listener {
	
	private static boolean loaded = false;
	
	public static void open(Player player, String name) {
		Inventory gui = player.getServer().createInventory(player, 45, ChatColor.GOLD + "Select Armor Color");
		
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
		
		String savedColor = Data.getSelected(uuid, "SUITCOLOR");
		List<String> availableColors = Data.getAccess(uuid, "SUIT_COLORS");
		List<String> colors = UpdateSuits.getColors();
		int i = 11;
		for (String sColor : colors) {
			Color color = UpdateSuits.getColor(sColor);
			String colorTxt = CoreUtils.underscoreToSpace(sColor.toLowerCase());
			
			ChatColor dyeTxt = InventoryControl.getChatColorByColor(color);
			
			String available = "&cNot Available";
			ItemStack dye;
			if (availableColors.contains(sColor) || player.hasPermission("facthub.cosmetics.access-all")) {
				available = "&bAvailable";
				dye = InventoryControl.getDyeByColor(color);
			}
			else dye = new ItemStack(Material.GUNPOWDER, 1);
			if (sColor.equalsIgnoreCase(savedColor)) {
				available = "&a&oActive";
				ItemMeta meta = dye.getItemMeta();
				meta.addEnchant(Enchantment.ARROW_INFINITE, -1, true);
				meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
				dye.setItemMeta(meta);
			}
			
			List<String> lore = new ArrayList<String>();
			lore.add("&7Click to select " + sColor.toUpperCase());
			
			FileConfiguration valueData = Data.getValueData();
			int value = valueData.getInt("values.suit-colors." + sColor.toUpperCase());
			String valueName = Util.getNameByValue(value, valueData);
			int cost = valueData.getInt("value." + value + ".cost");
			
			lore.add("");
			lore.add(valueName);
			lore.add("");
			lore.add(available);
			if (available.equalsIgnoreCase("&cNot Available") && cost > -1) {
				lore.add("&7Click to buy for " + cost + " points");
			}
			
			ItemStack item = InventoryControl.getItemStack(dye, "" + dyeTxt + ChatColor.BOLD + colorTxt,
					lore);
			gui.setItem(i, item);
			
			if (i == 15) i += 4;
			else if (i == 25) i +=4;
			else i++;
			
		}
		
		ItemStack back = InventoryControl.getItemStack(Material.ARROW, "&6Back", "&7Return to parent menu");
		gui.setItem(40, back);
		
		loaded = true;
		player.openInventory(gui);
	}
	
	@EventHandler
	public void itemClicked(InventoryClickEvent event) {
		if (!loaded) return;
		final Player player = (Player) event.getWhoClicked();
		if (event.getView().getTitle().equalsIgnoreCase(ChatColor.GOLD + "Select Armor Color")) {
			
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
			List<String> loreList = item.getItemMeta().getLore();
			
			if (item.getItemMeta().getDisplayName() != null &&
					item.getItemMeta().getDisplayName().equalsIgnoreCase(InventoryControl.convertColors("&6Back"))) {
				
				SuitsGUI.open(player, playerName);
				return;
			}
			
			if (loreList == null) return;
			String lore = loreList.get(0);
			
			String cut = InventoryControl.convertColors("&7Click to select ");
			String color = lore.replaceAll(cut, "");
			if (item.getType() != Material.GUNPOWDER) {
				Data.setSelected(uuid, "SUITCOLOR", color.toUpperCase());
				SuitsGUI.open(player, playerName);
			}
			
			else {
				FileConfiguration valueData = Data.getValueData();
				int value = valueData.getInt("values.suit-colors." + color);
				int cost = valueData.getInt("value." + value + ".cost");
				
				if (FactSQLConnector.getPoints(uuid) >= cost) {
					ItemStack stack = InventoryControl.getItemStack(InventoryControl.getDyeByColor(UpdateSuits.getColor(color)),
							item.getItemMeta().getDisplayName());
					ConfirmGUI.open(player, playerName, stack, cost, "SUIT_COLORS", color.toUpperCase());
				}
			}
			
		}
	}
	
}