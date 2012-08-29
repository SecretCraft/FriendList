package com.github.secretcraft.java.listeners;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import net.windwaker.sql.Connection;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.github.secretcraft.java.FriendList;
import com.github.secretcraft.java.utils.Utils;

public class LoginListener implements Listener {
	
	private Connection connection;
	private FriendList plugin;
	private Map<Player,Boolean> toggleMap;
	
	public LoginListener(Connection connection, FriendList plugin) {
		this.connection = connection;
		this.plugin = plugin;
		toggleMap = new HashMap<Player,Boolean>();
	}
	
	@EventHandler
	public void onPlayerLogin(PlayerLoginEvent event) {
		Player player = event.getPlayer();
		if(!toggleMap.containsKey(player)) {
			toggleMap.put(player, true);
		}
		ResultSet res = Utils.getResults(player, connection);
		this.checkResults(res, player, true);
	}
	
	@EventHandler
	public void onPlayerLogout(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		if(!toggleMap.containsKey(player)) {
			toggleMap.put(player, true);
		}
		ResultSet res = Utils.getResults(player, connection);
		this.checkResults(res, player, false);
	}
	
	private void checkResults(ResultSet res, Player player, boolean flag) {
		String friend = "";
		String displayName = "";
		Player onlineFriend = null;
		try {
			while(res.next()) {
				friend = res.getString("friend");
				if(plugin.playerIsOnline(friend)) {
					onlineFriend = Utils.getPlayerByName(friend);
					if(toggleMap.get(onlineFriend)) {
						displayName = Utils.getDisplayNameFormat(player);
						if(flag) {
							onlineFriend.sendMessage(displayName + ChatColor.GRAY + " hat sich eingeloggt.");
						} else {
							onlineFriend.sendMessage(displayName + ChatColor.GRAY + " hat sich ausgeloggt.");
						}
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void putToggleMap(Player pl, boolean flag) {
		toggleMap.put(pl, flag);
	}
	
	public Map<Player,Boolean> getToggleMap() {
		return toggleMap;
	}
}
