package net.factmc.FactHub.gui.cosmetics;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import net.factmc.FactHub.Data;
import net.factmc.FactCore.CoreUtils;
import net.factmc.FactCore.FactSQLConnector;
import net.factmc.FactCore.bukkit.InventoryControl;
import net.factmc.FactHub.Main;
import net.factmc.FactHub.cosmetics.UpdateSuits;
import net.factmc.FactHub.crates.Util;
import net.factmc.FactHub.gui.ConfirmGUI;
import net.factmc.FactHub.gui.CosmeticsGUI;
import net.factmc.FactHub.gui.select.ArmorColorGUI;

public class SuitsGUI implements Listener {
	
	private static boolean loaded = false;
	private List<OfflinePlayer[]> changingHead = new ArrayList<OfflinePlayer[]>();
	
	public static void open(Player player, String name) {
		FileConfiguration suitData = Data.getSuitData();
		
		double count = (double) suitData.getConfigurationSection("suits").getKeys(false).size() / (double) 7;
		int i = 0;
		while (i < count) {
			i++;
		}
		int length = (4 + i) * 9;
		
		Inventory gui = player.getServer().createInventory(player, length, ChatColor.DARK_AQUA + "Suits");
		player.openInventory(gui);
		
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
		String savedSuit = Data.getSelected(uuid, "SUIT");
		List<String> availableTypes = Data.getAccess(uuid, "SUITS");
		String path = null;
		for (String suit : suitData.getConfigurationSection("suits").getKeys(false)) {
			path = "suits." + suit;
			
			String available = "&cNot Available";
			ItemStack stack = new ItemStack(Material.GUNPOWDER, 1);
			
			if (availableTypes.contains(suit.toUpperCase()) || player.hasPermission("facthub.cosmetics.access-all")) {
				available = "&bAvailable";
				String item = suitData.getString(path + ".item");
				stack = new ItemStack(Material.valueOf(item), 1);
			}
			if (savedSuit.equalsIgnoreCase(suit.toUpperCase())) {
				available = "&a&oActive";
				ItemMeta meta = stack.getItemMeta();
				meta.addEnchant(Enchantment.ARROW_INFINITE, -1, true);
				meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
				stack.setItemMeta(meta);
				
				if (path != null
						&& suitData.getString(path + ".helmet").equalsIgnoreCase("PLAYERHEAD")
						&& suitData.getString(path + ".force-head") == null) {
					
					String headName = Data.getSelected(uuid, "SUITHEAD");
					ItemStack playerHead = UpdateSuits.getHead(headName);
					ItemMeta headMeta = playerHead.getItemMeta();
					headMeta.setUnbreakable(false);
					playerHead.setItemMeta(headMeta);
					
					ItemStack pickHead = InventoryControl.getItemStack(playerHead, "&eChange Head",
							"&7Pick a player to use for", "&7the head of your suit", "", "&7Current Head: &a" + headName);
					gui.setItem(length - 17, pickHead);
				}
			}
			
			String suitName = suitData.getString(path + ".name");
			
			List<String> lore = suitData.getStringList(path + ".description");
			if (lore != null && lore.size() == 0) {
				lore.add("&7No Description");
				lore.add("&7Tell an admin if");
				lore.add("&7you are seeing this");
			}
			
			FileConfiguration valueData = Data.getValueData();
			int value = valueData.getInt("values.suits." + suit.toUpperCase());
			String valueName = Util.getNameByValue(value, valueData);
			int cost = valueData.getInt("value." + value + ".cost");
			
			lore.add("");
			lore.add(valueName);
			lore.add("");
			lore.add(available);
			if (available.equalsIgnoreCase("&cNot Available") && cost > -1) {
				lore.add("&7Click to buy for " + cost + " points");
			}
			
			ItemStack pickStack = InventoryControl.getItemStack(stack, suitName, lore);
			gui.setItem(i, pickStack);
			
			if ((i-7) % 9 == 0) i += 3;
			else i++;
		}
		
		String savedColor = Data.getSelected(uuid, "SUITCOLOR");
		Color color = UpdateSuits.getColor(savedColor);
		String colorTxt = CoreUtils.underscoreToSpace(savedColor.toLowerCase());
		ItemStack dye = InventoryControl.getDyeByColor(color);
		ChatColor dyeTxt = InventoryControl.getChatColorByColor(color);
		if (dyeTxt == ChatColor.BLACK) dyeTxt = ChatColor.DARK_GRAY;
		
		ItemStack pickColor = InventoryControl.getItemStack(dye, "&eChange Color",
				"&7Pick a main color for your suits to use", "", "&7Current Color: "
				+ dyeTxt + ChatColor.BOLD + colorTxt);
		gui.setItem(length - 11, pickColor);
		
		ItemStack reset = InventoryControl.getItemStack(Material.BARRIER, "&cReset Suit", "&7Turn off your cloak");
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
		if (event.getView().getTitle().equalsIgnoreCase(ChatColor.DARK_AQUA + "Suits")) {
			
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
			
			else if (name.equalsIgnoreCase(InventoryControl.convertColors("&cReset Suit"))) {
				Data.setSelected(uuid, "SUIT", "NONE");
				open(player, playerName);
			}
			else if (name.equalsIgnoreCase(InventoryControl.convertColors("&eChange Color"))) {
				ArmorColorGUI.open(player, playerName);
			}
			else if (name.equalsIgnoreCase(InventoryControl.convertColors("&eChange Head"))) {
				final OfflinePlayer[] array = {player, Bukkit.getOfflinePlayer(uuid)};
				changingHead.add(array);
				player.sendMessage(ChatColor.GOLD + "Enter a new name in chat: (Use =cancel to cancel)");
				player.closeInventory();
				
				Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), new Runnable() {

					@Override
					public void run() {
						if (changingHead.contains(array)) {
							changingHead.remove(array);
							player.sendMessage(ChatColor.RED + "Time expired, entry cancelled");
						}
					}
					
				}, 2400);
			}
			
