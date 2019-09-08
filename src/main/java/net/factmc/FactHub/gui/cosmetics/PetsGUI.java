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
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import de.myzelyam.api.vanish.VanishAPI;
import net.factmc.FactHub.Data;
import net.factmc.FactCore.CoreUtils;
import net.factmc.FactCore.FactSQLConnector;
import net.factmc.FactCore.bukkit.InventoryControl;
import net.factmc.FactHub.Main;
import net.factmc.FactHub.cosmetics.Pet;
import net.factmc.FactHub.crates.Util;
import net.factmc.FactHub.gui.ConfirmGUI;
import net.factmc.FactHub.gui.CosmeticsGUI;
import net.factmc.FactHub.gui.select.CatColorGUI;
import net.factmc.FactHub.gui.select.DyeColorGUI;
import net.factmc.FactHub.gui.select.HorseColorGUI;
import net.factmc.FactHub.gui.select.HorseStyleGUI;
import net.factmc.FactHub.gui.select.LlamaColorGUI;
import net.factmc.FactHub.gui.select.ParrotColorGUI;
import net.factmc.FactHub.gui.select.RabbitColorGUI;
import net.factmc.FactHub.listeners.PetManager;

public class PetsGUI implements Listener {
	
	private static boolean loaded = false;
	private List<Player[]> changingName = new ArrayList<Player[]>();
	
