package enviromine.trackers;

import java.util.ArrayList;
import java.util.Random;
import enviromine.core.EnviroMine;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraft.world.EnumSkyBlock;

public class Hallucination
{
	public EntityCreature falseEntity;
	public String falseSound;
	public int x;
	public int y;
	public int z;
	public int time;
	public static int maxTime = 60;
	public static ArrayList<Hallucination> list = new ArrayList<Hallucination>();
	
	public Hallucination(EntityLivingBase entityLiving)
	{
		if(!(entityLiving instanceof EntityPlayer))
		{
			return;
		}
		
		Random rand = entityLiving.getRNG();
		
		x = (int)(entityLiving.posX + rand.nextInt(20) - 10);
		y = (int)(entityLiving.posY + rand.nextInt(2) - 1);
		z = (int)(entityLiving.posZ + rand.nextInt(20) - 10);
		
		switch(rand.nextInt(3))
		{
			case 0:
			{
				falseSound = "mob.zombie.say";
				falseEntity = new EntityZombie(entityLiving.worldObj);
				break;
			}
			case 1:
			{
				falseSound = "random.fuse";
				falseEntity = new EntityCreeper(entityLiving.worldObj);
				break;
			}
			case 2:
			{
				falseSound = "mob.spider.say";
				falseEntity = new EntitySpider(entityLiving.worldObj);
				break;
			}
			case 3:
			{
				falseSound = "mob.skeleton.say";
				falseEntity = new EntitySkeleton(entityLiving.worldObj);
				break;
			}
		}
		
		falseEntity.setPositionAndRotation(x, y, z, rand.nextFloat() * 360F, 0.0F);
		
		if(!isAtValidSpawn(falseEntity))
		{
			return;
		} else if(!entityLiving.worldObj.spawnEntityInWorld(falseEntity))
		{
			return;
		}
		list.add(this);
		
		if(EnviroMine.proxy.isClient())
		{
			Minecraft.getMinecraft().sndManager.playSound(falseSound, x, y, z, 1.0F, 1.0F);
		}
	}
	
	public static void update()
	{
		if(list.size() >= 1)
		{
			for(int i = list.size() -1; i >= 0; i -= 1)
			{
				Hallucination subject = list.get(i);
				if(subject.time >= maxTime)
				{
					subject.time += 1;
					subject.falseEntity.setDead();
				} else
				{
					subject.time += 1;
				}
			}
		}
	}
	
	public static boolean isAtValidSpawn(EntityCreature creature)
	{
        return creature.worldObj.checkNoEntityCollision(creature.boundingBox) && creature.worldObj.getCollidingBoundingBoxes(creature, creature.boundingBox).isEmpty() && !creature.worldObj.isAnyLiquid(creature.boundingBox) && isValidLightLevel(creature);
	}

    /**
     * Checks to make sure the light is not too bright where the mob is spawning
     */
    protected static boolean isValidLightLevel(EntityCreature creature)
    {
        int i = MathHelper.floor_double(creature.posX);
        int j = MathHelper.floor_double(creature.boundingBox.minY);
        int k = MathHelper.floor_double(creature.posZ);

        if (creature.worldObj.getSavedLightValue(EnumSkyBlock.Sky, i, j, k) > creature.getRNG().nextInt(32))
        {
            return false;
        }
        else
        {
            int l = creature.worldObj.getBlockLightValue(i, j, k);

            if (creature.worldObj.isThundering())
            {
                int i1 = creature.worldObj.skylightSubtracted;
                creature.worldObj.skylightSubtracted = 10;
                l = creature.worldObj.getBlockLightValue(i, j, k);
                creature.worldObj.skylightSubtracted = i1;
            }

            return l <= creature.getRNG().nextInt(8);
        }
    }
}
