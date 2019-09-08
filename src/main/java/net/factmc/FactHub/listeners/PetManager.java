package net.factmc.FactHub.listeners;

import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.spigotmc.event.entity.EntityMountEvent;

import de.myzelyam.api.vanish.PlayerHideEvent;
import de.myzelyam.api.vanish.PlayerShowEvent;
import net.citizensnpcs.api.npc.NPC;
import net.factmc.FactCore.FactSQLConnector;
import net.factmc.FactHub.Data;
import net.factmc.FactHub.Main;
import net.factmc.FactHub.cosmetics.Pet;

public class PetManager implements Listener {
	
	public static ArrayList<Pet> activePets = new ArrayList<Pet>();
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		UUID uuid = FactSQLConnector.getUUID(player.getName());
		if (uuid == null) {
			Pet pet = new Pet(player, false);
			activePets.add(pet);
			return;
		}
		
		boolean autoSpawn = Data.getBoolean(uuid, "PETAUTOSPAWN");
		Pet pet = new Pet(player, autoSpawn, 20L);
		activePets.add(pet);
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		Pet pet = getPet(player);
		if (pet != null) {
			pet.despawn();
			
			NPC npc = pet.getNPC();
			if (npc != null) npc.destroy();
		}
		
	}
	
	@EventHandler
	public void onPetMounted(EntityMountEvent event) {
		Entity mounter = event.getEntity();
		if (mounter instanceof Player) {
			
			Player player = (Player) mounter;
			Pet pet = getPet(event.getMount());
			if (pet != null) {
				if (player != pet.getOwner()) {
					event.setCancelled(true);
				}
			}
			
		}
	}
	
	@EventHandler
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		Location from = event.getFrom();
		Location to = event.getTo();
		if (from.distance(to) < 10) return;
		
		Player player = event.getPlayer();
		Pet pet = getPet(player);
		if (pet != null) {
			pet.despawn();
			Bukkit.getScheduler().runTaskLater(Main.getPlugin(), new Runnable() {
				@Override
				public void run() {
					pet.spawn(event.getTo().clone(), false);
				}
			}, 20L);
			
		}
	}
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		Player player = event.getEntity();
		Pet pet = getPet(player);
		if (pet != null) {
			pet.despawn();
		}
	}
	
	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		Player player = event.getPlayer();
		Pet pet = getPet(player);
		if (pet != null) {
			pet.spawn(event.getRespawnLocation().clone(), false);
		}
	}
	
	public static void onLoad() {
		for (Player nextPlayer : Bukkit.getOnlinePlayers()) {
			boolean autoSpawn = Data.getBoolean(nextPlayer.getUniqueId(), "PETAUTOSPAWN");
			Pet pet = new Pet(nextPlayer, autoSpawn);
			activePets.add(pet);
		}
	}
	
	public static void onUnload() {
		for (Pet nextPet : activePets) {
			if (nextPet != null) {
				nextPet.despawn();
				
				NPC npc = nextPet.getNPC();
				if (npc != null) npc.destroy();
			}
		}
		
		activePets.clear();
	}
	
	@EventHandler
	public void onPetDamaged(EntityDamageEvent event) {
		Entity entity = event.getEntity();
		DamageCause damage = event.getCause();
		if (damage == DamageCause.VOID) return;
		if (getPet(entity) != null) event.setCancelled(true);
	}
	
	@EventHandler
	public void onPetClicked(PlayerInteractAtEntityEvent event) {
		Entity entity = event.getRightClicked();
		Pet pet = getPet(entity);
		if (pet != null) {
			Player player = event.getPlayer();
			if (player.equals(pet.getOwner())) {
				if (!player.hasPermission("facthub.mount")) {
					player.sendMessage(ChatColor.YELLOW + "You must be " + ChatColor.AQUA + ChatColor.BOLD + "MVP"
							+ ChatColor.YELLOW + " to ride your pet!");
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerVanish(PlayerHideEvent event) {
		getPet(event.getPlayer()).despawn();
	}
	@EventHandler
	public void onPlayerUnvanish(PlayerShowEvent event) {
		Pet pet = getPet(event.getPlayer());
		if (pet.autoSpawns()) pet.spawn(null, false);
	}
	
	
	public Pet getPet(Entity entity) {
		for (Pet nextPet : activePets) {
			if (nextPet.getNPC() != null && nextPet.getNPC().isSpawned()) {
				if (nextPet.getNPC().getEntity() == entity) {
					return nextPet;
				}
			}
		}
		
		return null;
	}
	
	public static Pet getPet(Player player) {
		if (player == null) return null;
		for (Pet nextPet : activePets) {
			if (nextPet.getOwner() == player) {
				return nextPet;
			}
		}
		
		return null;
	}
	
	
	public static Location findGround(Location loc) {
		
		Block block = loc.getBlock();
		if (loc.getBlock().getType() != Material.AIR) {
			while (block.getType() != Material.AIR) {
				
				block = block.getWorld().getBlockAt(block.getLocation().add(0, 1, 0));
				
			}
			return new Location(loc.getWorld(), loc.getX(), block.getY(), loc.getZ());
		}
		
		else {
			
			block = loc.getWorld().getBlockAt(loc.add(0, -1, 0));
			while (block.getType() == Material.AIR) {
				
				block = block.getWorld().getBlockAt(block.getLocation().add(0, -1, 0));
				
			}
			return new Location(loc.getWorld(), loc.getX(), block.getY() + 1, loc.getZ());
			
		}
		
	}
	
}