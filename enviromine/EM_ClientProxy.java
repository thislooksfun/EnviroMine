package enviromine;

import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;

public class EM_ClientProxy extends EM_CommonProxy
{
	public boolean isClient()
	{
		return true;
	}
	
	public boolean isOpenToLAN()
	{
		if(Minecraft.getMinecraft().isIntegratedServerRunning())
		{
			if(Minecraft.getMinecraft().getIntegratedServer().getPublic())
			{
				return true;
			} else
			{
				return false;
			}
		} else
		{
			return false;
		}
	}
	
	public void registerTickHandlers()
	{
		super.registerTickHandlers();
		TickRegistry.registerTickHandler(new EM_ClientScheduledTickHandler(), Side.CLIENT);
    }
	
	public void registerEventHandlers()
	{
		super.registerEventHandlers();
		MinecraftForge.EVENT_BUS.register(new EM_GuiEnviroMeters(Minecraft.getMinecraft()));
    }
}
