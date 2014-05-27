package enviromine.gases;

import java.awt.Color;
import java.util.logging.Level;
import enviromine.core.EnviroMine;

public class EnviroGasDictionary
{
	public static EnviroGas[] gasList = new EnviroGas[128];
	
	public static EnviroGas gasFire = (new GasFire("Gas Fire", 0)).setColor(Color.ORANGE).setDensity(-1F);
	public static EnviroGas carbonMonoxide = (new GasCarbonMonoxide("Carbon Monoxide", 1)).setColor(Color.BLACK).setDensity(-1F);
	
	public static void addNewGas(EnviroGas newGas, int id)
	{
		if(id < 128 && id >= 0)
		{
			if(gasList[id] == null)
			{
				gasList[id] = newGas;
				EnviroMine.logger.log(Level.INFO, "Registered gas " + newGas.name);
			} else
			{
				EnviroMine.logger.log(Level.WARNING, "Unable to add gas " + newGas.name + " to dictionary: ID " + id + " is used!");
			}
		} else
		{
			EnviroMine.logger.log(Level.WARNING, "Unable to add gas " + newGas.name + " to dictionary: ID out of bounds!");
		}
	}
}
