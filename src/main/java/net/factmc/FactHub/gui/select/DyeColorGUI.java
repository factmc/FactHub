package net.factmc.FactHub.gui.select;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
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
import net.factmc.FactHub.cosmetics.Morphs;
import net.factmc.FactHub.cosmetics.Pet;
import net.factmc.FactHub.crates.Util;
import net.factmc.FactHub.gui.ConfirmGUI;
import net.factmc.FactHub.gui.cosmetics.MorphsGUI;
import net.factmc.FactHub.gui.cosmetics.PetsGUI;
import net.factmc.FactHub.listeners.PetManager;

public class DyeColorGUI implements Listener {
	
	private static boolean loaded = false;
	
	public static void open(Player player, String name, String path) {
		String title = "";
		String col;
		if (path.equalsIgnoreCase("LLAMA")) {title = "Select Carpet Color"; col = "PETSTYLE";}
		else if (path.equalsIgnoreCase("SHEEP")) {title = "Select Sheep Color"; col = "PETCOLOR";}
		else if (path.equalsIgnoreCase("WOLF")) {title = "Select Collar Color"; col = "PETCOLOR";}
		else if (path.equalsIgnoreCase("MORPH")) {title = "Select Morph Color"; col = "MORPHCOLOR";}
		else return;
		
		Inventory gui = player.getServer().createInventory(player, 45, ChatColor.GOLD + title);
		
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
		
		String savedColor = Data.getSelected(uuid, col);
		List<String> availableColors = Data.getAccess(uuid, "DYE_COLORS");
		DyeColor[] colors = DyeColor.values();
		int i = 11;
		for (DyeColor dyeColor : colors) {
			String color = String.valueOf(dyeColor).toUpperCase();
			String colorTxt = CoreUtils.underscoreToSpace(color.toLowerCase());
			
			String available = "&cNot Available";
			ItemStack dye;
			if (availableColors.contains(color) || player.hasPermission("facthub.cosmetics.access-all")) {
				available = "&bAvailable";
				dye = new ItemStack(PetsGUI.getDyeMaterial(dyeColor));
			}
			else dye = new ItemStack(Material.GUNPOWDER, 1);
			if (color.equalsIgnoreCase(savedColor)) {
				available = "&a&oActive";
				ItemMeta meta = dye.getItemMeta();
				meta.addEnchant(Enchantment.ARROW_INFINITE, -1, true);
				meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
				dye.setItemMeta(meta);
			}
			
			List<String> lore = new ArrayList<String>();
			lore.add("&7Click to select " + color.toUpperCase());
			
			FileConfiguration valueData = Data.getValueData();
			int value = valueData.getInt("values.dye-colors." + color.toUpperCase());
			String valueName = Util.getNameByValue(value, valueData);
			int cost = valueData.getInt("value." + value + ".cost");
			
			lore.add("");
			lore.add(valueName);
			lore.add("");
			lore.add(available);
			if (available.equalsIgnoreCase("&cNot Available") && cost > -1) {
				lore.add("&7Click to buy for " + cost + " points");
			}
			
			ItemStack item = InventoryControl.getItemStack(dye, "" + PetsGUI.getChatColor(dyeColor) + ChatColor.BOLD + colorTxt,
					lore);
			gui.setItem(i, item);
			
			if (i == 15) i += 4;
			else if (i == 25) i +=4;
			else i++;
			
		}
		
		String color = "RAINBOW";
		String colorTxt = "&c&lR&6&la&e&li&a&ln&b&lb&d&lo&9&lw";
		
		String available = "&cNot Available";
		ItemStack dye;
		if (availableColors.contains(color) || player.hasPermission("facthub.cosmetics.access-all")) {
			available = "&bAvailable";
			dye = new ItemStack(Material.MAGMA_CREAM, 1);
		}
		else dye = new ItemStack(Material.GUNPOWDER, 1);
		if (color.equalsIgnoreCase(savedColor)) {
			available = "&a&oActive";
			ItemMeta meta = dye.getItemMeta();
			meta.addEnchant(Enchantment.ARROW_INFINITE, -1, true);
			meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
			dye.setItemMeta(meta);
		}
		
		ItemStack item = InventoryControl.getItemStack(dye, colorTxt,
				"&7Click to select " + color, "", available);
		gui.setItem(i, item);
		
		
		ItemStack back = InventoryControl.getItemStack(Material.ARROW, "&6Back", "&7Return to parent menu");
		gui.setItem(40, back);
		
		loaded = true;
		player.openInventory(gui);
	}
	
