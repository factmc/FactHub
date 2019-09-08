package net.factmc.FactHub.crates;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;

public class CratesCommand implements CommandExecutor {
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("crates")) {
        	
        	boolean isPlayer = false;
        	Player player = null;
        	if (sender instanceof Player) {
        		isPlayer = true;
        		player = (Player) sender;
        	}
        	
    		if (isPlayer) {
    			CratesGUI.open(player);
    			return true;
    		}
    		else {
    			sender.sendMessage(ChatColor.RED + "Only in-game players can do that");
    			return false;
    		}
        	
        }
		return false;   
    }

}