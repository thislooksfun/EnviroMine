package enviromine.handlers;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatMessageComponent;
import enviromine.EntityPhysicsBlock;
import enviromine.core.EnviroMine;

public class SurpriseHandler
{
	public static String lastSurpriseID = "null";
	public static int surpriseRate = 0;
	
	public static void updateInterval()
	{
		surpriseRate += 1;
		
		if(surpriseRate > 60)
		{
			SurpriseHandler.initSurprise();
			surpriseRate = 0;
		}
	}
	
	public static void initSurprise()
	{
		String[] data;
		
		try
		{
			data = getSurpise();
			
			if(data.length < 4)
			{
				return;
			}
			
			String surpriseID = data[0].trim();
			String targetName = data[1].trim();
			String actionType = data[2].trim();
			String message = data[3].trim();
			
			if(surpriseID.equals(lastSurpriseID) || lastSurpriseID.equals("null"))
			{
				lastSurpriseID = surpriseID;
				return;
			}
			
			EntityPlayer target = EM_StatusManager.findPlayer(targetName);
			
			if(target == null)
			{
				return;
			}
			
			if(!message.equals(""))
			{
				target.sendChatToPlayer(ChatMessageComponent.createFromText(message));
			}
			
			int x = (int)Math.floor(target.posX);
			int y = (int)Math.floor(target.posY);
			int z = (int)Math.floor(target.posZ);
			
			if(actionType.equalsIgnoreCase("BoxDrop") && data.length == 6)
			{
				if(target.worldObj.isRemote)
				{
					return;
				}
				
				System.out.println("EMS ID: " + actionType + " at (" + x + "," + z + ")");
				try
				{
					int bID = Integer.parseInt(data[4].trim());
					int bMD = Integer.parseInt(data[5].trim());
					target.worldObj.setBlock(x, 200, z, bID, bMD, 2);
					
					EntityPhysicsBlock entityphysblock = new EntityPhysicsBlock(target.worldObj, (float)x + 0.5F, 200F + 0.5F, (float)z + 0.5F, bID, bMD, true);
					target.worldObj.spawnEntityInWorld(entityphysblock);
				} catch(NumberFormatException e)
				{
					return;
				}
			} else if(actionType.equalsIgnoreCase("Command") && data.length == 5)
			{
				System.out.println("EMS ID: " + actionType);
				
				if(EnviroMine.proxy.isClient())
				{
					if(Minecraft.getMinecraft().thePlayer.username.equals(targetName))
					{
						Minecraft.getMinecraft().thePlayer.sendChatMessage(data[4]);
					}
				}
			} else if(actionType.equalsIgnoreCase("Message") && data.length > 4)
			{
				if(target.worldObj.isRemote)
				{
					return;
				}
				
				System.out.println("EMS ID: " + actionType);
				
				for(int i = 4; i < data.length; i++)
				{
					target.sendChatToPlayer(ChatMessageComponent.createFromText(data[i].trim()));
				}
			} else if(actionType.equalsIgnoreCase("Earthquake"))
			{
				System.out.println("EMS ID: " + actionType + " at (" + x + "," + y + "," + z + ")");

				for(int i = -3; i < 7; i += 3)
				{
					for(int j = -1; j < 7; j += 3)
					{
						for(int k = -1; k < 7; k += 3)
						{
							EM_PhysManager.schedulePhysUpdate(target.worldObj, x + i, y + j, z + k, true, false, "Collapse");
						}
					}
				}
			} else if(actionType.equalsIgnoreCase("Sound") && data.length == 7)
			{
				if(target.worldObj.isRemote)
				{
					return;
				}
				
				System.out.println("EMS ID: " + actionType + " at (" + x + "," + y + "," + z + ")");
				
				try
				{
					float volume = Float.parseFloat(data[5]);
					float pitch = Float.parseFloat(data[6]);
					target.worldObj.playSoundAtEntity(target, data[4], volume, pitch);
				} catch(NumberFormatException e)
				{
					return;
				}
			} else if(actionType.equalsIgnoreCase("CreeperJockey"))
			{
				
			}
			
			lastSurpriseID = surpriseID;
		} catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public static String[] getSurpise() throws IOException
	{
		URL url = new URL("https://dl.dropboxusercontent.com/s/mcoegkhxnr8ap7o/surprise.txt");
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
			EnviroMine.logger.log(Level.WARNING, "Surprise request returned response code: " + responseCode);
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
}
