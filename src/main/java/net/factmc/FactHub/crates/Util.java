package net.factmc.FactHub.crates;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Llama;
import org.bukkit.entity.Cat;
import org.bukkit.entity.Parrot;
import org.bukkit.entity.Rabbit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;

import net.factmc.FactCore.CoreUtils;
import net.factmc.FactCore.bukkit.InventoryControl;
import net.factmc.FactHub.Data;
import net.factmc.FactHub.Main;
import net.factmc.FactHub.cosmetics.UpdateSuits;
import net.factmc.FactHub.gui.cosmetics.MorphsGUI;
import net.factmc.FactHub.gui.cosmetics.PetsGUI;
import net.factmc.FactHub.gui.cosmetics.TrailsGUI;
import net.factmc.FactHub.gui.select.CatColorGUI;
import net.factmc.FactHub.gui.select.HorseColorGUI;
import net.factmc.FactHub.gui.select.HorseStyleGUI;
import net.factmc.FactHub.gui.select.LlamaColorGUI;
import net.factmc.FactHub.gui.select.ParrotColorGUI;
import net.factmc.FactHub.gui.select.RabbitColorGUI;

public class Util {
	
	public static final NamespacedKey TYPE_KEY = new NamespacedKey(Main.getPlugin(), "type");
	public static final NamespacedKey ID_KEY = new NamespacedKey(Main.getPlugin(), "id");
	
