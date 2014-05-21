package enviromine.gases;

import java.awt.Color;
import net.minecraft.entity.EntityLivingBase;

public class EnviroGas
{
	public int gasID;
	public float volitility;
	public float density;
	public String name;
	public Color color;
	
	public EnviroGas(String name, int ID)
	{
		this.gasID = ID;
		this.density = 0F;
		this.name = name;
		volitility = 0F;
		color = Color.WHITE;
		
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
	
	public void applyEffects(EntityLivingBase entityLiving)
	{
	}
	
	public EnviroGas getGasOnDeath()
	{
		return null;
	}
}
