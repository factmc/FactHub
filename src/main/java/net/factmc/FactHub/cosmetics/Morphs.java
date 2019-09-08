package net.factmc.FactHub.cosmetics;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager.Profession;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;
import me.libraryaddict.disguise.disguisetypes.MobDisguise;
import me.libraryaddict.disguise.disguisetypes.watchers.SheepWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.VillagerWatcher;
import net.factmc.FactHub.Data;
import net.factmc.FactHub.Main;
import net.factmc.FactHub.gui.select.DyeColorGUI;

public class Morphs {
	
	public static void setMorph(Player player, EntityType morph, boolean baby, boolean viewSelf, DyeColor sheepColor) {
		if (!Main.morphs) {
			player.sendMessage(ChatColor.RED + "Sorry, morphs are currently disabled. Please contact an admin if you believe this is an error");
			return;
		}
		
		Morphs.unmorph(player);
		if (sheepColor != null) {
			Morphs.morph(player, morph, baby, viewSelf, sheepColor);
		}
		else {
			Morphs.morph(player, morph, baby, viewSelf);
		}
		
	}
	
	
	public static int taskID;
	
	public static boolean morph(Player player, EntityType morph, boolean baby, boolean viewSelf) {
		if (!Main.morphs) {
			player.sendMessage(ChatColor.RED + "Sorry, morphs are currently disabled. Please contact an admin if you believe this is an error");
			return false;
		}
		if (player == null) return false;
		if (DisguiseAPI.isDisguised(player)) return false;
		
		DisguiseType type = DisguiseType.getType(morph);
		MobDisguise disguise = new MobDisguise(type, !baby);
		disguise.setKeepDisguiseOnPlayerDeath(true);
		disguise.setViewSelfDisguise(viewSelf);
		FlagWatcher watcher = disguise.getWatcher();
		
		/*String prefix = "";
		prefix = "[" + CoreUtils.getColoredRank(Main.perms.getPrimaryGroup(player)) + ChatColor.RESET + "]";
		String name = prefix + player.getDisplayName();
		watcher.setCustomName(name);*/
		watcher.setCustomNameVisible(false);
		
		if (watcher instanceof VillagerWatcher) {
			VillagerWatcher flags = (VillagerWatcher) watcher;
			flags.setProfession(Profession.FARMER);
			disguise.setWatcher(flags);
		}
		else disguise.setWatcher(watcher);
		
		disguise.setEntity(player);
		disguise.startDisguise();
		
		DisguiseAPI.disguiseToAll(player, disguise);
		return true;
	}
	
	public static void changeViewSelf(Player player, boolean viewSelf) {
		if (!Main.morphs) {
			player.sendMessage(ChatColor.RED + "Sorry, morphs are currently disabled. Please contact an admin if you believe this is an error");
			return;
		}
		if (player == null) return;
		getMorph(player).setViewSelfDisguise(viewSelf);
	}
	
	public static boolean morph(Player player, EntityType morph, boolean baby, boolean viewSelf, DyeColor sheepColor) {
		if (!Main.morphs) {
			player.sendMessage(ChatColor.RED + "Sorry, morphs are currently disabled. Please contact an admin if you believe this is an error");
			return false;
		}
		if (!morph(player, morph, baby, viewSelf)) return false;
		
		FlagWatcher watcher = getMorph(player).getWatcher();
		if (!(watcher instanceof SheepWatcher)) {
			unmorph(player);
			return false;
		}
		
		SheepWatcher flags = (SheepWatcher) watcher;
		flags.setColor(sheepColor);
		return true;
	}
	
	public static boolean unmorph(Player player) {
		if (!Main.morphs) {
			player.sendMessage(ChatColor.RED + "Sorry, morphs are currently disabled. Please contact an admin if you believe this is an error");
			return false;
		}
		if (player == null) return false;
		if (!DisguiseAPI.isDisguised(player)) return false;
		
		DisguiseAPI.getDisguise(player).stopDisguise();
		DisguiseAPI.undisguiseToAll(player);
		
		return true;
	}
	
	public static Disguise getMorph(Player player) {
		if (!Main.morphs) return null;
		if (player == null) return null;
		if (!DisguiseAPI.isDisguised(player)) return null;
		
		Disguise disguise = DisguiseAPI.getDisguise(player);
		return disguise;
	}
	
	public static boolean isMorphed(Player player) {
		if (!Main.morphs) return false;
		if (player == null) return false;
		return DisguiseAPI.isDisguised(player);
	}
	
	public static void onLoad() {
		if (!Main.morphs) return;
		taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getPlugin(), new Runnable() {

			@Override
			public void run() {
				for (Player player : Bukkit.getOnlinePlayers()) {
					final Disguise morph = Morphs.getMorph(player);
					if (morph == null) return;
					
					if (morph.getType().getEntityType() == EntityType.SHEEP
							&& Data.getSelected(player.getUniqueId(), "MORPHCOLOR")
							.equalsIgnoreCase("RAINBOW")) {
						
						FlagWatcher watcher = morph.getWatcher();
						if (watcher instanceof SheepWatcher) {
							SheepWatcher flags = (SheepWatcher) watcher;
							flags.setColor(DyeColorGUI.getRandom());
							morph.setWatcher(flags);
							
							MobDisguise change = new MobDisguise(morph.getType(),
									Data.getBoolean(player.getUniqueId(), "MORPHBABY"));
							
							flags.setCustomName(player.getName());
							flags.setCustomNameVisible(true);
							
							change.setWatcher(flags);
							change.setEntity(player);
							change.startDisguise();
							DisguiseAPI.disguiseToAll(player, change);
							
							Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), new Runnable() {

								@Override
								public void run() {
									morph.stopDisguise();
									morph.removeDisguise();
								}
								
							}, 20L);
							
						}
						
					}
					
				}
			}
			
		}, 0L, 30L);
	}
	
	public static void onUnload() {
		if (!Main.morphs) return;
		Bukkit.getScheduler().cancelTask(taskID); 
	}
	
}