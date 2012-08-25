package com.github.secretcraft.java;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import net.windwaker.sql.Driver;
import net.windwaker.sql.Connection;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.earth2me.essentials.commands.EssentialsCommand;
import com.github.secretcraft.java.utils.Utils;

public class FriendList extends JavaPlugin{
	
	private Connection connection;
	private Configuration config;
	private FLCommand flCommand;
	private FLMessage flMessage;
	private Map<Player,Player> denyMap;
	Logger log;
	
	//---------------------------------------------------------------------------------------------------------------
	
	@Override
	public void onEnable() {
		super.onEnable();
		
		log = getLogger();
		config = this.getConfig();
		denyMap = new HashMap<Player,Player>();
		
		if(config.get("enable") == null) {
			initConfig();
		}
		
		this.reloadConfig();
		
		if(config.getBoolean("enable") == true) {
			connectToDB();
			flCommand = new FLCommand(log, this);
			flMessage = new FLMessage(flCommand);
			log.info("FriendList enabled.");
		} else {
			log.info("FriendList disabled, check config.yml");
			this.getPluginLoader().disablePlugin(this);
		}
		
	}
	
	//---------------------------------------------------------------------------------------------------------------
	
	@Override
	public void onDisable() {
		super.onDisable();
		
		log.info("FriendList disabled.");
		
	}
	
	//---------------------------------------------------------------------------------------------------------------
	
	private void connectToDB() {
		// zur Datenbank verbinden
		connection = new Connection( config.getString("Settings.MySQL.Host") + "/" + config.getString("Settings.MySQL.Database") , Driver.getByProtocol("mysql"));
		
		try {
			connection.connect( config.getString("Settings.MySQL.User"), config.getString("Settings.MySQL.Password"));
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//---------------------------------------------------------------------------------------------------------------
	
		public boolean onCommand(CommandSender sender, Command cmd, String cmdLabel, String[] args) {
			
			Player player = null;
			
			if(sender instanceof Player) {
				player = (Player)sender;
			} else {
				log.info("Command kann nur Ingame ausgeführt werden.");
				return false;
			}
			
			String error = ChatColor.GRAY + "Korrekte Schreibweise ist: " + ChatColor.RED + "/fl <add>,<remove>,<list>,<online>"+ ChatColor.AQUA +" <Name>";
			
			String cmdName = cmd.getName();
			
			if(cmdName.equals("fl")) {
				
				if(args[0].equalsIgnoreCase("msg")) {
					String message = EssentialsCommand.getFinalArg(args, 1);
					flMessage.sendMsg(player, message);
					return true;
				}
				
				if(args.length > 2 || args.length < 1){
					player.sendMessage(error);
					return false;
				}
				
				if(args[0].equalsIgnoreCase("add")) {
					if(args.length != 2) {
						player.sendMessage(error);
						return false;
					}
					
					if(playerExists(args[1])) {
						if(playerIsOnline(args[1])) {
							denyMap.put(Utils.getPlayerByName(args[1]), player);
							flCommand.playerAddMessage(player, args[1]);
							return true;
						} else {
							player.sendMessage(ChatColor.RED + "Der eingegebene Spieler ist nicht online.");
							return false;
						}
					} else {
						player.sendMessage(ChatColor.RED + "Der eingegebene Spieler existiert nicht.");
						return false;
					}
					
				}
				
				if(args[0].equalsIgnoreCase("accept")) {
					if(args.length != 1) {
						player.sendMessage(error);
						return false;
					}
					if(flCommand.getPlayer(player) == null) {
						player.sendMessage(ChatColor.RED + "Es gibt keinen Antrag dem du zustimmen könntest.");
						return false;
					}
					flCommand.addFriend(flCommand.getPlayer(player), player);
					flCommand.addFriend(player, flCommand.getPlayer(player));
					flCommand.removeRequest(player);
					
					return true;
				}
				
				if(args[0].equalsIgnoreCase("deny")) {
					if(args.length != 1 || flCommand.getPlayer(player) == null) {
						player.sendMessage(error);
						return false;
					}
					Player p = denyMap.get(player);
					denyMap.remove(player);
					flCommand.removeRequest(player);
					p.sendMessage(ChatColor.RED + "Du wurdest abgelehnt.");
					player.sendMessage(ChatColor.RED + "Du hast abgelehnt.");
					return true;
				}
				
				if(args[0].equalsIgnoreCase("remove")) {
					if(args.length != 2) {
						player.sendMessage(error);
						return false;
					}
					
					if(playerExists(args[1])) {
						flCommand.removeFriend(player,args[1]);
						return true;
					} else {
						player.sendMessage(ChatColor.RED + "Der eingegebene Spieler existiert nicht.");
						return false;
					}
				}
		
				if(args[0].equalsIgnoreCase("list")) {
					if(args.length != 1) {
						player.sendMessage(error);
						return false;
					}
					
					flCommand.showList(player);
					
				}
				
				if(args[0].equalsIgnoreCase("reload")) {
					if(args.length != 1) {
						player.sendMessage(error);
						return false;
					}
				}
				
			}
			return false;
		}
	
		//---------------------------------------------------------------------------------------------------------------
		
		private void initConfig() {
			config.addDefault("enable", false);
			config.addDefault("add-only-online-players", true);
			config.addDefault("Settings.MySQL.Host", "localhost");
			config.addDefault("Settings.MySQL.Database", "friendlist");
			config.addDefault("Settings.MySQL.User", "friendlist");
			config.addDefault("Settings.MySQL.Password", "password");
			config.options().copyDefaults(true);
			this.saveConfig();
		}
		
		//---------------------------------------------------------------------------------------------------------------
		
		private boolean playerExists(String name) {
			
			for(OfflinePlayer player : this.getServer().getOfflinePlayers()) {
				
				if(player.getName().equals(name)) {
					return true;
				}
				
			}
			return false;
		}
		
		//---------------------------------------------------------------------------------------------------------------
		
		public boolean playerIsOnline(String name) {
			
			for(Player player : this.getServer().getOnlinePlayers()) {
				
				if(player.getName().equals(name)) {
					return true;
				}
			}
			return false;
		}
		
		//---------------------------------------------------------------------------------------------------------------
		
		public Connection getConnection() {
			return connection;
		}
		
	}
