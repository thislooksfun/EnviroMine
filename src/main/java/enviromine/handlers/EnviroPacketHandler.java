package enviromine.handlers;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.server.MinecraftServer;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;
import enviromine.core.EM_Settings;
import enviromine.core.EnviroMine;
import enviromine.trackers.EnviroDataTracker;

public class EnviroPacketHandler implements IPacketHandler
{
	@Override
	public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player playerEntity)
	{
		if(packet.channel.equals(EM_Settings.Channel))
		{
			handleEnviroPacket(packet);
		}
	}
	
	public void handleEnviroPacket(Packet250CustomPayload packet)
	{
		try
		{
			String[] data;
			
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			DataOutputStream outputStream = new DataOutputStream(bos);
			try
			{
				outputStream.write(packet.data);
			} catch(IOException e1)
			{
				e1.printStackTrace();
				return;
			}
			data = bos.toString().split(",");
			
			if(data[0].trim().equalsIgnoreCase("ID:0"))
			{
				this.trackerSync(data);
			} else if(data[0].trim().equalsIgnoreCase("ID:1"))
			{
				this.emptyRightClick(data);
			} else
			{
				EnviroMine.logger.log(Level.WARNING, "EnviroMine received an unknown packet with data: " + data);
				EnviroMine.logger.log(Level.WARNING, "Please report this to the author as this is not supposed to happen!");
			}
			
			outputStream.close();
			bos.close();
		} catch (IOException e)
		{
			EnviroMine.logger.log(Level.SEVERE, "EnviroMine has encountered an error while parsing a packet!", e);
		}
	}
	
	void trackerSync(String[] data)
	{
		
		EnviroDataTracker tracker = EM_StatusManager.lookupTrackerFromUsername(data[1].trim());
		
		if(tracker != null)
		{
			tracker.prevAirQuality = tracker.airQuality;
			tracker.prevBodyTemp = tracker.bodyTemp;
			tracker.prevHydration = tracker.hydration;
			tracker.prevSanity = tracker.sanity;
			tracker.airQuality = Float.valueOf(data[2]);
			tracker.bodyTemp = Float.valueOf(data[3]);
			tracker.hydration = Float.valueOf(data[4]);
			tracker.sanity = Float.valueOf(data[5]);
			tracker.airTemp = Float.valueOf(data[6]);
		} else
		{
			EnviroMine.logger.log(Level.WARNING, "Failed to sync tracker for " + data[1].trim());
			EnviroMine.logger.log(Level.WARNING, "Stats may be inaccurate!");
		}
	}
	
	void emptyRightClick(String[] data)
	{
		EntityPlayer player = EM_StatusManager.findPlayer(data[1].trim());
		
		if(player != null)
		{
			EM_EventManager.drinkWater(player, null);
		}
	}
}
