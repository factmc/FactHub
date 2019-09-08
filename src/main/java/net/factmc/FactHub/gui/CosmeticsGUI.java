package net.factmc.FactHub.gui;

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
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import net.factmc.FactHub.Data;
import net.factmc.FactHub.Main;
import net.factmc.FactCore.FactSQLConnector;
import net.factmc.FactCore.bukkit.InventoryControl;
import net.factmc.FactHub.cosmetics.Morphs;
import net.factmc.FactHub.cosmetics.Pet;
import net.factmc.FactHub.gui.cosmetics.*;
import net.factmc.FactHub.listeners.PetManager;
import net.factmc.FactHub.parkour.Parkour;

public class CosmeticsGUI implements Listener {
	
	private static boolean loaded = false;
	
	public static void open(Player player, String name) {
		
		if (Parkour.inParkour(player)) {
			player.sendMessage(Parkour.prefix + ChatColor.RED + "You cannot use cosmetics during the parkour. Use "
					+ ChatColor.GOLD + "/parkour quit" + ChatColor.RED + " to quit the parkour");
			return;
		}
		
		Inventory gui = player.getServer().createInventory(player, 45, ChatColor.GOLD + "Cosmetics");
		
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
		
		int petCount = Data.getAccess(uuid, "PETS").size();
		int trailCount = Data.getAccess(uuid, "TRAILS").size();
		int cloakCount = Data.getAccess(uuid, "CLOAKS").size();
		int suitCount = Data.getAccess(uuid, "SUITS").size();
		int morphCount = Data.getAccess(uuid, "MORPHS").size();
		
		int allPets = PetsGUI.getUseableEntities().size();
		int allTrails = TrailsGUI.getUseableParticles().size();
		int allCloaks = Data.getCloakData().getConfigurationSection("cloaks").getKeys(false).size();
		int allSuits = Data.getSuitData().getConfigurationSection("suits").getKeys(false).size();
		int allMorphs = MorphsGUI.getUseableEntities().size();
		
		if (player.hasPermission("facthub.cosmetics.access-all")) {
			petCount = allPets;
			trailCount = allTrails;
			cloakCount = allCloaks;
			suitCount = allSuits;
			morphCount = allMorphs;
		}
		
		ItemStack pets = InventoryControl.getItemStack(Material.BONE, "&aPets", "&7Available: " + petCount  + "/" + allPets);
		ItemStack trails = InventoryControl.getItemStack(Material.ENDER_PEARL, "&dTrails", "&7Available: " + trailCount  + "/" + allTrails);
		ItemStack cloaks = InventoryControl.getItemStack(Material.BEACON, "&cCloaks", "&7Available: " + cloakCount  + "/" + allCloaks);
		ItemStack suits = InventoryControl.getItemStack(Material.GOLDEN_CHESTPLATE, "&bSuits", "&7Available: " + suitCount + "/" + allSuits);
		ItemStack morphs = InventoryControl.getItemStack(Material.ROTTEN_FLESH, "&9Morphs", "&7Available: " + morphCount + "/" + allMorphs);
		if (!Main.morphs) {
			ItemMeta meta = morphs.getItemMeta(); List<String> lore = meta.getLore();
			lore.add(0, ChatColor.RED + "Morphs are currently disabled");
			lore.add(1, ChatColor.RED + "Please contact an admin if you");
			lore.add(2, ChatColor.RED + "believe this is an error");
			meta.setLore(lore); morphs.setItemMeta(meta);
		}
		ItemStack reset = InventoryControl.getItemStack(Material.BARRIER, "&6Disable All", "&7Disable all your cosmetics");
		
		gui.setItem(11, pets);
		gui.setItem(15, trails);
		gui.setItem(29, suits);
		gui.setItem(33, cloaks);
		gui.setItem(22, morphs);
		gui.setItem(40, reset);
		
		loaded = true;
		player.openInventory(gui);
	}
	
	@EventHandler
	public void itemClicked(InventoryClickEvent event) {
		if (!loaded) return;
		final Player player = (Player) event.getWhoClicked();
		if (event.getView().getTitle().equalsIgnoreCase(ChatColor.GOLD + "Cosmetics")) {
			
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
			String name = event.getCurrentItem().getItemMeta().getDisplayName();
			if (name == null) return;
			
			if (name.equalsIgnoreCase(InventoryControl.convertColors("&aPets"))) {
				PetsGUI.open(player, playerName);
				return;
			}
			else if (name.equalsIgnoreCase(InventoryControl.convertColors("&dTrails"))) {
				TrailsGUI.open(player, playerName);
				return;
			}
			else if (name.equalsIgnoreCase(InventoryControl.convertColors("&cCloaks"))) {
				CloaksGUI.open(player, playerName);
				return;
			}
			else if (name.equalsIgnoreCase(InventoryControl.convertColors("&bSuits"))) {
				SuitsGUI.open(player, playerName);
				return;
			}
			else if (name.equalsIgnoreCase(InventoryControl.convertColors("&9Morphs"))) {
				if (Main.morphs)// || player.hasPermission("facthub.cosmetics.access-all"))
					MorphsGUI.open(player, playerName);
				return;
			}
			else if (name.equalsIgnoreCase(InventoryControl.convertColors("&6Disable All"))) {
				Data.setSelected(uuid, "CLOAK", "NONE");
				Data.setSelected(uuid, "SUIT", "NONE");
				Data.setSelected(uuid, "TRAIL", "NONE");
				
				Pet pet = PetManager.getPet(Bukkit.getPlayerExact(playerName));
				Data.setSelected(uuid, "PET", "NONE");
				if (pet != null) pet.despawn();
				if (Main.morphs)
					Morphs.unmorph(Bukkit.getPlayerExact(playerName));
				
				open(player, playerName);
				return;
			}
			
		}
	}
	
}