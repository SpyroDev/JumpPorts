package net.dwdg.jumpports;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class Lang {
	
	private JumpPortsPlugin plugin;
	
	File langFile;
	FileConfiguration lang;
	
	public Lang(JumpPortsPlugin plugin) {
		this.plugin = plugin;
		setupLanguage();
	}
	
	public void setupLanguage() {
		langFile = new File(plugin.getDataFolder(), "lang.yml");
		lang = new YamlConfiguration();
		
		if(!langFile.exists()){
			langFile.getParentFile().mkdirs();
	        copy(plugin.getResource("lang.yml"), langFile);
	    }
		loadLang();
	}
	
	public void saveLang() {
	    try {
	        lang.save(langFile);
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}
	
	public void loadLang() {
	    try {
	        lang.load(langFile);
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
	private void copy(InputStream in, File file) {
	    try {
	        OutputStream out = new FileOutputStream(file);
	        byte[] buf = new byte[1024];
	        int len;
	        while((len=in.read(buf))>0){
	            out.write(buf,0,len);
	        }
	        out.close();
	        in.close();
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
	public FileConfiguration getLang() {
		return this.lang;
	}
	
	public static String get(String path) {
		String val = JumpPortsPlugin.lang.getLang().getString(path);
		return parseColours(val);
	}
	
	public static String parseColours(String str) {
		if(str == null) {
			str = "";
		}
		Pattern color_codes = Pattern.compile("&([0-9A-Fa-fKkLlOoMmNn])");
		Matcher find_colors = color_codes.matcher(str);
		while (find_colors.find()) {
		 str = find_colors.replaceFirst(new StringBuilder().append(ChatColor.COLOR_CHAR).append(find_colors.group(1)).toString());
		 find_colors = color_codes.matcher(str);
		}
		return str;
	}
}
