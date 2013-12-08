package enviromine;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;

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
		DataInputStream inputStream = new DataInputStream(new ByteArrayInputStream(packet.data));
		
		String[] data;
		boolean isPlayer;
		EnviroDataTracker tracker;

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
		
		try
		{
			tracker = EM_StatusManager.lookupTrackerFromID(Integer.parseInt(data[0]));
			isPlayer = false;
		} catch(NumberFormatException e)
		{
			tracker = EM_StatusManager.lookupPlayerTrackerFromName(data[0]);
			isPlayer = true;
		}
		
		if(tracker != null)
		{
			tracker.airQuality = Float.valueOf(data[1]);
			tracker.bodyTemp = Float.valueOf(data[2]);
			tracker.hydration = Float.valueOf(data[3]);
			tracker.sanity = Float.valueOf(data[4]);
			tracker.airTemp = Float.valueOf(data[5]);
		}
	}
}