			else {
				FileConfiguration suitData = Data.getSuitData();
				
				if (item.getType() == Material.GUNPOWDER) {
					FileConfiguration valueData = Data.getValueData();
					for (String suit : suitData.getConfigurationSection("suits").getKeys(false)) {
						String suitName = ChatColor.translateAlternateColorCodes('&',
								suitData.getString("suits." + suit + ".name"));
						if (ChatColor.stripColor(suitName).equalsIgnoreCase(ChatColor.stripColor(name))) {
							int value = valueData.getInt("values.suits." + suit.toUpperCase());
							int cost = valueData.getInt("value." + value + ".cost");
							
							if (FactSQLConnector.getPoints(uuid) >= cost) {
								String materialName = suitData.getString("suits." + suit + ".item");
								ItemStack stack = InventoryControl.getItemStack(Material.valueOf(materialName),
										suitData.getString("suits." + suit + ".name"));
								ConfirmGUI.open(player, playerName, stack, cost, "SUITS", suit.toUpperCase());
							}
							break;
						}
					}
				}
				
				else {
					for (String suit : suitData.getConfigurationSection("suits").getKeys(false)) {
						String suitName = suitData.getString("suits." + suit + ".name");
						
						if (ChatColor.stripColor(name).equalsIgnoreCase(ChatColor.stripColor(InventoryControl.convertColors(suitName)))) {
							Data.setSelected(uuid, "SUIT", suit.toUpperCase());
							open(player, playerName);
						}
					}
				}
				
			}
			
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onHeadSetting(AsyncPlayerChatEvent event) {
		final Player player = event.getPlayer();
		OfflinePlayer[] remove = null;
		for (final OfflinePlayer[] array : changingHead) {
			
			if (array[0] == player) {
				event.setCancelled(true);
				
				String msg = event.getMessage();
				
				if (!msg.equalsIgnoreCase("=cancel")) {
					Data.setSelected(array[1].getUniqueId(), "SUITHEAD",  msg);
					
					Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), new Runnable() {

						@Override
						public void run() {
							open(player, array[1].getName());
						}
						
					});
					
				}
				
				else {
					player.sendMessage(ChatColor.RED + "Name change cancelled");
				}
				remove = array;
				break;
			}
		}
		if (remove != null) {
			changingHead.remove(remove);
		}
		
	}
	
}