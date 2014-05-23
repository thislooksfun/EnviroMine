package enviromine.gases;

import enviromine.EnviroDamageSource;
import net.minecraft.entity.EntityLivingBase;

public class GasFire extends EnviroGas
{
	public GasFire(String name, int ID)
	{
		super(name, ID);
	}
	
	@Override
	public void applyEffects(EntityLivingBase entityLiving)
	{
		entityLiving.attackEntityFrom(EnviroDamageSource.gasfire, 1F);
	}
	
	@Override
	public EnviroGas getGasOnDeath()
	{
		return EnviroGasDictionary.carbonMonoxide;
	}
}
