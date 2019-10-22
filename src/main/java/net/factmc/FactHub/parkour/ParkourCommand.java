package net.factmc.FactHub.parkour;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import net.factmc.FactCore.CoreUtils;
import net.factmc.FactCore.FactSQL;
import net.factmc.FactHub.Main;
import net.md_5.bungee.api.ChatColor;

public class ParkourCommand implements CommandExecutor, TabCompleter {
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("parkour")) {
        	
        	Player player = null;
        	if (sender instanceof Player) {
        		player = (Player) sender;
        	}
        	else {
        		sender.sendMessage(ChatColor.RED + "Only players can use this command");
        		return false;
        	}
        	
    		if (args.length > 0) {
    			
    			if (args[0].equalsIgnoreCase("time")) {
    				
    				if (args.length > 1) {
    					if (!sender.hasPermission("factbukkit.stats.others")) {
    						sender.sendMessage(ChatColor.YELLOW + "You must be "
    								+ ChatColor.AQUA + ChatColor.BOLD + "MVP"
    								+ ChatColor.YELLOW + " to do that!");
    						return false;
    					}
    					
    					UUID uuid = FactSQL.getInstance().getUUID(args[1]);
    					if (uuid == null) {
    	        			sender.sendMessage(ChatColor.RED + args[1] + " has never joined the server");
    	        			return false;
    	        		}
    					
    					int tickTime = (int) FactSQL.getInstance().get(FactSQL.getStatsTable(), uuid, "PARKOURTIME");
						
						if (tickTime == 0) {
							sender.sendMessage(Parkour.PREFIX + ChatColor.AQUA + args[1] + " has never completed the parkour");
						}
						
						else {
							String time = CoreUtils.convertTicks(tickTime);
							sender.sendMessage(Parkour.PREFIX + ChatColor.AQUA + args[1] + "'s best time is: " + time);
						}
    				}
    				
    				else {
						int tickTime = (int) FactSQL.getInstance().get(FactSQL.getStatsTable(), player.getUniqueId(), "PARKOURTIME");
						
						if (tickTime == 0) {
							sender.sendMessage(Parkour.PREFIX + ChatColor.AQUA + "You have never completed the parkour");
						}
						
						else {
							String time = CoreUtils.convertTicks(tickTime);
							sender.sendMessage(Parkour.PREFIX + ChatColor.AQUA + "Your best time is: " + time);
						}
    				}
    				
    				return true;
    			}
    			
    			if (!Parkour.inParkour(player)) {
    				sender.sendMessage(Parkour.PREFIX + ChatColor.RED + "You are not doing the parkour");
    				return false;
    			}
    			
    			if (args[0].equalsIgnoreCase("quit")) {
    				Parkour.leave(player);
    				sender.sendMessage(Parkour.PREFIX + ChatColor.AQUA + "You are no longer doing the parkour");
    				
    		        player.teleport(Main.getSpawn());
					return true;
    			}
    			
    			else if (args[0].equalsIgnoreCase("checkpoint")) {
    				int index = (int) Parkour.currentRuns.get(player)[1];
    				Location loc;
    				try {
    					loc = Parkour.getCheckpoint(index-1);
    				} catch (IndexOutOfBoundsException e) {
    					loc = Parkour.getFinish();
    				}
    				
    				Parkour.allowTeleport.add(player);
    				player.teleport(loc);
    				
    				return true;
    			}
    			
    		}
    		
    		sender.sendMessage(ChatColor.RED + "Usage: /parkour <time|quit|checkpoint> [player]");
    		return false;
        	
        }
		return false;   
    }
	
	
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
		if (cmd.getName().equalsIgnoreCase("parkour")) {
			List<String> list = new ArrayList<String>();
			
			if (args.length == 1) {
				list.add("time");
				list.add("quit");
				list.add("checkpoint");
				return list;
			}
			
			else if (args.length == 2) {
				for (Player p : Bukkit.getServer().getOnlinePlayers()) {
					list.add(p.getName());
				}
				return list;
			}
			
			else {
				return list;
			}
				
		}
		return null;
	}
	
}