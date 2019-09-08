package net.factmc.FactHub.bossbar;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.event.Listener;
import net.factmc.FactHub.Main;

public class Bossbar implements Listener {
	
	public static BossBar Bossbar;
	
	public static void load() {
		
		String startText = UpdateBossbar.getText(0);
		BarColor startColor = UpdateBossbar.getColor(0);
		BarStyle startStyle = BarStyle.valueOf(Main.getConfigString("bossbar.style"));
		
		Bossbar = Bukkit.getServer().createBossBar(startText, startColor, startStyle);
		Bossbar.setVisible(true);
		
		UpdateBossbar.start(Main.getPlugin());
		
	}
	
}