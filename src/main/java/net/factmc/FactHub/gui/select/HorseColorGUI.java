package net.factmc.FactHub.gui.select;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Horse;
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
import net.factmc.FactHub.cosmetics.Pet;
import net.factmc.FactHub.crates.Util;
import net.factmc.FactHub.gui.ConfirmGUI;
import net.factmc.FactHub.gui.cosmetics.PetsGUI;
import net.factmc.FactHub.listeners.PetManager;

public class HorseColorGUI implements Listener {
	
	private static boolean loaded = false;
	
	public static void open(Player player, String name) {
		Inventory gui = player.getServer().createInventory(player, 45, ChatColor.GOLD + "Select Color");
		
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
		
		String savedColor = Data.getSelected(uuid, "PETCOLOR");
		List<String> availableColors = Data.getAccess(uuid, "HORSE_COLORS");
		int i = 10;
		for (Horse.Color horseColor : Horse.Color.values()) {
			String color = String.valueOf(horseColor).toUpperCase();
			String colorTxt = CoreUtils.underscoreToSpace(color.toLowerCase());
			
			ChatColor chatColor = getChatColor(horseColor);
			ItemStack mat = getStack(horseColor);
			
			String available = "&cNot Available";
			ItemStack stack;
			if (availableColors.contains(color) || player.hasPermission("facthub.cosmetics.access-all")) {
				available = "&bAvailable";
				stack = mat;
			}
			else stack = new ItemStack(Material.GUNPOWDER, 1);
			if (color.equalsIgnoreCase(savedColor)) {
				available = "&a&oActive";
				ItemMeta meta = stack.getItemMeta();
				meta.addEnchant(Enchantment.ARROW_INFINITE, -1, true);
				meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
				stack.setItemMeta(meta);
			}
			
			List<String> lore = new ArrayList<String>();
			lore.add("&7Click to select " + color.toUpperCase());
			
			FileConfiguration valueData = Data.getValueData();
			int value = valueData.getInt("values.horse-colors." + color.toUpperCase());
			String valueName = Util.getNameByValue(value, valueData);
			int cost = valueData.getInt("value." + value + ".cost");
			
			lore.add("");
			lore.add(valueName);
			lore.add("");
			lore.add(available);
			if (available.equalsIgnoreCase("&cNot Available") && cost > -1) {
				lore.add("&7Click to buy for " + cost + " points");
			}
			
			ItemStack item = InventoryControl.getItemStack(stack, "" + chatColor + ChatColor.BOLD + colorTxt,
					lore);
			gui.setItem(i, item);
			
			i++;
			
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
		if (event.getView().getTitle().equalsIgnoreCase(ChatColor.GOLD + "Select Color")) {
			
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
				
				PetsGUI.open(player, playerName);
				return;
			}
			
			if (loreList == null) return;
			String lore = loreList.get(0);
			
			String cut = InventoryControl.convertColors("&7Click to select ");
			String color = lore.replaceAll(cut, "").toUpperCase();
			if (item.getType() != Material.GUNPOWDER) {
				Data.setSelected(uuid, "PETCOLOR", color.toUpperCase());
				
				Pet pet = PetManager.getPet(Bukkit.getPlayerExact(playerName));
				if (pet != null) pet.respawn();
				
				PetsGUI.open(player, playerName);
			}
			
			else {
				FileConfiguration valueData = Data.getValueData();
				int value = valueData.getInt("values.horse-colors." + color);
				int cost = valueData.getInt("value." + value + ".cost");
				
				if (FactSQLConnector.getPoints(uuid) >= cost) {
					ItemStack stack = InventoryControl.getItemStack(getStack(Horse.Color.valueOf(color)), item.getItemMeta().getDisplayName());
					ConfirmGUI.open(player, playerName, stack, cost, "HORSE_COLORS", color.toUpperCase());
				}
			}
			
		}
	}
	
	public static ItemStack getStack(Horse.Color color) {
		switch (color) {
		
			case BLACK:
				return new ItemStack(Material.INK_SAC, 1);
			case BROWN:
				return new ItemStack(Material.BROWN_CONCRETE_POWDER, 1);
			case CHESTNUT:
				return new ItemStack(Material.BEETROOT_SEEDS, 1);
			case CREAMY:
				return new ItemStack(Material.STRIPPED_JUNGLE_WOOD, 1);
			case DARK_BROWN:
				return new ItemStack(Material.BROWN_TERRACOTTA, 1);
			case GRAY:
				return new ItemStack(Material.LIGHT_GRAY_DYE, 1);
			case WHITE:
				return new ItemStack(Material.BONE_MEAL, 1);
			default:
				return null;
				
		}
	}
	
	public static ChatColor getChatColor(Horse.Color color) {
		switch (color) {
		
			case BLACK:
				return ChatColor.DARK_GRAY;
			case BROWN:
				return ChatColor.GOLD;
			case CHESTNUT:
				return ChatColor.DARK_GREEN;
			case CREAMY:
				return ChatColor.WHITE;
			case DARK_BROWN:
				return ChatColor.DARK_BLUE;
			case GRAY:
				return ChatColor.GRAY;
			case WHITE:
				return ChatColor.WHITE;
			default:
				return null;
				
		}
	}
	
}