	@EventHandler
	public void itemClicked(InventoryClickEvent event) {
		if (!loaded) return;
		final Player player = (Player) event.getWhoClicked();
		String title = event.getView().getTitle();
		if (title.equalsIgnoreCase(ChatColor.GOLD + "Select Carpet Color")
				|| title.equalsIgnoreCase(ChatColor.GOLD + "Select Sheep Color")
				|| title.equalsIgnoreCase(ChatColor.GOLD + "Select Collar Color")
				|| title.equalsIgnoreCase(ChatColor.GOLD + "Select Morph Color")) {
			
			if ((event.getCurrentItem() == null) || (event.getCurrentItem().getType().equals(Material.AIR))) {
                return;
            }
			
			title = title.replaceAll(ChatColor.GOLD + "", "");
			String path = null;
			String col;
			if (title.equalsIgnoreCase("Select Carpet Color")) {path = "LLAMA"; col = "PETSTYLE";}
			else if (title.equalsIgnoreCase("Select Sheep Color")) {path = "SHEEP"; col = "PETCOLOR";}
			else if (title.equalsIgnoreCase("Select Collar Color")) {path = "WOLF"; col = "PETCOLOR";}
			else if (title.equalsIgnoreCase("Select Morph Color")) {path = "MORPH"; col = "MORPHCOLOR";}
			else return;
			
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
				
				if (path.equals("MORPH")) MorphsGUI.open(player, playerName);
				else PetsGUI.open(player, playerName);
				return;
			}
			
			if (loreList == null) return;
			String lore = loreList.get(0);
			
			String cut = InventoryControl.convertColors("&7Click to select ");
			String color = lore.replaceAll(cut, "");
			if (item.getType() != Material.GUNPOWDER) {
				
				
				Data.setSelected(uuid, col, color.toUpperCase());
				
				if (path.equals("morph.sheep-color")) {
					DyeColor woolColor;
					if (color.equalsIgnoreCase("RAINBOW")) woolColor = DyeColor.WHITE;
					else woolColor = DyeColor.valueOf(color);
					
					Player morphedPlayer = Bukkit.getPlayerExact(playerName);
					if (morphedPlayer != null) {
					
						if (Morphs.getMorph(morphedPlayer).getType().getEntityType() == EntityType.SHEEP) {
							Morphs.unmorph(morphedPlayer);
							Morphs.morph(morphedPlayer, EntityType.SHEEP, Data.getBoolean(uuid, "MORPHBABY"),
									Data.getBoolean(uuid, "MORPHVIEWSELF"), woolColor);
						}
						
					}
					
					MorphsGUI.open(player, playerName);
				}
				else {
					Pet pet = PetManager.getPet(Bukkit.getPlayerExact(playerName));
					if (pet != null) pet.respawn();
					
					PetsGUI.open(player, playerName);
				}
			}
			
			else {
				FileConfiguration valueData = Data.getValueData();
				int value = valueData.getInt("values.dye-colors." + color);
				int cost = valueData.getInt("value." + value + ".cost");
				
				if (FactSQLConnector.getPoints(uuid) >= cost) {
					ItemStack stack;
					if (color.equalsIgnoreCase("RAINBOW"))
						stack = InventoryControl.getItemStack(Material.MAGMA_CREAM, "&c&lR&6&la&e&li&a&ln&b&lb&d&lo&9&lw");
					else stack = InventoryControl.getItemStack(PetsGUI.getDyeMaterial(DyeColor.valueOf(color)), item.getItemMeta().getDisplayName());
					ConfirmGUI.open(player, playerName, stack, cost, "DYE_COLORS:" + path, color.toUpperCase());
				}
			}
			
		}
	}
	
	public static DyeColor getRandom() {
		DyeColor[] colors = DyeColor.values();
		
		int min = 0;
		int max = colors.length - 1;
		int rand = new Random().nextInt((max - min) + 1) + min;
		
		return colors[rand];
	}
	
}