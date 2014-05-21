package enviromine;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.ByteOrder;
import net.minecraft.potion.Potion;
import enviromine.core.EM_Settings;

public class EnviroUtils
{
	public static int getColorFromRGBA_F(float par1, float par2, float par3, float par4)
	{
		int R = (int)(par1 * 255.0F);
		int G = (int)(par2 * 255.0F);
		int B = (int)(par3 * 255.0F);
		int A = (int)(par4 * 255.0F);
		
		return getColorFromRGBA(R, G, B, A);
	}
	
	public static int getColorFromRGBA(int R, int G, int B, int A)
	{
		if(R > 255)
		{
			R = 255;
		}
		
		if(G > 255)
		{
			G = 255;
		}
		
		if(B > 255)
		{
			B = 255;
		}
		
		if(A > 255)
		{
			A = 255;
		}
		
		if(R < 0)
		{
			R = 0;
		}
		
		if(G < 0)
		{
			G = 0;
		}
		
		if(B < 0)
		{
			B = 0;
		}
		
		if(A < 0)
		{
			A = 0;
		}
		
		if(ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN)
		{
			return A << 24 | R << 16 | G << 8 | B;
		} else
		{
			return B << 24 | G << 16 | R << 8 | A;
		}
	}
	
	public static void extendPotionList()
	{
		int maxID = 32;
		
		if(EM_Settings.heatstrokePotionID >= maxID)
		{
			maxID = EM_Settings.heatstrokePotionID + 1;
		}
		
		if(EM_Settings.hypothermiaPotionID >= maxID)
		{
			maxID = EM_Settings.hypothermiaPotionID + 1;
		}
		
		if(EM_Settings.frostBitePotionID >= maxID)
		{
			maxID = EM_Settings.frostBitePotionID + 1;
		}
		
		if(EM_Settings.dehydratePotionID >= maxID)
		{
			maxID = EM_Settings.dehydratePotionID + 1;
		}
		
		if(EM_Settings.insanityPotionID >= maxID)
		{
			maxID = EM_Settings.insanityPotionID + 1;
		}
		
		if(Potion.potionTypes.length >= maxID)
		{
			return;
		}
		
		
		Potion[] potionTypes = null;

		for (Field f : Potion.class.getDeclaredFields())
		{
			f.setAccessible(true);
			
			try
			{
				if (f.getName().equals("potionTypes") || f.getName().equals("field_76425_a"))
				{
					Field modfield = Field.class.getDeclaredField("modifiers");
					modfield.setAccessible(true);
					modfield.setInt(f, f.getModifiers() & ~Modifier.FINAL);

					potionTypes = (Potion[])f.get(null);
					final Potion[] newPotionTypes = new Potion[maxID];
					System.arraycopy(potionTypes, 0, newPotionTypes, 0, potionTypes.length);
					f.set(null, newPotionTypes);
				}
			}
			catch (Exception e)
			{
				System.err.println("[ERROR] Failed to extend potion list for EnviroMine");
				System.err.println(e);
			}
		}
	}
}
