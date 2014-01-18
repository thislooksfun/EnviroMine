package enviromine.core;

import java.io.File;
import java.util.HashMap;

import net.minecraft.item.ItemArmor;
import net.minecraftforge.common.Configuration;

public class EM_Settings
{
	//Mod Data
	public static final String Version = "1.0.14";
	public static final String ID = "EnviroMine";
	public static final String Channel = "EM_CH";
	public static final String Name = "EnviroMine";
	public static final String Proxy = "enviromine.core.proxies";
	
	
	public static boolean enablePhysics = true;
	public static boolean enableLandslide = true;
	public static boolean enableAirQ = true;
	public static boolean enableHydrate = true;
	public static boolean enableSanity = true;
	public static boolean enableBodyTemp = true;
	public static boolean trackNonPlayer_default = true;
		public static boolean trackNonPlayer_actual;
	
	//public static boolean saddleRecipe = true;
	
	
	//Gui settings
	public static boolean sweatParticals_default = true;
		public static boolean sweatParticals_actual;
	public static boolean insaneParticals_default = true;
		public static boolean insaneParticals_actual;
	
	public static boolean useFarenheit = false;
	public static String heatBarPos_default = "Bottom_Left";
		public static String heatBarPos_actual;
	public static String waterBarPos_default = "Bottom_Left";
		public static String waterBarPos_actual;
	public static String sanityBarPos_default = "Bottom_Right";
		public static String sanityBarPos_actual;
	public static String oxygenBarPos_default = "Bottom_Right";
		public static String oxygenBarPos_actual;
	
	public static int dirtBottleID = 5001;
	public static int saltBottleID = 5002;
	public static int coldBottleID = 5003;
	public static int camelPackID = 5004;
	
	public static int frostBitePotionID = 29;
	public static int dehydratePotionID = 30;
	public static int insanityPotionID = 31;
	
	//Properties
	public static HashMap<Integer,Object[]> armorProperties = new HashMap<Integer,Object[]>();
	public static HashMap<Integer,Object[]> blockProperties = new HashMap<Integer,Object[]>();
	public static HashMap<String,Object[]> livingProperties = new HashMap<String,Object[]>();
    
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
        
