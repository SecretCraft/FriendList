package com.github.secretcraft.java;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import net.windwaker.sql.Connection;
import net.windwaker.sql.DataType;
import net.windwaker.sql.Table;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.github.secretcraft.java.utils.Utils;

public class FLCommand {
	
	private Connection connection;
	private Table friend_list;
	private FriendList plugin;
	Logger log;
	private Map<Player,Player> playerMap = null;
	private Map<Player,ArrayList<String>> friendMap;
	
	//---------------------------------------------------------------------------------------------------------------
	
	public FLCommand(Logger log, FriendList plugin) {
		connection = plugin.getConnection();
		this.plugin = plugin;
		this.log = log;
		friend_list = new Table(connection, "friend_list");
		playerMap = new HashMap<Player,Player>();
		friendMap = new HashMap<Player,ArrayList<String>>();
		
		try {
			if(!(friend_list.exists())) {
				
				Map<String, DataType> columnDataTypeMap = new HashMap<String, DataType>();
				columnDataTypeMap.put("id", new DataType("INT AUTO_INCREMENT PRIMARY KEY"));
				columnDataTypeMap.put("player", new DataType("VARCHAR(20) NOT NULL"));
				columnDataTypeMap.put("friend", new DataType("VARCHAR(20) NOT NULL"));
				friend_list.create(columnDataTypeMap);
				
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//---------------------------------------------------------------------------------------------------------------
	
	public void addFriend(Player player, Player friend) {
		
		String name = friend.getName();
		String friendName = "";
		String playerName = "";
		
		try {
			ResultSet res = connection.query("SELECT friend FROM friend_list WHERE player ='"+player.getName()+"' && friend = '" + friend.getName() + "'");
			while (res.next()) {
				friendName = res.getString("friend");
				playerName = res.getString("player");
				if(friendName.equalsIgnoreCase(name) && playerName.equalsIgnoreCase(player.getName())) {
					player.sendMessage(ChatColor.RED + "Der Spieler: " + ChatColor.AQUA + name + ChatColor.RED +
							" ist bereits in deiner Freundesliste!");
					return;
				}
			}
			
			String[] fields = {"player", "friend"};
			String offlinePlayer = plugin.getServer().getOfflinePlayer(name).getName();
			String[] values = {player.getName(), offlinePlayer};
			friend_list.add(fields, values);
			this.updateFriendMap(player);
			this.updateFriendMap(friend);
			
		} catch(SQLException e) {
			e.getStackTrace();
		}
		
		player.sendMessage(ChatColor.GRAY + "Spieler: " + ChatColor.AQUA + name + ChatColor.GRAY + " hinzugefügt.");
	}
	
	//---------------------------------------------------------------------------------------------------------------
	
	public void removeFriend(Player player, String name) {
		
		try {
			
			if(!(friend_list.containsValue("friend", name))) {
				
				player.sendMessage(ChatColor.RED + "Der Spieler: " + ChatColor.AQUA + name + ChatColor.RED +
						" existiert nicht in deiner Freundesliste!");
				return;
				
			}
			
			connection.update("DELETE FROM friend_list WHERE player = '" + player.getName() + "' && friend = '" + name + "'");
			connection.update("DELETE FROM friend_list WHERE player = '" + name + "' && friend = '" + player.getName() + "'");
			this.updateFriendMap(player);
			this.updateFriendMap(Utils.getPlayerByName(name));
			
		} catch(SQLException e) {
			e.getStackTrace();
		}
		if(plugin.playerIsOnline(name)) {
			Player p = Utils.getPlayerByName(name);
			p.sendMessage(ChatColor.GRAY + "Spieler: " + ChatColor.AQUA + player.getName() + ChatColor.GRAY + " entfernt.");
		}
		player.sendMessage(ChatColor.GRAY + "Spieler: " + ChatColor.AQUA + name + ChatColor.GRAY + " entfernt.");
	}
	
	//---------------------------------------------------------------------------------------------------------------
	
	public void showList(Player player) {
		
		List<String> strList = new ArrayList<String>();
		String tempMessageOnline = "";
		String tempMessageOffline = "";
		
		ArrayList<String> results = friendMap.get(player);
		if(results == null || results.isEmpty()) {
			player.sendMessage(ChatColor.GRAY + "Deine Freundesliste ist leer!");
			return;
		}
		String friend = "";
		Player onlineFriend = null;
		for(int i = 0; i < results.size(); i++) {
			friend = results.get(i);
			strList.add(friend);
		}
		strList = Utils.sortAlphabetic(strList);
		for(int i = 0; i < strList.size(); i++) {
			onlineFriend = Utils.getPlayerByName(strList.get(i));
			if(onlineFriend == null) {
				tempMessageOffline += strList.get(i) + ChatColor.WHITE + ", ";
			} else {
				String displayName = Utils.getDisplayNameFormat(onlineFriend);
				tempMessageOnline = displayName + ChatColor.GRAY + ", ";
			}
		}
		if((tempMessageOnline.equals("") || tempMessageOnline.length() == 0) && 
				(tempMessageOffline.equals("") || tempMessageOffline.length() == 0)) {
				
			player.sendMessage(ChatColor.GRAY + "Deine Freundesliste ist leer!");
			return;
				
		}
		player.sendMessage(ChatColor.GOLD + "----Deine Freundesliste----");
		if(!tempMessageOnline.equals("")) {
			tempMessageOnline = tempMessageOnline.substring(0, tempMessageOnline.length() - 2);
			this.showOnline(player, tempMessageOnline);
		}
		if(!tempMessageOffline.equals("")) {
			tempMessageOffline = tempMessageOffline.substring(0, tempMessageOffline.length() - 2);
			this.showOffline(player, tempMessageOffline);
		}
	}
	
	//---------------------------------------------------------------------------------------------------------------
	
	public void showOnlyOnline(Player player) {
		
		List<String> strList = new ArrayList<String>();
		String tempMessage = "";
		
		ArrayList<String> results = friendMap.get(player);
		if(results == null || results.isEmpty()) {
			player.sendMessage(ChatColor.GRAY + "Deine Freundesliste ist leer!");
			return;
		}
		String friend = "";
		Player onlineFriend = null;
		for(int i = 0; i < results.size(); i++) {
			friend = results.get(i);
			strList.add(friend);
		}
		Utils.sortAlphabetic(strList);
		for(int i = 0; i < strList.size(); i++) {
			onlineFriend = Utils.getPlayerByName(strList.get(i));
			if(onlineFriend == null) {
				tempMessage += "";
			} else {
				String displayName = Utils.getDisplayNameFormat(onlineFriend);
				tempMessage = displayName + ChatColor.WHITE + ", " + tempMessage;
			}
		}
		if(tempMessage.equals("") || tempMessage.length() == 0) {
			player.sendMessage(ChatColor.GRAY + "Keiner deiner Freunde ist online.");
			return;
		}
		tempMessage = tempMessage.substring(0, tempMessage.length() - 2);
		player.sendMessage(ChatColor.GOLD + "----Deine Freundesliste----");
		this.showOnline(player, tempMessage);
	}
	
	//---------------------------------------------------------------------------------------------------------------
	
	public void playerAddMessage(Player player, String name) {
		Player p = Utils.getPlayerByName(name);
		
		if(p == null) {
			player.sendMessage(ChatColor.RED + "Der Spieler " + ChatColor.AQUA + name + ChatColor.RED + " ist nicht online.");
			return;
		}
		
		if((getPlayer(p) != null && getPlayer(p).equals(player)) || (getPlayer(player) != null && getPlayer(player).equals(p))) {
			player.sendMessage(ChatColor.RED + "Es gibt bereits einen Antrag.");
			return;
		}
		
		player.sendMessage(ChatColor.AQUA + "Du hast den Spieler: " + Utils.getDisplayNameFormat(p) + ChatColor.AQUA + " in deine Freundesliste eingeladen.");
		p.sendMessage(ChatColor.AQUA + "Der Spieler " + Utils.getDisplayNameFormat(player) + ChatColor.AQUA + " versucht dich");
		p.sendMessage(ChatColor.AQUA + "in seine Freundesliste aufzunehmen.");
		p.sendMessage(ChatColor.AQUA + "/fl accept - den Spieler in deine Freundesliste aufnehmen.");
		p.sendMessage(ChatColor.AQUA + "/fl deny   - den Spieler nicht aufnehmen.");
				
		playerMap.put(p,player);
	}
	
	//---------------------------------------------------------------------------------------------------------------
	
	private void showOnline(Player player, String tempMessage) {
		player.sendMessage(ChatColor.GREEN + "Online:");
		player.sendMessage(tempMessage);
	}
	
	//---------------------------------------------------------------------------------------------------------------
	
	private void showOffline(Player player, String tempMessage) {
		player.sendMessage(ChatColor.RED + "Offline:");
		player.sendMessage(tempMessage);
	}
	
	//---------------------------------------------------------------------------------------------------------------
	
	public Player getPlayer(Player player) {
		return playerMap.get(player);
	}
	
	//---------------------------------------------------------------------------------------------------------------
	
	public void removeRequest(Player player) {
		playerMap.remove(player);
	}
	
	//---------------------------------------------------------------------------------------------------------------
	
	public void updateFriendMap(Player player) {
		friendMap.put(player, Utils.getResults(player, connection));
	}
	
	//---------------------------------------------------------------------------------------------------------------
	
	public Map<Player,ArrayList<String>> getFriendMap() {
		return friendMap;
	}
	
	//---------------------------------------------------------------------------------------------------------------
	
	public ArrayList<String> getFriendsFromFriendMap(Player player) {
		return friendMap.get(player);
	}
	
	//---------------------------------------------------------------------------------------------------------------

	public void removeFromFriendMap(Player player) {
		friendMap.remove(player);
	}
	
	//---------------------------------------------------------------------------------------------------------------
	
	public boolean checkPlayerInFriendMap(Player player, String friend) {
		String f = "";
		ArrayList<String> arr = friendMap.get(player);
		if(arr == null || arr.isEmpty()) {
			return false;
		}
		for(int i = 0; i < arr.size(); i++) {
			f = arr.get(i);
			if(friend.equals(f)) {
				return true;
			}
		}
		return false;
	}
	
}
