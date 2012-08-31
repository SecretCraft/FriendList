package com.github.secretcraft.java.utils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import net.windwaker.sql.Connection;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

public class Utils {
	
	protected static Pattern chatColorPattern = Pattern.compile("(?i)&([0-9A-F])");
	protected static Pattern chatMagicPattern = Pattern.compile("(?i)&([K])");
	protected static Pattern chatBoldPattern = Pattern.compile("(?i)&([L])");
	protected static Pattern chatStrikethroughPattern = Pattern.compile("(?i)&([M])");
	protected static Pattern chatUnderlinePattern = Pattern.compile("(?i)&([N])");
	protected static Pattern chatItalicPattern = Pattern.compile("(?i)&([O])");
	protected static Pattern chatResetPattern = Pattern.compile("(?i)&([R])");
	
	//---------------------------------------------------------------------------------------------------------------
	
	public static String getDisplayNameFormat(Player onlineFriend) {
		PermissionUser user = PermissionsEx.getPermissionManager().getUser(onlineFriend);
		String worldName = onlineFriend.getWorld().getName();
		if(user == null) {
			return null;
		}
		String displayNameFormat = user.getOption("display-name-format", worldName, "%prefix%player%suffix");
		String dpName = displayNameFormat.replace("%prefix", translateColorCodes(user.getPrefix(worldName))).replace("%suffix", translateColorCodes(user.getSuffix(worldName))).replace("%player", onlineFriend.getName());
		return dpName;
	}
	
	//---------------------------------------------------------------------------------------------------------------
	
	protected static String translateColorCodes(String string) {
		if (string == null) {
			return "";
		}

		String newstring = string;
		newstring = chatColorPattern.matcher(newstring).replaceAll("\u00A7$1");
		newstring = chatMagicPattern.matcher(newstring).replaceAll("\u00A7$1");
		newstring = chatBoldPattern.matcher(newstring).replaceAll("\u00A7$1");
		newstring = chatStrikethroughPattern.matcher(newstring).replaceAll("\u00A7$1");
		newstring = chatUnderlinePattern.matcher(newstring).replaceAll("\u00A7$1");
		newstring = chatItalicPattern.matcher(newstring).replaceAll("\u00A7$1");
		newstring = chatResetPattern.matcher(newstring).replaceAll("\u00A7$1");
		return newstring;
	}
	
	//---------------------------------------------------------------------------------------------------------------
	
	public static Player getPlayerByName(String name) {
		for(Player p : Bukkit.getOnlinePlayers()) {
			if(p.getName().equals(name)) {
				return p;
			}
		}
		return null;
	}
	
	//---------------------------------------------------------------------------------------------------------------
	
	public static List<String> sortAlphabetic(List<String> str) {
		String temp;
		for ( int i = 0;  i < str.size() - 1;  i++ ){
            for ( int j = i + 1;  j < str.size();  j++ ){ 
            	if ( str.get(i).compareToIgnoreCase( str.get(j) ) > 0 ){                                             
            		// ascending sort
            		temp = str.get(i);
            		str.add(i, str.get(j));    // swapping
            		str.add(j, temp);
            	}
            }
		} 
		return str;
	}
	
	//---------------------------------------------------------------------------------------------------------------
	
	public static ArrayList<String> getResults(Player player, Connection connection) {
		ArrayList<String> arr = new ArrayList<String>();
		ResultSet results = null;
		try {
			results = connection.query("SELECT friend FROM friend_list WHERE player ='"+player.getName()+"'");
			if(results.next()) {
				do {
					arr.add(results.getString("friend"));
				} while(results.next());
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return arr;
	}
	
	//---------------------------------------------------------------------------------------------------------------
	
	public static boolean checkFriendResult(Player player, ResultSet res) throws SQLException {
		while(res.next()) {
			String friend = res.getString("friend");
			if(player.getName().equals(friend)) {
				return true;
			}
		}
		return false;
	}
	
}
