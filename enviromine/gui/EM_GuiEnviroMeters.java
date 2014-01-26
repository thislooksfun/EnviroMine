package enviromine.gui;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.ByteOrder;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import enviromine.core.EM_Settings;
import enviromine.handlers.EM_StatusManager;
import enviromine.trackers.BlockProperties;
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
		
		if(event.type != ElementType.EXPERIENCE && event.type != ElementType.JUMPBAR || event.isCancelable())
		{
			return;
		}
		
		// count gui ticks
		if(ticktimer >= 60)
		{
			//System.out.println("Tick timer hit " +ticktimer);
			blink = !blink; // blink for warning system 
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
			barPos[0] = (String)EM_Settings.sanityBarPos_actual.toString();
			barPos[1] = (String)EM_Settings.oxygenBarPos_actual.toString();
			barPos[2] = (String)EM_Settings.waterBarPos_actual.toString();
			barPos[3] = (String)EM_Settings.heatBarPos_actual.toString();
			
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
				
				switch(barPos[i].toString().toLowerCase())
				{
					case "top_left":
						TL += 2;
						curMeterHeight = meterHeight * TL;
						curPosX = Top_Left_X;
						curPosY = Top_Left_Y + curMeterHeight;
						textPos = Top_Left_X + barWidth;
						iconPos = textPos + (textWidth * addTW);
						break;
					
					case "top_right":
						TR += 2;
						curMeterHeight = meterHeight * TR;
						curPosX = Top_Right_X;
						curPosY = Top_Right_Y + curMeterHeight;
						textPos = Top_Right_X - (textWidth * addTW);
						iconPos = textPos - iconWidth;
						break;
					
					case "top_center":
						TC += 2;
						curMeterHeight = meterHeight * TC;
						curPosX = Top_Center_X;
						curPosY = Top_Center_Y + curMeterHeight;
						textPos = Top_Center_X + barWidth;
						iconPos = textPos + (textWidth * addTW);
						break;
					
					case "bottom_left":
						BL += 2;
						curMeterHeight = meterHeight * BL;
						curPosX = Bottom_Left_X;
						curPosY = Bottom_Left_Y - curMeterHeight;
						textPos = Bottom_Left_X + barWidth;
						iconPos = textPos + (textWidth * addTW);
						break;
					
					case "bottom_right":
						BR += 2;
						curMeterHeight = meterHeight * BR;
						curPosX = Bottom_Right_X;
						curPosY = Bottom_Right_Y - curMeterHeight;
						textPos = Bottom_Right_X - (textWidth * addTW);
						iconPos = textPos - iconWidth;
						break;
					
					case "bottom_center_right":
						BCR += 2;
						curMeterHeight = meterHeight * BCR;
						curPosX = Bottom_Center_Right_X;
						curPosY = Bottom_Center_Right_Y - curMeterHeight;
						textPos = Bottom_Center_Right_X + barWidth;
						iconPos = textPos + (textWidth * addTW);
						
						break;
					
					case "bottom_center_left":
						BCL += 2;
						curMeterHeight = meterHeight * BCL;
						curPosX = Bottom_Center_Left_X;
						curPosY = Bottom_Center_Left_Y - curMeterHeight;
						textPos = Bottom_Center_Left_X - (textWidth * addTW);
						iconPos = textPos - iconWidth;
						break;
				
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
					Minecraft.getMinecraft().fontRenderer.drawString(((dispHeat * (9 / 5)) + 32F) + "F", HTcurX, HTcurY, 16777215);
				} else
				{
					Minecraft.getMinecraft().fontRenderer.drawString(dispHeat + "C", HTcurX, HTcurY, 16777215);
				}
				Minecraft.getMinecraft().fontRenderer.drawString(tracker.hydration + "%", WAcurX, WAcurY, 16777215);
				Minecraft.getMinecraft().fontRenderer.drawString(tracker.sanity + "%", SAcurX, SAcurY, 16777215);
			}
			
			this.mc.renderEngine.bindTexture(new ResourceLocation("enviromine", guiResource));
			
			// Screen Overlays FX Screen
			if(tracker.bodyTemp <= 0F)
				
				this.mc.renderEngine.bindTexture(new ResourceLocation("enviromine", "textures/gui/new_ui.png"));
			
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
				this.drawGradientRect(0, 0, width, height, getColorFromRGBA(255, 255, 255, grad), getColorFromRGBA(255, 255, 255, grad));
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
				this.drawGradientRect(0, 0, width, height, getColorFromRGBA(125, 255, 255, grad), getColorFromRGBA(125, 255, 255, grad));
			} else if(tracker.airQuality < 50F)
			{
				int grad = (int)((50 - tracker.airQuality) / 50 * 64);
				this.drawGradientRect(0, 0, width, height, getColorFromRGBA(32, 96, 0, grad), getColorFromRGBA(32, 96, 0, grad));
			} else if(tracker.sanity < 50F)
			{
				int grad = (int)((50 - tracker.sanity) / 50 * 64);
				this.drawGradientRect(0, 0, width, height, getColorFromRGBA(200, 0, 249, grad), getColorFromRGBA(200, 0, 249, grad));
			}
		}
	}
	
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
	
	public static boolean DB_nearLava = false;
	public static float DB_bodyTemp = 0;
	public static float DB_abientTemp = 0;
	public static float DB_sanityrate = 0;
	public static float DB_airquality = 0;
	
	public static float DB_dropspeed = 0.002F;
	public static float DB_raisespeed = 0.002F;
	
	public static float DB_cooling = 0;
	public static float DB_dehydrateBonus = 0;
	
	public static String DB_timer = "";
	
	public static String DB_biomeName = "";
	
	@ForgeSubscribe
	@SideOnly(Side.CLIENT)
	public void ShowDebugText(RenderGameOverlayEvent event)
	{
		boolean debuggingText = true;
		if(event.type != ElementType.EXPERIENCE && event.type != ElementType.JUMPBAR || event.isCancelable())
		{
			return;
		}
		
		if(debuggingText != true || this.mc.gameSettings.showDebugInfo)
			return;
		
		try
		{
			if(EM_Settings.useFarenheit == true)
			{
				Minecraft.getMinecraft().fontRenderer.drawString("Body Temp: " + ((tracker.bodyTemp * (9 / 5)) + 32F) + "%", 10, 10, 16777215);
				Minecraft.getMinecraft().fontRenderer.drawString("Ambient Temp: " + ((DB_abientTemp * (9 / 5)) + 32F) + "% | Cur Biome:" + DB_biomeName, 10, 10 * 2, 16777215);
				Minecraft.getMinecraft().fontRenderer.drawString("Ambient Cooling: " + ((DB_cooling * (9 / 5))) + "%", 10, 10 * 3, 16777215);
				Minecraft.getMinecraft().fontRenderer.drawString("Temp Drop Speed: " + ((DB_dropspeed * (9 / 5))) + "%", 10, 10 * 4, 16777215);
				Minecraft.getMinecraft().fontRenderer.drawString("Temp Raise Speed: " + ((DB_raisespeed * (9 / 5))) + "% | Near lava:" + DB_nearLava, 10, 10 * 5, 16777215);
				
			} else
			{
				Minecraft.getMinecraft().fontRenderer.drawString("Body Temp: " + tracker.bodyTemp + "%", 10, 10, 16777215);
				Minecraft.getMinecraft().fontRenderer.drawString("Ambient Temp: " + DB_abientTemp + "% | Cur Biome:" + DB_biomeName, 10, 10 * 2, 16777215);
				Minecraft.getMinecraft().fontRenderer.drawString("Ambient Cooling: " + DB_cooling + "%", 10, 10 * 3, 16777215);
				Minecraft.getMinecraft().fontRenderer.drawString("Temp Drop Speed: " + DB_dropspeed + "%", 10, 10 * 4, 16777215);
				Minecraft.getMinecraft().fontRenderer.drawString("Temp Raise Speed: " + DB_raisespeed + "% | Near lava:" + DB_nearLava, 10, 10 * 5, 16777215);
			}
			
			Minecraft.getMinecraft().fontRenderer.drawString("Sanity Rate: " + DB_sanityrate + "%", 10, 10 * 6, 16777215);
			Minecraft.getMinecraft().fontRenderer.drawString("Air Quality Rate: " + DB_airquality + "%", 10, 10 * 7, 16777215);
			Minecraft.getMinecraft().fontRenderer.drawString("Dehydrating Bonus: " + DB_dehydrateBonus + "%", 10, 10 * 8, 16777215);
			Minecraft.getMinecraft().fontRenderer.drawString("Status Update Execution: " + DB_timer, 10, 10 * 9, 16777215);
		} catch(NullPointerException e)
		{
			
		}
	}
}
