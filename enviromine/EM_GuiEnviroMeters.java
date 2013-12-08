package enviromine;

import java.util.Collection;
import java.util.Iterator;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.resources.I18n;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
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
	public static final int meterHSpace = meterWidth + 4;
	public static final int meterVSpace = meterHeight + 4;
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
			Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(tracker.airQuality + "%", (width - xPos) - 144, (height - yPos) - 24, 16777215);
			if(EM_Settings.useFarenheit)
			{
				Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(((tracker.bodyTemp * (9/5))+32F) + "F", xPos + 112, (height - yPos) - 24, 16777215);
			} else
			{
				Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(tracker.bodyTemp + "C", xPos + 112, (height - yPos) - 24, 16777215);
			}
			Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(tracker.hydration + "%", xPos + 112, (height - yPos) - 8, 16777215);
			Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(tracker.sanity + "%", (width - xPos) - 144, (height - yPos) - 8, 16777215);
			
			int waterBar = MathHelper.ceiling_float_int((tracker.hydration / 100) * 96);
			int heatBar = MathHelper.ceiling_float_int(((tracker.bodyTemp + 50) / 150) * 96);
			int preheatBar = MathHelper.ceiling_float_int(((tracker.airTemp + 50) / 150) * 96);
			int sanityBar = MathHelper.ceiling_float_int((tracker.sanity / 100) * 96);
			int airBar = MathHelper.ceiling_float_int((tracker.airQuality / 100) * 96);
			
			if(waterBar > 96)
			{
				waterBar = 96;
			} else if(waterBar < 0)
			{
				waterBar = 0;
			}
			
			if(heatBar > 96)
			{
				heatBar = 96;
			} else if(heatBar < 0)
			{
				heatBar = 0;
			}
			
			if(preheatBar > 96)
			{
				preheatBar = 96;
			} else if(preheatBar < 0)
			{
				preheatBar = 0;
			}
			
			if(sanityBar > 96)
			{
				sanityBar = 96;
			} else if(sanityBar < 0)
			{
				sanityBar = 0;
			}
			
			if(airBar > 96)
			{
				airBar = 96;
			} else if(airBar < 0)
			{
				airBar = 0;
			}
			
			this.mc.renderEngine.bindTexture(new ResourceLocation("enviromine", "textures/gui/bars.png"));
			
			if(tracker.hydration < 25F && tracker.curAttackTime == 1)
			{
				this.drawTexturedModalRect(xPos, (height - yPos) - 8, 96, 0, 96, 8);									//Bottom-Left	Water-Empty
				this.drawTexturedModalRect(xPos, (height - yPos) - 8, 96, 16, waterBar, 8);								//Bottom-Left	Water-Full
				this.drawTexturedModalRect(xPos + 96, (height - yPos) - 16, 80, 48, 16, 16);							//Bottom-Left	Water-Icon
			} else
			{
				this.drawTexturedModalRect(xPos, (height - yPos) - 8, 0, 0, 96, 8);										//Bottom-Left	Water-Empty
				this.drawTexturedModalRect(xPos, (height - yPos) - 8, 0, 16, waterBar, 8);								//Bottom-Left	Water-Full
				this.drawTexturedModalRect(xPos + 96, (height - yPos) - 16, 0, 48, 16, 16);								//Bottom-Left	Water-Icon
			}
			
			if((tracker.bodyTemp < 0F || tracker.bodyTemp > 50F) && tracker.curAttackTime == 1)
			{
				this.drawTexturedModalRect(xPos, (height - yPos) - 24, 96, 0, 96, 8);									//Top-Left		Heat-Empty
				this.drawTexturedModalRect(xPos, (height - yPos) - 24, 96, 40, 96, 8);									//Top-Left		Heat-Full
				this.drawTexturedModalRect(xPos + 96, (height - yPos) - 32, 80, 48, 16, 16);							//Top-Left		Heat-Icon
				this.drawTexturedModalRect(xPos + heatBar - 8, (height - yPos + 4) - 32, 96, 48, 16, 16);				//Top-Left		Heat-Indicator
				this.drawTexturedModalRect(xPos + preheatBar - 8, (height - yPos + 4) - 32, 112, 48, 16, 16);			//Top-Left		Heat-Predicted
			} else
			{
				this.drawTexturedModalRect(xPos, (height - yPos) - 24, 0, 0, 96, 8);									//Top-Left		Heat-Empty
				this.drawTexturedModalRect(xPos, (height - yPos) - 24, 0, 40, 96, 8);									//Top-Left		Heat-Full
				if(tracker.bodyTemp < 0F)
				{
					this.drawTexturedModalRect(xPos + 96, (height - yPos) - 32, 0, 64, 16, 16);							//Top-Left		Heat-Icon
				} else if(tracker.bodyTemp > 50F)
				{
					this.drawTexturedModalRect(xPos + 96, (height - yPos) - 32, 16, 64, 16, 16);						//Top-Left		Heat-Icon
				} else
				{
					this.drawTexturedModalRect(xPos + 96, (height - yPos) - 32, 48, 48, 16, 16);						//Top-Left		Heat-Icon
				}
				this.drawTexturedModalRect(xPos + heatBar - 8, (height - yPos + 4) - 32, 96, 48, 16, 16);				//Top-Left		Heat-Indicator
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
			}
		}
	}
}