	public static void open(Player player, String name) {
		double count = (double) getUseableEntities().size() / (double) 7;
		int i = 0;
		while (i < count) {
			i++;
		}
		int length = (4 + i) * 9;
		
		Inventory gui = player.getServer().createInventory(player, length, ChatColor.DARK_GREEN + "Pets");
		
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
		String savedType = Data.getSelected(uuid, "PET");
		List<String> availableTypes = Data.getAccess(uuid, "PETS");
		for (EntityType type : getUseableEntities()) {
			//player.sendMessage("Loading " + type.name() + " ...");//DEBUG
			
			String available = "&cNot Available";
			ItemStack stack = new ItemStack(Material.GUNPOWDER, 1);
			
			if (availableTypes.contains(String.valueOf(type)) || player.hasPermission("facthub.cosmetics.access-all")) {
				available = "&bAvailable";
				stack = getStack(type);
			}
			if (savedType.equalsIgnoreCase(String.valueOf(type))) {
				available = "&a&oActive";
				ItemMeta meta = stack.getItemMeta();
				meta.addEnchant(Enchantment.ARROW_INFINITE, -1, true);
				meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
				stack.setItemMeta(meta);
			}
			
			String petName = getName(type);
			
			List<String> lore = getDescription(type);
			
			FileConfiguration valueData = Data.getValueData();
			int value = valueData.getInt("values.pets." + String.valueOf(type).toUpperCase());
			String valueName = Util.getNameByValue(value, valueData);
			int cost = valueData.getInt("value." + value + ".cost");
			
			lore.add("");
			lore.add(valueName);
			lore.add("");
			lore.add(available);
			if (available.equalsIgnoreCase("&cNot Available") && cost > -1) {
				lore.add("&7Click to buy for " + cost + " points");
			}
			
			ItemStack pickStack = InventoryControl.getItemStack(stack, petName, lore);
			gui.setItem(i, pickStack);
			
			/*if (type == EntityType.RABBIT) {
				player.sendMessage(stack.getType().toString());
				player.sendMessage(petName);
				player.sendMessage("Lore:");
				for (String line : lore) {
					player.sendMessage("  " + line);
				}
				player.sendMessage("Placed item in slot " + i);//DEBUG
			}*/
			
			if ((i-7) % 9 == 0) i += 3;
			else i++;
			
			//player.sendMessage("Done.");//DEBUG
			
		}
		
		Pet pet = PetManager.getPet(Bukkit.getPlayerExact(name));
		
		String currentType = Data.getSelected(uuid, "PET").toUpperCase();
		if (!currentType.equalsIgnoreCase("NONE")) {
			
			//player.sendMessage("Loading Extra Options...");//DEBUG
			
			EntityType type = EntityType.valueOf(currentType);
			if (type != EntityType.IRON_GOLEM && type != EntityType.PARROT) {
				boolean baby = Data.getBoolean(uuid, "PETBABY");
				String babyTxt = "&c&lFALSE";
				if (baby) babyTxt = "&a&lTRUE";
				
				ItemStack pickAge = InventoryControl.getItemStack(Material.GOLDEN_APPLE, "&bChange Age",
						"&7Choose whether or not your", "&7pet should be a baby", "", "&7Baby: "
						+ babyTxt);
				gui.setItem(length - 12, pickAge);
			}
			
			String cName = null;
			List<String> cLore = new ArrayList<String>();
			Material cMat = null;
			int cLoc = 13;
			
			if (type == EntityType.HORSE) {
				ItemStack pickStyle = InventoryControl.getItemStack(Material.LEATHER, "&dChange Horse Style",
						"&7Pick a pattern for your horse", "", "&7Current Style: " + Data.getSelected(uuid, "PETSTYLE"));
				gui.setItem(length - 20, pickStyle);
				
				cName = "&aChange Horse Color";
				cMat = Material.INK_SAC;
				
				cLore.add("&7Pick your horse's color");
				cLore.add("");
				cLore.add("&7Current Color: " + Data.getSelected(uuid, "PETCOLOR"));
			}
			else if (type == EntityType.LLAMA) {
				String savedColor = Data.getSelected(uuid, "PETSTYLE");
				String colorTxt;
				ItemStack carpetMat;
				if (savedColor.equalsIgnoreCase("RAINBOW")) {
					colorTxt = "&c&lR&6&la&e&li&a&ln&b&lb&d&lo&9&lw";
					carpetMat = new ItemStack(Material.ORANGE_GLAZED_TERRACOTTA, 1);
				}
				else {
					DyeColor color = DyeColor.valueOf(savedColor);
					colorTxt = "" + getChatColor(color) + ChatColor.BOLD + CoreUtils.underscoreToSpace(String.valueOf(color).toLowerCase());
					carpetMat = new ItemStack(getCarpetMaterial(color), 1);
				}
				ItemStack pickCarpet = InventoryControl.getItemStack(carpetMat, "&dChange Carpet Color",
						"&7Pick a color for the", "&7carpet on your llama", "",
						"&7Current Color: " + colorTxt);
				gui.setItem(length - 20, pickCarpet);
				
				cName = "&aChange Llama Color";
				cMat = Material.MILK_BUCKET;
				
				cLore.add("&7Pick your llama's color");
				cLore.add("");
				cLore.add("&7Current Color: " + Data.getSelected(uuid, "PETCOLOR"));
			}
			else if (type == EntityType.SHEEP) {
				cName = "&aChange Wool Color";
				cMat = Material.WHITE_WOOL;
				
				cLore.add("&7Pick a color for the wool");
				cLore.add("&7of your sheep");
				cLore.add("");
				
				String savedColor = Data.getSelected(uuid, "PETCOLOR");
				String colorTxt;
				if (savedColor.equalsIgnoreCase("RAINBOW")) {
					colorTxt = "&c&lR&6&la&e&li&a&ln&b&lb&d&lo&9&lw";
				}
				else {
					DyeColor color = DyeColor.valueOf(savedColor);
					colorTxt = "" + getChatColor(color) + ChatColor.BOLD + CoreUtils.underscoreToSpace(String.valueOf(color).toLowerCase());
				}
				cLore.add("&7Current Color: " + colorTxt);
			}
			else if (type == EntityType.PARROT) {
				cName = "&aChange Parrot Color";
				cMat = Material.FEATHER;
				cLoc = 12;
				
				cLore.add("&7Pick your parrot's color");
				cLore.add("");
				cLore.add("&7Current Color: " + Data.getSelected(uuid, "PETCOLOR"));
			}
			else if (type == EntityType.WOLF) {
				cName = "&aChange Collar Color";
				cMat = Material.REDSTONE;
				
				cLore.add("&7Pick your wolf's collar color");
				cLore.add("");
				
				String savedColor = Data.getSelected(uuid, "PETCOLOR");
				String colorTxt;
				if (savedColor.equalsIgnoreCase("RAINBOW")) {
					colorTxt = "&c&lR&6&la&e&li&a&ln&b&lb&d&lo&9&lw";
				}
				else {
					DyeColor color = DyeColor.valueOf(savedColor);
					colorTxt = "" + getChatColor(color) + ChatColor.BOLD + CoreUtils.underscoreToSpace(String.valueOf(color).toLowerCase());
				}
				cLore.add("&7Current Color: " + colorTxt);
			}
			else if (type == EntityType.CAT) {
				cName = "&aChange Cat Color";
				cMat = Material.SLIME_BALL;
				
				cLore.add("&7Pick a type of cat");
				cLore.add("");
				cLore.add("&7Current Color: " + Data.getSelected(uuid, "PETCOLOR"));
			}
			else if (type == EntityType.RABBIT) {
				cName = "&aChange Rabbit Color";
				cMat = Material.COOKED_RABBIT;
				
				cLore.add("&7Pick a color for you rabbit");
				cLore.add("");
				cLore.add("&7Current Color: " + Data.getSelected(uuid, "PETCOLOR"));
			}
			
			if (cName != null) {
				ItemStack pickColor = InventoryControl.getItemStack(cMat, cName, cLore);
				gui.setItem(length - cLoc, pickColor);
			}
			
			//player.sendMessage("Done.");//DEBUG
			
		}
		
		//player.sendMessage("Adding General Options...");//DEBUG
		
		String currentName = Data.getSelected(uuid, "PETNAME");
		if (currentName.equalsIgnoreCase("NONE")) currentName = "&7&oDefault";
		
		ItemStack pickName = InventoryControl.getItemStack(Material.NAME_TAG, "&eChange Name",
				"&7Pick a name for your pet", "", "&7Current Name: &r" + currentName);
		gui.setItem(length - 11, pickName);
		
		String toggleTxt = "&c&lFALSE";
		if (pet != null)
			if (pet.getNPC() != null && pet.getNPC().isSpawned()) toggleTxt = "&a&lTRUE";
		if (VanishAPI.isInvisible(pet.getOwner())) toggleTxt += " &8&o(VANISHED)";
		
		ItemStack toggle = InventoryControl.getItemStack(Material.NETHER_STAR, "&eSpawn / Despawn",
				"&7Instantly spawn or", "&7despawn your pet", "", "&7Spawned: " + toggleTxt);
		gui.setItem(length - 17, toggle);
		
		String spawns;
		if (Data.getBoolean(uuid, "PETAUTOSPAWN")) spawns = "&a&lON";
		else spawns = "&c&lOFF";
		
		ItemStack autoSpawn = InventoryControl.getItemStack(Material.DRAGON_EGG, "&eToggle Auto-Spawn",
				"&7Spawns pet when you join", "", "&7Auto Spawn: " + spawns);
		gui.setItem(length - 16, autoSpawn);
		
		ItemStack reset = InventoryControl.getItemStack(Material.BARRIER, "&cReset Pet Type", "&7Disable your pet");
		gui.setItem(length - 14, reset);
		ItemStack back = InventoryControl.getItemStack(Material.ARROW, "&6Back", "&7Return to parent menu");
		gui.setItem(length - 15, back);
		
		//player.sendMessage("Opening GUI");//DEBUG
		loaded = true;
		player.openInventory(gui);
	}
	