        // Comments in Config File handled bu config handler
       // config.addCustomCategoryComment("Armor Properties", "Add properties for custom armors here. Add the armor name to 'Custom Objects' before creating the property here. \nArmour Properties Explained: \nItemID - The ID of the armour item \nAddTempAtNight - How much it adds/takes from the surrounding air temperature at night \nAddTempInShade - How much it adds/takes from the surrounding air temperature in the day but not in direct sunlight \nAddTempInSun - How much it adds/takes from the surrounding air temperature in the day and in direct sunlight \nAirTempMultiplier - How much the outside air temp is multiplied when wearing this armour \nF/C - Set whether the configuration temperatures are in Fahrenheit or Celsius \n Property Format: ItemID, AddTempAtNight, AddTempInShade, AddTempInSun, AirTempMultiplyer, F/C (Farenheit or Celcius)");
       // config.addCustomCategoryComment("Block Properties", "Add properties for custom blocks here. Add the block name to 'Custom Objects' before creating the property here. \nBlockID - The ID of the block you wish to edit \nBlockMeta - The metadata(such as a variation of wool). -1 makes it affect all subtypes in the same way. \nDropID - The ID of the dropped Block/Item \nDropMeta - The metadata of the dropped item. -1 makes it affect all subtypes in the same way.\nDropNum - Number of items/blocks dropped (0 <= tries to drop as block) \nBlockTemp - The temperature of the block (affect surrounding air temperature) \nSanityPerSecond - How much sanity the block takes/adds per second \nAirQualityPerSecond - How much air quality the block takes/adds per second \nCanFallThreshold - How many attached blocks have to be missing for it to have a chance to fall (0-26)\n MustFallThreshold - How many attached blocks have to be missing for it to fall guaranteed (0-26)\n F/C - Set whether the configuration temperatures is in Fahrenheit or Celsius \n Property Format: BlockID, BlockMeta, DropID, DropMeta, DropNum, BlockTemp, SanityPerSecond, AirQualityPerSecond, CanFallThreshold(0-26)>, <MustFallThreshold(0-26)>, Farenheit or Celcius(F/C)> \n Example: S:SnowBlock= 78, -1, 78, -1, 0, 30.0, 0.0, 0.0, 1, 1, C");
		//config.addCustomCategoryComment("EntityLiving Properties", "Add properties for custom living entities here. Add the entity name to 'Custom Objects' before creating the property here. \nProperty Format: <EntityName(E.G. EntityPigZombie)>, <Dehydrate(T/F)>, <BodyTemp?(T/F)>, <AirQuality?(T/F)>");
       // config.addCustomCategoryComment("Custom Objects", "Lists of objects that have envionmental properties");
        
        
        //General Settings
        enablePhysics = config.get(Configuration.CATEGORY_GENERAL, "Enable Physics", true, "Turn physics On/Off").getBoolean(true);
        enableLandslide =  config.get(Configuration.CATEGORY_GENERAL, "Enable Physics", true, "Turn physics On/Off").getBoolean(true);
        enableSanity = config.get(Configuration.CATEGORY_GENERAL, "Allow Sanity", true).getBoolean(true);
        enableHydrate = config.get(Configuration.CATEGORY_GENERAL, "Allow Hydration", true).getBoolean(true);
        enableBodyTemp = config.get(Configuration.CATEGORY_GENERAL, "Allow Body Temperature", true).getBoolean(true);
        enableAirQ = config.get(Configuration.CATEGORY_GENERAL, "Allow Air Quality", true, "True/False to turn Enviromine Trackers for Sanity, Air Quality, Hydration, and Body Temperature.").getBoolean(true);
        trackNonPlayer_actual = config.get(Configuration.CATEGORY_GENERAL, "Track NonPlayer entitys", trackNonPlayer_default , "Track enviromine properties on Non-player entites(mobs & animals)").getBoolean(trackNonPlayer_default);
        
        
        // Gui settings
        String GuiSetCat = "GUI Settings";
        sweatParticals_actual = config.get(GuiSetCat, "Show Sweat Particales",  sweatParticals_default).getBoolean(true);
        insaneParticals_actual =  config.get(GuiSetCat, "Show Insanity Particles", insaneParticals_default).getBoolean(true);
        useFarenheit = config.get(GuiSetCat, "Use Farenheit instead of Celsius", false, "Will display either Farenhit or Celcius on GUI").getBoolean(false);
        heatBarPos_actual = config.get(GuiSetCat, "Position Heat Bat", "Bottom_Left").getString();
    	waterBarPos_actual = config.get(GuiSetCat, "Position Thirst Bar", "Bottom_Left").getString();
    	sanityBarPos_actual = config.get(GuiSetCat, "Position Sanity Bar", "Bottom_Right").getString();
    	oxygenBarPos_actual = config.get(GuiSetCat, "Position Air Quality Bar", "Bottom_Right", "Change position of Enviro Bars. //n Options: Bottom_Left, Bottom_Right, Bottom_Center, Top_Left, Top_Right, Top_Center").getString();
        
        
        //removed
        //saddleRecipe = config.get(Configuration.CATEGORY_GENERAL, "Enable Saddle Recipe", true , "True will allow you to build Saddles for horses.").getBoolean(true);
        
        // Config Item ID's
        dirtBottleID = config.get(Configuration.CATEGORY_ITEM, "Dirty Water Bottle", 5001).getInt(5001);
        saltBottleID = config.get(Configuration.CATEGORY_ITEM, "Salt Water Bottle", 5002).getInt(5002);
        coldBottleID = config.get(Configuration.CATEGORY_ITEM, "Cold Water Bottle", 5003).getInt(5003);
        camelPackID= config.get(Configuration.CATEGORY_ITEM, "Camel Pack", 5004).getInt(5004);
        
        // Potion ID's
        frostBitePotionID = config.get("Potions", "Frostbite", 29).getInt(50);
        dehydratePotionID = config.get("Potions", "Dehydration", 30).getInt(51);
        insanityPotionID = config.get("Potions", "Insanity", 31).getInt(52);
        
        
        
        
        // Using new ConfigHandlers Custom Loading
        /* Load Custom Objects
        loadArmorProperties(config);
        loadBlockProperties(config);
        loadEntityLivingProperties(config);
        */

        config.save();
        
        System.out.println("Successfully loaded EnviroMine configs");
	}

	
	// This Pulls an armor id from "Custom Objects" string array and than grabs that armors data from "Armor Properties" 
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
			System.out.println("Loaded custom properties for entity '" + customLiving[i] + "'");
			addLivingPropToHash(customLiving[i], config.get("Entity Properties", customLiving[i], "").getString());
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
				System.out.println("Invalid property format for custom block: " + name +" - "+ rawData.length +" - ");
				return;
			}
			
			Object[] blockProps = new Object[11];

			Integer metaData = Integer.valueOf((String)rawData[1]);
			
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

			System.out.println("Error Invalid property format for custom block: " + name);
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
