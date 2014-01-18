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
		if(event.isCancelable() || event.type != ElementType.EXPERIENCE)
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
			
			this.mc.renderEngine.bindTexture(new ResourceLocation("enviromine", "textures/gui/new_ui.png"));
			
			
			
			
			// Static Vars for Bar Positions..
			
			int Top_Left_X = xPos;
			int Top_Left_Y = yPos;
			
			int Top_Right_X = (width - xPos) - barWidth;
			int Top_Right_Y = yPos;
			
			int Top_Center_X = ((width/2) - xPos) - (barWidth/2);
			int Top_Center_Y = yPos;
			
			int Bottom_Center_Left_X = ((width/2) - xPos) - (barWidth/2) - barWidth + 2;
			int Bottom_Center_Left_Y = (height - yPos) - 50;
			
			int Bottom_Center_Right_X = ((width/2) - xPos) - (barWidth/2) + barWidth - 2;
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

			
			//Draw bars Pos Based on Settings
			for(int i = 0; i <= barPos.length - 1; i++ )
			{
				int curMeterHeight = 0;
				int curPosX = 0;
				int curPosY = 0;
				
					
				switch(barPos[i].toString().toLowerCase())
				{
					case "top_left":	TL += 2;
								curMeterHeight = meterHeight * TL;
								curPosX  = Top_Left_X;
								curPosY = Top_Left_Y + curMeterHeight;
							break;
					
					case "top_right":	TR += 2;
								curMeterHeight = meterHeight * TR;
								curPosX = Top_Right_X;
								curPosY = Top_Right_Y + curMeterHeight;
							break;
					
					case "top_center": 	TC += 2;
								curMeterHeight = meterHeight * TC;
								curPosX = Top_Center_X;
								curPosY =Top_Center_Y + curMeterHeight;
							break;
					
					case "bottom_left":  BL += 2;
								curMeterHeight = meterHeight * BL;
								curPosX = Bottom_Left_X;
								curPosY = Bottom_Left_Y - curMeterHeight;
							break;
					
					case "bottom_right": BR += 2;
								curMeterHeight = meterHeight * BR;
								curPosX = Bottom_Right_X;
								curPosY = Bottom_Right_Y - curMeterHeight;
							break;
					
					case "bottom_center_right": 	BCR += 2;
								curMeterHeight = meterHeight * BCR;
								curPosX = Bottom_Center_Right_X;
								curPosY = Bottom_Center_Right_Y - curMeterHeight;
							
							break;
					
					case "bottom_center_left": 		BCL += 2;
								curMeterHeight = meterHeight * BCL;
								curPosX = Bottom_Center_Left_X;
								curPosY = Bottom_Center_Left_Y  - curMeterHeight;
							break;
					

				}
				
				// 0 = Sanity Bar
				if(i == 0)
				{
					this.drawTexturedModalRect(curPosX,curPosY, 0, 16, barWidth, meterHeight);
					this.drawTexturedModalRect(curPosX, curPosY, 64, 16, sanityBar, meterHeight);
					this.drawTexturedModalRect(curPosX + sanityBar - 2, curPosY + 2, 28, 64, 4, 4);
	
					// sanity frame
					//this.drawTexturedModalRect(curPosX,curPosY, 0, meterHeight*6, meterWidth - 10, meterHeight);

					this.drawTexturedModalRect(curPosX,curPosY, 0, meterHeight*4, meterWidth-31, meterHeight);
				}
				
				// 1 = Air Quality Bar
				else if(i == 1)
				{
					this.drawTexturedModalRect(curPosX, curPosY, 0, 8, barWidth, meterHeight);
					this.drawTexturedModalRect(curPosX + airBar - 2, curPosY + 2, 8, 64, 4, 4);
		
					// oyxygen fraom
					//this.drawTexturedModalRect(curPosX, curPosY, 0, meterHeight*6, meterWidth - 10, meterHeight);
					
					this.drawTexturedModalRect(curPosX,curPosY, 0, meterHeight*4, meterWidth-31, meterHeight);
			
				}
				// 2 = Water Bar
				else if(i == 2)
				{
					 //water bar
					this.drawTexturedModalRect(curPosX, curPosY, 0, 0, barWidth, meterHeight);
					this.drawTexturedModalRect(curPosX, curPosY, 64, 0, waterBar, meterHeight);
					this.drawTexturedModalRect(curPosX + waterBar - 2, curPosY + 2, 16, 64, 4, 4);
					
				    // water frame
					//this.drawTexturedModalRect(curPosX, curPosY, 0, meterHeight*4, meterWidth, meterHeight);

					this.drawTexturedModalRect(curPosX,curPosY, 0, meterHeight*4, meterWidth-31, meterHeight);					
				}
				// 0 = Heat Bar
				else
				{
					// heat Bar
					this.drawTexturedModalRect(curPosX, curPosY , 0, 24, barWidth, meterHeight);
					this.drawTexturedModalRect(curPosX + preheatBar - 4, curPosY , 32, 64, 8, 8);
					this.drawTexturedModalRect(curPosX + heatBar - 2, curPosY + 2, 20, 64, 4, 4);
					
					// heat frame
					this.drawTexturedModalRect(curPosX,curPosY, 0, meterHeight*4, meterWidth-31, meterHeight);

				}
			
			}
		
				
			
			
			
			


			
			//copy of old
			//this.drawTexturedModalRect(xPos, (height - yPos) - meterHeight*3, 0, 24, barWidth, meterHeight);
			//this.drawTexturedModalRect(xPos + preheatBar - 4, (height - yPos) - meterHeight*3, 32, 64, 8, 8);
			//this.drawTexturedModalRect(xPos + heatBar - 2, (height - yPos) - meterHeight*3 + 2, 20, 64, 4, 4);
			
			//this.drawTexturedModalRect(xPos, (height - yPos) - meterHeight*1, 0, 0, barWidth, meterHeight);
			//this.drawTexturedModalRect(xPos, (height - yPos) - meterHeight*1, 64, 0, waterBar, meterHeight);
			//this.drawTexturedModalRect(xPos + waterBar - 2, (height - yPos) - meterHeight*1 + 2, 16, 64, 4, 4);
			
			//this.drawTexturedModalRect((width - xPos) - barWidth, (height - yPos) - meterHeight*3, 0, 8, barWidth, meterHeight);
			//this.drawTexturedModalRect((width - xPos) - barWidth + airBar - 2, (height - yPos) - meterHeight*3 + 2, 8, 64, 4, 4);
			
			//this.drawTexturedModalRect((width - xPos) - barWidth, (height - yPos) - meterHeight*1, 0, 16, barWidth, meterHeight);
			//this.drawTexturedModalRect((width - xPos) - barWidth, (height - yPos) - meterHeight*1, 64, 16, sanityBar, meterHeight);
			//this.drawTexturedModalRect((width - xPos) - barWidth + sanityBar - 2, (height - yPos) - meterHeight*1 + 2, 28, 64, 4, 4);
	
			
			
			/*if(tracker.hydration < 25F && tracker.curAttackTime == 1)
			{
				this.drawTexturedModalRect(xPos, (height - yPos) - 8, meterWidth, 0, meterWidth, 8);									//Bottom-Left	Water-Empty
				this.drawTexturedModalRect(xPos, (height - yPos) - 8, meterWidth, 16, waterBar, 8);								//Bottom-Left	Water-Full
				this.drawTexturedModalRect(xPos + 96, (height - yPos) - 16, 80, 48, 16, 16);							//Bottom-Left	Water-Icon
			} else
			{
				this.drawTexturedModalRect(xPos, (height - yPos) - 8, 0, 0, meterWidth, 8);										//Bottom-Left	Water-Empty
				this.drawTexturedModalRect(xPos, (height - yPos) - 8, 0, 16, waterBar, 8);								//Bottom-Left	Water-Full
				this.drawTexturedModalRect(xPos + 96, (height - yPos) - 16, 0, 48, 16, 16);								//Bottom-Left	Water-Icon
			}
			
			if((tracker.bodyTemp < 0F || tracker.bodyTemp > 50F) && tracker.curAttackTime == 1)
			{
				this.drawTexturedModalRect(xPos, (height - yPos) - 24, meterWidth, 0, 96, 8);									//Top-Left		Heat-Empty
				this.drawTexturedModalRect(xPos, (height - yPos) - 24, meterWidth, 40, 96, 8);									//Top-Left		Heat-Full
				this.drawTexturedModalRect(xPos + 96, (height - yPos) - 32, 80, 48, 16, 16);							//Top-Left		Heat-Icon
				this.drawTexturedModalRect(xPos + heatBar - 8, (height - yPos + 4) - 32, meterWidth, 48, 16, 16);				//Top-Left		Heat-Indicator
				this.drawTexturedModalRect(xPos + preheatBar - 8, (height - yPos + 4) - 32, 112, 48, 16, 16);			//Top-Left		Heat-Predicted
			} else
			{
				this.drawTexturedModalRect(xPos, (height - yPos) - 24, 0, 0, meterWidth, 8);									//Top-Left		Heat-Empty
				this.drawTexturedModalRect(xPos, (height - yPos) - 24, 0, 40, meterWidth, 8);									//Top-Left		Heat-Full
				if(tracker.bodyTemp < 0F)
				{
					this.drawTexturedModalRect(xPos + meterWidth, (height - yPos) - 32, 0, 64, 16, 16);							//Top-Left		Heat-Icon
				} else if(tracker.bodyTemp > 50F)
				{
					this.drawTexturedModalRect(xPos + meterWidth, (height - yPos) - 32, 16, 64, 16, 16);						//Top-Left		Heat-Icon
				} else
				{
					this.drawTexturedModalRect(xPos + meterWidth, (height - yPos) - 32, 48, 48, 16, 16);						//Top-Left		Heat-Icon
				}
				this.drawTexturedModalRect(xPos + heatBar - 8, (height - yPos + 4) - 32, meterWidth, 48, 16, 16);				//Top-Left		Heat-Indicator
				this.drawTexturedModalRect(xPos + preheatBar - 8, (height - yPos + 4) - 32, 112, 48, 16, 16);			//Top-Left		Heat-Predicted
			}
			
			if(tracker.sanity < 25F && tracker.curAttackTime == 1)
			{
				this.drawTexturedModalRect((width - xPos) - 96, (height - yPos) - 8, 96, 8, 96, 8);						//Bottom-Right	Sanity-Empty
				this.drawTexturedModalRect((width - xPos) - 96, (height - yPos) - 8, 96, 32, sanityBar, 8);				//Bottom-Right	Sanity-Full
				this.drawTexturedModalRect((width - xPos) - 112, (height - yPos) - 16, 80, 48, 16, 16);					//Bottom-Right	Sanity-Icon
			} else
			{
				this.drawTexturedModalRect((width - xPos) - 96, (height - yPos) - 8, 0, 8, 96, 8);						//Bottom-Right	Sanity-Empty
				this.drawTexturedModalRect((width - xPos) - 96, (height - yPos) - 8, 0, 32, sanityBar, 8);				//Bottom-Right	Sanity-Full
				this.drawTexturedModalRect((width - xPos) - 112, (height - yPos) - 16, 32, 48, 16, 16);					//Bottom-Right	Sanity-Icon
			}
			
			if(tracker.airQuality < 25F && tracker.curAttackTime == 1)
			{
				this.drawTexturedModalRect((width - xPos) - 96, (height - yPos) - 24, 96, 0, 96, 8);					//Top-Right		Oxygen-Empty
				this.drawTexturedModalRect((width - xPos) - 96, (height - yPos) - 24, 96, 24, 96, 8);					//Top-Right		Oxygen-Full
				this.drawTexturedModalRect((width - xPos) - 112, (height - yPos) - 32, 80, 48, 16, 16);					//Top-Right		Oxygen-Icon
				this.drawTexturedModalRect((width - xPos) + airBar - 104, (height - yPos + 4) - 32, 96, 48, 16, 16);	//Top-Left		Oxygen-Indicator
			} else
			{
				this.drawTexturedModalRect((width - xPos) - 96, (height - yPos) - 24, 0, 0, 96, 8);						//Top-Right		Oxygen-Empty
				this.drawTexturedModalRect((width - xPos) - 96, (height - yPos) - 24, 0, 24, 96, 8);					//Top-Right		Oxygen-Full
				this.drawTexturedModalRect((width - xPos) - 112, (height - yPos) - 32, 16, 48, 16, 16);					//Top-Right		Oxygen-Icon
				this.drawTexturedModalRect((width - xPos) + airBar - 104, (height - yPos + 4) - 32, 96, 48, 16, 16);	//Top-Left		Oxygen-Indicator
			}*/
			
			// Draw Frames COPY
		
			//this.drawTexturedModalRect(xPos, (height - yPos) - meterHeight, 0, meterHeight*4, meterWidth, meterHeight);
			//this.drawTexturedModalRect(xPos, (height - yPos) - meterHeight*3, 0, meterHeight*4, meterWidth, meterHeight);
			
			//this.drawTexturedModalRect((width - xPos) - meterWidth, (height - yPos) - meterHeight, 0, meterHeight*6, meterWidth, meterHeight);
			//this.drawTexturedModalRect((width - xPos) - meterWidth, (height - yPos) - meterHeight*3, 0, meterHeight*6, meterWidth, meterHeight);
			
			// Draw Text
			
			//Minecraft.getMinecraft().fontRenderer.drawString(tracker.airQuality + "%", (width - xPos) - barWidth - textWidth, (height - yPos) - meterHeight*3, 16777215);
			if(EM_Settings.useFarenheit)
			{
				//Minecraft.getMinecraft().fontRenderer.drawString(((tracker.bodyTemp * (9/5))+32F) + "F", xPos + barWidth, (height - yPos) - meterHeight*3, 16777215);
			} else
			{
				//Minecraft.getMinecraft().fontRenderer.drawString(tracker.bodyTemp + "C", xPos + barWidth, (height - yPos) - meterHeight*3, 16777215);
			}
			//Minecraft.getMinecraft().fontRenderer.drawString(tracker.hydration + "%", xPos + barWidth, (height - yPos) - meterHeight, 16777215);
			//Minecraft.getMinecraft().fontRenderer.drawString(tracker.sanity + "%", (width - xPos) - barWidth - textWidth, (height - yPos) - meterHeight, 16777215);
			
			// Draw Screen FX
			
			this.mc.renderEngine.bindTexture(new ResourceLocation("enviromine", "textures/gui/new_ui.png"));
            
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
	
	private void Switch() {
		// TODO Auto-generated method stub
		
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
   /* 
    private static int getBarPos(String where, int xPos, int yPos, int width, int height, int BL, int BR, int TL, int TR, int TC, int BCR, int BCL )
    {
		// Static Vars for Bar Positions..
		
		int Top_Left_X = xPos;
		int Top_Left_Y = yPos + meterHeight*1;
		
		int Top_Right_X = (width - xPos) - barWidth;
		int Top_Right_Y = yPos + meterHeight*1;
		
		int Top_Center_X = ((width/2) - xPos) - (barWidth/2);
		int Top_Center_Y = yPos + meterHeight*1;
		
		int Bottom_Center_Left_X = ((width/2) - xPos) - (barWidth/2) - barWidth + 2;
		int Bottom_Center_Left_Y = (height - yPos) - meterHeight*1 - 50;
		
		int Bottom_Center_Right_X = ((width/2) - xPos) - (barWidth/2) + barWidth - 2;
		int Bottom_Center_Right_Y = (height - yPos) - meterHeight*1 - 50;
		
		int Bottom_Left_X = xPos;
		int Bottom_Left_Y = (height - yPos);
		
		int Bottom_Right_X = (width - xPos) - barWidth;
		int Bottom_Right_Y = (height - yPos);
    	
		int curMeterHeight = 0;
		int curPosX = 0;
		int curPosY = 0;
		
			
		switch(where.toString().toLowerCase())
		{
			case "top_left":	TL += 2;
						curMeterHeight = meterHeight * TL;

					break;
			
			case "top_right":	TR += 2;
						curMeterHeight = meterHeight * TR;
					break;
			
			case "top_center": 	TC += 2;
						curMeterHeight = meterHeight * TC;
					break;
			
			case "bottom_left":  BL += 2;
						curMeterHeight = meterHeight * BL;
						curPosX = Bottom_Left_X;
						curPosY = Bottom_Left_Y - curMeterHeight;
					break;
			
			case "bottom_right": BR += 2;
						curMeterHeight = meterHeight * BR;
						curPosX = Bottom_Right_X;
						curPosY = Bottom_Right_Y - curMeterHeight;
					break;
			
			case "bottom_center_right": 	BCR += 2;
						curMeterHeight = meterHeight * BCR;
					break;
			
			case "bottom_center_left": 		BCL += 2;
						curMeterHeight = meterHeight * BCL;
					break;
			

		}
		
	//	return curPosX, curPosY;
    	
    }*/
}
