package net.factmc.FactHub.gui.cosmetics;

import java.util.ArrayList;
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

public class TrailsGUI implements Listener {
	
	private static boolean loaded = false;
	
	public static void open(Player player, String name) {
		double count = (double) getUseableParticles().size() / (double) 7;
		int i = 0;
		while (i < count) {
			i++;
		}
		int length = (4 + i) * 9;
		
		Inventory gui = player.getServer().createInventory(player, length, ChatColor.DARK_PURPLE + "Trails");
		
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
		String savedTrail = Data.getSelected(uuid, "TRAIL");
		List<String> availableTypes = Data.getAccess(uuid, "TRAILS");
		for (String trail : getUseableParticles()) {
			String available = "&cNot Available";
			ItemStack stack = new ItemStack(Material.GUNPOWDER, 1);
			
			if (availableTypes.contains(String.valueOf(trail)) || player.hasPermission("facthub.cosmetics.access-all")) {
				available = "&bAvailable";
				stack = getStack(trail);
			}
			if (savedTrail.equalsIgnoreCase(String.valueOf(trail))) {
				available = "&a&oActive";
				ItemMeta meta = stack.getItemMeta();
				meta.addEnchant(Enchantment.ARROW_INFINITE, -1, true);
				meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
				stack.setItemMeta(meta);
			}
			
			String particleName = getName(trail);
			
			List<String> lore = getDescription(trail);
			
			FileConfiguration valueData = Data.getValueData();
			int value = valueData.getInt("values.trails." + String.valueOf(trail).toUpperCase());
			String valueName = Util.getNameByValue(value, valueData);
			int cost = valueData.getInt("value." + value + ".cost");
			
			lore.add("");
			lore.add(valueName);
			lore.add("");
			lore.add(available);
			if (available.equalsIgnoreCase("&cNot Available") && cost > -1) {
				lore.add("&7Click to buy for " + cost + " points");
			}
			
			ItemStack pickStack = InventoryControl.getItemStack(stack, particleName, lore);
			gui.setItem(i, pickStack);
			
			if ((i-7) % 9 == 0) i += 3;
			else i++;
		}
		
		ItemStack reset = InventoryControl.getItemStack(Material.BARRIER, "&cReset Trail", "&7Turn off your trail");
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
		if (event.getView().getTitle().equalsIgnoreCase(ChatColor.DARK_PURPLE + "Trails")) {
			
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
			
			else if (name.equalsIgnoreCase(InventoryControl.convertColors("&cReset Trail"))) {
				Data.setSelected(uuid, "TRAIL", "NONE");
				open(player, playerName);
			}
			
			else {
				if (item.getType() == Material.GUNPOWDER) {
					for (String trail : getUseableParticles()) {
						String trailName = ChatColor.translateAlternateColorCodes('&',
								getName(trail));
						if (ChatColor.stripColor(trailName).equalsIgnoreCase(ChatColor.stripColor(name))) {
							FileConfiguration valueData = Data.getValueData();
							int value = valueData.getInt("values.trails." + String.valueOf(trail).toUpperCase());
							int cost = valueData.getInt("value." + value + ".cost");
							
							if (FactSQLConnector.getPoints(uuid) >= cost) {
								ItemStack stack = InventoryControl.getItemStack(getStack(trail), getName(trail));
								ConfirmGUI.open(player, playerName, stack, cost, "TRAILS", String.valueOf(trail).toUpperCase());
							}
							break;
						}
					}
				}
				
				else {
					for (String trail : getUseableParticles()) {
						String particleName = getName(trail);
						
						if (ChatColor.stripColor(name).equalsIgnoreCase(ChatColor.stripColor(InventoryControl.convertColors(particleName)))) {
							Data.setSelected(uuid, "TRAIL", trail.toUpperCase());
							open(player, playerName);
						}
					}
				}
				
			}
			
		}
	}
	
	public static List<String> getUseableParticles() {
		List<String> list = new ArrayList<String>();
		list.add("CRIT_MAGIC");
		list.add("DRIP_LAVA");
		list.add("DRIP_WATER");
		list.add("ENCHANTMENT_TABLE");
		list.add("FALLING_DUST");
		list.add("HEART");
		list.add("LAVA");
		list.add("NOTE");
		list.add("PORTAL");
		list.add("REDSTONE");
		list.add("SNOWBALL");
		list.add("FACTMC");
		list.add("VILLAGER_HAPPY");
		list.add("WATER_DROP");
		
		return list;
	}
	
