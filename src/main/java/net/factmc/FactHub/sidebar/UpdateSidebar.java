package net.factmc.FactHub.sidebar;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import net.factmc.FactHub.Main;

public class UpdateSidebar implements Runnable {

    // Main class for bukkit scheduling
    private static JavaPlugin plugin;
    
    // Our scheduled task's assigned id,needed for canceling
    private static Integer assignedTaskId;
    
    public static void start(JavaPlugin plugin) {
        // Initializing fields
        UpdateSidebar.plugin = plugin;
        UpdateSidebar.rate = (int) (Main.getPlugin().getConfig().getDouble("sidebar.rate") * 20);
        schedule();
    }
    
    
    private final int textMax = Main.getConfigStringList("sidebar.title").size();
    
    private int textNum = 0;
    
    private int tick = 1;
    private static int rate;
    
    public void run() {
    	
    	if (tick >= rate) {
    		
    		Sidebar.requestPlayerCount();
    		
    		Sidebar.title = ChatColor.translateAlternateColorCodes('&', Main.getConfigStringList("sidebar.title").get(textNum));
    		
    		textNum++;
    		if (textNum >= textMax) textNum = 0;
    		
    		tick = 1;
    		
    		updateAll();
    		
    	}
    	
    	else tick++;
    	
    }
    
    public static void updateAll() {
    	
    	for (Player player : Bukkit.getOnlinePlayers()) {
			
			Sidebar.updatePlayer(player);
			
		}
    	
    }
    
    
    
    public static void end() {
    	if (assignedTaskId != null) Bukkit.getScheduler().cancelTask(assignedTaskId);
    	UpdateSidebar.plugin = null;
    }
    
    public static void schedule() {
        // Initialize our assigned task's id, for later use so we can cancel
        assignedTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new UpdateSidebar(), 20L, 1L);
    }

}