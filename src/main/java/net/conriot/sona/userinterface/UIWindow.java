package net.conriot.sona.userinterface;

import java.util.Arrays;
import java.util.Iterator;

import lombok.Getter;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class UIWindow implements Listener {
	private UserInterface plugin;
	private UIWindow self;
	@Getter private Player owner;
	@Getter private int rows;
	private String title;
	private UICallback callback;
	@Getter private boolean open;
	private boolean[] movable;
	private Inventory inv;
	private boolean renamed;
	
	
	public UIWindow(UserInterface plugin, Player owner, int rows, String title, UICallback callback) {
		this.self = this;
		this.plugin = plugin;
		this.owner = owner;
		this.rows = rows;
		this.title = title;
		this.callback = callback;
		this.open = false;
		this.movable = new boolean[rows * 9];
		this.inv = Bukkit.createInventory(this.owner, 9 * this.rows, cleanTitle(this.title));
		this.renamed = false;
		
		// Lock all slots by default
		lockAllSlots();
		
		// Register events for this inventory
		Bukkit.getServer().getPluginManager().registerEvents(this, this.plugin);
		
	}
	
	public void open() {
		// Do not open if the inventory is already open
		if(this.open)
			return;
		
		// Perform a rename if one is pending
		if(this.renamed)
			rename(this.title);
		
		// Flag the inventory as open
		this.open = true;
		
		// Show the inventory to the owner
		this.owner.openInventory(this.inv);
	}
	
	public void close() {
		if(!this.open)
			return;
		
		// Close the currently open inv
		this.owner.closeInventory();
	}
	
	public void rename(String title) {
		this.title = title;
		
		// Flag this inventory as needing to be renamed
		this.renamed = true;
		
		// Perform the switch if the inv isnt open
		if(!this.open) {
			// Copy out the items from the inventory
			ItemStack[] temp = new ItemStack[rows * 9];
			for(int i = 0; i < temp.length; i++) {
				temp[i] = this.inv.getContents()[i];
			}
			
			// Create a new inventory object
			this.inv = Bukkit.createInventory(this.owner, 9 * this.rows, cleanTitle(this.title));
			
			// Copy the items back into inventory
			for(int i = 0; i < temp.length; i++) {
				this.inv.getContents()[i] = temp[i];
			}
			
			// Flag this inventory as not needed a rename
			this.renamed = false;
		}
	}
	
	public void lockAllSlots() {
		// Iterate over all slots and lock each one
		for(int i = 0; i < this.movable.length; i++)
			this.movable[i] = false;
	}
	
	public void lock(int slot) {
		// Lock the given slot if it's in range
		if(slot < this.movable.length && slot >= 0)
			this.movable[slot] = false;
	}
	
	public void unlock(int slot) {
		// Unlock the given slot if it's in range
		if(slot < this.movable.length && slot >= 0)
			this.movable[slot] = true;
	}
	
	public void setRaw(int slot, ItemStack item) {
		// Purify item stack input
		item = purify(item);
		
		// Set the item is the given slot is in range
		if(slot < this.rows * 9 && slot >= 0)
			this.inv.setItem(slot, item);
	}
	
	public void setAllRaw(ItemStack[] items) {
		// Set the item is the given slot is in range
		if(items.length == this.rows * 9)
			for(int i = 0; i < items.length; i++)
				setRaw(i, items[i]);
	}
	
	public void set(int slot, ItemStack item, String name, String[] lore) {
		// Purify item stack input
		item = purify(item);
		
		// Set the item is the given slot is in range
		if(slot < this.rows * 9 && slot >= 0) {
			// Set any applicable metadata
			ItemMeta im = item.getItemMeta();
			if(name != null)
				im.setDisplayName(name);
			if(lore != null)
				im.setLore(Arrays.asList(lore));
		    item.setItemMeta(im);
		    
		    // Insert the item stack
		    this.inv.setItem(slot, item);
		}
	}
	
	public void set(int slot, Material mat, int count, String name, String[] lore) {
		// Set the item is the given slot is in range
		if(slot < this.rows * 9 && slot >= 0) {
			// Create a new itemstack based on material and count
			ItemStack item = new ItemStack(mat, count);
			
			// Set any applicable metadata
			ItemMeta im = item.getItemMeta();
			if(name != null)
				im.setDisplayName(name);
			if(lore != null)
				im.setLore(Arrays.asList(lore));
		    item.setItemMeta(im);
		    
		    // Insert the item stack
		    this.inv.setItem(slot, item);
		}
	}
	
	public void clear(int slot) {
		// Clear the item is the given slot is in range
		if(slot < this.rows * 9 && slot >= 0) {
			// Create an emtpy itemstack
			ItemStack clear = new ItemStack(Material.AIR, 0);
			
			// Insert it into the slot to be cleared
			this.inv.setItem(slot, clear);
		}
	}
	
	private String cleanTitle(String title) {
		if(title.length() > 32)
			return title.substring(0, 32);
		return title;
	}
	
	private ItemStack purify(ItemStack item) {
		if(item == null)
			return new ItemStack(Material.AIR, 0);
		return item;
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		// Check if this event pertains to this inventory's owner
		if(event.getPlayer() == this.owner) {
			// Force the inventory to close if the player leaves
			close();
			//forceClose();
		}
	}
	
	@EventHandler
	public void onDeath(PlayerDeathEvent event) {
		// Check if this event pertains to this inventory's owner
		if(event.getEntity() instanceof Player && (Player)event.getEntity() == this.owner) {
			// Force the inventory to close if the player dies
			close();
			//forceClose();
		}
	}
	
	@EventHandler
	public void onInvClick(InventoryClickEvent event) {
		// Check if this event pertains to this inventory interface
		if(this.inv.getViewers().size() > 0 && event.getView().getPlayer() == this.owner) {
			// Check if this click was inside the UI space
			if(event.getRawSlot() < (this.rows * 9) && event.getRawSlot() >= 0) {
				// Check if the slot clicked is locked
				if(!this.movable[event.getRawSlot()]) {
					// Cancel the event and send a callback
					event.setCancelled(true);
					this.callback.clicked(this, this.owner, event.getRawSlot(), event.getCurrentItem());
				} else {
					// Handle pickup and place events
					if(event.getAction() == InventoryAction.PICKUP_ALL) {
						// Send a callback notifying of the item taken event
						this.callback.taken(this, this.owner, event.getRawSlot(), event.getCurrentItem());
					} else if(event.getAction() == InventoryAction.PLACE_ALL && event.getCurrentItem().getAmount() == 0) {
						event.getResult();
						// Send a callback notifying of the item placed event
						this.callback.placed(this, this.owner, event.getRawSlot(), event.getCursor());
					} else {
						// Disallow any other type of item movement for simplicities sake
						event.setCancelled(true);
					}
				}
			} else {
				// Disallow certain events that could break the UI, such as collection
				if(event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY || event.getAction() == InventoryAction.COLLECT_TO_CURSOR ||event.getAction() == InventoryAction.UNKNOWN)
					event.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onInvDrag(InventoryDragEvent event) {
		// Check if this event pertains to this inventory interface
		if(this.inv.getViewers().size() > 0 && event.getView().getPlayer() == this.owner) {
			// If any slots involved were a part of the ui inventory, cancel the event
			Iterator<Integer> iter = event.getRawSlots().iterator();
			while(iter.hasNext()) {
				if(iter.next() < this.rows * 9) {
					event.setCancelled(true);
					break;
				}
			}
		}
	}
	
	@EventHandler
	public void onInvClose(InventoryCloseEvent event) {
		// Check if this event pertains to this inventory interface
		if(this.inv.getViewers().size() > 0 && event.getView().getPlayer() == this.owner) {
			// Check if the inventory was supposedly open
			if(this.open) {
				// Flag the inventory as closed
				this.open = false;
				
				// Notify that the inventory was closed on the text tick
				Bukkit.getScheduler().runTaskLater(this.plugin, new Runnable() {
					@Override
					public void run() {
						callback.closed(self, owner);
					}
				}, 0);
			}
		}
	}
}
