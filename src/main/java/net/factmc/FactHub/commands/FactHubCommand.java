package net.factmc.FactHub.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import java.util.List;

import org.bukkit.ChatColor;

import net.factmc.FactCore.CoreUtils;
import net.factmc.FactHub.Main;
import net.factmc.FactHub.listeners.WorldProtection;
import net.factmc.FactHub.parkour.Parkour;

public class FactHubCommand implements CommandExecutor, TabExecutor {
	
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (command.getName().equalsIgnoreCase("facthub")) {
				
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
		        
		        /*UpdateSidebar.end();
		        Sidebar.load();
		        Main.getPlugin().getLogger().info("Loaded Sidebar");*/
				
				sender.sendMessage(Main.PREFIX + ChatColor.GREEN + "The plugin has been reloaded");
				return true;
				
			}
			
		}
		
		return false;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		if (command.getName().equalsIgnoreCase("facthub")) {
			
			if (args.length < 2) return CoreUtils.filter(CoreUtils.toList("reload"), args[0]);
			
			return CoreUtils.toList();
			
		}
		
		return null;
	}
	
}