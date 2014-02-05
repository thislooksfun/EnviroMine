package enviromine.core;

import java.nio.ByteOrder;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.minecraft.block.Block;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.EnumArmorMaterial;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.world.gen.structure.MapGenStructureIO;
import net.minecraftforge.common.EnumHelper;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.client.registry.KeyBindingRegistry;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import cpw.mods.fml.common.registry.VillagerRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import enviromine.EM_VillageMineshaft;
import enviromine.EnviroPotion;
import enviromine.core.proxies.EM_CommonProxy;
import enviromine.gui.UpdateNotification;
import enviromine.handlers.EnviroPacketHandler;
import enviromine.handlers.EnviroShaftCreationHandler;
import enviromine.items.EnviroArmor;
import enviromine.items.EnviroItemBadWaterBottle;
import enviromine.items.EnviroItemColdWaterBottle;
import enviromine.items.EnviroItemSaltWaterBottle;

@Mod(modid = EM_Settings.ID, name = EM_Settings.Name, version = EM_Settings.Version)
@NetworkMod(clientSideRequired = true, serverSideRequired = true, channels = {EM_Settings.Channel}, packetHandler = EnviroPacketHandler.class)
public class EnviroMine
{
	public static Logger logger;
	public static Item badWaterBottle;
	public static Item saltWaterBottle;
	public static Item coldWaterBottle;
	
	public static EnumArmorMaterial camelPackMaterial;
	public static ItemArmor camelPack;
	
	@Instance("EM_Instance")
	public static EnviroMine instance;
	
	@SidedProxy(clientSide = EM_Settings.Proxy + ".EM_ClientProxy", serverSide = EM_Settings.Proxy + ".EM_CommonProxy")
	public static EM_CommonProxy proxy;
	
	@EventHandler
	public static void preInit(FMLPreInitializationEvent event)
	{
		logger = event.getModLog();
		logger.setParent(FMLLog.getLogger());
		
		proxy.preInit(event);
		
		// Load Configuration files And Custom files
		EM_ConfigHandler.initConfig();
		
		// Create Items
		badWaterBottle = new EnviroItemBadWaterBottle(EM_Settings.dirtBottleID).setMaxStackSize(1).setUnlocalizedName("dirtyWaterBottle").setCreativeTab(CreativeTabs.tabBrewing);
		saltWaterBottle = new EnviroItemSaltWaterBottle(EM_Settings.saltBottleID).setMaxStackSize(1).setUnlocalizedName("saltWaterBottle").setCreativeTab(CreativeTabs.tabBrewing);
		coldWaterBottle = new EnviroItemColdWaterBottle(EM_Settings.coldBottleID).setMaxStackSize(1).setUnlocalizedName("coldWaterBottle").setCreativeTab(CreativeTabs.tabBrewing);
		
		camelPackMaterial = EnumHelper.addArmorMaterial("camelPack", 100, new int[]{0, 0, 0, 0}, 0);
		
		camelPack = (ItemArmor)new EnviroArmor(EM_Settings.camelPackID, camelPackMaterial, 4, 1).setTextureName("camel_pack").setUnlocalizedName("camelPack").setCreativeTab(CreativeTabs.tabTools);
		
		if(EM_Settings.shaftGen_actual == true)
		{
			VillagerRegistry.instance().registerVillageCreationHandler(new EnviroShaftCreationHandler());
			MapGenStructureIO.func_143031_a(EM_VillageMineshaft.class, "ViMS");
		}
	}
	