	@EventHandler
	public void itemClicked(InventoryClickEvent event) {
		if (!loaded) return;
		final Player player = (Player) event.getWhoClicked();
		if (event.getView().getTitle().equalsIgnoreCase(ChatColor.DARK_GREEN + "Pets")) {
			
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
			
			Pet pet = PetManager.getPet(Bukkit.getPlayerExact(playerName));
			if (pet == null) return;
			
			if (name.equalsIgnoreCase(InventoryControl.convertColors("&cReset Pet Type"))) {
				pet.changeType("NONE");
				open(player, playerName);
			}
			
			else if (name.equalsIgnoreCase(InventoryControl.convertColors("&eChange Name"))) {
				final Player[] array = {player, pet.getOwner()};
				changingName.add(array);
				player.sendMessage(ChatColor.GOLD + "Enter a new name in chat: (Use =cancel to cancel)");
				player.closeInventory();
				
				Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), new Runnable() {

					@Override
					public void run() {
						if (changingName.contains(array)) {
							changingName.remove(array);
							player.sendMessage(ChatColor.RED + "Time expired, entry cancelled");
						}
					}
					
				}, 2400);
			}
			
			else if (item.getItemMeta().getDisplayName().equalsIgnoreCase(InventoryControl.convertColors("&eToggle Auto-Spawn"))) {
				if (pet.autoSpawns()) {
					pet.setAutoSpawn(false);
				}
				else {
					pet.setAutoSpawn(true);
				}
				open(player, playerName);
			}
			
