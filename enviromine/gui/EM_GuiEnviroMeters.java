package enviromine.gui;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.nio.ByteOrder;
import org.lwjgl.opengl.GL11;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import enviromine.core.EM_Settings;
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
			tracker = EM_StatusManager.lookupPlayerTrackerFromName(this.mc.thePlayer.username);
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
			
			int Top_Center_X = ((width/2) - xPos) - (barWidth/2);
			int Top_Center_Y = yPos;
			
			int Bottom_Center_Left_X = ((width/2) - xPos) - (barWidth/2) - barWidth;
			int Bottom_Center_Left_Y = (height - yPos) - 50;
			
			int Bottom_Center_Right_X = ((width/2) - xPos) - (barWidth/2) + barWidth;
			int Bottom_Center_Right_Y = (height - yPos) - 50;
			
			int Bottom_Left_X = xPos;
			int Bottom_Left_Y = (height - yPos);
			
			int Bottom_Right_X = (width - xPos) - barWidth;
			int Bottom_Right_Y = (height - yPos);
			
			
			// Add Bars to String Array for looping
			String[] barPos = new String[4];
			barPos[0] = (String) EM_Settings.sanityBarPos_actual.toString();
			barPos[1] = (String) EM_Settings.oxygenBarPos_actual.toString();
			barPos[2] = (String) EM_Settings.waterBarPos_actual.toString();
			barPos[3] = (String) EM_Settings.heatBarPos_actual.toString();
			
			// Cnt for Each section of screen
			int BL = -1;	 int BR = -1;
			int BCR = -1; 	 int BCL = -1;
			int TL = -1;	 int TR = -1;
			int TC = -1; 
			int addTW = 0;
			int AQcurX = 0; int AQcurY = 0;
			int HTcurX = 0; int HTcurY = 0;
			int SAcurX = 0; int SAcurY = 0;
			int WAcurX = 0; int WAcurY = 0;
			int textPos = 0; int iconPos = 0;
			//Draw bars Pos Based on Settings
			for(int i = 0; i <= barPos.length - 1; i++ )
			{
				int curMeterHeight = 0;
				int curPosX = 0;
				int curPosY = 0;
				
				if(EM_Settings.ShowText_actual == true) addTW = textWidth;
					
				switch(barPos[i].toString().toLowerCase())
				{
					case "top_left":	TL += 2;
								curMeterHeight = meterHeight * TL;
								curPosX  = Top_Left_X;
								curPosY = Top_Left_Y + curMeterHeight;
								textPos = Top_Left_X + (textWidth*2);
								iconPos = textPos + addTW;
							break;
					
					case "top_right":	TR += 2;
								curMeterHeight = meterHeight * TR;
								curPosX = Top_Right_X;
								curPosY = Top_Right_Y + curMeterHeight;
								textPos = Top_Right_X - textWidth;
								iconPos = Top_Right_X - textWidth/2 - addTW;
							break;
					
					case "top_center": 	TC += 2;
								curMeterHeight = meterHeight * TC;
								curPosX = Top_Center_X;
								curPosY =Top_Center_Y + curMeterHeight;
								textPos = Top_Center_X + (textWidth*2);
								iconPos = textPos + addTW;
							break;
					
					case "bottom_left":  BL += 2;
								curMeterHeight = meterHeight * BL;
								curPosX = Bottom_Left_X;
								curPosY = Bottom_Left_Y - curMeterHeight;
								textPos = Bottom_Left_X + (textWidth*2);
								iconPos = textPos + addTW;
							break;
					
					case "bottom_right": BR += 2;
								curMeterHeight = meterHeight * BR;
								curPosX = Bottom_Right_X;
								curPosY = Bottom_Right_Y - curMeterHeight;
								textPos = Bottom_Right_X - textWidth;
								iconPos = Bottom_Right_X - textWidth/2 - addTW;
							break;
					
					case "bottom_center_right": 	BCR += 2;
								curMeterHeight = meterHeight * BCR;
								curPosX = Bottom_Center_Right_X;
								curPosY = Bottom_Center_Right_Y - curMeterHeight;
								textPos = Bottom_Center_Right_X + (textWidth*2);
								iconPos = textPos + addTW;
							
							break;
					
					case "bottom_center_left": 		BCL += 2;
								curMeterHeight = meterHeight * BCL;
								curPosX = Bottom_Center_Left_X;
								curPosY = Bottom_Center_Left_Y  - curMeterHeight;
								textPos = Bottom_Center_Left_X - textWidth + 4;
								iconPos = Bottom_Center_Left_X - textWidth/2 - addTW;
							break;
					

				}
				
				// 0 = Sanity Bar
				if(i == 0 && EM_Settings.enableSanity == true)
				{
					SAcurX = textPos; SAcurY = curPosY;
					
					this.drawTexturedModalRect(curPosX,curPosY, 0, 16, barWidth, meterHeight);
					this.drawTexturedModalRect(curPosX, curPosY, 64, 16, sanityBar, meterHeight);
					this.drawTexturedModalRect(curPosX + sanityBar - 2, curPosY + 2, 28, 64, 4, 4);
	
					// sanity frame
					this.drawTexturedModalRect(curPosX,curPosY, 0, meterHeight*4, meterWidth-32, meterHeight);

					if(EM_Settings.ShowGuiIcons_actual == true) this.drawTexturedModalRect(iconPos ,SAcurY - 4, 32, 80, 14,14);
				}
				
				// 1 = Air Quality Bar
				else if(i == 1 && EM_Settings.enableAirQ == true)
				{
					AQcurX = textPos; AQcurY = curPosY;
					
					this.drawTexturedModalRect(curPosX, curPosY, 0, 8, barWidth, meterHeight);
					this.drawTexturedModalRect(curPosX + airBar - 2, curPosY + 2, 8, 64, 4, 4);
		
					// oxygen frame
					this.drawTexturedModalRect(curPosX,curPosY, 0, meterHeight*4, meterWidth-32, meterHeight);
			
					if(EM_Settings.ShowGuiIcons_actual == true) this.drawTexturedModalRect(iconPos ,AQcurY - 4, 46, 80, 14,14);
					
				}
				// 2 = Water Bar
				else if(i == 2 && EM_Settings.enableHydrate == true)
				{
					WAcurX = textPos; WAcurY = curPosY;

					//water bar
					this.drawTexturedModalRect(curPosX, curPosY, 0, 0, barWidth, meterHeight);
					this.drawTexturedModalRect(curPosX, curPosY, 64, 0, waterBar, meterHeight);
					this.drawTexturedModalRect(curPosX + waterBar - 2, curPosY + 2, 16, 64, 4, 4);
					
				    // water frame
					this.drawTexturedModalRect(curPosX,curPosY, 0, meterHeight*4, meterWidth-32, meterHeight);
					if(EM_Settings.ShowGuiIcons_actual == true)  this.drawTexturedModalRect(iconPos ,WAcurY - 4, 18, 80, 14,14);

				}
				// 3 = Heat Bar
				else if(i == 3 && EM_Settings.enableBodyTemp == true)
				{
					HTcurX = textPos; HTcurY = curPosY;
					
					// heat Bar
					this.drawTexturedModalRect(curPosX, curPosY , 0, 24, barWidth, meterHeight);
					this.drawTexturedModalRect(curPosX + preheatBar - 4, curPosY , 32, 64, 8, 8);
					this.drawTexturedModalRect(curPosX + heatBar - 2, curPosY + 2, 20, 64, 4, 4);
					
					// heat frame
					this.drawTexturedModalRect(curPosX,curPosY, 0, meterHeight*4, meterWidth-32, meterHeight);

					if(EM_Settings.ShowGuiIcons_actual == true) this.drawTexturedModalRect(iconPos ,HTcurY - 4, 4, 80, 14,14);
				}
				
					
			
			}
		

			// Display Debugging Text
			if(EM_Settings.ShowText_actual == true)
			{
				this.drawTexturedModalRect(AQcurX,AQcurY, 63, meterHeight*4, 33, meterHeight);
				this.drawTexturedModalRect(HTcurX,HTcurY, 63, meterHeight*4, 33, meterHeight);
				this.drawTexturedModalRect(WAcurX,WAcurY, 63, meterHeight*4, 33, meterHeight);
				this.drawTexturedModalRect(SAcurX,SAcurY, 63, meterHeight*4, 33, meterHeight);
			
				Minecraft.getMinecraft().fontRenderer.drawString(tracker.airQuality + "%", AQcurX, AQcurY, 16777215);
				if(EM_Settings.useFarenheit)
				{
					Minecraft.getMinecraft().fontRenderer.drawString(((tracker.bodyTemp * (9/5))+32F) + "F", HTcurX, HTcurY, 16777215);
				} else
				{
					Minecraft.getMinecraft().fontRenderer.drawString(tracker.bodyTemp + "C", HTcurX, HTcurY, 16777215);
				}
				Minecraft.getMinecraft().fontRenderer.drawString(tracker.hydration + "%", WAcurX, WAcurY, 16777215);
				Minecraft.getMinecraft().fontRenderer.drawString(tracker.sanity + "%", SAcurX,SAcurY, 16777215);
			}
			

			this.mc.renderEngine.bindTexture(new ResourceLocation("enviromine", guiResource));
			
			// Screen Overlays FX Screen
			if(tracker.bodyTemp <= 0F)
			{
				int grad = (int)(Math.abs(tracker.bodyTemp)/10 * 64);
				if(tracker.bodyTemp <= -10F)
				{
					grad = 64;
				}
				this.drawGradientRect(0, 0, width, height, getColorFromRGBA(125, 255, 255, grad), getColorFromRGBA(125, 255, 255, grad));
			} else if(tracker.airQuality < 50F)
			{
				int grad = (int)((50 - tracker.airQuality)/50 * 64);
				this.drawGradientRect(0, 0, width, height, getColorFromRGBA(32, 96, 0, grad), getColorFromRGBA(32, 96, 0, grad));
			} else if(tracker.sanity < 50F)
			{
				int grad = (int)((50 - tracker.sanity)/50 * 64);
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
        if (R > 255)
        {
            R = 255;
        }

        if (G > 255)
        {
            G = 255;
        }

        if (B > 255)
        {
            B = 255;
        }

        if (A > 255)
        {
            A = 255;
        }

        if (R < 0)
        {
            R = 0;
        }

        if (G < 0)
        {
            G = 0;
        }

        if (B < 0)
        {
            B = 0;
        }

        if (A < 0)
        {
            A = 0;
        }

        if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN)
        {
            return A << 24 | R << 16 | G << 8 | B;
        }
        else
        {
            return B << 24 | G << 16 | R << 8 | A;
        }
    }
}
