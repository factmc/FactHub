package net.factmc.FactHub;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import net.factmc.FactCore.bukkit.InventoryControl;
import net.factmc.FactHub.bossbar.Bossbar;
import net.factmc.FactHub.commands.*;
import net.factmc.FactHub.gui.*;
import net.factmc.FactHub.listeners.*;
import net.factmc.FactHub.parkour.Parkour;
import net.factmc.FactHub.parkour.ParkourCommand;
import net.factmc.FactHub.parkour.Timer;
import net.factmc.FactHub.sidebar.Sidebar;

public class Main extends JavaPlugin {
	
	public static JavaPlugin plugin;
	
	public static final String PREFIX = ChatColor.GOLD + "[" + ChatColor.DARK_GREEN +
			ChatColor.BOLD + "FactHub" + ChatColor.GOLD + "] ";
	
	//public static Permission perms = null;
	public static boolean morphs = true;
	public static boolean sv = false;
	
    @Override
    public void onEnable() {
    	plugin = this;
    	registerEvents();
    	registerCommands();
    	saveDefaultConfig();
    	
    	Bukkit.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        Bukkit.getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", new Sidebar());
        plugin.getLogger().info("Registered BungeeCord channel");
        
        if (Bukkit.getPluginManager().getPlugin("SuperVanish") != null) sv = true;
        if (Bukkit.getPluginManager().getPlugin("LibsDisguises") == null) morphs = false;
        
        Parkour.load();
        WorldProtection.load();
        
        Bossbar.load();
        plugin.getLogger().info("Loaded Bossbar");
        /*Sidebar.load();
        plugin.getLogger().info("Loaded Sidebar");*/
        
        Timer.start();
        
    	PlayerManager.load();
    }
    
    @Override
    public void onDisable() {
    	Timer.end();
    	
    	Bossbar.Bossbar.removeAll();
    	plugin.getLogger().info("Unloaded Bossbar");
    	
    	Bukkit.getScheduler().cancelTasks(plugin);
    	plugin.getLogger().info("Cancelled Tasks");
    	
    	plugin = null;
    }
    
    public void registerEvents() {
    	
    	List<Listener> listeners = new ArrayList<Listener>();
    	listeners.add(new PlayerManager());
    	listeners.add(new WorldProtection());
    	
    	listeners.add(new Bossbar());
    	//listeners.add(new Sidebar());
    	listeners.add(new LaunchPads());
    	
    	listeners.add(new Parkour());
    	
    	listeners.add(new InventoryControl());
    	listeners.add(new ServerGUI());
    	
        for (Listener listener : listeners) {
            Bukkit.getServer().getPluginManager().registerEvents(listener, plugin);
        }
    }
    
    public void registerCommands() {
    	PluginCommand facthubCmd = plugin.getCommand("facthub");
    	facthubCmd.setExecutor(new FactHubCommand());
    	List<String> aliases = new ArrayList<String>();
    	aliases.add("fhub"); aliases.add("fh");
    	facthubCmd.setAliases(aliases);
    	
    	plugin.getCommand("servers").setExecutor(new ServersCommand());
    	
    	plugin.getCommand("vote").setExecutor(new VoteCommand());
    	plugin.getCommand("parkour").setExecutor(new ParkourCommand());
    	plugin.getCommand("parkour").setAliases(Collections.singletonList("p"));
    	plugin.getCommand("parkour").setTabCompleter(new ParkourCommand());
    }
    
    public static JavaPlugin getPlugin() {
        return plugin;
    }
    
    public static String getConfigString(String path) {
    	if (plugin.getConfig().getString(path) != null) {
    		String string = plugin.getConfig().getString(path);
    		string = ChatColor.translateAlternateColorCodes('&', string);
    		return string;
    	}
    	
    	else {
    		return path;	
    	}
    }
    
    public static List<String> getConfigStringList(String path) {
    	if (plugin.getConfig().getString(path) != null) {
    		List<String> rawStrings = plugin.getConfig().getStringList(path);
    		List<String> strings = new ArrayList<String>();
    		for (String nextString : rawStrings) {
    			strings.add(ChatColor.translateAlternateColorCodes('&', nextString));
    		}
    		return strings;
    	}
    	
    	else {
    		List<String> paths = new ArrayList<String>();
    		paths.add(path);
    		return paths;
    	}
    }
    
    public static Location getSpawn() {
    	World world = Bukkit.getWorlds().get(0);
    	Location spawn = world.getSpawnLocation();
    	return new Location(world, spawn.getX()+0.5,
    			spawn.getY(), spawn.getZ()+0.5, 180, 0);
    }
    
}