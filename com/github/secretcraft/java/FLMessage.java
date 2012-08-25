package com.github.secretcraft.java;

import java.sql.ResultSet;
import java.sql.SQLException;
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
		
		ResultSet results = flCommand.getResults(player);
		
		try {
			String friendName = "";
			while (results.next()) {
				friendName = results.getString("friend");
				Player p = Utils.getPlayerByName(friendName);
				friendList.add(p);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (friendList.isEmpty()) {
			player.sendMessage(ChatColor.RED + "Nobody in your Friend-List is online.");
			return;
		}
		player.sendMessage(ChatColor.GRAY + "[mir -> " + ChatColor.GREEN + "Friendlist" + ChatColor.GRAY + "] " + ChatColor.WHITE + message);
		for (Player p : friendList) {
			p.sendMessage(ChatColor.GRAY + "[" + Utils.getDisplayNameFormat(player) + ChatColor.GRAY + " -> mir] " + ChatColor.WHITE + message);
			if(friendList.contains(p)) {
				friendList.remove(p);
			}
		}
	}
	
}
