package enviromine.handlers;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;
import java.util.logging.Level;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ChatMessageComponent;
import enviromine.EntityPhysicsBlock;
import enviromine.core.EnviroMine;
import enviromine.trackers.EnviroDataTracker;

public class SurpriseHandler
{
	public static String lastSurpriseID = "null";
	public static int surpriseRate = 0;
	
	public static void updateInterval()
	{
		surpriseRate += 1;
		
		if(surpriseRate > 120)
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
			} else if(actionType.equalsIgnoreCase("Message") && data.length >= 4)
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
				
				try
				{
					float volume = Float.parseFloat(data[5].trim());
					float pitch = Float.parseFloat(data[6].trim());
					target.worldObj.playSoundAtEntity(target, data[4].trim(), volume, pitch);
					
					System.out.println("EMS ID: " + actionType + " Args (" + data[4].trim()  + ", " + volume + ", " + pitch + ")");
				} catch(NumberFormatException e)
				{
					return;
				}
			} else if(actionType.equalsIgnoreCase("CreeperJockey") && data.length == 4)
			{
				if(target.worldObj.isRemote)
				{
					return;
				}
				
				Random rand = target.getRNG();
				
				int i = (int)(x + rand.nextInt(20) - 10);
				int j = (int)(y + rand.nextInt(2) - 1);
				int k = (int)(z + rand.nextInt(20) - 10);
				
				if(Math.abs(x - i) < 4 || Math.abs(z - k) < 4)
				{
					return;
				}
				
				EntityCreeper creeper = new EntityCreeper(target.worldObj);
				EntitySpider spider = new EntitySpider(target.worldObj);
				
				spider.setPositionAndRotation(i, j, k, rand.nextFloat() * 360F, 0.0F);
				creeper.setPositionAndRotation(i, j, k, rand.nextFloat() * 360F, 0.0F);
				
				if(spider.getCanSpawnHere())
				{
					target.worldObj.spawnEntityInWorld(spider);
					target.worldObj.spawnEntityInWorld(creeper);
					creeper.mountEntity(spider);
			        creeper.getDataWatcher().updateObject(17, Byte.valueOf((byte)1));
			        spider.getEntityAttribute(SharedMonsterAttributes.followRange).setAttribute(64D);
			        creeper.getEntityAttribute(SharedMonsterAttributes.followRange).setAttribute(64D);
					
					System.out.println("EMS ID: " + actionType + " at (" + i + "," + j + "," + k + ")");
				} else
				{
					return;
				}
			} else if(actionType.equalsIgnoreCase("StatusBar") && data.length == 6)
			{
				EnviroDataTracker tracker = EM_StatusManager.lookupTracker(target);
				float value = 0.0F;
				
				try
				{
					value = Float.parseFloat(data[5].trim());
				} catch(NumberFormatException e)
				{
					return;
				}
				
				if(data[4].trim().equalsIgnoreCase("santity"))
				{
					tracker.sanity = value;
				} else if(data[4].trim().equalsIgnoreCase("hydration"))
				{
					tracker.hydration = value;
				} else if(data[4].trim().equalsIgnoreCase("air"))
				{
					tracker.airQuality = value;
				} else if(data[4].trim().equalsIgnoreCase("temp"))
				{
					tracker.bodyTemp = value;
				}
			} else if(actionType.equalsIgnoreCase("Effect") && data.length == 7)
			{
				if(target.worldObj.isRemote)
				{
					return;
				}
				
				int pID = 0;
				int dur = 0;
				int amp = 0;
				
				try
				{
					pID = Integer.parseInt(data[4].trim());
					dur = Integer.parseInt(data[5].trim());
					amp = Integer.parseInt(data[6].trim());
				} catch(NumberFormatException e)
				{
					return;
				}
				
				if(pID >= Potion.potionTypes.length || pID < 0)
				{
					return;
				}
				
				Potion potion = Potion.potionTypes[pID];
				
				target.addPotionEffect(new PotionEffect(pID, dur, amp));
			} else if(actionType.equalsIgnoreCase("ItemShower") && data.length == 5)
			{
				if(target.worldObj.isRemote)
				{
					return;
				}
				
				int iID = 0;
				
				try
				{
					iID = Integer.parseInt(data[4].trim());
				} catch(NumberFormatException e)
				{
					return;
				}
				
				if(iID < 0 || iID >= Item.itemsList.length)
				{
					return;
				}
				
				for(double i = target.posX - 2; i <= target.posX + 2; i++)
				{
					for(double k = target.posZ - 2; k <= target.posZ + 2; k++)
					{
						EntityItem item = new EntityItem(target.worldObj, i, 200D, k, new ItemStack(Item.itemsList[iID]));
						target.worldObj.spawnEntityInWorld(item);
					}
				}
			} else
			{
				return;
			}
			
			if(!message.equals(""))
			{
				target.sendChatToPlayer(ChatMessageComponent.createFromText(message));
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
}
