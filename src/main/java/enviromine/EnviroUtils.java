package enviromine;

import java.awt.Color;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.ByteOrder;
import net.minecraft.potion.Potion;
import net.minecraftforge.common.ForgeDirection;
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
	
	public static Color blendColors(int a, int b, float ratio)
	{
		if(ratio > 1f)
		{
			ratio = 1f;
		} else if(ratio < 0f)
		{
			ratio = 0f;
		}
		float iRatio = 1.0f - ratio;
		
		int aA = (a >> 24 & 0xff);
		int aR = ((a & 0xff0000) >> 16);
		int aG = ((a & 0xff00) >> 8);
		int aB = (a & 0xff);
		
		int bA = (b >> 24 & 0xff);
		int bR = ((b & 0xff0000) >> 16);
		int bG = ((b & 0xff00) >> 8);
		int bB = (b & 0xff);
		
		int A = (int)((aA * iRatio) + (bA * ratio));
		int R = (int)((aR * iRatio) + (bR * ratio));
		int G = (int)((aG * iRatio) + (bG * ratio));
		int B = (int)((aB * iRatio) + (bB * ratio));
		
		return new Color(R, G, B);
		//return A << 24 | R << 16 | G << 8 | B;
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
		
		for(Field f : Potion.class.getDeclaredFields())
		{
			f.setAccessible(true);
			
			try
			{
				if(f.getName().equals("potionTypes") || f.getName().equals("field_76425_a"))
				{
					Field modfield = Field.class.getDeclaredField("modifiers");
					modfield.setAccessible(true);
					modfield.setInt(f, f.getModifiers() & ~Modifier.FINAL);
					
					potionTypes = (Potion[])f.get(null);
					final Potion[] newPotionTypes = new Potion[maxID];
					System.arraycopy(potionTypes, 0, newPotionTypes, 0, potionTypes.length);
					f.set(null, newPotionTypes);
				}
			} catch(Exception e)
			{
				System.err.println("[ERROR] Failed to extend potion list for EnviroMine");
				System.err.println(e);
			}
		}
	}
	
	public static int[] getAdjacentBlockCoordsFromSide(int x, int y, int z, int side)
	{
		int[] coords = new int[3];
		coords[0] = x;
		coords[1] = y;
		coords[2] = z;
		
		ForgeDirection dir = ForgeDirection.getOrientation(side);
		switch(dir)
		{
			case NORTH:
			{
				coords[2] -= 1;
				break;
			}
			case SOUTH:
			{
				coords[2] += 1;
				break;
			}
			case WEST:
			{
				coords[0] -= 1;
				break;
			}
			case EAST:
			{
				coords[0] += 1;
				break;
			}
			case UP:
			{
				coords[1] += 1;
				break;
			}
			case DOWN:
			{
				coords[1] -= 1;
				break;
			}
			case UNKNOWN:
			{
				break;
			}
		}
		
		return coords;
	}
}
