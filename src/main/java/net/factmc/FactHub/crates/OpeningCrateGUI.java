package net.factmc.FactHub.crates;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class OpeningCrateGUI implements Listener {
	
	public static void open(Player player, int level) {
		String title;
		if (level == 1) title = ChatColor.DARK_AQUA + "Common Crate";
		else if (level == 2) title = ChatColor.DARK_AQUA + "Rare Crate";
		else title = ChatColor.DARK_AQUA + "Legendary Crate";
		
		Inventory gui = player.getServer().createInventory(player, 27, title);
		
		ItemStack middle = new ItemStack(Material.BLACK_STAINED_GLASS, 1);
		gui.setItem(4, middle);
		gui.setItem(22, middle);
		
		for (int i = 9; i < 18; i++) {
			gui.setItem(i, Util.randomItem(level));
		}
		
		new ScrollTask(gui, player, level);
		
		player.openInventory(gui);
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void itemClicked(InventoryClickEvent event) {
		String title = event.getView().getTitle();
		if (title.equalsIgnoreCase(ChatColor.DARK_AQUA + "Common Crate")
				|| title.equalsIgnoreCase(ChatColor.DARK_AQUA + "Rare Crate")
				|| title.equalsIgnoreCase(ChatColor.DARK_AQUA + "Legendary Crate")) {
			
			//Player player = (Player) event.getWhoClicked();
			event.setCancelled(true);
			
		}
	}
	
}