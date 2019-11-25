package net.factmc.FactHub.sidebar;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import net.factmc.FactCore.bukkit.CustomSidebar;
import net.factmc.FactHub.Main;

public class Sidebar implements Listener {
	
	public static int onlinePlayers = 0;
	private static CustomSidebar sidebar;
	
	public static void load() {
		
		String title = Main.getConfigString("sidebar.title");
		ChatColor first = ChatColor.valueOf(Main.getPlugin().getConfig().getString("sidebar.first-color"));
		ChatColor second = ChatColor.valueOf(Main.getPlugin().getConfig().getString("sidebar.second-color"));
		List<String> lines = Main.getConfigStringList("sidebar.lines");
		long rate = (long) (Main.getPlugin().getConfig().getDouble("sidebar.rate") * 20);
		
		sidebar = new CustomSidebar(Main.getPlugin(), new ArrayList<Scoreboard>(), title, first.toString(), second.toString(), lines, rate);
		
		for (Player player : Bukkit.getOnlinePlayers()) {
			loadPlayer(player);
		}
		
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerJoin(PlayerJoinEvent event) {
		loadPlayer(event.getPlayer());
	}
	
	
	
	public static void loadPlayer(Player player) {
		
		ScoreboardManager manager = Bukkit.getServer().getScoreboardManager();
		Scoreboard main = manager.getMainScoreboard();
		
		Scoreboard board = player.getScoreboard();
		if (board == main) {
			board = manager.getNewScoreboard();
		}
		
		sidebar.add(board);
		player.setScoreboard(board);
		
	}
	
}