	public static ItemStack getStack(String particle) {
		ItemStack stack;
		
		switch (particle) {
			case "CRIT_MAGIC": stack = new ItemStack(Material.BEACON, 1); break;
			case "DRIP_LAVA": stack = new ItemStack(Material.LAVA_BUCKET, 1); break;
			case "DRIP_WATER": stack = new ItemStack(Material.WATER_BUCKET, 1); break;
			case "ENCHANTMENT_TABLE": stack = new ItemStack(Material.ENCHANTING_TABLE, 1); break;
			case "FALLING_DUST": stack = new ItemStack(Material.COBWEB, 1); break;
			case "HEART": stack = new ItemStack(Material.APPLE, 1); break;
			case "LAVA": stack = new ItemStack(Material.FIRE_CHARGE, 1); break;
			case "NOTE": stack = new ItemStack(Material.JUKEBOX, 1); break;
			case "PORTAL": stack = new ItemStack(Material.PURPLE_WOOL, 1); break;
			case "REDSTONE": stack = new ItemStack(Material.REDSTONE, 1); break;
			case "SNOWBALL": stack = new ItemStack(Material.SNOWBALL, 1); break;
			case "VILLAGER_ANGRY": stack = new ItemStack(Material.BLAZE_POWDER, 1); break;
			case "FACTMC": stack = new ItemStack(Material.COOKIE, 1); break;
			case "VILLAGER_HAPPY": stack = new ItemStack(Material.EMERALD, 1); break;
			case "WATER_DROP": stack = new ItemStack(Material.COD, 1); break;
			default: stack = new ItemStack(Material.GUNPOWDER, 1);
		}
		
		return stack;
	}
	
	public static String getName(String particle) {
		String name;
		
		switch (particle) {
			case "CRIT_MAGIC": name = "&3Magic"; break;
			case "DRIP_LAVA": name = "&6Dripping Lava"; break;
			case "DRIP_WATER": name = "&9Dripping Water"; break;
			case "ENCHANTMENT_TABLE": name = "&dGlyphs"; break;
			case "FALLING_DUST": name = "&7Dust"; break;
			case "HEART": name = "&cHearts"; break;
			case "LAVA": name = "&4Volcano"; break;
			case "NOTE": name = "&eMusic Notes"; break;
			case "PORTAL": name = "&5Teleportation"; break;
			case "REDSTONE": name = "&cR&6a&ei&an&bb&do&9w"; break;
			case "SNOWBALL": name = "&rSnow"; break;
			case "VILLAGER_ANGRY": name = "&6Angry Villager"; break;
			case "FACTMC": name = "&2&lFACT &6&lMC"; break;
			case "VILLAGER_HAPPY": name = "&aHappy Villager"; break;
			case "WATER_DROP": name = "&bWater"; break;
			default: name = String.valueOf(particle);
		}
		
		return name;
	}
	
	private static List<String> getDescription(String particle) {
		List<String> list = new ArrayList<String>();
		
		switch (particle) {
			case "CRIT_MAGIC":
				list.add("&7Magic cyan effects");
				list.add("&7fly out of you");
				break;
			case "DRIP_LAVA":
				list.add("&7Drip Drip Drip");
				list.add("&7Oh thats lava");
				break;
			case "DRIP_WATER":
				list.add("&7Plop Plop Plop");
				break;
			case "ENCHANTMENT_TABLE":
				list.add("&7Enchantment Table");
				list.add("&7glyphs spawn and swoop");
				list.add("&7around you");
				break;
			case "FALLING_DUST":
				list.add("&7Dust spawns above");
				list.add("&7you and then falls");
				list.add("&7down");
				break;
			case "HEART":
				list.add("&7A bunch of hearts");
				list.add("&7above your head,");
				list.add("&7how lovely");
				break;
			case "LAVA":
				list.add("&7Boom Boom Boom");
				list.add("&7fire and smoke");
				list.add("&7everywhere");
				break;
			case "NOTE":
				list.add("&7Love music? Then");
				list.add("&7get some rainbow");
				list.add("&7notes above you");
				break;
			case "PORTAL":
				list.add("&7The same effects");
				list.add("&7that an enderman");
				list.add("&7and portals give off!");
				break;
			case "REDSTONE":
				list.add("&7Simple but colorful,");
				list.add("&7just a nice rainbow");
				list.add("&7trail");
				break;
			case "SNOWBALL":
				list.add("&7Love winter weather?");
				list.add("&7Get some snow with");
				list.add("&7this trail");
				break;
			case "VILLAGER_ANGRY":
				list.add("&7Know those particles");
				list.add("&7that appear when you");
				list.add("&7hurt a villager? Well");
				list.add("&7here they are!");
				break;
			case "FACTMC":
				list.add("&7Show off your spirit with");
				list.add("&7this trail. It shows a");
				list.add("&7trail of green and yellow");
				break;
			case "VILLAGER_HAPPY":
				list.add("&7A nice green trail");
				list.add("&7A good choice if you");
				list.add("&7like emeralds");
				break;
			case "WATER_DROP":
				list.add("&7If you really like");
				list.add("&7rain then you should");
				list.add("&7use this trail");
				break;
			default:
				list.add("&7No Description");
				list.add("&7Tell an admin if");
				list.add("&7you are seeing this");
		}
		
		return list;
	}
	
}