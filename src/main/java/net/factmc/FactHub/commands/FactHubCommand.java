package net.factmc.FactHub.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.ChatColor;

import net.factmc.FactHub.Main;
import net.factmc.FactHub.bossbar.Bossbar;
import net.factmc.FactHub.bossbar.UpdateBossbar;
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
				
				sender.sendMessage(Main.PREFIX + ChatColor.GREEN + "The plugin has been reloaded");
				return true;
				
			}
			
		}
		
		return false;
	}
	
}