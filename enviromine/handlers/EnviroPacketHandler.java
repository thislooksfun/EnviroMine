package enviromine.handlers;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;
import enviromine.core.EM_Settings;
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
		String[] data;
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
		
		tracker = EM_StatusManager.lookupTrackerFromUsername(data[0]);
		
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
