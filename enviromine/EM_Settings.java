package enviromine;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.Configuration;

public class EM_Settings
{
	//Mod Data
	public static final String Version = "1.0.13";
	public static final String ID = "EnviroMine";
	public static final String Channel = "EM_CH";
	public static final String Name = "EnviroMine";
	public static final String Proxy = "enviromine";
	
	public static boolean useFarenheit = false;
	public static boolean enablePhysics = true;
	public static boolean enableAirQ = true;
	public static boolean enableHydrate = true;
	public static boolean enableSanity = true;
	public static boolean enableBodyTemp = true;
	public static boolean saddleRecipe = true;
	
	public static int dirtBottleID = 5001;
	public static int saltBottleID = 5002;
	public static int coldBottleID = 5003;
	
	public static int frostBitePotionID = 50;
	public static int dehydratePotionID = 51;
	public static int insanityPotionID = 52;
	
	//Properties
	public static HashMap<Integer,Object[]> armorProperties = new HashMap();
	public static HashMap<Integer,Object[]> blockProperties = new HashMap();
	public static HashMap<String,Object[]> livingProperties = new HashMap();
    
	public static void LoadConfig(File file)
	{
		Configuration config;
		try
		{
			config = new Configuration(file);
		} catch(NullPointerException e)
		{
			e.printStackTrace();
			System.out.println("FAILED TO LOAD CONFIGS!\nBACKUP SETTINGS ARE NOW IN EFFECT!");
			return;
		} catch(StringIndexOutOfBoundsException e)
		{
			e.printStackTrace();
			System.out.println("FAILED TO LOAD CONFIGS!\nBACKUP SETTINGS ARE NOW IN EFFECT!");
			return;
		}
		System.out.println("Loading EnviroMine Config: " + file.getAbsolutePath());
		
        config.load();
        
        config.addCustomCategoryComment("Armor Properties", "Add properties for custom armors here. Add the armor name to 'Custom Objects' before creating the property here. \nProperty Format: <ItemID>, <AddTempAtNight>, <AddTempInShade>, <AddTempInSun>, <AirTempMultiplyer>, <F/C (Farenheit or Celcius)>");
        config.addCustomCategoryComment("Block Properties", "Add properties for custom blocks here. Add the block name to 'Custom Objects' before creating the property here. \nProperty Format: <BlockID>, <BlockMeta(-1 to ignore)>, <DropID>, <DropMeta(-1 to ignore)>, <DropNum>, <BlockTemp>, <SanityPerSecond>, <AirQualityPerSecond>, <CanFallThreshold(0-26)>, <MustFallThreshold(0-26)>, <F/C (Farenheit or Celcius)>");
		config.addCustomCategoryComment("EntityLiving Properties", "Add properties for custom living entities here. Add the entity name to 'Custom Objects' before creating the property here. \nProperty Format: <EntityName(E.G. EntityPigZombie)>, <Dehydrate(T/F)>, <BodyTemp?(T/F)>, <AirQuality?(T/F)>");
        config.addCustomCategoryComment("Custom Objects", "Lists of objects that have envionmental properties");
        
        useFarenheit = config.get(config.CATEGORY_GENERAL, "Use Farenheit instead of Celsius", false).getBoolean(false);
        enablePhysics = config.get(config.CATEGORY_GENERAL, "Enable Physics", true).getBoolean(true);
        enableSanity = config.get(config.CATEGORY_GENERAL, "Enable Sanity", true).getBoolean(true);
        enableHydrate = config.get(config.CATEGORY_GENERAL, "Enable Hydration", true).getBoolean(true);
        enableBodyTemp = config.get(config.CATEGORY_GENERAL, "Enable Body Temperature", true).getBoolean(true);
        enableAirQ = config.get(config.CATEGORY_GENERAL, "Enable Air Quality", true).getBoolean(true);
        saddleRecipe = config.get(config.CATEGORY_GENERAL, "Enable Saddle Recipe", true).getBoolean(true);
        
        dirtBottleID = config.get(config.CATEGORY_ITEM, "Dirty Water Bottle", 5001).getInt(5001);
        saltBottleID = config.get(config.CATEGORY_ITEM, "Salt Water Bottle", 5002).getInt(5002);
        coldBottleID = config.get(config.CATEGORY_ITEM, "Cold Water Bottle", 5003).getInt(5003);
        
        frostBitePotionID = config.get("Potions", "Frostbite", 50).getInt(50);
        dehydratePotionID = config.get("Potions", "Dehydration", 51).getInt(51);
        insanityPotionID = config.get("Potions", "Insanity", 52).getInt(52);
        
        loadArmorProperties(config);
        loadBlockProperties(config);
        loadEntityLivingProperties(config);
        
        config.save();
        
        System.out.println("Successfully loaded EnviroMine configs");
	}

