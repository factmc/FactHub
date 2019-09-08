package net.factmc.FactHub.cosmetics;

import java.lang.ClassCastException;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Horse.Color;
import org.bukkit.entity.Horse.Style;
import org.bukkit.entity.Parrot.Variant;
import org.bukkit.entity.Llama;
import org.bukkit.entity.Cat;
import org.bukkit.entity.Parrot;
import org.bukkit.entity.Player;
import org.bukkit.entity.Rabbit;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Wolf;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import de.myzelyam.api.vanish.VanishAPI;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.nms.v1_14_R1.trait.LlamaTrait;
import net.citizensnpcs.nms.v1_14_R1.trait.CatTrait;
import net.citizensnpcs.nms.v1_14_R1.trait.ParrotTrait;
import net.citizensnpcs.trait.Age;
import net.citizensnpcs.trait.Controllable;
import net.citizensnpcs.trait.HorseModifiers;
import net.citizensnpcs.trait.RabbitType;
import net.citizensnpcs.trait.SheepTrait;
import net.citizensnpcs.trait.WolfModifiers;
import net.factmc.FactCore.CoreUtils;
import net.factmc.FactHub.Data;
import net.factmc.FactHub.Main;
import net.factmc.FactHub.gui.cosmetics.PetsGUI;
import net.factmc.FactHub.gui.select.DyeColorGUI;
import net.factmc.FactHub.listeners.PetManager;

public class Pet {
	
	private NPC npc;
	private final Player owner;
	private Integer taskID;
	private boolean autoSpawn;
	
	public Pet(Player owner, boolean autoSpawn, long delay) {
		this.npc = null;
		this.owner = owner;
		this.autoSpawn = autoSpawn;
		taskID = null;
		
		if (autoSpawn && !VanishAPI.isInvisible(owner)) {
			if (delay > 0) {
				Bukkit.getScheduler().runTaskLater(Main.getPlugin(), new Runnable() {
					@Override
					public void run() {
						Pet.this.spawn(null, false);
					}
				}, delay);
			}
			else {
				this.spawn(null, false);
			}
		}
	}
	
	public Pet(Player owner, boolean autoSpawn) {
		this(owner, autoSpawn, 0);
	}
	
