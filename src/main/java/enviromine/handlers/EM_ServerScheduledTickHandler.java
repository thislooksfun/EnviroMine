package enviromine.handlers;

import java.util.EnumSet;
import net.minecraft.world.World;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;
import enviromine.core.EM_Settings;

public class EM_ServerScheduledTickHandler implements ITickHandler
{
	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData)
	{
	}
	
	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData)
	{
		if(EM_Settings.enablePhysics)
		{
			EM_PhysManager.updateSchedule();
		}
	}
	
	@Override
	public EnumSet<TickType> ticks()
	{
		return EnumSet.of(TickType.SERVER);
	}
	
	@Override
	public String getLabel()
	{
		return null;
	}
}
