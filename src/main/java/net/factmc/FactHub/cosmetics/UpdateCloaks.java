package net.factmc.FactHub.cosmetics;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import de.myzelyam.api.vanish.VanishAPI;
import net.factmc.FactHub.Data;
import net.factmc.FactHub.Main;

public class UpdateCloaks implements Runnable, Listener {
	
	private static List<String[]> particleData = new ArrayList<String[]>();
	
	private static void copyData() {
		particleData.clear();
		for (Player player : Bukkit.getOnlinePlayers()) {
			UUID uuid = player.getUniqueId();
			String[] data = Data.getSelected(uuid, "TRAIL", "CLOAK");
			
			particleData.add(new String[] {uuid.toString(), data[0], data[1]});
		}
	}
	
	private static String[] getArray(UUID uuid) {
		for (String[] array : particleData) {
			UUID check = UUID.fromString(array[0]);
			if (check.equals(uuid)) {
				return array;
			}
		}
		return null;
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerJoin(PlayerJoinEvent event) {
		copyData();
	}
	
	

    // Main class for bukkit scheduling
    private static JavaPlugin plugin;
    
    // Our scheduled task's assigned id,needed for canceling
    private static Integer assignedTaskId;
    
    public static void start(JavaPlugin plugin) {
        // Initializing fields
        UpdateCloaks.plugin = plugin;
        copyData();
        schedule();
    }
    
    private int tick;
    private int yaw = 0;
    
    private int reload = 0;
    
    public void run() {
    	if (reload > 39) { copyData(); reload = 0; }
    	else reload++;
    	
    	for (Player player : Bukkit.getOnlinePlayers()) {
    		if (player.getGameMode() != GameMode.SPECTATOR && !(Main.sv && VanishAPI.isInvisible(player))) {
    			String[] array = getArray(player.getUniqueId());
	    		String cloakShape = array[2];
	    		String cloakParticle = "NONE";
	    		int rate = 1;
	    		for (String cloak : Data.getCloakData().getConfigurationSection("cloaks").getKeys(false)) {
	        		if (cloakShape.equalsIgnoreCase(cloak)) {
	        			cloakParticle = Data.getCloakData().getString("cloaks." + cloak + ".particle");
	        			rate = Data.getCloakData().getInt("cloaks." + cloak + ".rate");
	        		}
	        	}
	    		
	    		if (!cloakParticle.equalsIgnoreCase("NONE") && !cloakShape.equalsIgnoreCase("NONE")
	    				&& tick % rate == 0) {
	    			
		    		try {
		    			Particle particle = Particle.valueOf(cloakParticle.toUpperCase());
		    			spawnCloak(player, particle, cloakShape);
		    			
		    		} catch (IllegalArgumentException e) {
		    			Main.getPlugin().getLogger().warning("The cloak particle for " + player.getName() + ": '" + cloakParticle + "' is not valid");
		    			Data.setSelected(player.getUniqueId(), "CLOAK", "NONE");
		    		}
		    		
	    		}
    		}
    	}
    	
    	tick++;
    	if (tick > 40) tick = 1;
    	
    	if (yaw >= 350) yaw = 0;
    	else yaw += 10;
    	
    }
    
    public void spawnCloak(Player player, Particle particle, String shape) {
    	
    	FileConfiguration cloakData = Data.getCloakData();
    	String pointsPath = null;
    	for (String cloak : cloakData.getConfigurationSection("cloaks").getKeys(false)) {
    		if (shape.equalsIgnoreCase(cloak)) {
    			pointsPath = "cloaks." + cloak + ".points";
    			break;
    		}
    	}
    	if (pointsPath == null) {
    		Main.getPlugin().getLogger().warning("The cloak shape for " + player.getName() + ": '" + shape + "' is not valid");
    		Data.setSelected(player.getUniqueId(), "CLOAK", "NONE");
    		return;
    	}
    	
    	List<Object[]> points = getPoints(cloakData.getStringList(pointsPath));
    	if (points.isEmpty()) return;
    	
    	for (Object[] point : points) {
    		
        	Location playerLoc = player.getLocation().add(0, 1.2, 0);
        	playerLoc.setPitch(0);
        	playerLoc.setYaw(playerLoc.getYaw() + 180);
        	Vector vec = playerLoc.getDirection().multiply(0.1875);
        	Location baseLoc = playerLoc.add(vec);
    		
			try {
    			double side = 0.2 * Double.parseDouble(point[0].toString());
    			double height = 0.2 * Double.parseDouble(point[1].toString());
    			
				Location loc = getOffset(baseLoc, side, height);
    	    	
    	    	//Main.getPlugin().getLogger().info("Base Location: " + String.valueOf(baseLoc));//DEBUG
				Object data = null;
    			if (particle == Particle.REDSTONE)  {
    				Color color = (Color) point[2];
    				data = new DustOptions(color, 1);
    				
    				/*PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(EnumParticle.REDSTONE, true,
    						(float) loc.getX(), (float) loc.getY(), (float) loc.getZ(),
    						color.getRed(), color.getGreen(), color.getBlue(), 0, 0);*/
    				
    			}
    			
    			for (Player p : Bukkit.getOnlinePlayers()) {
            		if (p.canSee(player))
            				p.spawnParticle(particle, loc, 1, data);
            	}
    			
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
    	}
    	
    }
    
    private Location getOffset(final Location location, final double side, final double height) {
    	Vector vec = null;
    	double absSide = Math.abs(side);
    	if (side < 0) vec = rotateLocation(location, 90).getDirection().multiply(absSide);
    	else if (side > 0) vec = rotateLocation(location, -90).getDirection().multiply(absSide);
    	else vec = null;
    	
    	Location loc = null;
    	if (vec != null) loc = location.add(vec);
    	else loc = location;
    	
    	loc.add(0, height, 0);
    	
    	return loc;
    }
    
    private Location rotateLocation(Location startLoc, float amount) {
    	float yaw = startLoc.getYaw() + amount;
    	startLoc.setYaw(yaw);
    	return startLoc;
    }
    
    private List<Object[]> getPoints(List<String> list) {
    	
    	List<Object[]> finalPoints = new ArrayList<Object[]>();
    	
    	for (String cPoint : list) {
    		
    		String[] a = cPoint.split("=");
    		String point = a[0];
    		
    		Color color = null;
    		try {
    			color = UpdateSuits.getColor(a[1]);
    		} catch (IndexOutOfBoundsException e) {
    			
    		}
    		
    		try {
    			String[] coords = point.split(",");
    			if (coords.length == 2) {
    				// Get X coord(s)
    				String xCoord = coords[0];
					List<Integer> xList = new ArrayList<Integer>();
					if (xCoord.split(":").length == 2) {
						String[] range = xCoord.split(":");
						int from = Integer.parseInt(range[0]);
						int to = Integer.parseInt(range[1]);
						
						int i = from;
						while (i <= to) {
							xList.add(i);
							i++;
						}
					}
					if (xList.isEmpty()) {
						xList.add(Integer.parseInt(xCoord));
    				}
					
					// Get Y coord(s)
    				String yCoord = coords[1];
					List<Integer> yList = new ArrayList<Integer>();
					if (yCoord.split(":").length == 2) {
						String[] range = yCoord.split(":");
						int from = Integer.parseInt(range[0]);
						int to = Integer.parseInt(range[1]);
						
						int i = from;
						while (i <= to) {
							yList.add(i);
							i++;
						}
					}
					if (yList.isEmpty()) {
						yList.add(Integer.parseInt(yCoord));
					}
					
					// Add all points
					for (int xValue : xList) {
						for (int yValue: yList) {
							
							Object[] p = {xValue, yValue, color};
							finalPoints.add(p);
							
						}
					}
    			}
    		
    		} catch (NumberFormatException e ) {
    			e.printStackTrace();
    		}
    	}
    	
    	return finalPoints;
    }
    
    public static Color randomColor() {
    	
    	List<Integer> rgb = new ArrayList<Integer>();
    	for (int i = 0; i < 3; i++) {
    		int rand = (int) (Math.random() * 256);
    		rgb.add(rand);
    	}
    	
    	return Color.fromRGB(rgb.get(0), rgb.get(1), rgb.get(2));
    	
    }
    
    
    public static void end() {
    	if (assignedTaskId != null) Bukkit.getScheduler().cancelTask(assignedTaskId);
    	UpdateCloaks.plugin = null;
    }

    /**
     * Schedules this instance to "run" every second
     */
    public static void schedule() {
        // Initialize our assigned task's id, for later use so we can cancel
        assignedTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new UpdateCloaks(), 10L, 1L);
    }

}