package net.factmc.FactHub.gui.cosmetics;

import java.util.ArrayList;
import java.util.List;
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

import me.libraryaddict.disguise.disguisetypes.Disguise;
import net.factmc.FactHub.Data;
import net.factmc.FactCore.CoreUtils;
import net.factmc.FactCore.FactSQLConnector;
import net.factmc.FactCore.bukkit.InventoryControl;
import net.factmc.FactHub.cosmetics.Morphs;
import net.factmc.FactHub.crates.Util;
import net.factmc.FactHub.gui.ConfirmGUI;
import net.factmc.FactHub.gui.CosmeticsGUI;
import net.factmc.FactHub.gui.select.DyeColorGUI;

public class MorphsGUI implements Listener {
	
	private static boolean loaded = false;
	
	public static void open(Player player, String name) {
		double count = (double) getUseableEntities().size() / (double) 7;
		int i = 0;
		while (i < count) {
			i++;
		}
		int length = (4 + i) * 9;
		
		Inventory gui = player.getServer().createInventory(player, length, ChatColor.DARK_BLUE + "Morphs");
		
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
		
		Disguise morph = Morphs.getMorph(player);
		EntityType active = null; 
		if (morph != null) active = morph.getType().getEntityType();
		
		i = 10;
		List<String> availableTypes = Data.getAccess(uuid, "MORPHS");
		for (EntityType type : getUseableEntities()) {
			String available = "&cNot Available";
			ItemStack stack = new ItemStack(Material.GUNPOWDER, 1);
			
			if (availableTypes.contains(String.valueOf(type)) || player.hasPermission("facthub.cosmetics.access-all")) {
				available = "&bAvailable";
				stack = getStack(type);
			}
			if (active == type) {
				available = "&a&oActive";
				ItemMeta meta = stack.getItemMeta();
				meta.addEnchant(Enchantment.ARROW_INFINITE, -1, true);
				meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
				stack.setItemMeta(meta);
			}
			
			String morphName = getName(type);
			
			List<String> lore = getDescription(type);
			
			FileConfiguration valueData = Data.getValueData();
			int value = valueData.getInt("values.morphs." + String.valueOf(type).toUpperCase());
			String valueName = Util.getNameByValue(value, valueData);
			int cost = valueData.getInt("value." + value + ".cost");
			
			lore.add("");
			lore.add(valueName);
			lore.add("");
			lore.add(available);
			if (available.equalsIgnoreCase("&cNot Available") && cost > -1) {
				lore.add("&7Click to buy for " + cost + " points");
			}
			
			ItemStack pickStack = InventoryControl.getItemStack(stack, morphName, lore);
			gui.setItem(i, pickStack);
			
			if ((i-7) % 9 == 0) i += 3;
			else i++;
		}
		
		if (true) {
			boolean baby = Data.getBoolean(uuid, "MORPHBABY");
			String babyTxt = "&c&lFALSE";
			if (baby) babyTxt = "&a&lTRUE";
			
			ItemStack pickAge = InventoryControl.getItemStack(Material.GOLDEN_APPLE, "&bChange Age",
					"&7Choose whether or not your", "&7morph should be a baby", "", "&7Baby: "
					+ babyTxt);
			gui.setItem(length - 12, pickAge);
		}
		
		if (active == EntityType.SHEEP) {
			String cName = "&eChange Wool Color";
			
			List<String> cLore = new ArrayList<String>();
			cLore.add("&7Pick a color for the wool");
			cLore.add("&7of your sheep");
			cLore.add("");
			
			String savedColor = Data.getSelected(uuid, "MORPHCOLOR");
			String colorTxt;
			ItemStack cMat;
			if (savedColor.equalsIgnoreCase("RAINBOW")) {
				colorTxt = "&c&lR&6&la&e&li&a&ln&b&lb&d&lo&9&lw";
				cMat = new ItemStack(Material.ORANGE_GLAZED_TERRACOTTA, 1);
			}
			else {
				DyeColor color = DyeColor.valueOf(savedColor);
				colorTxt = "" + PetsGUI.getChatColor(color) + ChatColor.BOLD + CoreUtils.underscoreToSpace(String.valueOf(color).toLowerCase());
				cMat = new ItemStack(PetsGUI.getWoolMaterial(color));
			}
			cLore.add("&7Current Color: " + colorTxt);
			
			ItemStack cStack = InventoryControl.getItemStack(cMat, cName, cLore);
			gui.setItem(length - 17, cStack);
		}
		
		boolean viewSelf = Data.getBoolean(uuid, "MORPHVIEWSELF");
		String viewTxt = "&c&lFALSE";
		if (viewSelf) viewTxt = "&a&lTRUE";
		ItemStack viewToggle = InventoryControl.getItemStack(Material.ENDER_EYE, "&aToggle View Self",
				"&7Toggle seeing your own morph", "", "&7View Self: " + viewTxt);
		gui.setItem(length - 11, viewToggle);
		
		ItemStack reset = InventoryControl.getItemStack(Material.BARRIER, "&cReset Morph", "&7Turn off your morph");
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
		if (event.getView().getTitle().equalsIgnoreCase(ChatColor.DARK_BLUE + "Morphs")) {
			
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
				return;
			}
			
			if (name.equalsIgnoreCase(InventoryControl.convertColors("&cReset Morph"))) {
				Morphs.unmorph(Bukkit.getPlayerExact(playerName));
				open(player, playerName);
			}
			
			else if (name.equalsIgnoreCase(InventoryControl.convertColors("&aToggle View Self"))) {
				boolean viewSelf = Data.getBoolean(uuid, "MORPHVIEWSELF");
				Data.setBoolean(uuid, "MORPHVIEWSELF", !viewSelf);
				
				//boolean baby = data.getBoolean("cosmetics.morph.baby");
				boolean refresh = Morphs.isMorphed(Bukkit.getPlayerExact(playerName));
				if (refresh) {
					Morphs.changeViewSelf(Bukkit.getPlayerExact(playerName), !viewSelf);
				}
				
				open(player, playerName);
			}
			
			else if (name.equalsIgnoreCase(InventoryControl.convertColors("&bChange Age"))) {
				boolean isBaby = Data.getBoolean(uuid, "MORPHBABY");
				Data.setBoolean(uuid, "MORPHBABY", !isBaby);
				
				boolean viewSelf = Data.getBoolean(uuid, "MORPHVIEWSELF");
				boolean refresh = Morphs.isMorphed(Bukkit.getPlayerExact(playerName));
				if (refresh) {
					EntityType morph = Morphs.getMorph(Bukkit.getPlayerExact(playerName)).getType().getEntityType();
					Morphs.unmorph(Bukkit.getPlayerExact(playerName));
					Morphs.morph(Bukkit.getPlayerExact(playerName), morph, !isBaby, viewSelf);
				}
				
				open(player, playerName);
			}
			
			else if (name.equalsIgnoreCase(InventoryControl.convertColors("&eChange Wool Color"))) {
				DyeColorGUI.open(player, playerName, "MORPH");
			}
			
			else {
				if (item.getType() == Material.GUNPOWDER) {
					for (EntityType morph : getUseableEntities()) {
						String morphName = ChatColor.translateAlternateColorCodes('&',
								getName(morph));
						if (ChatColor.stripColor(morphName).equalsIgnoreCase(ChatColor.stripColor(name))) {
							FileConfiguration valueData = Data.getValueData();
							int value = valueData.getInt("values.morphs." + String.valueOf(morph).toUpperCase());
							int cost = valueData.getInt("value." + value + ".cost");
							
							if (FactSQLConnector.getPoints(uuid) >= cost) {
								ItemStack stack = InventoryControl.getItemStack(getStack(morph), getName(morph));
								ConfirmGUI.open(player, playerName, stack, cost, "MORPHS", String.valueOf(morph).toUpperCase());
							}
							break;
						}
					}
				}
				
				else {
					for (EntityType type : getUseableEntities()) {
						String typeName = getName(type);
						
						if (ChatColor.stripColor(name).equalsIgnoreCase(ChatColor.stripColor(InventoryControl.convertColors(typeName)))) {
							if (type == EntityType.SHEEP) {
								String dataColor = Data.getSelected(uuid, "MORPHCOLOR");
								if (dataColor.equalsIgnoreCase("NONE")) {
									Data.setSelected(uuid, "MORPHCOLOR", "WHITE");
									dataColor = "WHITE";
								}
								
								DyeColor color;
								if (dataColor.equalsIgnoreCase("RAINBOW")) color = DyeColor.WHITE;
								else color = DyeColor.valueOf(dataColor);
								
								Morphs.setMorph(Bukkit.getPlayerExact(playerName), type, Data.getBoolean(uuid, "MORPHBABY"),
										Data.getBoolean(uuid, "MORPHVIEWSELF"), color);
							}
							
							else {
								Morphs.setMorph(Bukkit.getPlayerExact(playerName), type, Data.getBoolean(uuid, "MORPHBABY"),
										Data.getBoolean(uuid, "MORPHVIEWSELF"), null);
							}
							
							open(player, playerName);
						}
					}
				}
				
			}
			
		}
	}
	
	public static List<EntityType> getUseableEntities() {
		List<EntityType> list = new ArrayList<EntityType>();
		list.add(EntityType.CHICKEN);
		list.add(EntityType.COW);
		list.add(EntityType.IRON_GOLEM);
		list.add(EntityType.MUSHROOM_COW);
		list.add(EntityType.OCELOT);
		list.add(EntityType.PIG);
		list.add(EntityType.SHEEP);
		list.add(EntityType.CREEPER);
		list.add(EntityType.ENDERMAN);
		list.add(EntityType.POLAR_BEAR);
		list.add(EntityType.SKELETON);
		list.add(EntityType.SPIDER);
		list.add(EntityType.TURTLE);
		list.add(EntityType.ZOMBIE);
		
		return list;
	}
	
	public static ItemStack getStack(EntityType type) {
		ItemStack stack;
		
		switch (type) {
			case CHICKEN: stack = new ItemStack(Material.CHICKEN, 1); break;
			case COW: stack = new ItemStack(Material.BEEF, 1); break;
			case IRON_GOLEM: stack = new ItemStack(Material.IRON_INGOT, 1); break;
			case MUSHROOM_COW: stack = new ItemStack(Material.RED_MUSHROOM, 1); break;
			case OCELOT: stack = new ItemStack(Material.COD, 1); break;
			case PIG: stack = new ItemStack(Material.PORKCHOP, 1); break;
			case SHEEP: stack = new ItemStack(Material.MUTTON, 1); break;
			case CREEPER: stack = new ItemStack(Material.TNT, 1); break;
			case ENDERMAN: stack = new ItemStack(Material.ENDER_PEARL, 1); break;
			case POLAR_BEAR: stack = new ItemStack(Material.SNOWBALL, 1); break;
			case SKELETON: stack = new ItemStack(Material.BONE, 1); break;
			case SPIDER: stack = new ItemStack(Material.STRING, 1); break;
			case TURTLE: stack = new ItemStack(Material.TURTLE_EGG, 1); break;
			case ZOMBIE: stack = new ItemStack(Material.ROTTEN_FLESH, 1); break;
			default: stack = new ItemStack(Material.GUNPOWDER, 1);
		}
		
		return stack;
	}
	
	public static String getName(EntityType type) {
		String name;
		
		switch (type) {
			case CHICKEN: name = "&rChicken"; break;
			case COW: name = "&6Cow"; break;
			case IRON_GOLEM: name = "&rIron Golem"; break;
			case MUSHROOM_COW: name = "&cMooshroom"; break;
			case OCELOT: name = "&eOcelot"; break;
			case PIG: name = "&dPig"; break;
			case SHEEP: name = "&rSheep"; break;
			case CREEPER: name = "&aCreeper"; break;
			case ENDERMAN: name = "&5Enderman"; break;
			case POLAR_BEAR: name = "&bPolar Bear"; break;
			case SKELETON: name = "&rSkeleton"; break;
			case SPIDER: name = "&8Spider"; break;
			case TURTLE: name = "&eTurtle"; break;
			case ZOMBIE: name = "&2Zombie"; break;
			default: name = String.valueOf(type);
		}
		
		return name;
	}
	
	private static List<String> getDescription(EntityType type) {
		List<String> list = new ArrayList<String>();
		
		switch (type) {
			case CHICKEN:
				list.add("&7Be a chicken, or");
				list.add("&7a duck?");
				break;
			case COW:
				list.add("&7The basic cow");
				break;
			case IRON_GOLEM:
				list.add("&7Really Strong!");
				break;
			case MUSHROOM_COW:
				list.add("&7Like mushrooms?");
				break;
			case OCELOT:
				list.add("&7A wild ocelot is");
				list.add("&7really fast!");
				break;
			case PIG:
				list.add("&7Who wouldn't want");
				list.add("&7to be a pig?");
				break;
			case SHEEP:
				list.add("&7Customizable wool");
				list.add("&7colors, including");
				list.add("&7rainbow!");
				break;
			case CREEPER:
				list.add("&7Hissss...");
				list.add("...Boom!");
				break;
			case ENDERMAN:
				list.add("&7Transform into a");
				list.add("&7scary purple");
				list.add("&7eyed monster");
				break;
			case POLAR_BEAR:
				list.add("&7Brrrrrrrrr");
				break;
			case SKELETON:
				list.add("&7The most scary morph");
				break;
			case SPIDER:
				list.add("&7Crawl around on");
				list.add("&7eight legs");
				break;
			case TURTLE:
				list.add("&7Whether you like sea life");
				list.add("&7or just want to be a cute");
				list.add("&7little turtle, this morph");
				list.add("&7is for you!");
				break;
			case ZOMBIE:
				list.add("&7The plain original");
				list.add("&7morph. A zombie.");
				break;
			default:
				list.add("&7No Description");
				list.add("&7Tell an admin if");
				list.add("&7you are seeing this");
		}
		
		return list;
	}
	
}