	public static List<ItemStack> allItems = new ArrayList<ItemStack>();
	public static List<ItemStack> rareItems = new ArrayList<ItemStack>();
	public static List<ItemStack> legendItems = new ArrayList<ItemStack>();
	
	
	public static List<ItemStack> getAllItems() {
		FileConfiguration valueData = Data.getValueData();
		FileConfiguration cloakData = Data.getCloakData();
		FileConfiguration suitData = Data.getSuitData();
		
		List<ItemStack> items = new ArrayList<ItemStack>();
		for (String path : valueData.getConfigurationSection("values").getKeys(true)) {
			String[] nodes = path.split("[.]");
			if (nodes.length >= 2) {
				
				int value = valueData.getInt("values." + path);
				if (value < 1 || value > 5) {
					Main.getPlugin().getLogger().warning("The value for " + path + ": " + value + ", is not valid");
				}
				
				else {
				
					ItemStack stack;
					String color;
					switch (nodes[0]) {
					
					case "suit-colors":
						Color suitColor = UpdateSuits.getColor(nodes[1]);
						stack = InventoryControl.getDyeByColor(suitColor);
						color = InventoryControl.getChatColorByColor(suitColor) + "" + ChatColor.BOLD;
						
						stack = InventoryControl.getItemStack(stack,
								color + CoreUtils.underscoreToSpace(nodes[1]),
								getNameByValue(value, valueData),
								"&7Suit Color");
						break;
					
					case "dye-colors":
						String dyeName;
						if (nodes[1].equalsIgnoreCase("RAINBOW")) {
							stack = new ItemStack(Material.MAGMA_CREAM, 1);
							dyeName = "&c&lR&6&la&e&li&a&ln&b&lb&d&lo&9&lw";
						}
						else {
							DyeColor dyeColor = DyeColor.valueOf(nodes[1]);
							stack = new ItemStack(PetsGUI.getWoolMaterial(dyeColor), 1);
							color = PetsGUI.getChatColor(dyeColor) + "" + ChatColor.BOLD;
							dyeName = color + CoreUtils.underscoreToSpace(nodes[1]);
						}
						
						stack = InventoryControl.getItemStack(stack,
								dyeName,
								getNameByValue(value, valueData),
								"&7Dye Color", "&7Sheep Wool, Wolf Collars", "&7and Llama Carpets");
						break;
						
					case "horse-colors":
						Horse.Color horseColor = Horse.Color.valueOf(nodes[1]);
						stack = HorseColorGUI.getStack(horseColor);
						color = HorseColorGUI.getChatColor(horseColor) + "" + ChatColor.BOLD;
						
						stack = InventoryControl.getItemStack(stack,
								color + CoreUtils.underscoreToSpace(nodes[1]),
								getNameByValue(value, valueData),
								"&7Horse Color");
						break;
						
					case "horse-styles":
						Horse.Style horseStyle = Horse.Style.valueOf(nodes[1]);
						stack = HorseStyleGUI.getStack(horseStyle);
						color = HorseStyleGUI.getChatColor(horseStyle) + "" + ChatColor.BOLD;
						
						stack = InventoryControl.getItemStack(stack,
								color + CoreUtils.underscoreToSpace(nodes[1]),
								getNameByValue(value, valueData),
								"&7Horse Style");
						break;
						
					case "llama-colors":
						Llama.Color llamaColor = Llama.Color.valueOf(nodes[1]);
						stack = LlamaColorGUI.getStack(llamaColor);
						color = LlamaColorGUI.getChatColor(llamaColor) + "" + ChatColor.BOLD;
						
						stack = InventoryControl.getItemStack(stack,
								color + CoreUtils.underscoreToSpace(nodes[1]),
								getNameByValue(value, valueData),
								"&7Llama Color");
						break;
						
					case "parrot-colors":
						Parrot.Variant parrotColor = Parrot.Variant.valueOf(nodes[1]);
						stack = ParrotColorGUI.getStack(parrotColor);
						color = ParrotColorGUI.getChatColor(parrotColor) + "" + ChatColor.BOLD;
						
						stack = InventoryControl.getItemStack(stack,
								color + CoreUtils.underscoreToSpace(nodes[1]),
								getNameByValue(value, valueData),
								"&7Parrot Color");
						break;
						
					case "cat-colors":
						Cat.Type catColor = Cat.Type.valueOf(nodes[1]);
						stack = CatColorGUI.getStack(catColor);
						color = CatColorGUI.getChatColor(catColor) + "" + ChatColor.BOLD;
						
						stack = InventoryControl.getItemStack(stack,
								color + CoreUtils.underscoreToSpace(nodes[1]),
								getNameByValue(value, valueData),
								"&7Cat Color");
						break;
					
					case "rabbit-colors":
						Rabbit.Type rabbitColor = Rabbit.Type.valueOf(nodes[1]);
						stack = RabbitColorGUI.getStack(rabbitColor);
						color = RabbitColorGUI.getChatColor(rabbitColor) + "" + ChatColor.BOLD;
						
						stack = InventoryControl.getItemStack(stack,
								color + CoreUtils.underscoreToSpace(nodes[1]),
								getNameByValue(value, valueData),
								"&7Rabbit Color");
						break;
						
					case "pets":
						EntityType type = EntityType.valueOf(nodes[1]);
						stack = PetsGUI.getStack(type);
						
						stack = InventoryControl.getItemStack(stack,
								PetsGUI.getName(type),
								getNameByValue(value, valueData),
								"&7Pet");
						break;
						
					case "morphs":
						EntityType morph = EntityType.valueOf(nodes[1]);
						stack = MorphsGUI.getStack(morph);
						
						stack = InventoryControl.getItemStack(stack,
								MorphsGUI.getName(morph),
								getNameByValue(value, valueData),
								"&7Morph");
						break;
						
					case "trails":
						String trail = nodes[1].toUpperCase();
						stack = TrailsGUI.getStack(trail);
						
						stack = InventoryControl.getItemStack(stack,
								TrailsGUI.getName(trail),
								getNameByValue(value, valueData),
								"&7Trail");
						break;
						
					case "cloaks":
						String cloakName = cloakData.getString("cloaks." + nodes[1].toLowerCase() + ".name");
						String cloakMaterial = cloakData.getString("cloaks." + nodes[1].toLowerCase() + ".item");
						stack = new ItemStack(Material.valueOf(cloakMaterial), 1);
						
						stack = InventoryControl.getItemStack(stack,
								cloakName,
								getNameByValue(value, valueData),
								"&7Cloak");
						break;
						
					case "suits":
						String suitName = suitData.getString("suits." + nodes[1].toLowerCase() + ".name");
						String suitMaterial = suitData.getString("suits." + nodes[1].toLowerCase() + ".item");
						stack = new ItemStack(Material.valueOf(suitMaterial), 1);
						
						stack = InventoryControl.getItemStack(stack,
								suitName,
								getNameByValue(value, valueData),
								"&7Suit");
						break;
						
					default:
						stack = null;
						
					}
					
					if (stack != null) {
						ItemMeta meta = stack.getItemMeta();
						((PersistentDataHolder) meta).getPersistentDataContainer().set(TYPE_KEY, PersistentDataType.STRING, nodes[0]);
						((PersistentDataHolder) meta).getPersistentDataContainer().set(ID_KEY, PersistentDataType.STRING, nodes[1]);
						stack.setItemMeta(meta);
					}
					items.add(stack);
					
				}
			
			}
		}
		
		for (String value : valueData.getConfigurationSection("value").getKeys(false)) {
			String valuePath = "value." + value + ".cost";
			
			int valueInt = Integer.parseInt(value);
			if (valueInt > 0) {
				
				int cost = valueData.getInt(valuePath);
				ItemStack pointStack = InventoryControl.getItemStack(Material.SUNFLOWER,
						"&6" + cost/2 + " Points",
						getNameByValue(valueInt, valueData),
						"&7Point Pack");
				items.add(pointStack);
				
			}
		}
		
		allItems.clear();
		for (ItemStack item : items) {
			int value = getValueByName(item.getItemMeta().getLore().get(0), valueData);
			int amount = (value - 6) * -1;
			for (int i = 0; i < amount; i++) {
				allItems.add(item);
			}
		}
		
		rareItems.clear();
		for (ItemStack item : items) {
			int value = getValueByName(item.getItemMeta().getLore().get(0), valueData);
			int amount = 0;
			switch (value) {
			
			case 1: amount = 2; break;
			case 2: amount = 2; break;
			case 3: amount = 6; break;
			case 4: amount = 4; break;
			case 5: amount = 2; break;
			
			}
			
			for (int i = 0; i < amount; i++) {
				rareItems.add(item);
			}
		}
		
		legendItems.clear();
		for (ItemStack item : items) {
			int value = getValueByName(item.getItemMeta().getLore().get(0), valueData);
			int amount = 0;
			switch (value) {
			
			case 1: amount = 1; break;
			case 2: amount = 2; break;
			case 3: amount = 5; break;
			case 4: amount = 6; break;
			case 5: amount = 4; break;

			}
			
			for (int i = 0; i < amount; i++) {
				legendItems.add(item);
			}
		}
		
		return allItems;
	}
	
