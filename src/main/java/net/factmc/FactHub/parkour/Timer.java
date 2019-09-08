package net.factmc.FactHub.parkour;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import net.factmc.FactHub.Main;

public class Timer implements Runnable {
	
    private static int task = -1;
    
    public void run() {
    	List<Object[]> add = new ArrayList<Object[]>();
    	List<Object[]> remove = new ArrayList<Object[]>();
    	
    	for (Object[] array : Parkour.currentRuns) {
    		if ((boolean) array[3]) {
    			int time = (int) array[1];
    			time++;
    			
    			if (time >= 72000) {
    				Object[] a = {array[0], 0, -1, false};
    				remove.add(array);
        			add.add(a);
        			
        			Player player = (Player) array[0];
    				player.sendMessage(ChatColor.RED + "You ran out of time to finish the parkour");
    			}
    			
    			else {
    				Object[] a = {array[0], time, array[2], true};
    				remove.add(array);
        			add.add(a);
    			}
    		}
    	}
    	
    	Parkour.currentRuns.removeAll(remove);
    	Parkour.currentRuns.addAll(add);
    }
    

    public static void start() {
        task = Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getPlugin(), new Timer(), 10L, 1L);
    }
    
    public static void end() {
    	if (task != -1) Bukkit.getScheduler().cancelTask(task);
    }

}