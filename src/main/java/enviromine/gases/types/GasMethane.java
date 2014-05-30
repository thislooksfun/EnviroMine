package enviromine.gases.types;

import java.awt.Color;
import enviromine.gases.EnviroGas;

public class GasMethane extends EnviroGas
{
	public GasMethane(String name, int ID)
	{
		super(name, ID);
		this.setColor(new Color(0, 255, 0, 8));
		this.setDensity(1F);
		this.setVolitility(5F);
	}
}
