package com.github.secretcraft.java;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.github.secretcraft.java.utils.Utils;

public class FLMessage {
	
	private FLCommand flCommand;
	private List<Player> friendList = new LinkedList<Player>();
	
	//---------------------------------------------------------------------------------------------------------------
	
	public FLMessage(FLCommand flCommand) {
		this.flCommand = flCommand;
	}
	
	//---------------------------------------------------------------------------------------------------------------
	
	public void sendMsg(Player player, String message) {
		
		ArrayList<String> results = flCommand.getFriendsFromFriendMap(player);
		
		String friendName = "";
		for(int i = 0; i < results.size(); i++) {
			friendName = results.get(i);
			Player p = Utils.getPlayerByName(friendName);
			friendList.add(p);
		}
		if (friendList.isEmpty()) {
			player.sendMessage(ChatColor.RED + "Keiner in deiner Freundesliste ist online.");
			return;
		}
		player.sendMessage(ChatColor.GRAY + "[mir -> " + ChatColor.GREEN + "Friendlist" + ChatColor.GRAY + "] " + ChatColor.WHITE + message);
		Player p = null;
		for (int i = 0; i < friendList.size(); i++) {
			p = friendList.get(i);
			p.sendMessage(ChatColor.GRAY + "[" + Utils.getDisplayNameFormat(player) + ChatColor.GRAY + " -> mir] " + ChatColor.WHITE + message);
			if(friendList.contains(p)) {
				friendList.remove(p);
			}
		}
	}
	
}
