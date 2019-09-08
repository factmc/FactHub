package net.factmc.FactHub.commands;

import java.util.UUID;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.factmc.FactCore.FactSQLConnector;
import net.factmc.FactHub.gui.CosmeticsGUI;
import net.md_5.bungee.api.ChatColor;

public class CosmeticsCommand implements CommandExecutor {
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("cosmetics")) {
        	
        	boolean isPlayer = false;
        	Player player = null;
        	if (sender instanceof Player) {
        		isPlayer = true;
        		player = (Player) sender;
        	}
        	if (!isPlayer) {
        		sender.sendMessage(ChatColor.RED + "Only in-game players can do that");
        		return false;
        	}
        	
        	if (args.length == 0) {
        		if (isPlayer) CosmeticsGUI.open(player, player.getName());
        		return true;
        	}
        	
        	else if (args.length == 1) {
        		if (!sender.hasPermission("facthub.cosmetics.others")) {
        			if (isPlayer) CosmeticsGUI.open(player, player.getName());
        			return true;
        		}
        		
        		UUID uuid = FactSQLConnector.getUUID(args[0]);
        		if (uuid == null) {
        			sender.sendMessage(ChatColor.RED + "Unable to get data for " + args[0]);
        			return false;
        		}
        		CosmeticsGUI.open(player, args[0]);
        		return true;
        	}
        	
        	if (isPlayer) CosmeticsGUI.open(player, player.getName());
        	return true;
        	
        }
		return false;   
    }

}