			else if (name.equalsIgnoreCase(InventoryControl.convertColors("&eSpawn / Despawn"))) {
				if (pet.getNPC().isSpawned()) {
					pet.despawn();
				}
				else if (!VanishAPI.isInvisible(pet.getOwner())) {
					pet.spawn(null, false);
				}
				else return;
				open(player, playerName);
			}
			
			else if (name.equalsIgnoreCase(InventoryControl.convertColors("&bChange Age"))) {
				boolean isBaby = Data.getBoolean(uuid, "PETBABY");
				Data.setBoolean(uuid, "PETBABY", !isBaby);
				pet.respawn();
				open(player, playerName);
			}
			
			else if (name.equalsIgnoreCase(InventoryControl.convertColors("&dChange Horse Style"))) {
				HorseStyleGUI.open(player, playerName);
			}
			
			else if (name.equalsIgnoreCase(InventoryControl.convertColors("&dChange Carpet Color"))) {
				DyeColorGUI.open(player, playerName, "LLAMA");
			}
			
			else if (name.equalsIgnoreCase(InventoryControl.convertColors("&aChange Horse Color"))) {
				HorseColorGUI.open(player, playerName);
			}
			
			else if (name.equalsIgnoreCase(InventoryControl.convertColors("&aChange Llama Color"))) {
				LlamaColorGUI.open(player, playerName);
			}
			
			else if (name.equalsIgnoreCase(InventoryControl.convertColors("&aChange Wool Color"))) {
				DyeColorGUI.open(player, playerName, "SHEEP");
			}
			
			else if (name.equalsIgnoreCase(InventoryControl.convertColors("&aChange Parrot Color"))) {
				ParrotColorGUI.open(player, playerName);
			}
			
			else if (name.equalsIgnoreCase(InventoryControl.convertColors("&aChange Collar Color"))) {
				DyeColorGUI.open(player, playerName, "WOLF");
			}
			
			else if (name.equalsIgnoreCase(InventoryControl.convertColors("&aChange Cat Color"))) {
				CatColorGUI.open(player, playerName);
			}
			
