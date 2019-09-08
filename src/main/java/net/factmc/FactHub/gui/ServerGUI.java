package net.factmc.FactHub.gui;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import net.factmc.FactCore.bukkit.InventoryControl;
import net.factmc.FactHub.Main;

public class ServerGUI implements Listener {
	
	private static boolean loaded = false;
	
	public static void open(Player player/*, boolean showAll*/) {
		Inventory gui = player.getServer().createInventory(player, 27, ChatColor.LIGHT_PURPLE + "Server Selector");
		
		ItemStack creative = InventoryControl.getItemStack(Material.DIAMOND_BLOCK, "&2&lCreative", "&7Click to join");
		ItemStack survival = InventoryControl.getItemStack(Material.SPRUCE_BOAT, "&4&lSurvival", "&7Click to join");
		ItemStack skyblock = InventoryControl.getItemStack(Material.GRASS_BLOCK, "&1&lSkyblock", "&7Click to join");
		//ItemStack tardis = InventoryControl.getItemStack(Material.BEACON, "&1&lTARDIS", "&7Click to join");
		//ItemStack modded = InventoryControl.getItemStack(Material.GOLD_PICKAXE, "&e&lModded Survival", "&7Click to join");
		
		/*if (showAll) {*/
			gui.setItem(10, creative);
			gui.setItem(13, survival);
			gui.setItem(16, skyblock);
			//gui.setItem(16, modded);
		/*}
		
		else {
			gui.setItem(11, survival);
			gui.setItem(15, modded);
		}*/
		
		loaded = true;
		player.openInventory(gui);
	}
	
	@EventHandler
	public void itemClicked(InventoryClickEvent event) {
		if (!loaded) return;
		final Player player = (Player) event.getWhoClicked();
		if (event.getView().getTitle().equalsIgnoreCase(ChatColor.LIGHT_PURPLE + "Server Selector")) {
			
			event.setCancelled(true);
			if ((event.getCurrentItem() == null) || (event.getCurrentItem().getType().equals(Material.AIR))) {
                return;
            }
			String name = event.getCurrentItem().getItemMeta().getDisplayName();
			if (name == null) return;
			
			String server = null;
			if (name.equalsIgnoreCase(InventoryControl.convertColors("&2&lCreative"))) {
				server = "creative";
			}
			else if (name.equalsIgnoreCase(InventoryControl.convertColors("&4&lSurvival"))) {
				server = "survival";
			}
			else if (name.equalsIgnoreCase(InventoryControl.convertColors("&1&lSkyblock"))) {
				server = "skyblock";
			}
			else if (name.equalsIgnoreCase(InventoryControl.convertColors("&1&lTARDIS"))) {
				server = "tardiss";
			}
			else if (name.equalsIgnoreCase(InventoryControl.convertColors("&e&lModded Survival"))) {
				server = "modded";
				
				player.sendMessage(ChatColor.GOLD + "Please use " + ChatColor.RED + "modded.factmc.net"
						+ ChatColor.GOLD + " to connect to the modded survival server");
				return;
			}
			
			if (server == null) return;
			
			player.sendMessage(ChatColor.GOLD + "Sending you to the " + server + " server...");
			player.closeInventory();
			connect(player, server);
			
		}
	}
	
	
	public static void connect(Player player, String server) {
		
		Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), new Runnable() {
			@Override
			public void run() {
				try (
						ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
						DataOutputStream dos = new DataOutputStream(baos)
					){
					
			        dos.writeUTF("Connect");
			        dos.writeUTF(server);
			        player.sendPluginMessage(Main.getPlugin(), "BungeeCord", baos.toByteArray());
				} catch (IOException e){
					e.printStackTrace();
				}
				
			}
		}, (long) 20);
		
	}
	
}