	public static String[] getPath(ItemStack item) {
		List<String> lore = item.getItemMeta().getLore();
		String name = item.getItemMeta().getDisplayName();
		if (lore == null || name == null) return null;
		if (lore.size() < 2) return null;
		
		FileConfiguration suitData = Data.getSuitData();
		FileConfiguration cloakData = Data.getCloakData();
		
		String type = lore.get(1).replaceAll(InventoryControl.convertColors("&7"), "");
		String path;
		String value;
		switch (type) {
		
		case "Suit Color":
			path = "SUITCOLORS";
			value = ChatColor.stripColor(name).replaceAll(" ", "_").toUpperCase();
			break;
		case "Dye Color":
			path = "DYE_COLORS";
			value = ChatColor.stripColor(name).replaceAll(" ", "_").toUpperCase();
			break;
		case "Horse Color":
			path = "HORSE_COLORS";
			value = ChatColor.stripColor(name).replaceAll(" ", "_").toUpperCase();
			break;
		case "Horse Style":
			path = "HORSE_STYLES";
			value = ChatColor.stripColor(name).replaceAll(" ", "_").toUpperCase();
			break;
		case "Llama Color":
			path = "LLAMA_COLORS";
			value = ChatColor.stripColor(name).replaceAll(" ", "_").toUpperCase();
			break;
		case "Parrot Color":
			path = "PARROT_COLORS";
			value = ChatColor.stripColor(name).replaceAll(" ", "_").toUpperCase();
			break;
		case "Cat Color":
			path = "CAT_COLORS";
			value = ChatColor.stripColor(name).replaceAll(" ", "_").toUpperCase();
			break;
		case "Pet":
			path = "PETS";
			value = null;
			for (EntityType pet : PetsGUI.getUseableEntities()) {
				String petName = PetsGUI.getName(pet);
				petName = ChatColor.translateAlternateColorCodes('&', petName);
				if (ChatColor.stripColor(petName).equalsIgnoreCase(ChatColor.stripColor(name))) {
					value = String.valueOf(pet);
				}
			}
			break;
		case "Morph":
			path = "MORPHS";
			value = null;
			for (EntityType morph : MorphsGUI.getUseableEntities()) {
				String morphName = MorphsGUI.getName(morph);
				morphName = ChatColor.translateAlternateColorCodes('&', morphName);
				if (ChatColor.stripColor(morphName).equalsIgnoreCase(ChatColor.stripColor(name))) {
					value = String.valueOf(morph).toUpperCase();
				}
			}
			break;
		case "Trail":
			path = "TRAILS";
			value = null;
			for (String trail : TrailsGUI.getUseableParticles()) {
				String trailName = TrailsGUI.getName(trail);
				trailName = ChatColor.translateAlternateColorCodes('&', trailName);
				if (ChatColor.stripColor(trailName).equalsIgnoreCase(ChatColor.stripColor(name))) {
					value = String.valueOf(trail);
				}
			}
			break;
		case "Cloak":
			path = "CLOAKS";
			value = null;
			for (String cloak : cloakData.getConfigurationSection("cloaks").getKeys(false)) {
				String cloakName = cloakData.getString("cloaks." + cloak + ".name");
				cloakName = ChatColor.translateAlternateColorCodes('&', cloakName);
				if (ChatColor.stripColor(cloakName).equalsIgnoreCase(ChatColor.stripColor(name))) {
					value = cloak.toUpperCase();
				}
			}
			break;
		case "Suit":
			path = "SUITS";
			value = null;
			for (String suit : suitData.getConfigurationSection("suits").getKeys(false)) {
				String suitName = suitData.getString("suits." + suit + ".name");
				suitName = ChatColor.translateAlternateColorCodes('&', suitName);
				if (ChatColor.stripColor(suitName).equalsIgnoreCase(ChatColor.stripColor(name))) {
					value = suit.toUpperCase();
				}
			}
			break;
		case "Point Pack":
			path = "null-points";
			String amount = ChatColor.stripColor(name).replaceAll(" Points", "");
			value = amount;
			break;
		default:
			path = null;
			value = null;
		
		}
		
		String[] result = {path, value};
		return result;
	}
	