	public static void loadArmorProperties(Configuration config)
	{
		loadDefaultArmorProperties(config);
		
		String rawArmorList = config.get("Custom Objects", "Armor List", "").getString();
		
		if(rawArmorList.equals(""))
		{
			return;
		} else
		{
			System.out.println("Loaded custom armor list: ("+rawArmorList+")");
		}
		
		String[] customArmors = rawArmorList.split(", ");
		
		for(int i = customArmors.length - 1; i >= 0; i--)
		{
			if(customArmors[i].equals(""))
			{
				continue;
			}
			System.out.println("Loaded custom properties for armor '" + customArmors[i] + "'");
			addArmorPropToHash(customArmors[i], config.get("Armor Properties", customArmors[i], "").getString());
		}
	}

	public static void loadEntityLivingProperties(Configuration config)
	{
		String rawLivingList = config.get("Custom Objects", "Living List", "").getString();
		
		if(rawLivingList.equals(""))
		{
			return;
		} else
		{
			System.out.println("Loaded custom armor list: ("+rawLivingList+")");
		}
		
		String[] customLiving = rawLivingList.split(", ");
		
		for(int i = customLiving.length - 1; i >= 0; i--)
		{
			if(customLiving[i].equals(""))
			{
				continue;
			}
			System.out.println("Loaded custom properties for armor '" + customLiving[i] + "'");
			addLivingPropToHash(customLiving[i], config.get("Armor Properties", customLiving[i], "").getString());
		}
	}
	
	public static void loadBlockProperties(Configuration config)
	{
		String rawBlockList = config.get("Custom Objects", "Block List", "").getString();
		
		if(rawBlockList.equals(""))
		{
			return;
		} else
		{
			System.out.println("Loaded custom block list: ("+rawBlockList+")");
		}
		
		String[] customBlocks = rawBlockList.split(", ");
		
		for(int i = customBlocks.length - 1; i >= 0; i--)
		{
			if(customBlocks[i].equals(""))
			{
				continue;
			}
			System.out.println("Loaded custom properties for block '" + customBlocks[i] + "'");
			addBlockPropToHash(customBlocks[i], config.get("Block Properties", customBlocks[i], "").getString());
		}
	}

	public static void loadDefaultArmorProperties(Configuration config)
	{
		addArmorPropToHash("Leather Helmet"	, config.get("Armor Properties", "Leather Helmet", "" + ItemArmor.helmetLeather.itemID + 	", 2.0, 2.0, 2.0, 1.0, C").getString());
		addArmorPropToHash("Leather Chest"	, config.get("Armor Properties", "Leather Chest", "" + ItemArmor.plateLeather.itemID + 	", 2.0, 2.0, 2.0, 1.0, C").getString());
		addArmorPropToHash("Leather Legs"	, config.get("Armor Properties", "Leather Legs", "" + ItemArmor.legsLeather.itemID + 		", 2.0, 2.0, 2.0, 1.0, C").getString());
		addArmorPropToHash("Leather Boots"	, config.get("Armor Properties", "Leather Boots", "" + ItemArmor.bootsLeather.itemID + 	", 2.0, 2.0, 2.0, 1.0, C").getString());

		addArmorPropToHash("Iron Helmet"	, config.get("Armor Properties", "Iron Helmet", "" + ItemArmor.helmetIron.itemID + ", -1.0, 0.0, 5.0, 1.1, C").getString());
		addArmorPropToHash("Iron Chest"		, config.get("Armor Properties", "Iron Chest", "" + ItemArmor.plateIron.itemID + 	", -1.0, 0.0, 5.0, 1.1, C").getString());
		addArmorPropToHash("Iron Helmet"	, config.get("Armor Properties", "Iron Legs", "" + ItemArmor.legsIron.itemID + 	", -1.0, 0.0, 5.0, 1.1, C").getString());
		addArmorPropToHash("Iron Helmet"	, config.get("Armor Properties", "Iron Boots", "" + ItemArmor.bootsIron.itemID + 	", -1.0, 0.0, 5.0, 1.1, C").getString());
		
		addArmorPropToHash("Gold Helmet"	, config.get("Armor Properties", "Gold Helmet", "" + ItemArmor.helmetIron.itemID + ", 0.0, 0.0, 3.0, 1.1, C").getString());
		addArmorPropToHash("Gold Chest"		, config.get("Armor Properties", "Gold Chest", "" + ItemArmor.plateIron.itemID + 	", 0.0, 0.0, 3.0, 1.1, C").getString());
		addArmorPropToHash("Gold Legs"		, config.get("Armor Properties", "Gold Legs", "" + ItemArmor.legsIron.itemID + 	", 0.0, 0.0, 3.0, 1.1, C").getString());
		addArmorPropToHash("Gold Boots"		, config.get("Armor Properties", "Gold Boots", "" + ItemArmor.bootsIron.itemID + 	", 0.0, 0.0, 3.0, 1.1, C").getString());
		
		addArmorPropToHash("Diamond Helmet"	, config.get("Armor Properties", "Diamond Helmet", "" + ItemArmor.helmetDiamond.itemID + 	", 0.0, 0.0, 0.0, 0.5, C").getString());
		addArmorPropToHash("Diamond Chest"	, config.get("Armor Properties", "Diamond Chest", "" + ItemArmor.plateDiamond.itemID + 	", 0.0, 0.0, 0.0, 0.5, C").getString());
		addArmorPropToHash("Diamond Legs"	, config.get("Armor Properties", "Diamond Legs", "" + ItemArmor.legsDiamond.itemID + 		", 0.0, 0.0, 0.0, 0.5, C").getString());
		addArmorPropToHash("Diamond Boots"	, config.get("Armor Properties", "Diamond Boots", "" + ItemArmor.bootsDiamond.itemID + 	", 0.0, 0.0, 0.0, 0.5, C").getString());
	}
	
