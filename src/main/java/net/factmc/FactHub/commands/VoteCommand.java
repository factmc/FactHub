package net.factmc.FactHub.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.List;

import org.bukkit.ChatColor;

import net.factmc.FactCore.CoreUtils;
import net.factmc.FactHub.Main;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.ClickEvent.Action;

public class VoteCommand implements CommandExecutor, TabCompleter {
	
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("vote")) {
        	
        	sender.sendMessage(ChatColor.DARK_GREEN + "Voting Sites: " + ChatColor.GOLD + "(Click One)");
        	for (String path : Main.getPlugin().getConfig().getConfigurationSection("sites").getKeys(false)) {
        		
        		String name = Main.getConfigString("sites." + path + ".name");
        		String link = Main.getConfigString("sites." + path + ".url");
        		
    			TextComponent tc = new TextComponent(ChatColor.LIGHT_PURPLE + "- " + name);
    			tc.setClickEvent(new ClickEvent(Action.OPEN_URL, link));
    			
    			sender.spigot().sendMessage(tc);
    			
        		/*String json = "{\"text\":\"- " + name + "\",\"color\":\"light_purple\","
						+ "\"clickEvent\":{\"action\":\"open_url\",\"value\":\"" + link + "\"}}";
				Packet<?> packet = new PacketPlayOutChat(ChatSerializer.a(json));
				((CraftPlayer) sender).getHandle().playerConnection.sendPacket(packet);*/
        		
        	}
        	
        }
        
		return false;   
    }
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		if (command.getName().equalsIgnoreCase("vote")) {
			
			return CoreUtils.toList();
			
		}
		
		return null;
	}

}