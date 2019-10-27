package net.factmc.FactHub.commands;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import net.factmc.FactCore.CoreUtils;
import net.factmc.FactHub.gui.ServerGUI;
import net.md_5.bungee.api.ChatColor;

public class ServersCommand implements CommandExecutor, TabCompleter {
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("servers")) {
        	
        	boolean isPlayer = false;
        	Player player = null;
        	if (sender instanceof Player) {
        		isPlayer = true;
        		player = (Player) sender;
        	}
        	
        	
        	/*if (!isPlayer && args.length > 1 && args[0].equalsIgnoreCase("survivals")) {
        		Player target = Bukkit.getPlayerExact(args[1]);
        		if (target != null) {
        			ServerGUI.open(target, false);
        		}
        	}*/
        	
        	if (isPlayer) {
    			ServerGUI.open(player);
    			return true;
    		}
    		else {
    			sender.sendMessage(ChatColor.RED + "Only in-game players can do that");
    			return false;
    		}
        	
        }
        
		return false;   
    }
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		if (command.getName().equalsIgnoreCase("servers")) {
			
			return CoreUtils.toList();
			
		}
		
		return null;
	}

}