			else if (name.equalsIgnoreCase(InventoryControl.convertColors("&aChange Rabbit Color"))) {
				RabbitColorGUI.open(player, playerName);
			}
			
			
			else {
				if (item.getType() == Material.GUNPOWDER) {
					for (EntityType type : getUseableEntities()) {
						String typeName = ChatColor.translateAlternateColorCodes('&',
								getName(type));
						if (ChatColor.stripColor(typeName).equalsIgnoreCase(ChatColor.stripColor(name))) {
							FileConfiguration valueData = Data.getValueData();
							int value = valueData.getInt("values.pets." + String.valueOf(type).toUpperCase());
							int cost = valueData.getInt("value." + value + ".cost");
							
							if (FactSQLConnector.getPoints(uuid) >= cost) {
								ItemStack stack = InventoryControl.getItemStack(getStack(type), getName(type));
								ConfirmGUI.open(player, playerName, stack, cost, "PETS", String.valueOf(type).toUpperCase());
							}
							break;
						}
					}
				}
				
				else {
					for (EntityType type : getUseableEntities()) {
						String typeName = getName(type);
						
						if (ChatColor.stripColor(name).equalsIgnoreCase(ChatColor.stripColor(InventoryControl.convertColors(typeName)))) {
							pet.changeType(String.valueOf(type).toUpperCase());
							open(player, playerName);
						}
					}
				}
				
			}
			
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onNameSetting(AsyncPlayerChatEvent event) {
		final Player player = event.getPlayer();
		Player[] remove = null;
		for (final Player[] array : changingName) {
			
			if (array[0] == player) {
				event.setCancelled(true);
				String msg = event.getMessage();
				
				Pet pet = PetManager.getPet(array[1]);
				if (pet == null) {
					player.sendMessage(ChatColor.RED + "Error, please try again");
				}
				
				else if (msg.contains("jeb_")) {
					player.sendMessage(ChatColor.RED + "That name is not allowed");
				}
				
				else if (!msg.equalsIgnoreCase("=cancel")) {
					if (msg.length() > 16) {
						player.sendMessage(ChatColor.RED + "Your pet's name cannot be longer than 16 characters!");
					}
					else {
						pet.changeName(msg);
						
						Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), new Runnable() {
							
							@Override
							public void run() {
								open(player, array[1].getName());
							}
						});
					}
					
				}
				
				else player.sendMessage(ChatColor.RED + "Name change cancelled");

				remove = array;
				break;
			}
			
		}
		if (remove != null) {
			changingName.remove(remove);
		}
	}
	
	public static List<EntityType> getUseableEntities() {
		List<EntityType> list = new ArrayList<EntityType>();
		list.add(EntityType.CHICKEN);
		list.add(EntityType.COW);
		list.add(EntityType.DONKEY);
		list.add(EntityType.HORSE);
		list.add(EntityType.IRON_GOLEM);
		list.add(EntityType.LLAMA);
		list.add(EntityType.MUSHROOM_COW);
		list.add(EntityType.CAT);
		list.add(EntityType.PARROT);
		list.add(EntityType.PIG);
		list.add(EntityType.SHEEP);
		list.add(EntityType.WOLF);
		list.add(EntityType.RABBIT);
		list.add(EntityType.FOX);
		
		return list;
	}
	
	public static ItemStack getStack(EntityType type) {
		ItemStack stack;
		
		switch (type) {
			case CHICKEN: stack = new ItemStack(Material.CHICKEN, 1); break;
			case COW: stack = new ItemStack(Material.BEEF, 1); break;
			case DONKEY: stack = new ItemStack(Material.RABBIT, 1); break;
			case HORSE: stack = new ItemStack(Material.HAY_BLOCK, 1); break;
			case IRON_GOLEM: stack = new ItemStack(Material.PUMPKIN, 1); break;
			case LLAMA: stack = new ItemStack(Material.RABBIT_FOOT, 1); break;
			case MUSHROOM_COW: stack = new ItemStack(Material.RED_MUSHROOM, 1); break;
			case CAT: stack = new ItemStack(Material.COD, 1); break;
			case PARROT: stack = new ItemStack(Material.FEATHER, 1); break;
			case PIG: stack = new ItemStack(Material.PORKCHOP, 1); break;
			case SHEEP: stack = new ItemStack(Material.WHITE_WOOL, 1); break;
			case WOLF: stack = new ItemStack(Material.BONE, 1); break;
			case RABBIT: stack = new ItemStack(Material.CARROT, 1); break;
			case FOX: stack = new ItemStack(Material.SWEET_BERRIES, 1); break;
			default: stack = new ItemStack(Material.GUNPOWDER, 1);
		}
		
		return stack;
	}
	
	public static String getName(EntityType type) {
		String name;
		
		switch (type) {
			case CHICKEN: name = "&rChicken"; break;
			case COW: name = "&6Cow"; break;
			case DONKEY: name = "&9Donkey"; break;
			case HORSE: name = "&6Horse"; break;
			case IRON_GOLEM: name = "&rIron Golem"; break;
			case LLAMA: name = "&eLlama"; break;
			case MUSHROOM_COW: name = "&cMooshroom"; break;
			case CAT: name = "&eCat"; break;
			case PARROT: name = "&aParrot"; break;
			case PIG: name = "&dPig"; break;
			case SHEEP: name = "&3Sheep"; break;
			case WOLF: name = "&bWolf"; break;
			case RABBIT: name = "&8Rabbit"; break;
			case FOX: name = "&6Fox"; break;
			default: name = String.valueOf(type);
		}
		
		return name;
	}
	
	private static List<String> getDescription(EntityType type) {
		List<String> list = new ArrayList<String>();
		
		switch (type) {
			case CHICKEN:
				list.add("&7Cluck Cluck I'm a");
				list.add("&7Duck... Chicken?");
				break;
			case COW:
				list.add("&7Mooooooooooooo");
				break;
			case DONKEY:
				list.add("&7The Donkey pet can");
				list.add("&7be ridden!");
				break;
			case HORSE:
				list.add("&7Customizable colors,");
				list.add("&7styles, and rideable!");
				break;
			case IRON_GOLEM:
				list.add("&7Intimidate your friends");
				list.add("&7with the iron golem pet!");
				break;
			case LLAMA:
				list.add("&7It doesn't spit! And");
				list.add("&7it comes with customizable");
				list.add("&7color and carpet type");
				break;
			case MUSHROOM_COW:
				list.add("&7You don't need to find");
				list.add("&7an island to get one!");
				break;
			case CAT:
				list.add("&7Like cats? Then this is");
				list.add("&7for you! It comes with");
				list.add("&7customizable fur colors");
				break;
			case PARROT:
				list.add("&7The only flying pet");
				list.add("&7available! It also");
				list.add("&7comes with customizable");
				list.add("&7color patterns");
				break;
			case PIG:
				list.add("&7The owner's favorite!");
				list.add("&7Oink Oink Oink!");
				break;
			case SHEEP:
				list.add("&7Full choice of color,");
				list.add("&7including rainbow!");
				break;
			case WOLF:
				list.add("&7The original pet.");
				list.add("&7Customizable collar");
				list.add("&7color");
				break;
			case RABBIT:
				list.add("&7Hop hop hop!");
				list.add("&7A bunny with six");
				list.add("&7different colors!");
				break;
			case FOX:
				list.add("&7The brand new mob");
				list.add("&7added in 1.14!");
				list.add("&7It's so cute");
				break;
			default:
				list.add("&7No Description");
				list.add("&7Tell an admin if");
				list.add("&7you are seeing this");
		}
		
		return list;
	}
	
	public static Material getDyeMaterial(DyeColor color) {
		
		switch (color) {
			case BLACK: return Material.BLACK_DYE;
			case BLUE: return Material.BLUE_DYE;
			case BROWN: return Material.BROWN_DYE;
			case CYAN: return Material.CYAN_DYE;
			case GRAY: return Material.GRAY_DYE;
			case GREEN: return Material.GREEN_DYE;
			case LIGHT_BLUE: return Material.LIGHT_BLUE_DYE;
			case LIME: return Material.LIME_DYE;
			case MAGENTA: return Material.MAGENTA_DYE;
			case ORANGE: return Material.ORANGE_DYE;
			case PINK: return Material.PINK_DYE;
			case PURPLE: return Material.PURPLE_DYE;
			case RED: return Material.RED_DYE;
			case LIGHT_GRAY: return Material.LIGHT_GRAY_DYE;
			case WHITE: return Material.WHITE_DYE;
			case YELLOW: return Material.YELLOW_DYE;
			default: return Material.WHITE_DYE;
		}
	}
	
	public static Material getWoolMaterial(DyeColor color) {
		
		switch (color) {
			case BLACK: return Material.BLACK_WOOL;
			case BLUE: return Material.BLUE_WOOL;
			case BROWN: return Material.BROWN_WOOL;
			case CYAN: return Material.CYAN_WOOL;
			case GRAY: return Material.GRAY_WOOL;
			case GREEN: return Material.GREEN_WOOL;
			case LIGHT_BLUE: return Material.LIGHT_BLUE_WOOL;
			case LIME: return Material.LIME_WOOL;
			case MAGENTA: return Material.MAGENTA_WOOL;
			case ORANGE: return Material.ORANGE_WOOL;
			case PINK: return Material.PINK_WOOL;
			case PURPLE: return Material.PURPLE_WOOL;
			case RED: return Material.RED_WOOL;
			case LIGHT_GRAY: return Material.LIGHT_GRAY_WOOL;
			case WHITE: return Material.WHITE_WOOL;
			case YELLOW: return Material.YELLOW_WOOL;
			default: return Material.WHITE_WOOL;
		}
	}
	
	public static Material getCarpetMaterial(DyeColor color) {
		
		switch (color) {
			case BLACK: return Material.BLACK_CARPET;
			case BLUE: return Material.BLUE_CARPET;
			case BROWN: return Material.BROWN_CARPET;
			case CYAN: return Material.CYAN_CARPET;
			case GRAY: return Material.GRAY_CARPET;
			case GREEN: return Material.GREEN_CARPET;
			case LIGHT_BLUE: return Material.LIGHT_BLUE_CARPET;
			case LIME: return Material.LIME_CARPET;
			case MAGENTA: return Material.MAGENTA_CARPET;
			case ORANGE: return Material.ORANGE_CARPET;
			case PINK: return Material.PINK_CARPET;
			case PURPLE: return Material.PURPLE_CARPET;
			case RED: return Material.RED_CARPET;
			case LIGHT_GRAY: return Material.LIGHT_GRAY_CARPET;
			case WHITE: return Material.WHITE_CARPET;
			case YELLOW: return Material.YELLOW_CARPET;
			default: return Material.WHITE_CARPET;
		}
		
	}
	
	public static ChatColor getChatColor(DyeColor color) {
		
		switch (color) {
			case BLACK: return ChatColor.DARK_GRAY;
			case BLUE: return ChatColor.BLUE;
			case BROWN: return ChatColor.GOLD;
			case CYAN: return ChatColor.DARK_AQUA;
			case GRAY: return ChatColor.DARK_GRAY;
			case GREEN: return ChatColor.DARK_GREEN;
			case LIGHT_BLUE: return ChatColor.AQUA;
			case LIME: return ChatColor.GREEN;
			case MAGENTA: return ChatColor.LIGHT_PURPLE;
			case ORANGE: return ChatColor.GOLD;
			case PINK: return ChatColor.LIGHT_PURPLE;
			case PURPLE: return ChatColor.DARK_PURPLE;
			case RED: return ChatColor.RED;
			case LIGHT_GRAY: return ChatColor.GRAY;
			case WHITE: return ChatColor.WHITE;
			case YELLOW: return ChatColor.YELLOW;
			default: return ChatColor.RESET;
		}
	}
	
}