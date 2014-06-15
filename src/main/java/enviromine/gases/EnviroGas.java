package enviromine.gases;

import java.awt.Color;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;

public class EnviroGas
{
	public int gasID;
	public float volitility;
	public float density;
	public String name;
	public Color color;
	public int airDecay;
	public int normDecay;
	public int normDecayThresh;
	public int airDecayThresh;
	
	public EnviroGas(String name, int ID)
	{
		this.gasID = ID;
		this.density = 0F;
		this.name = name;
		this.volitility = 0F;
		this.color = Color.WHITE;
		this.airDecay = 0;
		this.normDecay = 0;
		this.normDecayThresh = 1;
		this.airDecayThresh = 1;
		
		EnviroGasDictionary.addNewGas(this, gasID);
	}
	
	public EnviroGas setDensity(float newDen)
	{
		this.density = newDen;
		return this;
	}
	
	public EnviroGas setColor(Color newCol)
	{
		this.color = newCol;
		return this;
	}
	
	public EnviroGas setVolitility(float newVol)
	{
		this.volitility = newVol;
		return this;
	}
	
	public EnviroGas setDecayRates(int airDecay, int normDecay, int adt, int ndt)
	{
		this.airDecay = airDecay;
		this.normDecay = normDecay;
		this.airDecayThresh = adt;
		this.normDecayThresh = ndt;
		return this;
	}
	
	public void applyEffects(EntityLivingBase entityLiving, int amplifier)
	{
	}
	
	public float getOpacity()
	{
		return this.color.getAlpha()/255F;
	}
	
	public int getGasOnDeath(World world, int i, int j, int k)
	{
		return -1;
	}
}
