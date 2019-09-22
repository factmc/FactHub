package net.factmc.FactHub.sidebar;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import net.factmc.FactCore.CoreUtils;
import net.factmc.FactCore.FactSQL;
import net.factmc.FactHub.Main;

public class Sidebar implements Listener, PluginMessageListener {
	
	public static int onlinePlayers = 0;
	public static String title;
	
	public static void load() {
		
		title = ChatColor.translateAlternateColorCodes('&', Main.getConfigStringList("sidebar.title").get(0));
		UpdateSidebar.start(Main.getPlugin());
		
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
		
//		if (Main.factBukkit) {
//			net.factmc.FactBukkit.Main.registerTeams(board);
//		}
		
		String name = title.replaceAll("%player%", player.getName());
		//name = replaceName(name);
		try {board.getObjective("sidebar").unregister();} catch (NullPointerException e) {};
		Objective sidebar = board.registerNewObjective("sidebar", "dummy", name);
		//sidebar.setDisplayName(title.replaceAll("%player%", player.getName()));
		//player.sendMessage(title.replaceAll("%player%", player.getName()));//DEBUG
		sidebar.setDisplaySlot(DisplaySlot.SIDEBAR);
		
		List<String> lines = Main.getConfigStringList("sidebar.lines");
		int length = lines.size();
		
		for (int i = 0; i < lines.size(); i++) {
			
			String raw = convertPlaceholders(lines.get(i), player);
			
			sidebar.getScore(raw).setScore(length);
			length--;
			
		}
		
		player.setScoreboard(board);
		
	}
	
	public static void updatePlayer(Player player) {
		
		Scoreboard board = player.getScoreboard();
		
		Objective sidebar = board.getObjective(DisplaySlot.SIDEBAR);
		if (sidebar == null) return;
		String name = title.replaceAll("%player%", player.getName());
		//name = replaceName(name);
		sidebar.setDisplayName(name);
		
		List<String> lines = Main.getConfigStringList("sidebar.lines");
		int length = lines.size();
		
		for (int i = 0; i < lines.size(); i++) {
			
			String raw = convertPlaceholders(lines.get(i), player);
			
			Score score = getScore(sidebar, length);
			if (!score.getEntry().equals(raw)) {
				board.resetScores(score.getEntry());
				sidebar.getScore(raw).setScore(length);
			}
			
			length--;
			
		}
		
		player.setScoreboard(board);
		
	}
	
	public static String convertPlaceholders(String string, Player player) {
		
		string = ChatColor.translateAlternateColorCodes('&', string);
		string = string
				.replaceAll("%name%", player.getName())
				.replaceAll("%displayname%", player.getDisplayName())
				.replaceAll("%rank%", CoreUtils.getColoredRank(player.getUniqueId()))
				.replaceAll("%votes%", FactSQL.getInstance().get(FactSQL.getStatsTable(), player.getUniqueId(), "TOTALVOTES").toString())
				.replaceAll("%points%", FactSQL.getInstance().get(FactSQL.getStatsTable(), player.getUniqueId(), "POINTS").toString())
				.replaceAll("%online%", String.valueOf(onlinePlayers))
				.replaceAll("%time%", getTime());
		return string;
		
	}
	
	public static Score getScore(Objective objective, int value) {
		
		Set<String> entries = objective.getScoreboard().getEntries();
		int d = 0;
		for (String entry : entries) {
			System.out.println(d + ": " + entry);
			
			Score score = objective.getScore(entry);
			if (score != null) {
				if (score.getScore() == value) {
					return score;
				}
			}
			d++;
			
		}
		
		return null;
		
	}
	
	
	public static void requestPlayerCount() {
		
		if (Bukkit.getOnlinePlayers().size() < 1) {
			return;
		}
		
		ByteArrayDataOutput out = ByteStreams.newDataOutput();
		out.writeUTF("PlayerCount");
		out.writeUTF("ALL");
		
		((Player)Bukkit.getOnlinePlayers().toArray()[0]).sendPluginMessage(Main.getPlugin(), "BungeeCord", out.toByteArray());
		
	}
	
	@Override
	public void onPluginMessageReceived(String channel, Player player, byte[] message) {
		
		if (!channel.equals("BungeeCord")) {
			return;
		}

		ByteArrayDataInput in = ByteStreams.newDataInput(message);
		String subchannel = in.readUTF();
		
		if (subchannel.equals("PlayerCount")) {
			String server = in.readUTF();
			if (server.equals("ALL")) {
				onlinePlayers = in.readInt();
			}
		}
		
	}
	
	
	public static String getTime() {
		
		ZonedDateTime date = ZonedDateTime.now();
		int hour = date.getHour();
		if (hour == 0) hour = 24;
		if (hour > 12) hour -= 12;
		
		String nm = "PM";
		if (date.getHour() < 12) nm = "AM";
		
		String minute = String.valueOf(date.getMinute());
		if (date.getMinute() < 10) minute = "0" + minute;
		
		String second = String.valueOf(date.getSecond());
		if (date.getSecond() < 10) second = "0" + second;
		
		String time = hour + ":" + minute + ":" + second + " " + nm;
		return time;
		
	}
	
}