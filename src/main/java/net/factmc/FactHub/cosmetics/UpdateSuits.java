package net.factmc.FactHub.cosmetics;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;

import net.factmc.FactHub.Data;
import net.factmc.FactHub.Main;
import net.factmc.FactHub.listeners.PlayerManager;

public class UpdateSuits implements Runnable, Listener {
	
	private static List<String[]> suitData = new ArrayList<String[]>();
	
	private static void copyData() {
		suitData.clear();
		for (Player player : Bukkit.getOnlinePlayers()) {
			UUID uuid = player.getUniqueId();
			String[] data = Data.getSelected(uuid, "SUIT","SUITCOLOR","SUITHEAD");
			
			suitData.add(new String[] {uuid.toString(), data[0], data[1], data[2]});
		}
	}
	
	private static String[] getArray(UUID uuid) {
		for (String[] array : suitData) {
			UUID check = UUID.fromString(array[0]);
			if (check.equals(uuid)) {
				return array;
			}
		}
		return null;
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerJoin(PlayerJoinEvent event) {
		copyData();
	}
	
	

    // Main class for bukkit scheduling
    private static JavaPlugin plugin;
    
    // Our scheduled task's assigned id,needed for canceling
    private static Integer assignedTaskId;
    
    public static void start(JavaPlugin plugin) {
        // Initializing fields
        UpdateSuits.plugin = plugin;
        copyData();
        schedule();
    }
    
    private static int reload = 0;
    
    public void run() {
    	if (reload > 0) { copyData(); reload = 0; }
    	else reload++;
    	
    	FileConfiguration suitData = Data.getSuitData();
    	for (Player player : Bukkit.getOnlinePlayers()) {
    		String suit = getArray(player.getUniqueId())[1];
    		applySuit(suit, player, suitData);
    		
    		Location loc = player.getLocation();
    		if (loc.getBlock().getBiome() == Biome.BADLANDS
    				|| getBlockAbove(loc) == Material.AIR
    				|| loc.add(0,-1,0).getBlock().getType() == Material.RED_SANDSTONE) {
    			
    			boolean sealed = suitData.getBoolean("suits." + suit.toLowerCase() + ".sealed");
    			if (suit.equalsIgnoreCase("NONE")
    					|| !sealed) {
    				PlayerManager.lackOxygen(player);
    			}
    			
    		}
    	}
    }
    
    private Material getBlockAbove(Location loc) {
    	int x = loc.getBlockX();
    	int y = loc.getBlockY() + 1;
    	int z = loc.getBlockZ();
    	
    	for (int i = y; i < 256; i++) {
    		Block block = loc.getWorld().getBlockAt(x, i, z);
    		if (block.getType() != Material.AIR) {
        		return block.getType();
        	}
    	}
    	
    	return Material.AIR;
    }
    
    public void applySuit(String type, Player player, FileConfiguration suitData) {
    	
    	// Reset Suit
    	if (type.equalsIgnoreCase("NONE")) {
    		ItemStack empty = new ItemStack(Material.AIR, 1);
    		
    		player.getInventory().setBoots(empty);
    		player.getInventory().setLeggings(empty);
    		player.getInventory().setChestplate(empty);
    		player.getInventory().setHelmet(empty);
    		player.getInventory().clear(2);
    		
    		return;
    	}
    	
    	String path = null;
    	for (String suit : suitData.getConfigurationSection("suits").getKeys(false)) {
    		if (type.equalsIgnoreCase(suit)) {
    			path = "suits." + suit;
    			break;
    		}
    	}
    	if (path == null) {
    		Main.getPlugin().getLogger().warning("The suit type for " + player.getName() + ": '" + type + "' is not valid");
    		Data.setSelected(player.getUniqueId(), "SUIT", "NONE");
    		return;
    	}
    	
    	String[] array = getArray(player.getUniqueId());
    	Color color = getColor(array[2]);
    	if (color == null) return;
    	
    	String headName = array[3];
    	if (headName == null) return;
    	
    	// Apply Suit
    	try {
    		boolean randomCol = false;
    		String forceColor = suitData.getString(path + ".force-color");
    		if (forceColor != null) {
    			if (forceColor.equalsIgnoreCase("RANDOM")) {
    				randomCol = true;
    			}
    			
    			else if (getColor(forceColor) != null) {
    				color = getColor(forceColor);
    			}
    		}
    		
    		String forceHead = suitData.getString(path + ".force-head");
    		if (forceHead != null) {
    			headName = forceHead;
    		}
    		
    		Material bootsMat = Material.valueOf(suitData.getString(path + ".boots"));
    		Material leggingsMat = Material.valueOf(suitData.getString(path + ".leggings"));
    		Material chestplateMat = Material.valueOf(suitData.getString(path + ".chestplate"));
    		
    		ItemStack helmet;
    		if (suitData.getString(path + ".helmet").equalsIgnoreCase("PLAYERHEAD")) {
    			ItemStack helmetMat = getHead(headName);
    			helmet = getArmor(helmetMat, color);
    		}
    		else if (suitData.getString(path + ".helmet").equalsIgnoreCase("SKELETONHEAD")) {
    			ItemStack helmetMat = new ItemStack(Material.SKELETON_SKULL, 1);
    			helmet = getArmor(helmetMat, color);
    		}
    		else if (suitData.getString(path + ".helmet").equalsIgnoreCase("WITHERHEAD")) {
    			ItemStack helmetMat = new ItemStack(Material.WITHER_SKELETON_SKULL, 1);
    			helmet = getArmor(helmetMat, color);
    		}
    		else if (suitData.getString(path + ".helmet").equalsIgnoreCase("ZOMBIEHEAD")) {
    			ItemStack helmetMat = new ItemStack(Material.ZOMBIE_HEAD, 1);
    			helmet = getArmor(helmetMat, color);
    		}
    		else if (suitData.getString(path + ".helmet").equalsIgnoreCase("CREEPERHEAD")) {
    			ItemStack helmetMat = new ItemStack(Material.CREEPER_HEAD, 1);
    			helmet = getArmor(helmetMat, color);
    		}
    		else if (suitData.getString(path + ".helmet").equalsIgnoreCase("DRAGONHEAD")) {
    			ItemStack helmetMat = new ItemStack(Material.DRAGON_HEAD, 1);
    			helmet = getArmor(helmetMat, color);
    		}
    		
    		else if (suitData.getString(path + ".helmet").equalsIgnoreCase("BED")) {
    			ItemStack helmetMat = new ItemStack(getBedMaterial(color), 1);
    			helmet = getArmor(helmetMat, color);
    		}
    		
    		else {
    			Material helmetMat = Material.valueOf(suitData.getString(path + ".helmet"));
    			helmet = getArmor(helmetMat, color);
    			
    			if (randomCol) {
    				helmet = getArmor(helmetMat, randomColor());
    			}
    		}
    		
			ItemStack boots = getArmor(bootsMat, color);
			ItemStack leggings = getArmor(leggingsMat, color);
			ItemStack chestplate = getArmor(chestplateMat, color);
			
			if (randomCol) {
				boots = getArmor(bootsMat, randomColor());
				leggings = getArmor(leggingsMat, randomColor());
				chestplate = getArmor(chestplateMat, randomColor());
			}
			
			player.getInventory().setBoots(boots);
			player.getInventory().setLeggings(leggings);
			player.getInventory().setChestplate(chestplate);
			player.getInventory().setHelmet(helmet);
			
    	} catch (IllegalArgumentException e) {
    		
    		e.printStackTrace();
    		Main.getPlugin().getLogger().warning("The armor type(s) for '" + type + "' are not valid");
    	}
    	
    	
    	if (suitData.getString(path + ".hand.name") != null) {
    		Material hMaterial = Material.valueOf(suitData.getString(path + ".hand.material"));
	    	if (player.getInventory().getItem(2) == null || !player.getInventory().getItem(2).getType().equals(hMaterial)
	    			|| player.getInventory().getItem(2).getAmount() < 16) {
	    		
	    		String hName = ChatColor.translateAlternateColorCodes('&', suitData.getString(path + ".hand.name"));
	    		ItemStack hItem = new ItemStack(hMaterial, 16);
	    		
	    		ItemMeta hMeta = hItem.getItemMeta();
	    		hMeta.setDisplayName(hName);
	    		hItem.setItemMeta(hMeta);
	    		
	    		if (hItem.getType() == Material.FIREWORK_ROCKET) {
	    			
	    			FireworkMeta fMeta = (FireworkMeta) hItem.getItemMeta();
	    			int fPower = suitData.getInt(path + ".hand.firework-power", 1);
	    			fMeta.setPower(fPower);
	    			
	    			hItem.setItemMeta(fMeta);
	    			
	    		}
	    		
	    		player.getInventory().setItem(2, hItem);
	    		
	    	}
    	}
    	
    	else {
    		player.getInventory().clear(2);
    	}
    	    	
    }
    
    public static ItemStack getArmor(Material type, Color color) {
    	ItemStack armor = new ItemStack(type, 1);
    	if (type == Material.AIR) return armor;
    	
    	ItemMeta meta = armor.getItemMeta();
    	meta.setUnbreakable(true);
    	armor.setItemMeta(meta);
    	
    	if (!(armor.getItemMeta() instanceof LeatherArmorMeta)) {
    		return armor;
    	}
    	
    	LeatherArmorMeta leatherMeta = (LeatherArmorMeta) armor.getItemMeta();
    	if (color != null) leatherMeta.setColor(color);
    	
    	armor.setItemMeta(leatherMeta);
    	return armor;
    }
    
    public static ItemStack getArmor(ItemStack armor, Color color) {
    	ItemMeta meta = armor.getItemMeta();
    	meta.setUnbreakable(true);
    	armor.setItemMeta(meta);
    	
    	if (!(armor.getItemMeta() instanceof LeatherArmorMeta)) {
    		return armor;
    	}
    	
    	LeatherArmorMeta leatherMeta = (LeatherArmorMeta) armor.getItemMeta();
    	if (color != null) leatherMeta.setColor(color);
    	
    	armor.setItemMeta(leatherMeta);
    	return armor;
    }
    
    @SuppressWarnings("deprecation")
	public static ItemStack getHead(String name) {
    	ItemStack head = new ItemStack(Material.PLAYER_HEAD, 1);
    	
    	SkullMeta meta = (SkullMeta) head.getItemMeta();
    	meta.setOwner(name);
    	meta.setUnbreakable(true);
    	
    	head.setItemMeta(meta);
    	return head;
    }
	
	public static List<String> getColors() {
		List<String> colors = new ArrayList<String>();
		colors.add("AQUA");
    	colors.add("BLACK");
    	colors.add("BLUE");
    	colors.add("DARK_AQUA");
    	colors.add("DARK_BLUE");
    	colors.add("DARK_GRAY");
    	colors.add("GREEN");
    	colors.add("PURPLE");
    	colors.add("DARK_RED");
    	colors.add("ORANGE");
    	colors.add("LIGHT_GRAY");
    	colors.add("LIME");
    	colors.add("PINK");
    	colors.add("RED");
    	colors.add("WHITE");
    	colors.add("YELLOW");
    	colors.add("OLIVE");
    	return colors;
	}
    
    public static Color getColor(String string) {
    	Color color = Color.WHITE;
    	
    	if (string.split(",").length == 3) {
    		try {
	    		String[] colorValues = string.split(",");
	    		int red = Integer.parseInt(colorValues[0]);
	    		int green = Integer.parseInt(colorValues[1]);
	    		int blue = Integer.parseInt(colorValues[2]);
	    		
	        	color = color.setRed(red).setGreen(green).setBlue(blue);
	        	
    		} catch (NumberFormatException e) {
    			Main.getPlugin().getLogger().warning("The color '" + string + "' is not valid");
    		}
    	}
    	
    	else {
    		switch (string) {
    		
	    		case "AQUA": color = Color.AQUA; break;
	        	case "BLACK": color = Color.BLACK; break;
	        	case "BLUE": color = Color.BLUE; break;
	        	case "DARK_AQUA": color = Color.TEAL; break;
	        	case "DARK_BLUE": color = Color.NAVY; break;
	        	case "DARK_GRAY": color = Color.GRAY; break;
	        	case "GREEN": color = Color.GREEN; break;
	        	case "PURPLE": color = Color.PURPLE; break;
	        	case "DARK_RED": color = Color.MAROON; break;
	        	case "ORANGE": color = Color.ORANGE; break;
	        	case "LIGHT_GRAY": color = Color.SILVER; break;
	        	case "LIME": color = Color.LIME; break;
	        	case "PINK": color = Color.FUCHSIA; break;
	        	case "RED": color = Color.RED; break;
	        	case "WHITE": color = Color.WHITE; break;
	        	case "YELLOW": color = Color.YELLOW; break;
	        	case "OLIVE": color = Color.OLIVE; break;
	    		default: color = null;
    		
    		}
    	}
    	
		return color;
    }
    
    private Color getColor(int red, int green, int blue) {
    	Color color = Color.WHITE;
    	color = color.setRed(red).setGreen(green).setBlue(blue);
		return color;
    }
    
    private Color randomColor() {
    	int max = 255;
    	int min = 0;
    	
    	int red = new Random().nextInt((max - min) + 1) + min;
    	int green = new Random().nextInt((max - min) + 1) + min;
    	int blue = new Random().nextInt((max - min) + 1) + min;
    	
    	Color color = getColor(red, green, blue);
    	return color;
    }
    
    public static Material getBedMaterial(Color color) {
		
		if (color == Color.BLACK) return Material.BLACK_BED;
		else if (color == Color.BLUE) return Material.BLUE_BED;
		else if (color == Color.NAVY) return Material.BLUE_BED;
		else if (color == Color.TEAL) return Material.CYAN_BED;
		else if (color == Color.GRAY) return Material.GRAY_BED;
		else if (color == Color.GREEN) return Material.GREEN_BED;
		else if (color == Color.OLIVE) return Material.GREEN_BED;
		else if (color == Color.AQUA) return Material.LIGHT_BLUE_BED;
		else if (color == Color.LIME) return Material.LIME_BED;
		else if (color == Color.ORANGE) return Material.ORANGE_BED;
		else if (color == Color.FUCHSIA) return Material.PINK_BED;
		else if (color == Color.PURPLE) return Material.PURPLE_BED;
		else if (color == Color.RED) return Material.RED_BED;
		else if (color == Color.MAROON) return Material.RED_BED;
		else if (color == Color.SILVER) return Material.LIGHT_GRAY_BED;
		else if (color == Color.WHITE) return Material.WHITE_BED;
		else if (color == Color.YELLOW) return Material.YELLOW_BED;
		else return Material.WHITE_BED;
		
	}
    
    public static void end() {
    	if (assignedTaskId != null) Bukkit.getScheduler().cancelTask(assignedTaskId);
    	UpdateSuits.plugin = null;
    }

    /**
     * Schedules this instance to "run" every second
     */
    public static void schedule() {
        // Initialize our assigned task's id, for later use so we can cancel
        assignedTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new UpdateSuits(), 10L, 20L);
    }

}