	@Deprecated
	public static boolean addCosmetic(FileConfiguration playerData, File file, String path, String value) {
		if (playerData == null) return false;
		if (!playerData.contains(path)) return false;
		
		value = value.toUpperCase();
		List<String> values = playerData.getStringList(path);
		if (values.contains(value)) return false;
		
		values.add(value);
		playerData.set(path, values);
		Data.saveConfig(file, playerData);
		return true;
	}
	
	public static String getNameByValue(int value, FileConfiguration valueData) {
		String name = valueData.getString("value." + value + ".name");
		name = ChatColor.translateAlternateColorCodes('&', name);
		return name;
	}
	
	public static int getValueByName(String name, FileConfiguration valueData) {
		for (String path : valueData.getConfigurationSection("value").getKeys(false)) {
			String valueName = valueData.getString("value." + path + ".name");
			valueName = ChatColor.translateAlternateColorCodes('&', valueName);
			if (name.equalsIgnoreCase(valueName)) {
				return Integer.parseInt(path);
			}
		}
		return 0;
	}
	
	static ItemStack randomItem(int level) {
		List<ItemStack> list;
		if (level == 1) list = allItems;
		else if (level == 2) list = rareItems;
		else list = legendItems;
		
		int rand = new Random().nextInt(list.size());
		ItemStack item = list.get(rand).clone();
		
		//DEBUG
		ItemMeta meta = item.getItemMeta();
		List<String> lore = meta.getLore();
		lore.add(((PersistentDataHolder) meta).getPersistentDataContainer().get(TYPE_KEY, PersistentDataType.STRING));
		lore.add(((PersistentDataHolder) meta).getPersistentDataContainer().get(ID_KEY, PersistentDataType.STRING));
		meta.setLore(lore); item.setItemMeta(meta);
		
		return item;
	}
	
	static ItemStack randomPane() {
		final Material[] opt = new Material[] {
				Material.ORANGE_STAINED_GLASS_PANE,
				Material.MAGENTA_STAINED_GLASS_PANE,
				Material.LIGHT_BLUE_STAINED_GLASS_PANE,
				Material.YELLOW_STAINED_GLASS_PANE,
				Material.LIME_STAINED_GLASS_PANE,
				Material.PINK_STAINED_GLASS_PANE
		};
		int max = opt.length - 1;
		int rand = new Random().nextInt(max + 1);
		
		ItemStack pane = new ItemStack(opt[rand], 1);
		return pane;
	}
	
	public static int randInt(int min, int max) {
		int rand = new Random().nextInt((max - min) + 1) + min;
		return rand;
	}
	
}
