package enviromine.handlers;

import java.util.EnumSet;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.util.EnumMovingObjectType;
import cpw.mods.fml.client.registry.KeyBindingRegistry.KeyHandler;
import cpw.mods.fml.common.TickType;
import enviromine.core.EM_ConfigHandler;


public class KeyBind extends KeyHandler
{
	Object[] dataToCustom = new Object[5];
	
	
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
			if(!(Minecraft.getMinecraft().isSingleplayer())) {
				Minecraft.getMinecraft().thePlayer.addChatMessage("Single player only function.");
				return;
			}
			try
			{
				EnumMovingObjectType type = Minecraft.getMinecraft().objectMouseOver.typeOfHit;

				if (type.name() == "ENTITY")
				{
					Entity lookingAt = Minecraft.getMinecraft().objectMouseOver.entityHit;
					String name = EntityList.getEntityString(lookingAt);
					// Call CONFIG
					EM_ConfigHandler.SaveMyCustom(type.name(), name, dataToCustom);
					
					Minecraft.getMinecraft().thePlayer.addChatMessage("Adding " + name + " to MyCustom.dat file.");
				}
				else if(type.name() == "TILE")
				{
					
					int blockX = Minecraft.getMinecraft().objectMouseOver.blockX;
					int blockY = Minecraft.getMinecraft().objectMouseOver.blockY;
					int blockZ = Minecraft.getMinecraft().objectMouseOver.blockZ;

					int blockID = Minecraft.getMinecraft().thePlayer.worldObj.getBlockId(blockX,blockY,blockZ);
					int blockMeta = Minecraft.getMinecraft().thePlayer.worldObj.getBlockMetadata(blockX, blockY, blockZ);
					Block block = Block.blocksList[blockID];
					String blockULName = block.getUnlocalizedName().toString();
					String blockName = block.getLocalizedName().toString();
					dataToCustom[0] = blockID; dataToCustom[1] = blockMeta;
					dataToCustom[2] = blockULName;
					//System.out.println(blockULName + "*" + blockName + "*" + blockID + ":" + blockMeta + "*");
					
					EM_ConfigHandler.SaveMyCustom(type.name(), blockName, dataToCustom);
					
					Minecraft.getMinecraft().thePlayer.addChatMessage("Adding " + blockName + "-"+ blockID +":"+ blockMeta +" to MyCustom.dat file.");
				} //else if
			} //try
			catch (NullPointerException e)
			{

			}
		
		} 


	}

	@Override
	public EnumSet<TickType> ticks() 
	{

		 return tickTypes;

	}

}
