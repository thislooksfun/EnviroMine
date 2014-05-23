package enviromine.gases;

import enviromine.handlers.EM_StatusManager;
import enviromine.trackers.EnviroDataTracker;
import net.minecraft.entity.EntityLivingBase;

public class GasCarbonMonoxide extends EnviroGas
{
	public GasCarbonMonoxide(String name, int id)
	{
		super(name, id);
	}
	
	public void applyEffects(EntityLivingBase entityLiving)
	{
		EnviroDataTracker tracker = EM_StatusManager.lookupTracker(entityLiving);
		
		if(tracker != null)
		{
			if(tracker.airQuality >= 0.01F)
			{
				tracker.airQuality -= 0.01F;
			} else
			{
				tracker.airQuality = 0.01F;
			}
		}
	}
}
