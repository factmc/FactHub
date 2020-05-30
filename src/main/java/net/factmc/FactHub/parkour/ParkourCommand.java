package net.factmc.FactHub.parkour;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import net.factmc.FactCore.CoreUtils;
import net.factmc.FactCore.FactSQL;
import net.factmc.FactCore.bukkit.BukkitMain;
import net.factmc.FactHub.Main;
import net.md_5.bungee.api.ChatColor;

public class ParkourCommand implements CommandExecutor, TabCompleter {
	
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("parkour")) {
        	
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
    					
    					FactSQL.getInstance().select(FactSQL.getStatsTable(), "PARKOURTIME", "`NAME`=?", args[1]).thenAccept((list) -> {
    						
    						if (list.isEmpty()) {
        	        			sender.sendMessage(ChatColor.RED + args[1] + " has never joined the server");
        	        			return;
        	        		}
    						
    						int tickTime = (int) list.get(0);
    						
    						if (tickTime == 0) {
    							sender.sendMessage(Parkour.PREFIX + ChatColor.AQUA + args[1] + " has never completed the parkour");
    						}
    						
    						else {
    							String time = CoreUtils.convertTicks(tickTime);
    							sender.sendMessage(Parkour.PREFIX + ChatColor.AQUA + args[1] + "'s best time is: " + time);
    						}
    						
    					});
    					return true;
    					
    				}
    				
    				else {
						FactSQL.getInstance().get(FactSQL.getStatsTable(), player.getUniqueId(), "PARKOURTIME").thenAccept((tickTimeObj) -> {
							
							int tickTime = (int) tickTimeObj;
							
							if (tickTime == 0) {
								sender.sendMessage(Parkour.PREFIX + ChatColor.AQUA + "You have never completed the parkour");
							}
							
							else {
								String time = CoreUtils.convertTicks(tickTime);
								sender.sendMessage(Parkour.PREFIX + ChatColor.AQUA + "Your best time is: " + time);
							}
							
						});
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
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		if (command.getName().equalsIgnoreCase("parkour")) {
			
			if (args.length < 2) return CoreUtils.filter(CoreUtils.toList("time", "quit", "checkpoint"), args[0]);
			
			else if (args[0].equalsIgnoreCase("time")) {
				
				if (args.length == 2) return CoreUtils.filter(BukkitMain.toList(Bukkit.getOnlinePlayers()), args[1]);
				
			}
			
			return CoreUtils.toList();
			
		}
		
		return null;
	}
	
}