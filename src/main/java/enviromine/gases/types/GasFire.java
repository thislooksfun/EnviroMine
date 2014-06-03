package enviromine.gases.types;

import java.awt.Color;
import enviromine.gases.EnviroGas;
import enviromine.gases.EnviroGasDictionary;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

public class GasFire extends EnviroGas
{
	public GasFire(String name, int ID)
	{
		super(name, ID);
		this.setColor(new Color(255, 128, 0, 192));
		this.setDensity(-1F);
		this.setDecayRates(1, 1);
	}
	
	@Override
	public void applyEffects(EntityLivingBase entityLiving, int amplifier)
	{
		entityLiving.attackEntityFrom(DamageSource.onFire, 0.5F * amplifier);
		entityLiving.setFire(10);
	}
	
	public int gasOnDeath(World world, int i, int j, int k)
	{
		if(j >= 48)
		{
			return EnviroGasDictionary.carbonDioxide.gasID;
		} else
		{
			return EnviroGasDictionary.carbonMonoxide.gasID;
		}
	}
}
