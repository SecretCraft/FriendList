package com.github.secretcraft.java.listeners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.github.secretcraft.java.FLCommand;
import com.github.secretcraft.java.FriendList;
import com.github.secretcraft.java.utils.Utils;

public class LoginListener implements Listener {
	
	private FriendList plugin;
	private FLCommand flCommand;
	private Map<Player,Boolean> toggleMap;
	
	//---------------------------------------------------------------------------------------------------------------
	
	public LoginListener(FLCommand flCommand, FriendList plugin) {
		this.plugin = plugin;
		this.flCommand = flCommand;
		toggleMap = new HashMap<Player,Boolean>();
	}
	
	//---------------------------------------------------------------------------------------------------------------
	
	@EventHandler
	public void onPlayerLogin(PlayerLoginEvent event) {
		Player player = event.getPlayer();
		if(!toggleMap.containsKey(player)) {
			toggleMap.put(player, true);
		}
		flCommand.updateFriendMap(player);
		if(flCommand.getFriendMap().containsKey(player) && 
				!(flCommand.getFriendsFromFriendMap(player) == null || flCommand.getFriendsFromFriendMap(player).isEmpty())) {
			this.checkResults(player, true);
		}
	}
	
	//---------------------------------------------------------------------------------------------------------------
	
	@EventHandler
	public void onPlayerLogout(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		if(!toggleMap.containsKey(player)) {
			toggleMap.put(player, true);
		}
		if(flCommand.getFriendMap().containsKey(player) && 
				!(flCommand.getFriendsFromFriendMap(player) == null || flCommand.getFriendsFromFriendMap(player).isEmpty())) {
			this.checkResults(player, false);
		}
	}
	
	//---------------------------------------------------------------------------------------------------------------
	
	private void checkResults(Player player, boolean flag) {
		ArrayList<String> res = flCommand.getFriendsFromFriendMap(player);
		String friend = "";
		String displayName = "";
		Player onlineFriend = null;
		for(int i = 0; i < res.size(); i++) {
			friend = res.get(i);
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
	}
	
	//---------------------------------------------------------------------------------------------------------------
	
	public void putToggleMap(Player pl, boolean flag) {
		toggleMap.put(pl, flag);
	}
	
	//---------------------------------------------------------------------------------------------------------------
	
	public Map<Player,Boolean> getToggleMap() {
		return toggleMap;
	}
}
