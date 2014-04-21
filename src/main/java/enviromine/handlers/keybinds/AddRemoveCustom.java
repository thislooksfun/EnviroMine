package enviromine.handlers.keybinds;

import java.util.EnumSet;

import org.lwjgl.input.Keyboard;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.util.EnumMovingObjectType;
import cpw.mods.fml.client.registry.KeyBindingRegistry.KeyHandler;
import cpw.mods.fml.common.TickType;
import enviromine.core.EM_ConfigHandler;
import enviromine.core.EnviroMine;

public class AddRemoveCustom extends KeyHandler
{
	Object[] dataToCustom = new Object[5];
	
	public boolean keydown = true;
	
	public AddRemoveCustom(KeyBinding[] keyBindings, boolean[] repeatings)
	{
		super(keyBindings, repeatings);
	}
	
	private EnumSet<TickType> tickTypes = EnumSet.of(TickType.CLIENT);
	
	@Override
	public String getLabel()
	{
		return "KeyBinding";
	}
	
	@Override
	public void keyDown(EnumSet<TickType> types, KeyBinding kb, boolean tickEnd, boolean isRepeat)
	{
		
	}
	
	@Override
	public void keyUp(EnumSet<TickType> types, KeyBinding kb, boolean tickEnd)
	{
		
		if(tickEnd)
		{
			
			Minecraft mc = Minecraft.getMinecraft();
			
			if(mc.currentScreen != null)
			{
				return;
			}
			
			if((!(Minecraft.getMinecraft().isSingleplayer()) || !EnviroMine.proxy.isClient()) && Minecraft.getMinecraft().thePlayer != null)
			{
				if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
				{
					mc.thePlayer.addChatMessage("Single player only function.");
				}
				return;
			}
			// prevents key press firing while gui screen or chat open, if that's what you want
			// if you want your key to be able to close the gui screen, handle it outside this if statement
			if(mc.currentScreen == null)
			{
				if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
				{
					try
					{
						
						String returnValue = "";
						if(mc.thePlayer.getHeldItem() != null)
						{
							
							Item item = mc.thePlayer.getHeldItem().getItem();
							int itemId = mc.thePlayer.getHeldItem().itemID;
							int itemMeta = mc.thePlayer.getHeldItem().getItemDamage();
							String unlocolizedName = mc.thePlayer.getHeldItem().getItem().getUnlocalizedName();
							String name = mc.thePlayer.getHeldItem().getDisplayName();
							
							unlocolizedName = replaceULN(unlocolizedName);
							name = replaceULN(name);

							
							dataToCustom[0] = itemId;
							dataToCustom[1] = itemMeta;
							dataToCustom[2] = unlocolizedName;

							if(item instanceof ItemArmor)
							{
								returnValue = EM_ConfigHandler.SaveMyCustom("ARMOR", name, dataToCustom);
								mc.thePlayer.addChatMessage(name + " " + returnValue + " in MyCustom.cfg file. ");
							} else if(item instanceof Item)
							{
								returnValue = EM_ConfigHandler.SaveMyCustom("ITEM", name, dataToCustom);
								mc.thePlayer.addChatMessage(name + " " + returnValue + " in MyCustom.cfg file. ");
							}
							
							return;
							
						}
						
						EnumMovingObjectType type = Minecraft.getMinecraft().objectMouseOver.typeOfHit;
						
						if(type.name() == "ENTITY")
						{
							Entity lookingAt = Minecraft.getMinecraft().objectMouseOver.entityHit;
							String name = EntityList.getEntityString(lookingAt);
							name = replaceULN(name);
							
							returnValue = EM_ConfigHandler.SaveMyCustom(type.name(), name, dataToCustom);
							mc.thePlayer.addChatMessage(name + " " + returnValue + " in MyCustom.cfg file.");
						} else if(type.name() == "TILE")
						{
							
							int blockX = Minecraft.getMinecraft().objectMouseOver.blockX;
							int blockY = Minecraft.getMinecraft().objectMouseOver.blockY;
							int blockZ = Minecraft.getMinecraft().objectMouseOver.blockZ;
							
							int blockID = Minecraft.getMinecraft().thePlayer.worldObj.getBlockId(blockX, blockY, blockZ);
							int blockMeta = Minecraft.getMinecraft().thePlayer.worldObj.getBlockMetadata(blockX, blockY, blockZ);
							Block block = Block.blocksList[blockID];
							String blockULName = block.getUnlocalizedName();
							String blockName = block.getLocalizedName();
							
							blockULName = replaceULN(blockULName);
							blockName = replaceULN(blockName);
								
							dataToCustom[0] = blockID;
							dataToCustom[1] = blockMeta;
							dataToCustom[2] = blockULName;
							
							returnValue = EM_ConfigHandler.SaveMyCustom(type.name(), blockName, dataToCustom);
							mc.thePlayer.addChatMessage(blockName + "(" + blockID + ":" + blockMeta + ") " + returnValue + "  in MyCustom.cfg file.");
						}
					}
					catch(NullPointerException e)
					{
						
					}
				}
				else
				{
					
					mc.thePlayer.addChatMessage("Must hold left shift to add/remove objects");
				}
				
				
			}
		}
		
	}
	
	@Override
	public EnumSet<TickType> ticks()
	{
		
		return tickTypes;
		
	}
	
	public static String replaceULN(String unlocalizedName)
	{
		String newName = unlocalizedName.replaceAll("\\.+", "\\_");
		return newName;
		
	}
	
	
}
