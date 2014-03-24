package enviromine.gui;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.lwjgl.opengl.GL11;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import enviromine.core.EM_Settings;
import enviromine.core.EnviroMine;
import enviromine.handlers.EM_StatusManager;
import enviromine.trackers.EnviroDataTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.event.ForgeSubscribe;

public class EM_GuiEnviroMeters extends Gui
{
	public Minecraft mc;
	
	public static final String guiResource = "textures/gui/status_Gui.png";
	
	public static final int meterWidth = 96;
	public static final int meterHeight = 8;
	public static final int barWidth = 64;
	public static final int textWidth = 32;
	public static final int iconWidth = 16;
	
	private static int ticktimer = 1;
	private static boolean blink = false;
	
	public static EnviroDataTracker tracker = null;
	
	public EM_GuiEnviroMeters(Minecraft mc)
	{
		this.mc = mc;
	}
	
	@ForgeSubscribe
	@SideOnly(Side.CLIENT)
	public void onGuiRender(RenderGameOverlayEvent event)
	{
		if((event.type != ElementType.EXPERIENCE && event.type != ElementType.JUMPBAR) || event.isCancelable())
		{
			return;
		}
		
		// count gui ticks
		if(ticktimer >= 60)
		{
			blink = !blink;
			ticktimer = 1;
		} else
			ticktimer++;
		
		int xPos = 4;
		int yPos = 4;
		ScaledResolution scaleRes = new ScaledResolution(Minecraft.getMinecraft().gameSettings, Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight);
		int width = scaleRes.getScaledWidth();
		int height = scaleRes.getScaledHeight();
		
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glDisable(GL11.GL_LIGHTING);
		
		if(tracker == null)
		{
			Minecraft.getMinecraft().fontRenderer.drawStringWithShadow("NO ENVIRONMENT DATA", xPos, (height - yPos) - 8, 16777215);
			tracker = EM_StatusManager.lookupTrackerFromUsername(this.mc.thePlayer.username);
		} else if(tracker.isDisabled)
		{
			tracker = null;
		} else
		{
			int waterBar = MathHelper.ceiling_float_int((tracker.hydration / 100) * barWidth);
			int heatBar = MathHelper.ceiling_float_int(((tracker.bodyTemp + 50) / 150) * barWidth);
			int preheatBar = MathHelper.ceiling_float_int(((tracker.airTemp + 50) / 150) * barWidth);
			int sanityBar = MathHelper.ceiling_float_int((tracker.sanity / 100) * barWidth);
			int airBar = MathHelper.ceiling_float_int((tracker.airQuality / 100) * barWidth);
			
			float dispHeat = new BigDecimal(String.valueOf(tracker.bodyTemp)).setScale(2, RoundingMode.DOWN).floatValue();
			float FdispHeat = new BigDecimal(String.valueOf((tracker.bodyTemp * 1.8)+32)).setScale(2, RoundingMode.DOWN).floatValue();
			float dispSanity = new BigDecimal(String.valueOf(tracker.sanity)).setScale(2, RoundingMode.DOWN).floatValue();
			
			if(waterBar > barWidth)
			{
				waterBar = barWidth;
			} else if(waterBar < 0)
			{
				waterBar = 0;
			}
			
			if(heatBar > barWidth)
			{
				heatBar = barWidth;
			} else if(heatBar < 0)
			{
				heatBar = 0;
			}
			
			if(preheatBar > barWidth)
			{
				preheatBar = barWidth;
			} else if(preheatBar < 0)
			{
				preheatBar = 0;
			}
			
			if(sanityBar > barWidth)
			{
				sanityBar = barWidth;
			} else if(sanityBar < 0)
			{
				sanityBar = 0;
			}
			
			if(airBar > barWidth)
			{
				airBar = barWidth;
			} else if(airBar < 0)
			{
				airBar = 0;
			}
			
			this.mc.renderEngine.bindTexture(new ResourceLocation("enviromine", guiResource));
			
			// Static Vars for Bar Positions..
			
			int Top_Left_X = xPos;
			int Top_Left_Y = yPos;
			
			int Top_Right_X = (width - xPos) - barWidth;
			int Top_Right_Y = yPos;
			
			int Top_Center_X = (width / 2) - (barWidth / 2);
			int Top_Center_Y = yPos;
			
			int Bottom_Center_Left_X = (width / 2) - (barWidth / 2) - barWidth;
			int Bottom_Center_Left_Y = (height - yPos) - 50;
			
			int Bottom_Center_Right_X = (width / 2) - (barWidth / 2) + barWidth;
			int Bottom_Center_Right_Y = (height - yPos) - 50;
			
			int Bottom_Left_X = xPos;
			int Bottom_Left_Y = (height - yPos);
			
			int Bottom_Right_X = (width - xPos) - barWidth;
			int Bottom_Right_Y = (height - yPos);
			
			// Add Bars to String Array for looping
			String[] barPos = new String[4];
			barPos[0] = EM_Settings.sanityBarPos_actual;
			barPos[1] = EM_Settings.oxygenBarPos_actual;
			barPos[2] = EM_Settings.waterBarPos_actual;
			barPos[3] = EM_Settings.heatBarPos_actual;
			
			boolean[] barTrue = new boolean[4];
			barTrue[0] = EM_Settings.enableSanity;
			barTrue[1] = EM_Settings.enableAirQ;
			barTrue[2] = EM_Settings.enableHydrate;
			barTrue[3] = EM_Settings.enableBodyTemp;
			
			// Cnt for Each section of screen
			int BL = -1;
			int BR = -1;
			int BCR = -1;
			int BCL = -1;
			int TL = -1;
			int TR = -1;
			int TC = -1;
			int addTW = 0;
			int AQcurX = 0;
			int AQcurY = 0;
			int HTcurX = 0;
			int HTcurY = 0;
			int SAcurX = 0;
			int SAcurY = 0;
			int WAcurX = 0;
			int WAcurY = 0;
			int textPos = 0;
			int iconPos = 0;
			//Draw bars Pos Based on Settings
			for(int i = 0; i <= barPos.length - 1; i++)
			{
				int curMeterHeight = 0;
				int curPosX = 0;
				int curPosY = 0;
				int frameborder = 4;
				
				if(EM_Settings.ShowText_actual == true)
				{
					addTW = 1;
				}
				if(!(barTrue[i]))
				{
					if(i <= 2)
						i += 1;
					else
						break;
				}
				
				String barPosName = barPos[i].toLowerCase().trim();
				
				if(barPosName.equalsIgnoreCase("top_left"))
				{
					TL += 2;
					curMeterHeight = meterHeight * TL;
					curPosX = Top_Left_X;
					curPosY = Top_Left_Y + curMeterHeight;
					textPos = Top_Left_X + barWidth;
					iconPos = textPos + (textWidth * addTW);
				} else if(barPosName.equalsIgnoreCase("top_right"))
				{
					TR += 2;
					curMeterHeight = meterHeight * TR;
					curPosX = Top_Right_X;
					curPosY = Top_Right_Y + curMeterHeight;
					textPos = Top_Right_X - (textWidth * addTW);
					iconPos = textPos - iconWidth;
				} else if(barPosName.equalsIgnoreCase("top_center"))
				{
					TC += 2;
					curMeterHeight = meterHeight * TC;
					curPosX = Top_Center_X;
					curPosY = Top_Center_Y + curMeterHeight;
					textPos = Top_Center_X + barWidth;
					iconPos = textPos + (textWidth * addTW);
				} else if(barPosName.equalsIgnoreCase("bottom_left"))
				{
					BL += 2;
					curMeterHeight = meterHeight * BL;
					curPosX = Bottom_Left_X;
					curPosY = Bottom_Left_Y - curMeterHeight;
					textPos = Bottom_Left_X + barWidth;
					iconPos = textPos + (textWidth * addTW);
				} else if(barPosName.equalsIgnoreCase("bottom_right"))
				{
					BR += 2;
					curMeterHeight = meterHeight * BR;
					curPosX = Bottom_Right_X;
					curPosY = Bottom_Right_Y - curMeterHeight;
					textPos = Bottom_Right_X - (textWidth * addTW);
					iconPos = textPos - iconWidth;
				} else if(barPosName.equalsIgnoreCase("bottom_center_right"))
				{
					BCR += 2;
					curMeterHeight = meterHeight * BCR;
					curPosX = Bottom_Center_Right_X;
					curPosY = Bottom_Center_Right_Y - curMeterHeight;
					textPos = Bottom_Center_Right_X + barWidth;
					iconPos = textPos + (textWidth * addTW);
				} else if(barPosName.equalsIgnoreCase("bottom_center_left"))
				{
					BCL += 2;
					curMeterHeight = meterHeight * BCL;
					curPosX = Bottom_Center_Left_X;
					curPosY = Bottom_Center_Left_Y - curMeterHeight;
					textPos = Bottom_Center_Left_X - (textWidth * addTW);
					iconPos = textPos - iconWidth;
				}
				
				// 0 = Sanity Bar
				if(i == 0 && EM_Settings.enableSanity == true)
				{
					SAcurX = textPos;
					SAcurY = curPosY;
					
					this.drawTexturedModalRect(curPosX, curPosY, 0, 16, barWidth, meterHeight);
					this.drawTexturedModalRect(curPosX, curPosY, 64, 16, sanityBar, meterHeight);
					this.drawTexturedModalRect(curPosX + sanityBar - 2, curPosY + 2, 28, 64, 4, 4);
					
					// sanity frame
					if(blink && tracker.sanity < 25)
						frameborder = 5;
					this.drawTexturedModalRect(curPosX, curPosY, 0, meterHeight * frameborder, meterWidth - 32, meterHeight);
					
					if(EM_Settings.ShowGuiIcons_actual == true)
						this.drawTexturedModalRect(iconPos, SAcurY - 4, 32, 80, 16, 16);
				}
				
				// 1 = Air Quality Bar
				else if(i == 1 && EM_Settings.enableAirQ == true)
				{
					AQcurX = textPos;
					AQcurY = curPosY;
					
					this.drawTexturedModalRect(curPosX, curPosY, 0, 8, barWidth, meterHeight);
					this.drawTexturedModalRect(curPosX + airBar - 2, curPosY + 2, 8, 64, 4, 4);
					
					// oxygen frame
					if(blink && tracker.airQuality < 25)
						frameborder = 5;
					this.drawTexturedModalRect(curPosX, curPosY, 0, meterHeight * frameborder, meterWidth - 32, meterHeight);
					
					if(EM_Settings.ShowGuiIcons_actual == true)
						this.drawTexturedModalRect(iconPos, AQcurY - 4, 48, 80, 16, 16);
					
				}
				// 2 = Water Bar
				else if(i == 2 && EM_Settings.enableHydrate == true)
				{
					WAcurX = textPos;
					WAcurY = curPosY;
					
					//water bar
					this.drawTexturedModalRect(curPosX, curPosY, 0, 0, barWidth, meterHeight);
					this.drawTexturedModalRect(curPosX, curPosY, 64, 0, waterBar, meterHeight);
					this.drawTexturedModalRect(curPosX + waterBar - 2, curPosY + 2, 16, 64, 4, 4);
					
					// water frame
					
					if(blink && tracker.hydration < 25)
						frameborder = 5;
					
					this.drawTexturedModalRect(curPosX, curPosY, 0, meterHeight * frameborder, meterWidth - 32, meterHeight);
					if(EM_Settings.ShowGuiIcons_actual == true)
					{
						this.drawTexturedModalRect(iconPos, WAcurY - 4, 16, 80, 16, 16);
					}
					
				}
				// 3 = Heat Bar
				else if(i == 3 && EM_Settings.enableBodyTemp == true)
				{
					HTcurX = textPos;
					HTcurY = curPosY;
					
					// heat Bar
					this.drawTexturedModalRect(curPosX, curPosY, 0, 24, barWidth, meterHeight);
					this.drawTexturedModalRect(curPosX + preheatBar - 4, curPosY, 32, 64, 8, 8);
					this.drawTexturedModalRect(curPosX + heatBar - 2, curPosY + 2, 20, 64, 4, 4);
					
					// heat frame
					if(blink && tracker.bodyTemp < 35 || blink && tracker.bodyTemp > 39)
						frameborder = 5;
					this.drawTexturedModalRect(curPosX, curPosY, 0, meterHeight * frameborder, meterWidth - 32, meterHeight);
					
					if(EM_Settings.ShowGuiIcons_actual == true)
						this.drawTexturedModalRect(iconPos, HTcurY - 4, 0, 80, 16, 16);
				}
				
			}
			
			// Display Debugging Text
			if(EM_Settings.ShowText_actual == true)
			{
				this.drawTexturedModalRect(AQcurX, AQcurY, 64, meterHeight * 4, 32, meterHeight);
				this.drawTexturedModalRect(HTcurX, HTcurY, 64, meterHeight * 4, 32, meterHeight);
				this.drawTexturedModalRect(WAcurX, WAcurY, 64, meterHeight * 4, 32, meterHeight);
				this.drawTexturedModalRect(SAcurX, SAcurY, 64, meterHeight * 4, 32, meterHeight);
				
				Minecraft.getMinecraft().fontRenderer.drawString(tracker.airQuality + "%", AQcurX, AQcurY, 16777215);
				if(EM_Settings.useFarenheit == true)
				{
					Minecraft.getMinecraft().fontRenderer.drawString( FdispHeat + "F", HTcurX, HTcurY, 16777215);
				} else
				{
					Minecraft.getMinecraft().fontRenderer.drawString(dispHeat + "C", HTcurX, HTcurY, 16777215);
				}
				Minecraft.getMinecraft().fontRenderer.drawString(tracker.hydration + "%", WAcurX, WAcurY, 16777215);
				Minecraft.getMinecraft().fontRenderer.drawString(dispSanity + "%", SAcurX, SAcurY, 16777215);
			}
			
			this.mc.renderEngine.bindTexture(new ResourceLocation("enviromine", guiResource));
			
			if(tracker.bodyTemp >= 39)
			{
				int grad = 0;
				if(tracker.bodyTemp >= 41F)
				{
					grad = 64;
				} else
				{
					grad = (int)((1F - (Math.abs(3 - (tracker.bodyTemp - 39)) / 3)) * 96);
				}
				this.drawGradientRect(0, 0, width, height, EnviroMine.getColorFromRGBA(255, 255, 255, grad), EnviroMine.getColorFromRGBA(255, 255, 255, grad));
			} else if(tracker.bodyTemp <= 35F)
			{
				int grad = 0;
				if(tracker.bodyTemp <= 32F)
				{
					grad = 64;
				} else
				{
					grad = (int)(((Math.abs(3 - (tracker.bodyTemp - 32)) / 3)) * 64);
				}
				this.drawGradientRect(0, 0, width, height, EnviroMine.getColorFromRGBA(125, 255, 255, grad), EnviroMine.getColorFromRGBA(125, 255, 255, grad));
			} else if(tracker.airQuality < 50F)
			{
				int grad = (int)((50 - tracker.airQuality) / 50 * 64);
				this.drawGradientRect(0, 0, width, height, EnviroMine.getColorFromRGBA(32, 96, 0, grad), EnviroMine.getColorFromRGBA(32, 96, 0, grad));
			} else if(tracker.sanity < 50F)
			{
				int grad = (int)((50 - tracker.sanity) / 50 * 64);
				this.drawGradientRect(0, 0, width, height, EnviroMine.getColorFromRGBA(200, 0, 249, grad), EnviroMine.getColorFromRGBA(200, 0, 249, grad));
			}
		}
		
		ShowDebugText(event);
	}
	
