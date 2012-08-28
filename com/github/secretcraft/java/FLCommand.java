package com.github.secretcraft.java;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
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
	
	//---------------------------------------------------------------------------------------------------------------
	
	public FLCommand(Logger log, FriendList plugin) {
		connection = plugin.getConnection();
		this.plugin = plugin;
		this.log = log;
		friend_list = new Table(connection, "friend_list");
		playerMap = new HashMap<Player,Player>();
		
		try {
			if(!(friend_list.exists())) {
				
				Map<String, DataType> columnDataTypeMap = new HashMap<String, DataType>();
				columnDataTypeMap.put("player", new DataType(DataType.TEXT));
				columnDataTypeMap.put("friend", new DataType(DataType.TEXT));
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
				if(friendName.equals(name) && playerName.equals(player.getName())) {
					player.sendMessage(ChatColor.RED + "Der Spieler: " + ChatColor.AQUA + name + ChatColor.RED +
							" ist bereits in deiner Freundesliste!");
					return;
				}
			}
			
			String[] fields = {"player", "friend"};
			String offlinePlayer = plugin.getServer().getOfflinePlayer(name).getName();
			String[] values = {player.getName(), offlinePlayer};
			friend_list.add(fields, values);
			
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
		
		String tempMessage = "";
		
		try {
			ResultSet results = this.getResults(player);
			String friend = "";
			Player onlineFriend = null;
			while(results.next()) {
				friend = results.getString("friend");
				onlineFriend = Utils.getPlayerByName(friend);
				if(onlineFriend == null) {
					tempMessage += ChatColor.RED + friend + ChatColor.WHITE + ", ";
				} else {
					String displayName = Utils.getDisplayNameFormat(onlineFriend);
					tempMessage = displayName + ChatColor.WHITE + ", " + tempMessage;
				}
			}
			if(tempMessage.equals("") || tempMessage.length() == 0) {
				player.sendMessage(ChatColor.GRAY + "Deine Freundesliste ist leer!");
				return;
			}
			tempMessage = tempMessage.substring(0, tempMessage.length() - 2);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		player.sendMessage(ChatColor.GRAY + "Deine Freundesliste:");
		player.sendMessage(tempMessage);
	}
	
	//---------------------------------------------------------------------------------------------------------------
	
	public void showOnline(Player player) {
		
		String tempMessage = "";
		
		try {
			ResultSet results = this.getResults(player);
			String friend = "";
			Player onlineFriend = null;
			while(results.next()) {
				friend = results.getString("friend");
				onlineFriend = Utils.getPlayerByName(friend);
				if(onlineFriend == null) {
					tempMessage += "";
				} else {
					String displayName = Utils.getDisplayNameFormat(onlineFriend);
					tempMessage = displayName + ChatColor.WHITE + ", " + tempMessage;
					log.info(tempMessage);
				}
			}
			if(tempMessage.equals("") || tempMessage.length() == 0) {
				player.sendMessage(ChatColor.GRAY + "Deine Freundesliste ist leer, oder keiner deiner Freunde ist online.");
				return;
			}
			tempMessage = tempMessage.substring(0, tempMessage.length() - 2);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		player.sendMessage(ChatColor.GRAY + "Deine Freundesliste:");
		player.sendMessage(tempMessage);
	}
	
	//---------------------------------------------------------------------------------------------------------------
	
	public ResultSet getResults(Player player) {
		ResultSet results = null;
		try {
			results = connection.query("SELECT friend FROM friend_list WHERE player ='"+player.getName()+"'");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return results;
	}
	
	//---------------------------------------------------------------------------------------------------------------
	
	public void playerAddMessage(Player player, String name) {
		Player p = Utils.getPlayerByName(name);
		if(p == null) {
			player.sendMessage(ChatColor.RED + "Der Spieler " + name + " ist nicht online.");
		}
		player.sendMessage(ChatColor.AQUA + "Du hast den Spieler: " + Utils.getDisplayNameFormat(p) + ChatColor.AQUA + " in deine Freundesliste eingeladen.");
		p.sendMessage(ChatColor.AQUA + "Der Spieler " + Utils.getDisplayNameFormat(player) + ChatColor.AQUA + " versucht dich");
		p.sendMessage(ChatColor.AQUA + "in seine Freundesliste aufzunehmen.");
		p.sendMessage(ChatColor.AQUA + "/fl accept - den Spieler in deine Freundesliste aufnehmen.");
		p.sendMessage(ChatColor.AQUA + "/fl deny   - den Spieler nicht aufnehmen.");
				
		playerMap.put(p,player);
	}
	
	//---------------------------------------------------------------------------------------------------------------
	
	public Player getPlayer(Player player) {
		return playerMap.get(player);
	}
	
	//---------------------------------------------------------------------------------------------------------------
	
	public void removeRequest(Player player) {
		playerMap.remove(player);
	}
	
}
