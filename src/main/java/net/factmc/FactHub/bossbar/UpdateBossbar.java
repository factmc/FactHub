package net.factmc.FactHub.bossbar;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import net.factmc.FactHub.Main;

public class UpdateBossbar implements Runnable {

    // Main class for bukkit scheduling
    private static JavaPlugin plugin;
    
    // Our scheduled task's assigned id,needed for canceling
    private static Integer assignedTaskId;
    
    public static void start(JavaPlugin plugin) {
        // Initializing fields
        UpdateBossbar.plugin = plugin;
        UpdateBossbar.rate = (int) (Main.getPlugin().getConfig().getDouble("bossbar.rate") * 20);
        schedule();
    }
    
    private final int textMax = Main.getConfigStringList("bossbar.text").size();
    private final int colorMax = Main.getConfigStringList("bossbar.color").size();
    
    private int textNum = 0;
    private int colorNum = 0;
    
    private int tick = 1;
    private static int rate;
    
    public void run() {
    	
    	if (tick >= rate) {
    		
    		String newText = getText(textNum);
    		Bossbar.Bossbar.setTitle(newText);
    	
    		BarColor newColor = getColor(colorNum);
    		Bossbar.Bossbar.setColor(newColor);
    		
    		List<Player> players = Bossbar.Bossbar.getPlayers();
    		for (Player player : players) {
    			
    			if (!player.isOnline()) Bossbar.Bossbar.removePlayer(player);
    			
    		}
    		for (Player player : Bukkit.getServer().getOnlinePlayers()) {
    			
    			if (!players.contains(player)) Bossbar.Bossbar.addPlayer(player);
    			
    		}
    	
    	
    		textNum++;
    		if (textNum >= textMax) textNum = 0;
    	
    		colorNum++;
    		if (colorNum >= colorMax) colorNum = 0;
    		
    		tick = 1;
    		
    	}
    	
    	else tick++;
    	
    }
    
    
    public static String getText(int num) {
    	
    	String raw = Main.getConfigStringList("bossbar.text").get(num);
    	String text = ChatColor.translateAlternateColorCodes('&', raw);
    	return text;
    	
    }
    
    public static BarColor getColor(int num) {
    	
    	String raw = Main.getConfigStringList("bossbar.color").get(num);
    	BarColor color = BarColor.valueOf(raw);
    	return color;
    	
    }
    
    
    
    public static void end() {
    	if (assignedTaskId != null) Bukkit.getScheduler().cancelTask(assignedTaskId);
    	UpdateBossbar.plugin = null;
    }
    
    public static void schedule() {
        // Initialize our assigned task's id, for later use so we can cancel
        assignedTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new UpdateBossbar(), 10L, 1L);
    }

}