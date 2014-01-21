package enviromine.handlers;

import java.util.EnumSet;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumMovingObjectType;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import cpw.mods.fml.client.registry.KeyBindingRegistry.KeyHandler;
import cpw.mods.fml.common.TickType;


public class KeyBind extends KeyHandler
{

	public boolean keydown = true;
	public KeyBind(KeyBinding[] keyBindings, boolean[] repeatings) 
	{
		super(keyBindings, repeatings);
	}

	private EnumSet tickTypes = EnumSet.of(TickType.CLIENT);


	@Override
	public String getLabel() 
	{
		return "KeyBinding";
	}

	@Override
	public void keyDown(EnumSet<TickType> types, KeyBinding kb,	boolean tickEnd, boolean isRepeat) 
	{


	}

	@Override
	public void keyUp(EnumSet<TickType> types, KeyBinding kb, boolean tickEnd) 
	{
		

		if(tickEnd)
		{
			try
			{
		 
				//what to do when key is pressed/down
				EnumMovingObjectType type = Minecraft.getMinecraft().objectMouseOver.typeOfHit;
			
				if (type.name() == "ENTITY")
				{
					Entity lookingAt = Minecraft.getMinecraft().objectMouseOver.entityHit;
					
					String name = EntityList.getEntityString(lookingAt);
					
					System.out.println(name);
					
					// Call CONFIG
				}
				else if(type.name() == "TILE")
				{
					
					int blockX = Minecraft.getMinecraft().objectMouseOver.blockX;
					int blockY = Minecraft.getMinecraft().objectMouseOver.blockY;
					int blockZ = Minecraft.getMinecraft().objectMouseOver.blockZ;
					
					//System.out.println("*"+ blockX + ","+ blockY + ","+ blockZ +"*");
				
					int blockID = Minecraft.getMinecraft().thePlayer.worldObj.getBlockId(blockX,blockY,blockZ);
					int blockMeta = Minecraft.getMinecraft().thePlayer.worldObj.getBlockMetadata(blockX, blockY, blockZ);
					//String blockMat = Minecraft.getMinecraft().thePlayer.worldObj.get;
					Block block = Block.blocksList[blockID];
					String blockULName = block.getUnlocalizedName().toString();
					String blockName = block.getLocalizedName().toString();
				
					//Call Config
					System.out.println(blockULName + "*" + blockName + "*" + blockID + ":" + blockMeta + "*");
				} //else if
			} //try
			catch (NullPointerException e)
			{
				// Doesn't contain Info
				System.out.println("Empty" + e);
			}
		
		}


	}

	@Override
	public EnumSet<TickType> ticks() 
	{

		 return tickTypes;

	}

}
