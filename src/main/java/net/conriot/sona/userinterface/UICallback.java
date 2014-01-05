package net.conriot.sona.userinterface;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface UICallback {
	// Define a callback method for when a slot is clicked
	public void clicked(UIWindow ui, Player owner, int slot, ItemStack item);
	// Define a callback method for when an item stack is taken
	public void taken(UIWindow ui, Player owner, int slot, ItemStack item);
	// Define a callback method for when an item stack is placed
	public void placed(UIWindow ui, Player owner, int slot, ItemStack item);
	// Define a callback method for when the inventory is closed
	public void closed(UIWindow ui, Player owner);
}