	public final void spawn(Location setLoc, boolean newType) {
		Location loc = owner.getLocation().clone();
		loc.setPitch(0);
		Vector vec = loc.getDirection().multiply(-2);
		Location at = loc.add(vec);
		
		// Find valid location
		Material nextBlock = null;
		Block b = null;
		int bY = at.getBlockY();
		while (nextBlock != Material.AIR) {
			
			b = at.getWorld().getBlockAt(at.getBlockX(), bY, at.getBlockZ());
			nextBlock = b.getType();
			bY = b.getLocation().getBlockY() + 1;
			
		}
		at = b.getLocation();
		
		UUID uuid = owner.getUniqueId();
		String[] data = Data.getSelected(uuid, "PET","PETNAME");
		String petType = data[0];
		
		String name = data[1];
		if (name.equalsIgnoreCase("NONE")) name = owner.getName() + "'s " + CoreUtils.underscoreToSpace(petType.toLowerCase());
		name = ChatColor.translateAlternateColorCodes('&', name);
		
		
		if (!petType.equalsIgnoreCase("NONE")) {

			try {
				
				EntityType type = EntityType.valueOf(petType);
				if (npc == null) {
					this.npc = CitizensAPI.getNPCRegistry().createNPC(type, name);
				}
				
				
				npc.setName(name);
				if (!npc.isSpawned()) npc.spawn(at);
				
				if (!npc.hasTrait(Controllable.class)) {
		            npc.addTrait(new Controllable(false));
		        }
		        Controllable trait = npc.getTrait(Controllable.class);
		        
		        boolean baby = Data.getBoolean(uuid, "PETBABY");
		        if (owner.hasPermission("facthub.mount")
		        		&& !((type == EntityType.HORSE || type == EntityType.DONKEY) && baby == false)) {
		        	
			        trait.setEnabled(true);
			        npc.getNavigator().getDefaultParameters().speedModifier(5000);
		        }
		        else {
		        	trait.setEnabled(false);
		        }
		        
				if (npc.getEntity() instanceof Ageable) {
					
					if (!baby) Data.setBoolean(uuid, "PETBABY", false);
					
					Age age = npc.getTrait(Age.class);
					if (baby) {
						age.setAge(-24000);
					}
					else {
						age.setAge(0);
					}
					
					String[] mods = Data.getSelected(uuid, "PETCOLOR","PETSTYLE");
					if (npc.getEntity() instanceof Horse) {
						
						String dataColor = mods[0];
						/*if (dataColor == null) {
							data.set("cosmetics.pet.horse-color", "WHITE");
							dataColor = "WHITE";
						}*/
						
						String dataStyle = mods[1];
						/*if (dataStyle == null) {
							data.set("cosmetics.pet.horse-style", "NONE");
							dataStyle = "NONE";
						}*/
						
						HorseModifiers horse = npc.getTrait(HorseModifiers.class);
						if (newType) {
							Data.setSelected(uuid, "PETCOLOR", "WHITE");
							horse.setColor(Color.WHITE);
						}
						else {
							horse.setColor(Color.valueOf(dataColor));
						}
						
						
						if (newType) {
							Data.setSelected(uuid, "PETSTYLE", "NONE");
							horse.setStyle(Style.NONE);
						}
						else {
							horse.setStyle(Style.valueOf(dataStyle));
						}
						
					}
					
					/*else if (npc.getEntity() instanceof Donkey) {
						
						HorseModifiers horse = npc.getTrait(HorseModifiers.class);
						horse.setSaddle(new ItemStack(Material.SADDLE, 1));
						
						Donkey eDonkey = (Donkey) npc.getEntity();
						eDonkey.getInventory().setSaddle(new ItemStack(Material.SADDLE, 1));
						
					}*/
					
					else if (npc.getEntity() instanceof Llama) {
						
						String dataColor = mods[0];
						/*if (dataColor == null) {
							data.set("cosmetics.pet.llama-color", "WHITE");
							dataColor = "WHITE";
						}*/
						
						LlamaTrait llama = npc.getTrait(LlamaTrait.class);
						if (newType) {
							Data.setSelected(uuid, "PETCOLOR", "WHITE");
							llama.setColor(Llama.Color.WHITE);
						}
						else {
							llama.setColor(Llama.Color.valueOf(dataColor));
						}
						
						dataColor = mods[1];
						/*if (dataColor == null) {
							data.set("cosmetics.pet.llama-carpet", "WHITE");
							dataColor = "WHITE";
						}*/
						
						if (!dataColor.equalsIgnoreCase("RAINBOW")) {
							Llama llamaPet = (Llama) npc.getEntity();
							DyeColor carpColor;
							if (newType) {
								Data.setSelected(uuid, "PETSTYLE", "WHITE");
								carpColor = DyeColor.WHITE;
							}
							else {
								carpColor = DyeColor.valueOf(dataColor);
							}
							
							llamaPet.getInventory().setDecor(new ItemStack(PetsGUI.getCarpetMaterial(carpColor), 1));
						}
						
					}
					
					if (npc.getEntity() instanceof Sheep) {
						
						String dataColor = mods[0];
						/*if (dataColor == null) {
							data.set("cosmetics.pet.sheep-color", "WHITE");
							dataColor = "WHITE";
						}*/
						
						if (!dataColor.equalsIgnoreCase("RAINBOW")) {
							SheepTrait sheep = npc.getTrait(SheepTrait.class);
							if (newType) {
								Data.setSelected(uuid, "PETCOLOR", "WHITE");
								sheep.setColor(DyeColor.WHITE);
							}
							else {
								sheep.setColor(DyeColor.valueOf(dataColor));
							}
						}
						
					}
					
					if (npc.getEntity() instanceof Parrot) {
						
						String dataColor = mods[0];
						/*if (dataColor == null) {
							data.set("cosmetics.pet.parrot-color", "GRAY");
							dataColor = "GRAY";
						}*/
						
						ParrotTrait parrot = npc.getTrait(ParrotTrait.class);
						if (newType) {
							Data.setSelected(uuid, "PETCOLOR", "GRAY");
							parrot.setVariant(Variant.GRAY);
						}
						else {
							parrot.setVariant(Variant.valueOf(dataColor));
						}
						
					}
					
					if (npc.getEntity() instanceof Wolf) {
						
						String dataColor = mods[0];
						/*if (dataColor == null) {
							data.set("cosmetics.pet.wolf-color", "WHITE");
							dataColor = "WHITE";
						}*/
						
						WolfModifiers wolf = npc.getTrait(WolfModifiers.class);
						wolf.setTamed(true);
						
						if (!dataColor.equalsIgnoreCase("RAINBOW")) {
							
							if (newType) {
								Data.setSelected(uuid, "PETCOLOR", "WHITE");
								wolf.setCollarColor(DyeColor.WHITE);
							}
							else {
								wolf.setCollarColor(DyeColor.valueOf(dataColor));
							}
							
						}
						
					}
					
					if (npc.getEntity() instanceof Cat) {
						
						String dataColor = mods[0];
						/*if (dataColor == null) {
							data.set("cosmetics.pet.cat-color", "BLACK_CAT");
							dataColor = "BLACK_CAT";
						}*/
						
						CatTrait cat = npc.getTrait(CatTrait.class);
						if (newType) {
							Data.setSelected(uuid, "PETCOLOR", "WHITE");
							cat.setType(Cat.Type.WHITE);
						}
						else {
							cat.setType(Cat.Type.valueOf(dataColor));
						}
						
					}
					
					if (npc.getEntity() instanceof Rabbit) {
						
						String dataColor = mods[0];
						/*if (dataColor == null) {
							data.set("cosmetics.pet.rabbit-color", "WHITE");
							dataColor = "WHITE";
						}*/
						
						RabbitType rabbit = npc.getTrait(RabbitType.class);
						if (newType) {
							Data.setSelected(uuid, "PETCOLOR", "WHITE");
							rabbit.setType(Rabbit.Type.WHITE);
						}
						else {
							rabbit.setType(Rabbit.Type.valueOf(dataColor));
						}
						
					}
					
				}
				
				if (taskID != null) {
					Bukkit.getScheduler().cancelTask(taskID);
					taskID = null;
				}
				
				// Pet Task
				taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getPlugin(), new Runnable() {
		            public void run() {
		            	if (!npc.isSpawned()) return;
		            	if (!owner.isOnline()) return;
		            	
		            	if (npc.getEntity().getLocation().distance(owner.getLocation()) > 10) {
		            		teleportToOwner();
		            	}
		            	
		            	/* FREEZES SERVER DO NOT USE
		            	else if (!npc.getNavigator().isNavigating()) {
		            		npc.getNavigator().setTarget(owner, false);
		            	}*/
		            	
		            	
		            	String color;
		            	String[] mods = Data.getSelected(uuid, "PETCOLOR","PETSTYLE");
		            	
		            	if (npc.getEntity().getType() == EntityType.LLAMA) {
		            		color = mods[1];
		            		if (color.equalsIgnoreCase("RAINBOW")) {
			            		Llama pet = (Llama) npc.getEntity();
			            		
			            		ItemStack current = pet.getInventory().getDecor();
			            		ItemStack next = current;
			            		while (next == current) {
			            			next = new ItemStack(PetsGUI.getCarpetMaterial(DyeColorGUI.getRandom()), 1);
			            		}
			            		
			            		pet.getInventory().setDecor(next);
			            		
		            		}
		            	}
		            	
		            	else if (npc.getEntity().getType() == EntityType.SHEEP) {
		            		color = mods[0];
		            		if (color.equalsIgnoreCase("RAINBOW")) {
			            		
		            			Sheep pet = (Sheep) npc.getEntity();
			            		DyeColor current = pet.getColor();
			            		DyeColor next = current;
			            		while (next == current) {
			            			next = DyeColorGUI.getRandom();
			            		}
			            		
			            		SheepTrait sheep = npc.getTrait(SheepTrait.class);
			            		sheep.setColor(next);
			            		
		            		}
		            	}
		            	
		            	else if (npc.getEntity().getType() == EntityType.WOLF) {
		            		color = mods[0];
		            		if (color.equalsIgnoreCase("RAINBOW")) {
		            			
		            			Wolf pet = (Wolf) npc.getEntity();
			            		DyeColor current = pet.getCollarColor();
			            		DyeColor next = current;
			            		while (next == current) {
			            			next = DyeColorGUI.getRandom();
			            		}
			            		
			            		WolfModifiers wolf = npc.getTrait(WolfModifiers.class);
			            		wolf.setCollarColor(next);
			            		
		            		}
		            	}
		            	
		            }
		            
		            public void teleportToOwner() {
		        		if (npc.isSpawned()) {
		        			Location loc = owner.getLocation().clone();
		        			loc.setPitch(0);
		        			Vector vec = loc.getDirection().multiply(-2);
		        			Location to = loc.add(vec);
		        			npc.teleport(to, TeleportCause.PLUGIN);
		        		}
		        	}
		        }, 10L, 30L);
				
			} catch (IllegalArgumentException | ClassCastException e) {
				if (npc.isSpawned()) npc.despawn();
				e.printStackTrace();
				Main.getPlugin().getLogger().warning("The pet type for " + owner.getName() + ": '" + petType + "' is not valid");
    			Data.setSelected(uuid, "PET", "NONE");
			}
		}
		
	}
	
	public final void despawn() {
		if (npc == null) return;
		
		npc.despawn();
	}
	
	public final void respawn() {
		this.despawn();
		this.spawn(null, false);
	}
	
	public void teleportToPet() {
		if (npc.isSpawned()) {
			owner.teleport(npc.getEntity().getLocation(), TeleportCause.PLUGIN);
		}
	}
	
	public void teleportToOwner() {
		if (npc.isSpawned()) {
			npc.getNavigator().cancelNavigation();
			Location loc = owner.getLocation().clone();
			loc.setPitch(0);
			Vector vec = loc.getDirection().multiply(-2);
			Location to = loc.add(vec);
			npc.teleport(PetManager.findGround(to), TeleportCause.PLUGIN);
		}
	}
	
	public NPC getNPC() {
		return npc;
	}
	
	public Player getOwner() {
		return owner;
	}
	
	public boolean taskRunning() {
		return (taskID != null);
	}
	
	public boolean autoSpawns() {
		return autoSpawn;
	}
	
	public final void setAutoSpawn(boolean autoSpawn) {
		
		this.autoSpawn = autoSpawn;
		Data.setBoolean(owner.getUniqueId(), "PETAUTOSPAWN", autoSpawn);
	}
	
	public final void changeName(String name) {
		
		if (npc != null) npc.setName(ChatColor.translateAlternateColorCodes('&', name));
		Data.setSelected(owner.getUniqueId(), "PETNAME", name);
	}
	
	public final void changeType(String type) {
		
		this.despawn();
		Data.setSelected(owner.getUniqueId(), "PET", type.toUpperCase());
		
		if (npc != null && !type.equalsIgnoreCase("NONE")) {
			npc.setBukkitEntityType(EntityType.valueOf(type));
		}
		this.spawn(null, true);
	}
	
}