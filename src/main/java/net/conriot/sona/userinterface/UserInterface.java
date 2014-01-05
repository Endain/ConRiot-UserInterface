package net.conriot.sona.userinterface;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class UserInterface extends JavaPlugin {
	private static UserInterface ui;
	
	@Override
	public void onEnable() {
		ui = this;
		// Nothing to do here
	}
	
	@Override
	public void onDisable() {
		// Nothing to do here
	}
	
	public static UIWindow create(Player owner, int rows, String title, UICallback callback) {
		return new UIWindow(ui, owner, rows, title, callback);
	}
}
