package net.factmc.FactHub.crates;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.ChatColor;

import net.factmc.FactCore.FactSQLConnector;
import net.factmc.FactHub.Data;
import net.factmc.FactHub.Main;

public class ScrollTask implements Runnable {
	
	public ScrollTask(Inventory gui, Player player, int level) {
		this.gui = gui;
		this.player = player;
		this.level = level;
		
		this.slowAt = Util.randInt(60, 130);
		this.taskid = Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getPlugin(), this, 0L, 1L);
	}
	
	private int taskid;
	
	private Inventory gui;
	private Player player;
	private int level;
	
	private int slowAt;
	private int speed = 2;
	private int tick = 0;
	private float pitch = 0.75F;
	private float change;
	
	@Override
	public void run() {
		// Open GUI
		if (!gui.getViewers().contains(player)) {
			player.openInventory(gui);
		}
		
		// Update Panes
		if (tick % 4 == 0) {
			for (int i = 0; i < 18; i++) {
				if (i != 4 && i != 13) {
					int j = i;
					if (j > 8) j += 9;
					gui.setItem(j, Util.randomPane());
				}
			}
		}
		
		if (tick % speed == 0) {
			
			if (!(speed == 20 && tick >= slowAt)) {
				// Scroll items
				for (int i = 16; i > 8; i--) {
					ItemStack item = gui.getItem(i);
					gui.setItem(i + 1, item);
				}
				
				// Play sound
				//player.playSound(player.getLocation(), Sound.BLOCK_NOTE_SNARE, SoundCategory.MASTER, 0.3f, 1.1f);
				player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, SoundCategory.MASTER, 0.3f, pitch);
				if (pitch == 1.25) change = -0.25F;
				if (pitch == 0.75) change = 0.25F;
				pitch += change;
				
				// Pick next item
				ItemStack next = Util.randomItem(level);
				gui.setItem(9, next);
			}
			
			// Pick next slow time
			if (speed == 2 && tick >= slowAt) {
				slowAt = Util.randInt(40, 115);
				speed = 3;
				tick = 0;
			}
			else if (speed == 3 && tick >= slowAt) {
				slowAt = Util.randInt(40, 100);
				speed = 5;
				tick = 0;
			}
			else if (speed == 5 && tick >= slowAt) {
				slowAt = Util.randInt(30, 80);
				speed = 10;
				tick = 0;
			}
			else if (speed == 10 && tick >= slowAt) {
				slowAt = Util.randInt(20, 75);
				speed = 15;
				tick = 0;
			}
			else if (speed == 15 && tick >= slowAt) {
				slowAt = Util.randInt(20, 60);
				speed = 20;
				tick = 0;
			}
			else if (speed == 20 && tick >= slowAt) {
				this.cancel();
				
				ItemStack empty = new ItemStack(Material.AIR);
				gui.setItem(9, empty); gui.setItem(10, empty);
				gui.setItem(11, empty); gui.setItem(12, empty);
				gui.setItem(14, empty); gui.setItem(15, empty);
				gui.setItem(16, empty); gui.setItem(17, empty);
				
				FileConfiguration valueData = Data.getValueData();
				
				ItemStack item = gui.getItem(13);
				List<String> lore = item.getItemMeta().getLore();
 				int value = Util.getValueByName(lore.get(0), valueData);
				Sound sound = Sound.ENTITY_EXPERIENCE_ORB_PICKUP;
				float pitch = 2;
				
				if (value == 2) {
					sound = Sound.ENTITY_ARROW_HIT_PLAYER;
					pitch = 1;
				}
				else if (value == 3) {
					sound = Sound.ENTITY_PLAYER_LEVELUP;
					pitch = 1;
				}
				else if (value == 4) {
					sound = Sound.ENTITY_EVOKER_PREPARE_SUMMON;
					pitch = 1;
				}
				else if (value == 5) {
					sound = Sound.UI_TOAST_CHALLENGE_COMPLETE;
					pitch = 1;
				}
				
				String type = lore.get(1).replaceAll(ChatColor.GRAY + "", "");
				String fullName = Util.getNameByValue(value, valueData)
						+ " " + item.getItemMeta().getDisplayName()
						+ " " + ChatColor.LIGHT_PURPLE + type;
				String a = "a ";
				String lowerName = ChatColor.stripColor(fullName.toLowerCase());
				if (lowerName.startsWith("a") || lowerName.startsWith("e")
						|| lowerName.startsWith("i") || lowerName.startsWith("o")
						|| lowerName.startsWith("u")) a = "an ";
				
				Main.getPlugin().getLogger().info(ChatColor.stripColor(CratesGUI.prefix + player.getName() + " won " + a + fullName));
				
				player.playSound(player.getLocation(), sound, SoundCategory.MASTER, 1, pitch);
				player.sendMessage(CratesGUI.prefix + ChatColor.LIGHT_PURPLE + "You won " + a
						+ fullName + ChatColor.LIGHT_PURPLE + "!");
				
				if (value > 3) {
					for (Player nextPlayer : Bukkit.getOnlinePlayers()) {
						if (nextPlayer != player) {
							
							nextPlayer.playSound(nextPlayer.getLocation(), sound, SoundCategory.MASTER, 1, pitch);
							nextPlayer.sendMessage(CratesGUI.prefix + ChatColor.LIGHT_PURPLE + player.getName()
									+ " won " + a + fullName + ChatColor.LIGHT_PURPLE + "!");
							
						}
					}
				}
				
				UUID uuid = player.getUniqueId();
				String[] cos = Util.getPath(item);
				
				if (cos[0].equalsIgnoreCase("points")) {
					int amount = Integer.parseInt(cos[1]);
					FactSQLConnector.changePoints(uuid, amount);
				}
				
				else {
					boolean has = Data.checkAccess(uuid, cos[0].toUpperCase(), cos[1].toUpperCase());
					//player.sendMessage("Check: " + cos[0].toUpperCase() + " - " + cos[1].toUpperCase() + " - " + has);//DEBUG
					
					if (has) {
						int points = valueData.getInt("value." + value + ".cost") / 4;
						FactSQLConnector.changePoints(uuid, points);
						
						player.sendMessage(CratesGUI.prefix + ChatColor.LIGHT_PURPLE
								+ "You were given " + ChatColor.GOLD + points + " Points"
								+ ChatColor.LIGHT_PURPLE + " because you already had that item");
					}
				}
				
				Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), new Runnable() {
					
					@Override
					public void run() {
						player.closeInventory();
						gui.clear();
					}
					
				}, 30L);
				
				return;
			}
		}
		
		tick++;
		
	}
	
	private boolean cancel() {
		if (this.taskid != -1) {
			Bukkit.getScheduler().cancelTask(this.taskid);
			this.taskid = -1;
			return true;
		}
		return false;
	}
	
}