package net.factmc.FactHub.parkour;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.spigotmc.event.entity.EntityMountEvent;

import net.factmc.FactCore.CoreUtils;
import net.factmc.FactCore.FactSQLConnector;
import net.factmc.FactHub.Main;

public class Parkour implements Listener {
	
	public static final ItemStack PARKOUR_WAND = new ItemStack(Material.SPECTRAL_ARROW);
	
	// [Player player, int ticks, int checkpoint, boolean inParkour]
	public static Map<Player, Object[]> currentRuns = new HashMap<Player, Object[]>();
	public static List<Player> allowTeleport = new ArrayList<Player>();
	
	public static List<Material> legalBlocks = new ArrayList<Material>();
	public static final String prefix = ChatColor.DARK_GRAY + "[" + ChatColor.BLUE
			+ "Parkour" + ChatColor.DARK_GRAY + "] ";
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		Object[] array = {0, -1, false};
		currentRuns.put(player, array);
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		currentRuns.remove(player);
	}
	
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onOpenCosmetics(PlayerCommandPreprocessEvent event) {
		if (event.getMessage().equals("/cosmetics") && inParkour(event.getPlayer())) {
			
			event.getPlayer().sendMessage(prefix + ChatColor.RED + "You cannot use cosmetics during the parkour. Use "
					+ ChatColor.GOLD + "/parkour quit" + ChatColor.RED + " to quit the parkour");
			event.setCancelled(true);
			
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onWandUsed(PlayerInteractEvent event) {
		
		Player player = event.getPlayer();
		
		ItemStack hand = player.getInventory().getItemInMainHand();
		if (event.getAction() != Action.PHYSICAL && hand != null) {
			
			if (hand.getType() == PARKOUR_WAND.getType() && inParkour(player)) {
				
				Object[] array = currentRuns.get(player);
				int index = (int) array[1];
				Location loc;
				try {
					loc = Parkour.getCheckpoint(index-1);
				} catch (IndexOutOfBoundsException e) {
					loc = Parkour.getFinish();
				}
				
				allowTeleport.add(player);
				player.teleport(loc);
				
				event.setCancelled(true);
				
			}
		}
		
	}
	
	
	public static void leave(Player player) {
		
		Object[] newArray = {0, -1, false};
		currentRuns.put(player, newArray);
		
		player.getInventory().setItem(3, new ItemStack(Material.AIR));
		if (player.hasPermission("essentials.fly")) {
			player.setAllowFlight(true);
		}
		
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		boolean inParkour = inParkour(player);
		
		if(inParkour) {
			Object[] array = currentRuns.get(player);
			int checkpoint = (int) array[1];
			
			if (player.isOnGround()) {
				
				boolean illegal = false;
				Material on = player.getWorld().getBlockAt(
						player.getLocation().add(0, -1, 0)).getType();
				if (on != Material.AIR && !legalBlocks.contains(on)) {
					illegal = true;
				}
				
				else if (on == Material.AIR) {
					for (int i = -1; i < 2; i++) {
						for (int j = -1; j < 2; j++) {
							Material mat = player.getWorld().getBlockAt(
									player.getLocation().add(i, -1, j)).getType();
							if (mat != Material.AIR && !legalBlocks.contains(mat)) {
								illegal = true;
								break;
							}
						}
						if (illegal) {
							break;
						}
					}
				}
				
				
				if (illegal) {
					Location loc;
					try {
						loc = getCheckpoint(checkpoint-1);
					} catch (IndexOutOfBoundsException e) {
						loc = getFinish();
					}
					
					allowTeleport.add(player);
					player.teleport(loc);
				}
			}
			
			Location loc;
			boolean finish = false;
			try {
				loc = getCheckpoint(checkpoint);
			} catch (IndexOutOfBoundsException e) {
				loc = getFinish();
				finish = true;
			}
			
			if (getBlockLocation(player).distance(loc) < 2) {
				if (finish) {
					player.sendMessage(prefix + ChatColor.AQUA + "Congratulations! You reached the end of the parkour"
							+ " in " + CoreUtils.convertToTime((int) array[0]));
					
					int record = FactSQLConnector.getIntValue(FactSQLConnector.getStatsTable(), player.getUniqueId(), "PARKOURTIME");
					if (record == 0 || (int) array[0] < record) {
						player.sendMessage(ChatColor.GREEN + "That's a new record for you!");
						FactSQLConnector.setValue(FactSQLConnector.getStatsTable(), player.getUniqueId(), "PARKOURTIME", (int) array[0]);
					}
					else {
						player.sendMessage(ChatColor.RED + "That did not beat your old record of "
								+ CoreUtils.convertToTime(record));
					}
					
					leave(player);
					
				}
				
				else {
					if (checkpoint-1 > -1) {
						player.sendMessage(prefix + ChatColor.GREEN + "Checkpoint!" + ChatColor.AQUA + " Use "
								+ ChatColor.GOLD + "/parkour checkpoint" + ChatColor.AQUA + " to come back here");
					}
					
					Object[] newArray = {array[0], checkpoint + 1, true};
					currentRuns.put(player, newArray);
				}
				
				
			}
			
		}
		
		else {
			if (getBlockLocation(player).distance(getStart()) < 2) {
				player.setFlying(false);
				player.setAllowFlight(false);
				FactSQLConnector.setValue(FactSQLConnector.getOptionsTable(), player.getUniqueId(), "SUIT", "NONE");
				
				Object[] newArray = {0, -1, true};
				currentRuns.put(player, newArray);
				
				player.getInventory().setItem(3, PARKOUR_WAND);
				
				player.sendMessage(prefix + ChatColor.AQUA + "You have started the parkour. Use "
						+ ChatColor.GOLD + "/parkour quit" + ChatColor.AQUA + " to quit");
			}
		}
		
	}
		
	
	@EventHandler
	public void onFlyEnabled(PlayerToggleFlightEvent event) {
		if (event.isFlying()) {
			Player player = event.getPlayer();
			if (inParkour(player)) {
				event.setCancelled(true);
				player.setAllowFlight(false);
				player.sendMessage(prefix + ChatColor.RED + "You cannot fly during the parkour. Use "
						+ ChatColor.GOLD + "/parkour quit" + ChatColor.RED + " to quit the parkour");
			}
		}
	}
	
	@EventHandler
	public void onTeleport(PlayerTeleportEvent event) {
		
		Player player = event.getPlayer();
		if (inParkour(player) && (!allowTeleport.contains(player) && event.getCause() != TeleportCause.UNKNOWN)) {
			event.setCancelled(true);
			player.sendMessage(prefix + ChatColor.RED + "You cannot teleport during the parkour. Use "
					+ ChatColor.GOLD + "/parkour quit" + ChatColor.RED + " to quit the parkour");
			
		}
		
		else if (allowTeleport.contains(player)) {
			allowTeleport.remove(player);
		}
		
	}
	
	@EventHandler
	public void onEntityMounted(EntityMountEvent event) {
		
		if (event.getEntity() instanceof Player) {
			
			Player player = (Player) event.getEntity();
			if (inParkour(player)) {
				
				event.setCancelled(true);
				player.sendMessage(prefix + ChatColor.RED + "You cannot mount entities during the parkour. Use "
						+ ChatColor.GOLD + "/parkour quit" + ChatColor.RED + " to quit the parkour");
				
			}
			
		}
		
	}
	
	
	public static Location getStart() {
		String coords = Main.getPlugin().getConfig().getString("parkour.start");
		String[] array = coords.split(",");
		
		try {
			int x = Integer.parseInt(array[0]);
			int y = Integer.parseInt(array[1]);
			int z = Integer.parseInt(array[2]);
			float yaw = Float.parseFloat(array[3]);
			float pitch = Float.parseFloat(array[4]);
			
			Location loc = new Location(Bukkit.getWorlds().get(0), 
					x+0.5, y, z+0.5, yaw, pitch);
			return loc;
			
			
		} catch (NumberFormatException | IndexOutOfBoundsException e) {
			Main.getPlugin().getLogger().info("Invalid start location for parkour");
			return null;
		}
		
	}
	
	public static Location getFinish() {
		String coords = Main.getPlugin().getConfig().getString("parkour.finish");
		String[] array = coords.split(",");
		
		try {
			int x = Integer.parseInt(array[0]);
			int y = Integer.parseInt(array[1]);
			int z = Integer.parseInt(array[2]);
			float yaw = Float.parseFloat(array[3]);
			float pitch = Float.parseFloat(array[4]);
			
			Location loc = new Location(Bukkit.getWorlds().get(0), 
					x+0.5, y, z+0.5, yaw, pitch);
			return loc;
			
			
		} catch (NumberFormatException | IndexOutOfBoundsException e) {
			Main.getPlugin().getLogger().info("Invalid finish location for parkour");
			return null;
		}
		
	}
	
	public static Location getCheckpoint(int index) throws IndexOutOfBoundsException {
		if (index < 0) {
			return getStart();
		}
		
		String coords = Main.getPlugin().getConfig().getStringList("parkour.checkpoints").get(index);
		String[] array = coords.split(",");
		
		try {
			int x = Integer.parseInt(array[0]);
			int y = Integer.parseInt(array[1]);
			int z = Integer.parseInt(array[2]);
			float yaw = Float.parseFloat(array[3]);
			float pitch = Float.parseFloat(array[4]);
			
			Location loc = new Location(Bukkit.getWorlds().get(0), 
					x+0.5, y, z+0.5, yaw, pitch);
			return loc;
			
			
		} catch (NumberFormatException e) {
			int i = index + 1;
			Main.getPlugin().getLogger().info("Invalid location for parkour checkpoint " + i);
			return null;
		}
		
	}
	
	public static boolean inParkour(Player player) {
		return (boolean) currentRuns.get(player)[2];
	}
	
	private static Location getBlockLocation(Player player) {
		int x = player.getLocation().getBlockX();
		int y = player.getLocation().getBlockY();
		int z = player.getLocation().getBlockZ();
		
		return new Location(player.getWorld(), x+0.5, y, z+0.5);
	}
	
	
	
	public static void load() {
		
		for (Player player : Bukkit.getServer().getOnlinePlayers()) {
			Object[] array = {0, -1, false};
			currentRuns.put(player, array);
		}
		
		
		ItemMeta meta = Parkour.PARKOUR_WAND.getItemMeta();
		meta.setDisplayName(ChatColor.BLUE + "Parkour Wand");
		meta.addEnchant(Enchantment.DURABILITY, -1, true);
		meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		
		List<String> lore = new ArrayList<String>();
		lore.add(ChatColor.GRAY + "Click to teleport to");
		lore.add(ChatColor.GRAY + "your last checkpoint");
		meta.setLore(lore);
		
		Parkour.PARKOUR_WAND.setItemMeta(meta);
		
		legalBlocks.clear();
		for (String block : Main.getConfigStringList("parkour.allow-touch")) {
			
			legalBlocks.add(Material.valueOf(block));
			
		}
		
	}
	
}