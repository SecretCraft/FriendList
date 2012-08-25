package com.github.secretcraft.java.utils;

import java.util.regex.Pattern;

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
	
	public static Player getPlayerByName(String name) {
		for(Player p : Bukkit.getOnlinePlayers()) {
			if(p.getName().equals(name)) {
				return p;
			}
		}
		return null;
	}
	
}