	public static float DB_bodyTemp = 0;
	public static float DB_abientTemp = 0;
	public static float DB_sanityrate = 0;
	public static float DB_airquality = 0;
	
	public static float DB_tempchange = 0;
	
	public static float DB_cooling = 0;
	public static float DB_dehydrateRate = 0;
	
	public static String DB_timer = "";
	public static String DB_physTimer = "";
	public static int DB_physUpdates = 0;
	
	public static String DB_biomeName = "";
	
	@SideOnly(Side.CLIENT)
	private void ShowDebugText(RenderGameOverlayEvent event)
	{
		if((event.type != ElementType.EXPERIENCE && event.type != ElementType.JUMPBAR) || event.isCancelable())
		{
			return;
		}
		
		if(!EM_Settings.ShowDebug_actual || this.mc.gameSettings.showDebugInfo || tracker == null)
		{
			return;
		}
		
		DB_abientTemp = tracker.airTemp;
		DB_biomeName = tracker.trackedEntity.worldObj.getBiomeGenForCoords(MathHelper.floor_double(tracker.trackedEntity.posX), MathHelper.floor_double(tracker.trackedEntity.posZ)).biomeName;
		DB_tempchange = new BigDecimal(String.valueOf(tracker.bodyTemp - tracker.prevBodyTemp)).setScale(3, RoundingMode.HALF_UP).floatValue();
		DB_sanityrate = new BigDecimal(String.valueOf(tracker.sanity - tracker.prevSanity)).setScale(3, RoundingMode.HALF_UP).floatValue();
		DB_airquality = new BigDecimal(String.valueOf(tracker.airQuality - tracker.prevAirQuality)).setScale(3, RoundingMode.HALF_UP).floatValue();
		DB_dehydrateRate = new BigDecimal(String.valueOf(tracker.hydration - tracker.prevHydration)).setScale(3, RoundingMode.HALF_UP).floatValue();
		
		try
		{
			if(EM_Settings.useFarenheit == true)
			{
				Minecraft.getMinecraft().fontRenderer.drawString("Body Temp: " + ((tracker.bodyTemp * 1.8) + 32F) + "%", 10, 10, 16777215);
				Minecraft.getMinecraft().fontRenderer.drawString("Ambient Temp: " + ((DB_abientTemp * 1.8) + 32F) + "% | Cur Biome: " + DB_biomeName, 10, 10 * 2, 16777215);
				Minecraft.getMinecraft().fontRenderer.drawString("Temp Rate: " + ((DB_tempchange * 1.8) + 32F) + "%", 10, 10 * 3, 16777215);
				
			} else
			{
				Minecraft.getMinecraft().fontRenderer.drawString("Body Temp: " + tracker.bodyTemp + "%", 10, 10, 16777215);
				Minecraft.getMinecraft().fontRenderer.drawString("Ambient Temp: " + DB_abientTemp + "% | Cur Biome: " + DB_biomeName, 10, 10 * 2, 16777215);
				Minecraft.getMinecraft().fontRenderer.drawString("Temp Rate: " + DB_tempchange + "%", 10, 10 * 3, 16777215);
			}
			
			Minecraft.getMinecraft().fontRenderer.drawString("Sanity Rate: " + DB_sanityrate + "%", 10, 10 * 4, 16777215);
			Minecraft.getMinecraft().fontRenderer.drawString("Air Quality Rate: " + DB_airquality + "%", 10, 10 * 5, 16777215);
			Minecraft.getMinecraft().fontRenderer.drawString("Dehydration Rate: " + DB_dehydrateRate + "%", 10, 10 * 6, 16777215);
			Minecraft.getMinecraft().fontRenderer.drawString("Status Update Speed: " + DB_timer, 10, 10 * 8, 16777215);
			Minecraft.getMinecraft().fontRenderer.drawString("Physics Update Speed: " + DB_physTimer, 10, 10 * 9, 16777215);
			Minecraft.getMinecraft().fontRenderer.drawString("No. Physics Updates: " + DB_physUpdates, 10, 10 * 10, 16777215);
		} catch(NullPointerException e)
		{
			
		}
	}
}
