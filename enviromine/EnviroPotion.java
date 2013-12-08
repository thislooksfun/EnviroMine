package enviromine;

import java.util.Collection;

import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.packet.Packet62LevelSound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;

public class EnviroPotion extends Potion
{
	public static EnviroPotion frostbite;
	public static EnviroPotion dehydration;
	public static EnviroPotion insanity;
	
	public static ResourceLocation textureResource = new ResourceLocation("enviromine", "textures/gui/bars.png");
	
	protected EnviroPotion(int par1, boolean par2, int par3)
	{
		super(par1, par2, par3);
	}
	
	public static void checkAndApplyEffects(EntityLivingBase entityLiving)
	{
		if(entityLiving.worldObj.isRemote)
		{
			return;
		}
		
		Collection effects = entityLiving.getActivePotionEffects();
		
		if(entityLiving.isPotionActive(frostbite))
		{
			if(entityLiving.getActivePotionEffect(frostbite).duration == 0)
			{
				entityLiving.removePotionEffect(frostbite.id);
			}
			entityLiving.attackEntityFrom(EnviroDamageSource.frostbite, 2.0F + (entityLiving.getActivePotionEffect(frostbite).getAmplifier() * 1.0F));
		}
		if(entityLiving.isPotionActive(dehydration.id))
		{
			if(entityLiving.getActivePotionEffect(dehydration).duration == 0)
			{
				entityLiving.removePotionEffect(dehydration.id);
			}
			
			EnviroDataTracker tracker = EM_StatusManager.lookupTracker(entityLiving);
			
			if(tracker != null)
			{
				tracker.dehydrate(1F + (entityLiving.getActivePotionEffect(dehydration).getAmplifier() * 1F));
			}
		}
		if(entityLiving.isPotionActive(insanity.id))
		{
			PotionEffect effect = entityLiving.getActivePotionEffect(insanity);
			if(effect.duration == 0)
			{
				entityLiving.removePotionEffect(insanity.id);
			}
			
			if(entityLiving.getRNG().nextInt(10) == 0)
			{
				if(effect.getAmplifier() >= 2)
				{
					entityLiving.addPotionEffect(new PotionEffect(Potion.blindness.id, 100));
				}
			}
			
			if(entityLiving.getRNG().nextInt(10) == 0)
			{
				if(effect.getAmplifier() >= 1)
				{
					entityLiving.addPotionEffect(new PotionEffect(Potion.confusion.id, 200));
					entityLiving.addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, 200));
				}
			}
			
			String sound = "";
			if(entityLiving.getRNG().nextInt(20) == 0 && entityLiving instanceof EntityPlayer)
			{
				switch(entityLiving.getRNG().nextInt(10))
				{
					case 0:
					{
						sound = "ambient.cave.cave";
						break;
					}
					case 1:
					{
						sound = "random.explode";
						break;
					}
					case 2:
					{
						sound = "random.fuse";
						break;
					}
					case 3:
					{
						sound = "mob.zombie.say";
						break;
					}
					case 4:
					{
						sound = "mob.endermen.idle";
						break;
					}
					case 5:
					{
						sound = "mob.skeleton.say";
						break;
					}
					case 6:
					{
						sound = "mob.wither.idle";
						break;
					}
					case 7:
					{
						sound = "mob.spider.say";
						break;
					}
					case 8:
					{
						sound = "ambient.weather.thunder";
						break;
					}
					case 9:
					{
						sound = "liquid.lava";
						break;
					}
					case 10:
					{
						sound = "liquid.water";
						break;
					}
					case 11:
					{
						sound = "mob.ghast.idle";
						break;
					}
				}
				
				EntityPlayer player = ((EntityPlayer)entityLiving);

				float rndX = (player.getRNG().nextInt(6)-3) * player.getRNG().nextFloat();
				float rndY = (player.getRNG().nextInt(6)-3) * player.getRNG().nextFloat();
				float rndZ = (player.getRNG().nextInt(6)-3) * player.getRNG().nextFloat();
				
				Packet62LevelSound packet = new Packet62LevelSound(sound, entityLiving.posX + rndX, entityLiving.posY + rndY, entityLiving.posZ + rndZ, 1.0F, 0.2F);
				
				if(!EnviroMine.proxy.isClient() && player instanceof EntityPlayerMP)
				{
					((EntityPlayerMP)player).playerNetServerHandler.sendPacketToPlayer(packet);
				} else if(EnviroMine.proxy.isClient() && !player.worldObj.isRemote)
				{
					player.worldObj.playSoundEffect(entityLiving.posX + rndX, entityLiving.posY + rndY, entityLiving.posZ + rndZ, sound, 1.0F, 0.2F);
				}
			}
		}
	}

	@Override
    @SideOnly(Side.CLIENT)

    /**
     * Returns true if the potion has a associated status icon to display in then inventory when active.
     */
    public boolean hasStatusIcon()
    {
    	Minecraft.getMinecraft().renderEngine.bindTexture(textureResource);
        return true;
    }
}
