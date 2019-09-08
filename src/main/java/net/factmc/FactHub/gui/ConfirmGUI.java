package net.factmc.FactHub.gui;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import net.factmc.FactHub.Data;
import net.factmc.FactCore.FactSQLConnector;
import net.factmc.FactCore.bukkit.InventoryControl;
import net.factmc.FactHub.gui.cosmetics.CloaksGUI;
import net.factmc.FactHub.gui.cosmetics.MorphsGUI;
import net.factmc.FactHub.gui.cosmetics.PetsGUI;
import net.factmc.FactHub.gui.cosmetics.SuitsGUI;
import net.factmc.FactHub.gui.cosmetics.TrailsGUI;
import net.factmc.FactHub.gui.select.ArmorColorGUI;
import net.factmc.FactHub.gui.select.CatColorGUI;
import net.factmc.FactHub.gui.select.DyeColorGUI;
import net.factmc.FactHub.gui.select.HorseColorGUI;
import net.factmc.FactHub.gui.select.HorseStyleGUI;
import net.factmc.FactHub.gui.select.LlamaColorGUI;
import net.factmc.FactHub.gui.select.ParrotColorGUI;
import net.factmc.FactHub.gui.select.RabbitColorGUI;

public class ConfirmGUI implements Listener {
	
	private static boolean loaded = false;
	
	public static void open(Player player, String name, ItemStack item, int cost, String category, String cosName) {
		
		Inventory gui = player.getServer().createInventory(player, 27, ChatColor.GOLD + "Confirm Purchase");
		
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
		
		item = InventoryControl.getItemStack(item.getType(), item.getItemMeta().getDisplayName(),
				"&7Cost: " + cost, "&7Type: " + category, "&7Cosmetic: " + cosName);
		
		gui.setItem(13, item);
		gui.setItem(10, InventoryControl.getItemStack(new ItemStack(Material.LIME_CONCRETE, 1),
				"&aPurchase", "&7You will lose &6" + cost + " &7points"));
		gui.setItem(16, InventoryControl.getItemStack(new ItemStack(Material.RED_CONCRETE, 1),
				"&cCancel", "&7You will not lose any points"));
		
		loaded = true;
		player.openInventory(gui);
		
	}
	
	@EventHandler
	public void itemClicked(InventoryClickEvent event) {
		if (!loaded) return;
		final Player player = (Player) event.getWhoClicked();
		if (event.getView().getTitle().equalsIgnoreCase(ChatColor.GOLD + "Confirm Purchase")) {
			
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
			
			ItemStack purchase = event.getInventory().getItem(13);
			List<String> lore = purchase.getItemMeta().getLore();
			int cost = Integer.parseInt(lore.get(0).replaceAll(InventoryControl.convertColors("&7Cost: "), ""));
			String category = lore.get(1).replaceAll(InventoryControl.convertColors("&7Type: "), "");
			String cosName = lore.get(2).replaceAll(InventoryControl.convertColors("&7Cosmetic: "), "");
			
			if (item.getType() == Material.LIME_CONCRETE) {
				
				FactSQLConnector.changePoints(uuid, -cost);
				Data.giveAccess(uuid, category.split(":")[0], cosName);
				
				exit(player, playerName, category);
				
			}
			
			else if (item.getType() == Material.RED_CONCRETE) {
				
				exit(player, playerName, category);
				
			}
			
		}
		
	}
	
	public static void exit(Player player, String playerName, String category) {
		
		String[] a = category.split(":");
		Method goTo = null;
		
		try {
			goTo = getClass(a[0]).getMethod("open", Player.class, String.class);
		} catch (NoSuchMethodException | SecurityException e) {
			try {
				goTo = getClass(a[0]).getMethod("open", Player.class, String.class, String.class);
			} catch (NoSuchMethodException | SecurityException e1) {
				e1.printStackTrace();
			}
		}
		
		try {
			
			if (goTo == null) CosmeticsGUI.open(player, playerName);
			
			else if (a.length > 1 && goTo.getParameterTypes().length > 2) {
				String path = a[1];
				goTo.invoke(null, player, playerName, path);
			}
			
			else {
				goTo.invoke(null, player, playerName);
			}
			
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
		
	}
	
	
	
	public static Class<?> getClass(String category) {
		
		switch(category) {
		
		case "CLOAKS":
			return CloaksGUI.class;
		case "MORPHS":
			return MorphsGUI.class;
		case "PETS":
			return PetsGUI.class;
		case "SUITS":
			return SuitsGUI.class;
		case "TRAILS":
			return TrailsGUI.class;
			
		case "SUIT_COLORS":
			return ArmorColorGUI.class;
		case "CAT_COLORS":
			return CatColorGUI.class;
		case "DYE_COLORS":
			return DyeColorGUI.class;
		case "HORSE_COLORS":
			return HorseColorGUI.class;
		case "HORSE_STYLES":
			return HorseStyleGUI.class;
		case "LLAMA_COLORS":
			return LlamaColorGUI.class;
		case "PARROT_COLORS":
			return ParrotColorGUI.class;
		case "RABBIT_COLORS":
			return RabbitColorGUI.class;
		default:
			return CosmeticsGUI.class;
		
		}
		
	}
	
}