	@EventHandler
	public static void init(FMLInitializationEvent event)
	{
		proxy.init(event);
		
		LanguageRegistry.addName(badWaterBottle, "Dirty Water Bottle");
		LanguageRegistry.addName(saltWaterBottle, "Salt Water Bottle");
		LanguageRegistry.addName(coldWaterBottle, "Cold Water Bottle");
		LanguageRegistry.addName(camelPack, "Camel Pack");
		
		EnviroPotion.frostbite = (EnviroPotion)new EnviroPotion(EM_Settings.frostBitePotionID, true, 8171462).setPotionName("potion.frostbite").setIconIndex(0, 0);
		EnviroPotion.dehydration = (EnviroPotion)new EnviroPotion(EM_Settings.dehydratePotionID, true, 3035801).setPotionName("potion.dehydration").setIconIndex(1, 0);
		EnviroPotion.insanity = (EnviroPotion)new EnviroPotion(EM_Settings.insanityPotionID, true, 5578058).setPotionName("potion.insanity").setIconIndex(2, 0);
		EnviroPotion.heatstroke = (EnviroPotion)new EnviroPotion(EM_Settings.heatstrokePotionID, true, getColorFromRGBA(255, 0, 0, 255)).setPotionName("potion.heatstroke").setIconIndex(3, 0);
		EnviroPotion.hypothermia = (EnviroPotion)new EnviroPotion(EM_Settings.hypothermiaPotionID, true, 8171462).setPotionName("potion.hypothermia").setIconIndex(4, 0);
		
		LanguageRegistry.instance().addStringLocalization("potion.hypothermia", "Hypothermia");
		LanguageRegistry.instance().addStringLocalization("potion.heatstroke", "Heat Stroke");
		LanguageRegistry.instance().addStringLocalization("potion.frostbite", "Frostbite");
		LanguageRegistry.instance().addStringLocalization("potion.dehydration", "Dehydration");
		LanguageRegistry.instance().addStringLocalization("potion.insanity", "Insanity");
		
		GameRegistry.addSmelting(badWaterBottle.itemID, new ItemStack(ItemPotion.potion.itemID, 1, 0), 0.0F);
		GameRegistry.addSmelting(saltWaterBottle.itemID, new ItemStack(ItemPotion.potion.itemID, 1, 0), 0.0F);
		GameRegistry.addSmelting(coldWaterBottle.itemID, new ItemStack(ItemPotion.potion.itemID, 1, 0), 0.0F);
		GameRegistry.addShapelessRecipe(new ItemStack(coldWaterBottle, 1, 0), new ItemStack(Item.potion, 1, 0), new ItemStack(Item.snowball, 1));
		GameRegistry.addShapelessRecipe(new ItemStack(badWaterBottle, 1, 0), new ItemStack(Item.potion, 1, 0), new ItemStack(Block.dirt, 1));
		GameRegistry.addShapelessRecipe(new ItemStack(saltWaterBottle, 1, 0), new ItemStack(Item.potion, 1, 0), new ItemStack(Block.sand, 1));
		
		GameRegistry.addRecipe(new ItemStack(camelPack, 1, camelPack.getMaxDamage()), "xxx", "xyx", "xxx", 'x', new ItemStack(Item.leather), 'y', new ItemStack(Item.glassBottle));
		
		GameRegistry.registerPlayerTracker(new UpdateNotification());
		
		EnviroMine.logger.log(Level.INFO, "Registering Handlers");
		proxy.registerTickHandlers();
		proxy.registerEventHandlers();
	}
	
	@EventHandler
	public static void postInit(FMLPostInitializationEvent event)
	{
		proxy.postInit(event);
	}
	
	@SideOnly(Side.CLIENT)
	public static void registerKeyBindings(FMLInitializationEvent event)
	{
		// Add remove Keybind
		KeyBinding[] key = {new KeyBinding("EnviroMine Add/Remove Custom Object", Keyboard.KEY_J)};
		boolean[] repeat = {false};
		KeyBindingRegistry.registerKeyBinding(new enviromine.handlers.keybinds.AddRemoveCustom(key, repeat));
		
		// Reload Custom Objects Files
		KeyBinding[] key1 = {new KeyBinding("EnviroMine Reload All Custom Object", Keyboard.KEY_K)};
		boolean[] repeat1 = {false};
		KeyBindingRegistry.registerKeyBinding(new enviromine.handlers.keybinds.ReloadCustomObjects(key1, repeat1));
	
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
}
