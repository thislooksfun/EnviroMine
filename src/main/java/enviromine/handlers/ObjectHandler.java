package enviromine.handlers;

import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import enviromine.EntityPhysicsBlock;
import enviromine.blocks.BlockElevator;
import enviromine.blocks.BlockGas;
import enviromine.blocks.TileEntityGas;
import enviromine.core.EM_Settings;
import enviromine.core.EnviroMine;
import enviromine.gases.RenderGasHandler;
import enviromine.items.EnviroArmor;
import enviromine.items.EnviroItemBadWaterBottle;
import enviromine.items.EnviroItemColdWaterBottle;
import enviromine.items.EnviroItemSaltWaterBottle;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.EnumArmorMaterial;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.EnumHelper;

public class ObjectHandler
{
	public static EnumArmorMaterial camelPackMaterial;
	
	public static Item badWaterBottle;
	public static Item saltWaterBottle;
	public static Item coldWaterBottle;
	
	public static Item airFilter;
	public static Item davyLamp;
	public static Item gasMeter;
	
	public static ItemArmor camelPack;
	public static ItemArmor gasMask;
	public static ItemArmor hardHat;
	
	public static Block elevator;
	public static Block gasBlock;
	public static Block fireGasBlock;
	
	public static int renderGasID;
	
	public static void RegisterItems()
	{
		badWaterBottle = new EnviroItemBadWaterBottle(EM_Settings.dirtBottleID).setMaxStackSize(1).setUnlocalizedName("enviromine.item.badwater").setCreativeTab(EnviroMine.enviroTab);
		saltWaterBottle = new EnviroItemSaltWaterBottle(EM_Settings.saltBottleID).setMaxStackSize(1).setUnlocalizedName("enviromine.item.saltwater").setCreativeTab(EnviroMine.enviroTab);
		coldWaterBottle = new EnviroItemColdWaterBottle(EM_Settings.coldBottleID).setMaxStackSize(1).setUnlocalizedName("enviromine.item.coldwater").setCreativeTab(EnviroMine.enviroTab);
		airFilter = new Item(EM_Settings.airFilterID).setMaxStackSize(1).setUnlocalizedName("enviromine.item.airfilter").setCreativeTab(EnviroMine.enviroTab).setTextureName("enviromine:air_filter");
		
		camelPackMaterial = EnumHelper.addArmorMaterial("camelPack", 100, new int[]{1, 0, 0, 0}, 0);
		
		camelPack = (ItemArmor)new EnviroArmor(EM_Settings.camelPackID, camelPackMaterial, 4, 1).setTextureName("camel_pack").setUnlocalizedName("enviromine.item.camelpack").setCreativeTab(EnviroMine.enviroTab);
		gasMask = (ItemArmor)new EnviroArmor(EM_Settings.gasMaskID, camelPackMaterial, 4, 0).setTextureName("gas_mask").setUnlocalizedName("enviromine.item.gasmask").setCreativeTab(EnviroMine.enviroTab);
		hardHat = (ItemArmor)new EnviroArmor(EM_Settings.hardHatID, camelPackMaterial, 4, 0).setTextureName("hard_hat").setUnlocalizedName("enviromine.item.hardhat").setCreativeTab(EnviroMine.enviroTab);
		//GameRegistry.registerItem(airFilter, "enviromine.airFilter");
	}
	
	public static void RegisterBlocks()
	{
		//elevator = new BlockElevator(EM_Settings.blockElevatorID, Material.iron);
		gasBlock = new BlockGas(EM_Settings.gasBlockID, Material.air).setUnlocalizedName("enviromine.block.gas").setCreativeTab(EnviroMine.enviroTab);
		fireGasBlock = new BlockGas(EM_Settings.fireGasBlockID, Material.air).setUnlocalizedName("enviromine.block.firegas").setCreativeTab(EnviroMine.enviroTab).setLightValue(1.0F);
		
		GameRegistry.registerBlock(gasBlock, "enviromine.block.gas");
		GameRegistry.registerBlock(fireGasBlock, "enviromine.block.firegas");
		renderGasID = RenderingRegistry.getNextAvailableRenderId();
		RenderingRegistry.registerBlockHandler(new RenderGasHandler());
	}
	
	public static void RegisterGases()
	{
	}
	
	public static void RegisterEntities()
	{
		EntityRegistry.registerGlobalEntityID(EntityPhysicsBlock.class, "EnviroPhysicsBlock", EM_Settings.physBlockID);
		GameRegistry.registerTileEntity(TileEntityGas.class, "enviromine.tile.gas");
	}
	
	public static void RegisterRecipes()
	{
		GameRegistry.addSmelting(badWaterBottle.itemID, new ItemStack(ItemPotion.potion.itemID, 1, 0), 0.0F);
		GameRegistry.addSmelting(saltWaterBottle.itemID, new ItemStack(ItemPotion.potion.itemID, 1, 0), 0.0F);
		GameRegistry.addSmelting(coldWaterBottle.itemID, new ItemStack(ItemPotion.potion.itemID, 1, 0), 0.0F);
		GameRegistry.addShapelessRecipe(new ItemStack(coldWaterBottle, 1, 0), new ItemStack(Item.potion, 1, 0), new ItemStack(Item.snowball, 1));
		GameRegistry.addShapelessRecipe(new ItemStack(badWaterBottle, 1, 0), new ItemStack(Item.potion, 1, 0), new ItemStack(Block.dirt, 1));
		GameRegistry.addShapelessRecipe(new ItemStack(saltWaterBottle, 1, 0), new ItemStack(Item.potion, 1, 0), new ItemStack(Block.sand, 1));
		
		GameRegistry.addRecipe(new ItemStack(camelPack, 1, camelPack.getMaxDamage()), "xxx", "xyx", "xxx", 'x', new ItemStack(Item.leather), 'y', new ItemStack(Item.glassBottle));
		GameRegistry.addRecipe(new ItemStack(airFilter, 1), "xyx", "xzx", "xyx", 'x', new ItemStack(Item.ingotIron), 'y', new ItemStack(Block.cloth), 'z', new ItemStack(Item.coal, 1, 1));
		GameRegistry.addRecipe(new ItemStack(gasMask, 1), "xxx", "xzx", "yxy", 'x', new ItemStack(Item.ingotIron), 'y', new ItemStack(airFilter), 'z', new ItemStack(Block.thinGlass));
		GameRegistry.addRecipe(new ItemStack(hardHat, 1), "xyx", "xzx", 'x', new ItemStack(Block.cloth, 1, 4), 'y', new ItemStack(Block.redstoneLampIdle), 'z', new ItemStack(Item.helmetIron, 1, 0));
	}
	
	public static void RegisterNames()
	{
		
		LanguageRegistry.addName(badWaterBottle, "Dirty Water Bottle");
		LanguageRegistry.addName(saltWaterBottle, "Salt Water Bottle");
		LanguageRegistry.addName(coldWaterBottle, "Cold Water Bottle");
		LanguageRegistry.addName(camelPack, "Camel Pack");
		LanguageRegistry.addName(gasMask, "Gas Mask");
		LanguageRegistry.addName(hardHat, "Hard Hat");
		LanguageRegistry.addName(airFilter, "Air Filter");
	}
}
