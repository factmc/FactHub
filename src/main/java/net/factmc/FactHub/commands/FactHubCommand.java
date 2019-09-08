package net.factmc.FactHub.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import java.util.UUID;

import org.bukkit.ChatColor;

import net.factmc.FactCore.FactSQLConnector;
import net.factmc.FactHub.Data;
import net.factmc.FactHub.Main;
import net.factmc.FactHub.bossbar.Bossbar;
import net.factmc.FactHub.bossbar.UpdateBossbar;
import net.factmc.FactHub.crates.Util;
import net.factmc.FactHub.listeners.WorldProtection;
import net.factmc.FactHub.parkour.Parkour;

public class FactHubCommand implements CommandExecutor {
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("facthub")) {
				
			if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
				
				if (!sender.hasPermission("facthub.reload")) {
					sender.sendMessage(ChatColor.RED + "You do not have permission to do that");
					return false;
				}
				
				Main.getPlugin().getLogger().info("Reloading...");
				
				Main.getPlugin().reloadConfig();
		    	
		    	/*RegisteredServiceProvider<Permission> permRSP = Main.getPlugin().getServer().getServicesManager().getRegistration(Permission.class);
		        Main.perms = permRSP.getProvider();
		        Main.getPlugin().getLogger().info("Connected to " + Main.perms.getName());*/
		        
		        Parkour.load();
		        WorldProtection.load();
		        
		        UpdateBossbar.end();
		        Bossbar.Bossbar.removeAll();
		        Bossbar.load();
		        Main.getPlugin().getLogger().info("Loaded Bossbar");
		        
		        /*UpdateSidebar.end();
		        Sidebar.load();
		        Main.getPlugin().getLogger().info("Loaded Sidebar");*/
		        
		        Util.getAllItems();
		        Main.getPlugin().getLogger().info("Prepared Crate System");
				
				sender.sendMessage(Main.PREFIX + ChatColor.GREEN + "The plugin has been reloaded");
				return true;
				
			}
			
			else if (args.length > 0 && args[0].equalsIgnoreCase("access")) {
				
				if (!sender.hasPermission("facthub.access")) {
					sender.sendMessage(ChatColor.RED + "You do not have permission to do that");
					return false;
				}
				
				if (args.length < 3) {
					sender.sendMessage(ChatColor.RED + "Usage: /" + label + " access <grant|revoke|check> <player> [category] [cosmetic]");
					return false;
				}
				
				if (args.length > 1 && args.length < 5 && !args[1].equalsIgnoreCase("check")) {
					sender.sendMessage(ChatColor.RED + "Usage: /" + label + " access <grant|revoke> <player> <category> <cosmetic>");
					return false;
				}
				
				UUID uuid = FactSQLConnector.getUUID(args[2]);
				if (uuid == null) {
					sender.sendMessage(Main.PREFIX + ChatColor.YELLOW + "Unable to find " + args[2]);
					return false;
				}
				
				String category = args[3].toUpperCase();
				String cosmetic = args[4].toUpperCase();
				if (args[1].equalsIgnoreCase("grant")) {
					
					if (Data.giveAccess(uuid, category, cosmetic))
						sender.sendMessage(Main.PREFIX + ChatColor.GREEN + args[2] + " now has access to " + cosmetic + " in " + category);
					else
						sender.sendMessage(Main.PREFIX + ChatColor.YELLOW + args[2] + " already has access to " + cosmetic + " in " + category);
					return true;
					
				}
				else if (args[1].equalsIgnoreCase("revoke")) {
					
					if (Data.removeAccess(uuid, category, cosmetic))
						sender.sendMessage(Main.PREFIX + ChatColor.GREEN + args[2] + " no longer has access to " + cosmetic + " in " + category);
					else
						sender.sendMessage(Main.PREFIX + ChatColor.YELLOW + args[2] + " never had access to " + cosmetic + " in " + category);
					return true;
					
				}
				else if (args[1].equalsIgnoreCase("check")) {
					
					boolean has = Data.checkAccess(uuid, category, cosmetic);
					String hasText = " has ";
					if (!has) {
						hasText = " does not have ";
					}
					sender.sendMessage(Main.PREFIX + ChatColor.GREEN + args[2] + hasText + "access to " + cosmetic + " in " + category);
					return true;
					
				}
				
				else {
					sender.sendMessage(ChatColor.RED + "Usage: /" + label + " access <grant|revoke|check> <player> [category] [cosmetic]");
					return false;
				}
				
			}
			
			sender.sendMessage(ChatColor.RED + "Usage: /" + label + " <reload|access>");
			return false;
			
		}
		
		return false;
	}
	
}