	public static void addArmorPropToHash(String name, String data)
	{
		try
		{
			String[] rawData = data.split(", ");
			
			if(rawData.length != 6)
			{
				System.out.println("Invalid property format for custom armor: " + name);
				return;
			}
			
			Object[] armorProps = new Object[6];
			
			for(int i = 5; i >= 0; i--)
			{
				armorProps[i] = rawData[i];
			}
			
			armorProps[0] = Integer.valueOf((String)armorProps[0]);
			armorProps[4] = Float.valueOf((String)armorProps[4]);
	    	
			if(armorProps[5] == "F")
			{
				for(int i = 1; i <= 3; i++)
				{
					armorProps[i] = convertToCelcius(Float.valueOf((String)armorProps[i]));
				}
			} else
			{
				for(int i = 1; i <= 3; i++)
				{
					armorProps[i] = Float.valueOf((String)armorProps[i]);
				}
			}
	    	armorProperties.put((Integer)armorProps[0], armorProps);
			System.out.println("Loaded custom property for: " + name);
		} catch(NumberFormatException e)
		{
			System.out.println("Invalid property format for custom armor: " + name);
		}
	}

	public static void addBlockPropToHash(String name, String data)
	{
		try
		{
			String[] rawData = data.split(", ");
			
			if(rawData.length != 11)
			{
				System.out.println("Invalid property format for custom block: " + name);
				return;
			}
			
			Object[] blockProps = new Object[11];
			
			for(int i = 10; i >= 0; i--)
			{
				blockProps[i] = rawData[i];
			}
	    	
			for(int i = 0; i < 10; i++)
			{
				if(i < 5 || i > 7)
				{
					blockProps[i] = Integer.valueOf((String)blockProps[i]);
				} else
				{
					blockProps[i] = Float.valueOf((String)blockProps[i]);
				}
			}
			
			if(blockProps[10] == "F")
			{
				blockProps[5] = convertToCelcius((Float)blockProps[1]);
			}
	    	blockProperties.put((Integer)blockProps[0], blockProps);
			System.out.println("Loaded custom property for: " + name);
		} catch(NumberFormatException e)
		{
			System.out.println("Invalid property format for custom block: " + name);
		}
	}
	
	public static void addLivingPropToHash(String name, String data)
	{
		try
		{
			String[] rawData = data.split(", ");
			
			if(rawData.length != 4)
			{
				System.out.println("Invalid property format for custom living entity: " + name);
				return;
			}
			
			Object[] livingProps = new Object[3];
			
			for(int i = 3; i >= 0; i--)
			{
				livingProps[i] = rawData[i];
			}
			
			for(int i = 3; i >= 1; i--)
			{
				if(((String)livingProps[i]).equalsIgnoreCase("t") || ((String)livingProps[i]).equalsIgnoreCase("true"))
				{
					livingProps[i] = true;
				} else
				{
					livingProps[i] = false;
				}
			}
			
	    	livingProperties.put((String)livingProps[0], livingProps);
			System.out.println("Loaded custom property for: " + name);
		} catch(NumberFormatException e)
		{
			System.out.println("Invalid property format for custom living entity: " + name);
		}
	}
	
	public static float convertToFarenheit(float num)
	{
		return ((num *(9/5))+32F);
	}
	
	public static float convertToCelcius(float num)
	{
		return ((num-32F) * (5/9));
	}
}
