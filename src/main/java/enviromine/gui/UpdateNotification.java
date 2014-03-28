package enviromine.gui;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumChatFormatting;
import cpw.mods.fml.common.IPlayerTracker;
import enviromine.core.EM_Settings;
import enviromine.core.EnviroMine;

public class UpdateNotification implements IPlayerTracker
{
	
	@Override
	public void onPlayerLogin(EntityPlayer player)
	{
		if(EM_Settings.Version == "FWG_EM_VER")
		{
			return;
		}
		
		String[] data;
		try
		{
			data = getNotification();
			
			String version = data[0].trim();
			String http = data[1].trim();
			
			int verStat = compareVersions(EM_Settings.Version, version);
			
			if(verStat == -1)
			{
				player.addChatMessage(EnumChatFormatting.RED + "Update " + version + " of EnviroMine is available");
				player.addChatMessage(EnumChatFormatting.RESET + "Download:\n" + EnumChatFormatting.BLUE + EnumChatFormatting.UNDERLINE + http);
				for(int i = 2; i < data.length; i++)
				{
					if(i > 5)
					{
						player.addChatMessage("" + (data.length - 6) + " more...");
						break;
					} else
					{
						player.addChatMessage(EnumChatFormatting.RESET + "" + data[i].trim());
					}
				}
			} else if(verStat == 0)
			{
				player.addChatMessage(EnumChatFormatting.YELLOW + "EnviroMine " + EM_Settings.Version + " is up to date");
			} else if(verStat == 1)
			{
				player.addChatMessage(EnumChatFormatting.YELLOW + "EnviroMine " + EM_Settings.Version + " is a debug version");
			}
			
		} catch(IOException e)
		{
			e.printStackTrace();
		}
		
	}
	
	@Override
	public void onPlayerLogout(EntityPlayer player)
	{
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onPlayerChangedDimension(EntityPlayer player)
	{
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onPlayerRespawn(EntityPlayer player)
	{
		// TODO Auto-generated method stub
		
	}
	
	private String[] getNotification() throws IOException
	{
		URL url = new URL("https://dl.dropboxusercontent.com/s/whvguhlicfgt9qb/version.txt");
		HttpURLConnection.setFollowRedirects(true);
		HttpURLConnection con = (HttpURLConnection)url.openConnection();
		con.setDoOutput(false);
		con.setReadTimeout(20000);
		con.setRequestProperty("Connection", "keep-alive");
		
		con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:16.0) Gecko/20100101 Firefox/16.0");
		((HttpURLConnection)con).setRequestMethod("GET");
		con.setConnectTimeout(5000);
		BufferedInputStream in = new BufferedInputStream(con.getInputStream());
		int responseCode = con.getResponseCode();
		if(responseCode != HttpURLConnection.HTTP_OK)
		{
			EnviroMine.logger.log(Level.WARNING, "Update request returned response code: " + responseCode);
		}
		StringBuffer buffer = new StringBuffer();
		int chars_read;
		//	int total = 0;
		while((chars_read = in.read()) != -1)
		{
			char g = (char)chars_read;
			buffer.append(g);
		}
		final String page = buffer.toString();
		
		String[] pageSplit = page.split("\\n");
		
		return pageSplit;
	}
	
	public int compareVersions(String oldVer, String newVer)
	{
		int result = 0;
		int[] oldNum;
		int[] newNum;
		String[] oldNumStr;
		String[] newNumStr;
		
		try
		{
			oldNumStr = oldVer.split("\\.");
			newNumStr = newVer.split("\\.");
			
			oldNum = new int[]{Integer.valueOf(oldNumStr[0]),Integer.valueOf(oldNumStr[1]),Integer.valueOf(oldNumStr[2])};
			newNum = new int[]{Integer.valueOf(newNumStr[0]),Integer.valueOf(newNumStr[1]),Integer.valueOf(newNumStr[2])};
		} catch(IndexOutOfBoundsException e)
		{
			EnviroMine.logger.log(Level.WARNING, "An IndexOutOfBoundsException occured while checking version!\n" + e.getMessage());
			return -2;
		} catch(NumberFormatException e)
		{
			EnviroMine.logger.log(Level.WARNING, "A NumberFormatException occured while checking version!\n" + e.getMessage());
			return -2;
		}
		
		for(int i = 0; i < 3; i++)
		{
			if(oldNum[i] < newNum[i])
			{
				return -1;
			} else if(oldNum[i] > newNum[i])
			{
				return 1;
			}
		}
		return result;
	}
}
