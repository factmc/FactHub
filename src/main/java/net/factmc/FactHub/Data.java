package net.factmc.FactHub;

import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.DyeColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Llama;
import org.bukkit.entity.Cat;
import org.bukkit.entity.Parrot;
import org.bukkit.entity.Player;
import org.bukkit.entity.Rabbit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import net.factmc.FactCore.FactSQLConnector;
import net.factmc.FactHub.Main;
import net.factmc.FactHub.cosmetics.UpdateSuits;
import net.factmc.FactHub.gui.cosmetics.MorphsGUI;
import net.factmc.FactHub.gui.cosmetics.PetsGUI;
import net.factmc.FactHub.gui.cosmetics.TrailsGUI;

public class Data implements Listener {
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		Data.setupPlayerData(player);
	}
	
	private static File mainDataDir = new File(Main.getPlugin().getDataFolder().getPath());
	
	private static File cloakFile = new File(mainDataDir.getPath() + File.separatorChar + "cloaks.yml");
	private static File suitFile = new File(mainDataDir.getPath() + File.separatorChar + "suits.yml");
	private static File valueFile = new File(mainDataDir.getPath() + File.separatorChar + "values.yml");
	
	public static FileConfiguration getCloakData() {
		FileConfiguration fileConfig = YamlConfiguration.loadConfiguration(cloakFile);
		return fileConfig;
	}
	
	public static FileConfiguration getSuitData() {
		FileConfiguration fileConfig = YamlConfiguration.loadConfiguration(suitFile);
		return fileConfig;
	}
	
	public static FileConfiguration getValueData() {
		FileConfiguration fileConfig = YamlConfiguration.loadConfiguration(valueFile);
		return fileConfig;
	}
	
	public static void saveConfig(File file, FileConfiguration config) {
		try {
			config.save(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	
	public static String[] getSelected(UUID uuid, String... cosmetic) {
		return FactSQLConnector.getStringValue(FactSQLConnector.getOptionsTable(), uuid, cosmetic);
	}
	public static String getSelected(UUID uuid, String cosmetic) {
		return FactSQLConnector.getStringValue(FactSQLConnector.getOptionsTable(), uuid, cosmetic);
	}
	
	public static boolean[] getBoolean(UUID uuid, String... cosmetic) {
		return FactSQLConnector.getBooleanValue(FactSQLConnector.getOptionsTable(), uuid, cosmetic);
	}
	public static boolean getBoolean(UUID uuid, String cosmetic) {
		return FactSQLConnector.getBooleanValue(FactSQLConnector.getOptionsTable(), uuid, cosmetic);
	}
	
	
	public static void setSelected(UUID uuid, String cosmetic, String selected) {
		FactSQLConnector.setValue(FactSQLConnector.getOptionsTable(), uuid, cosmetic, selected);
	}
	public static void setBoolean(UUID uuid, String cosmetic, boolean selected) {
		FactSQLConnector.setValue(FactSQLConnector.getOptionsTable(), uuid, cosmetic, selected);
	}
	
	
	public static List<String> getAccess(UUID uuid, String category) {
		
		List<String> list = new ArrayList<String>();
		try {
			
			PreparedStatement statement = FactSQLConnector.getMysql().getConnection()
					.prepareStatement("SELECT * FROM " + FactSQLConnector.getAccessTable()
					+ " WHERE `UUID`=? AND `CATEGORY`=?");
			statement.setString(1, uuid.toString());
			statement.setString(2, category);
			
			ResultSet result = statement.executeQuery();
			while (result.next()) {
				list.add(result.getString("VALUE"));
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return list;
		
	}
	
	public static boolean checkAccess(UUID uuid, String category, String name) {
		
		try {
			
			PreparedStatement statement = FactSQLConnector.getMysql().getConnection()
					.prepareStatement("SELECT * FROM " + FactSQLConnector.getAccessTable()
					+ " WHERE `UUID`=? AND `CATEGORY`=? AND `VALUE`=?");
			statement.setString(1, uuid.toString());
			statement.setString(2, category);
			statement.setString(3, name);
			
			ResultSet result = statement.executeQuery();
			if (result.next()) {
				return true;
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return false;
		
	}
	
	public static boolean giveAccess(UUID uuid, String category, String name) {
		
		if (!checkAccess(uuid, category, name)) {
			try {
				
				PreparedStatement insert = FactSQLConnector.getMysql().getConnection()
					.prepareStatement("INSERT INTO " + FactSQLConnector.getAccessTable()
					+ " (UUID,CATEGORY,VALUE) VALUE (?,?,?)");
				insert.setString(1, uuid.toString());
				insert.setString(2, category);
				insert.setString(3, name);
				
				insert.executeUpdate();
				return true;
				
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
		}
		
		return false;
		
	}
	
	public static boolean removeAccess(UUID uuid, String category, String name) {
		
		if (checkAccess(uuid, category, name)) {
			try {
				
				PreparedStatement delete = FactSQLConnector.getMysql().getConnection()
					.prepareStatement("DELETE FROM " + FactSQLConnector.getAccessTable()
					+ " WHERE `UUID`=? AND `CATEGORY`=? AND `VALUE`=?");
				delete.setString(1, uuid.toString());
				delete.setString(2, category);
				delete.setString(3, name);
				
				delete.executeUpdate();
				return true;
				
			} catch (SQLException e) {
				e.printStackTrace();
			}
		
		}
		
		return false;
		
	}
	
	
	public static void setupPlayerData(OfflinePlayer player) {
		
		UUID uuid = player.getUniqueId();
		try {
			PreparedStatement statement = FactSQLConnector.getMysql().getConnection()
					.prepareStatement("SELECT * FROM " + FactSQLConnector.getOptionsTable() + " WHERE `UUID`=?");
			statement.setString(1, uuid.toString());
			
			ResultSet results = statement.executeQuery();
			if (results.next()) return;
			
		} catch (SQLException e) {
			e.printStackTrace();
			return;
		}
		
		try {
			
			PreparedStatement insert = FactSQLConnector.getMysql().getConnection()
				.prepareStatement("INSERT INTO " + FactSQLConnector.getOptionsTable()
				+ " (UUID,HIDEPLAYERS,TRAIL,CLOAK,SUIT,SUITCOLOR,SUITHEAD,PET,PETNAME,PETBABY,PETAUTOSPAWN,PETCOLOR,PETSTYLE"
				+ ",MORPHBABY,MORPHVIEWSELF,MORPHCOLOR) VALUE (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
			insert.setString(1, uuid.toString());
			insert.setBoolean(2, false);
			insert.setString(3, "NONE");
			insert.setString(4, "NONE");
			insert.setString(5, "NONE");
			insert.setString(6, "WHITE");
			insert.setString(7, "Steve");
			insert.setString(8, "NONE");
			insert.setString(9, "NONE");
			insert.setBoolean(10, false);
			insert.setBoolean(11, true);
			insert.setString(12, "NONE");
			insert.setString(13, "NONE");
			insert.setBoolean(14, false);
			insert.setBoolean(15, true);
			insert.setString(16, "NONE");
			
			insert.executeUpdate();
			Main.getPlugin().getLogger().info("Added default settings for " + player.getName()
			+ " (" + String.valueOf(player.getUniqueId()) + ".yml)");
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		/*data = addPath(data, "hide-players", false);
		
		data = addPath(data, "points", 0);
		data = addPath(data, "total-votes", 0);
		data = addPath(data, "parkour-time", 0);
		
		data = addPath(data, "available.trails", "[]");
		data = addPath(data, "cosmetics.trail", "NONE");
		
		data = addPath(data, "available.cloaks", "[]");
		data = addPath(data, "cosmetics.cloak", "NONE");
		
		data = addPath(data, "available.suits", "[]");
		data = addPath(data, "cosmetics.suit.type", "NONE");
		data = addPath(data, "cosmetics.suit.suit-color", "WHITE");
		data = addPath(data, "cosmetics.suit.head", "TrollyLoki");
		
		data = addPath(data, "available.pets", "[]");
		data = addPath(data, "cosmetics.pet.name", "NONE");
		data = addPath(data, "cosmetics.pet.type", "NONE");
		data = addPath(data, "cosmetics.pet.auto-spawn", true);
		
		data = addPath(data, "available.morphs", "[]");
		data = addPath(data, "cosmetics.morph.baby", false);
		data = addPath(data, "cosmetics.morph.view-self", true);*/
		
		
		giveAccess(uuid, "SUIT_COLORS", "WHITE");
		giveAccess(uuid, "DYE_COLORS", "WHITE");
		giveAccess(uuid, "HORSE_COLORS", "WHITE");
		giveAccess(uuid, "HORSE_STYLES", "NONE");
		giveAccess(uuid, "LLAMA_COLORS", "WHITE");
		giveAccess(uuid, "PARROT_COLORS", "GRAY");
		giveAccess(uuid, "CAT_COLORS", "WHITE");
		giveAccess(uuid, "RABBIT_COLORS", "WHITE");
		
		/*List<String> defaultColors = new ArrayList<String>();
		defaultColors.add("WHITE");
		data = addPath(data, "available.suit-colors", defaultColors);
		
		List<String> defaultDyeColors = new ArrayList<String>();
		defaultDyeColors.add("WHITE");
		data = addPath(data, "available.dye-colors", defaultDyeColors);
		
		List<String> defaultHorseColors = new ArrayList<String>();
		defaultHorseColors.add("WHITE");
		data = addPath(data, "available.horse-colors", defaultHorseColors);
		
		List<String> defaultHorseStyles = new ArrayList<String>();
		defaultHorseStyles.add("NONE");
		data = addPath(data, "available.horse-styles", defaultHorseStyles);
		
		List<String> defaultLlamaColors = new ArrayList<String>();
		defaultLlamaColors.add("WHITE");
		data = addPath(data, "available.llama-colors", defaultLlamaColors);
		
		List<String> defaultParrotColors = new ArrayList<String>();
		defaultParrotColors.add("GRAY");
		data = addPath(data, "available.parrot-colors", defaultParrotColors);
		
		List<String> defaultCatColors = new ArrayList<String>();
		defaultCatColors.add("BLACK_CAT");
		data = addPath(data, "available.cat-colors", defaultCatColors);
		
		List<String> defaultRabbitColors = new ArrayList<String>();
		defaultCatColors.add("WHITE");
		data = addPath(data, "available.cat-colors", defaultRabbitColors);*/
		
		//saveConfig(getPlayerFile(player), data);
		Main.getPlugin().getLogger().info("Added default settings for " + player.getName()
				+ " (" + String.valueOf(player.getUniqueId()) + ".yml)");
	}
	
	
	public static boolean start() {
		
		if (!cloakFile.exists()) {
			try {
				cloakFile.createNewFile();
				FileConfiguration cloakData = getCloakData();
				
				cloakData.set("cloaks.wings.item", "FEATHER");
				cloakData.set("cloaks.wings.name", "&bWings");
				
				List<String> desc = new ArrayList<String>();
				desc.add("&7Puts rainbow wings on");
				desc.add("&7your player's back");
				cloakData.set("cloaks.wings.description", desc);
				
				cloakData.set("cloaks.wings.particle", "REDSTONE");
				
				List<String> points = new ArrayList<String>();
			    points.add("-2:2,-2:0");
			    points.add("-4:-3,-1:1");
			    points.add("3:4,-1:1");
			    points.add("-2,1");
			    points.add("2,1");
			    points.add("-6:-4,1:2");
			    points.add("4:6,1:2");
			    points.add("-5,0");
			    points.add("5,0");
				
				cloakData.set("cloaks.wings.points", points);
				
				saveConfig(cloakFile, cloakData);
				Main.getPlugin().getLogger().info("Created default cloaks.yml");
			} catch (IOException e) {
				e.printStackTrace();
				Main.getPlugin().getLogger().warning("Failed to create default cloaks.yml");
			}
		}
		
		if (!suitFile.exists()) {
			try {
				suitFile.createNewFile();
				FileConfiguration suitData = getSuitData();
				
				suitData.set("suits.color.item", "LEATHER_LEGGINGS");
				suitData.set("suits.color.name", "&cSolid Color Suit");
				
				List<String> desc = new ArrayList<String>();
				desc.add("&7Gives you a suit of");
				desc.add("&7one solid color");
				suitData.set("suits.color.description", desc);
				
				suitData.set("suits.color.boots", "LEATHER_BOOTS");
				suitData.set("suits.color.leggings", "LEATHER_LEGGINGS");
				suitData.set("suits.color.chestplate", "LEATHER_CHESTPLATE");
				suitData.set("suits.color.helmet", "AIR");
				
				saveConfig(suitFile, suitData);
				Main.getPlugin().getLogger().info("Created default suits.yml");
			} catch (IOException e) {
				e.printStackTrace();
				Main.getPlugin().getLogger().warning("Failed to create default suits.yml");
			}
		}
		
		if (!valueFile.exists()) {
			try {
				valueFile.createNewFile();
				FileConfiguration valueData = getValueData();
				
				valueData.set("value.1.name", "&aCommon");
				valueData.set("value.2.name", "&cUncommon");
				valueData.set("value.3.name", "&9Rare");
				valueData.set("value.4.name", "&dEpic");
				valueData.set("value.5.name", "&6Legendary");
				
				valueData.set("value.1.cost", 10);
				valueData.set("value.2.cost", 15);
				valueData.set("value.3.cost", 30);
				valueData.set("value.4.cost", 45);
				valueData.set("value.5.cost", 70);
				
				for (String color : UpdateSuits.getColors()) {
					valueData.set("values.suit-colors." + color, 0);
				}
				
				for (DyeColor dyeColor : DyeColor.values()) {
					String color = String.valueOf(dyeColor).toUpperCase();
					valueData.set("values.dye-colors." + color, 0);
				}
				valueData.set("values.dye-colors.RAINBOW", 0);
				
				for (Horse.Color horseColor : Horse.Color.values()) {
					String color = String.valueOf(horseColor).toUpperCase();
					valueData.set("values.horse-colors." + color, 0);
				}
				
				for (Horse.Style horseStyle : Horse.Style.values()) {
					String style = String.valueOf(horseStyle).toUpperCase();
					valueData.set("values.horse-styles." + style, 0);
				}
				
				for (Llama.Color llamaColor : Llama.Color.values()) {
					String color = String.valueOf(llamaColor).toUpperCase();
					valueData.set("values.llama-colors." + color, 0);
				}
				
				for (Parrot.Variant parrotColor : Parrot.Variant.values()) {
					String color = String.valueOf(parrotColor).toUpperCase();
					valueData.set("values.parrot-colors." + color, 0);
				}
				
				for (Cat.Type catColor : Cat.Type.values()) {
					String color = String.valueOf(catColor).toUpperCase();
					valueData.set("values.cat-colors." + color, 0);
				}
				
				for (Rabbit.Type rabbitColor : Rabbit.Type.values()) {
					if (rabbitColor != Rabbit.Type.THE_KILLER_BUNNY) {
						String color = String.valueOf(rabbitColor).toUpperCase();
						valueData.set("values.rabbit-colors." + color, 0);
					}
				}

				for (EntityType type : PetsGUI.getUseableEntities()) {
					valueData.set("values.pets." + type, 0);
				}
				
				for (EntityType morph : MorphsGUI.getUseableEntities()) {
					valueData.set("values.morphs." + morph, 0);
				}
				
				for (String trail : TrailsGUI.getUseableParticles()) {
					valueData.set("values.trails." + trail, 0);
				}
				
				for (String shape : getCloakData().getConfigurationSection("cloaks").getKeys(false)) {
					valueData.set("values.cloaks." + shape.toUpperCase(), 0);
				}
				
				for (String suit : getSuitData().getConfigurationSection("suits").getKeys(false)) {
					valueData.set("values.suits." + suit.toUpperCase(), 0);
				}
				
				saveConfig(valueFile, valueData);
				Main.getPlugin().getLogger().info("Created default values.yml");
			} catch (IOException e) {
				e.printStackTrace();
				Main.getPlugin().getLogger().warning("Failed to create default values.yml");
			}
		}
		
		return true;
	}
	
	
	/*
	public static FileConfiguration getPlayerData(OfflinePlayer player) {
		File file = getPlayerFile(player);
		if (file == null) return null;
		FileConfiguration fileConfig = YamlConfiguration.loadConfiguration(file);
		return fileConfig;
	}
	
	public static FileConfiguration getPlayerData(String name) {
		File file = getPlayerFile(name);
		if (file == null) return null;
		FileConfiguration fileConfig = YamlConfiguration.loadConfiguration(file);
		return fileConfig;
	}
	
	public static File getPlayerFile(OfflinePlayer player) {
		String path = playerDataDir.getPath() + File.separatorChar + "" + String.valueOf(player.getUniqueId()) + ".yml";
    	File dataFile = new File(path);
    	if (dataFile.exists()) {
    		return dataFile;
    	}
    	
    	return null;
	}
	
	public static File getPlayerFile(String name) {
		Player player = Bukkit.getPlayerExact(name);
		if (player != null) {
			return getPlayerFile(Bukkit.getPlayerExact(name));
		}
		
		for (File file : playerDataDir.listFiles()) {
			FileConfiguration data = YamlConfiguration.loadConfiguration(file);
			String nextName = data.getString("name");
			if (nextName.equals(name)) {
				return file;
			}
		}
		
		return null;
	}
	
	public static boolean createPlayerFile(OfflinePlayer player) {
		try {
			String path = playerDataDir.getPath() + File.separatorChar + "" + String.valueOf(player.getUniqueId()) + ".yml";
	    	File dataFile = new File(path);
	    	if (!dataFile.exists()) {
		    	dataFile.createNewFile();
		    	Main.getPlugin().getLogger().info("Created data file for " + player.getName()
		    			+ " (" + String.valueOf(player.getUniqueId()) + ")");
	    	}
		} catch (IOException e) {
			e.printStackTrace();
			Main.getPlugin().getLogger().warning("Failed to create data file for " + player.getName()
		    		+ " (" + String.valueOf(player.getUniqueId()) + ".yml)");
			return false;
		}
		return true;
	}*/
	
}