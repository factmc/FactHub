package net.factmc.FactHub.parkour;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import net.factmc.FactHub.Main;

public class Timer implements Runnable {
	
    private static int task = -1;
    
    public void run() {
    	for (Player player : Parkour.currentRuns.keySet()) {
    		Object[] array = Parkour.currentRuns.get(player);
    		
    		if ((boolean) array[2]) {
    			int time = (int) array[0];
    			time++;
    			
    			if (time >= 72000) {
    				Object[] a = {0, -1, false};
    				Parkour.currentRuns.put(player, a);
        			
    				player.sendMessage(ChatColor.RED + "You ran out of time to finish the parkour");
    			}
    			
    			else {
    				Object[] a = {time, array[1], true};
    				Parkour.currentRuns.put(player, a);
    			}
    		}
    	}
    }
    

    public static void start() {
        task = Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getPlugin(), new Timer(), 10L, 1L);
    }
    
    public static void end() {
    	if (task != -1) Bukkit.getScheduler().cancelTask(task);
    }

}