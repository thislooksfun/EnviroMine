package enviromine.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import enviromine.trackers.ArmorProperties;
import enviromine.trackers.BlockProperties;
import enviromine.trackers.EntityProperties;
import net.minecraft.item.ItemArmor;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.Property;

public class EM_ConfigHandler
{
	
	// Dirs for Custom Files
	public static String configPath = "config/enviromine/";
	static String dataPath = configPath + "EnviroProperties/";
	static String customPath = configPath + "CustomObjects/";
	
	// Categories for Custom Objects
	static String armorCat = "armor";
	static String blockCat = "blocks";
	static String entityCat = "entity";
	static String itemsCat = "items";
	//static String foodCat = "food";
	
	public static void initConfig()
	{
		// Check for Data Directory 
		CheckDir(new File(dataPath));
		CheckDir(new File(customPath));
		
		//CheckFile(new File(configPath + "Help_File_Custom.txt"));
		
		// Now load Files from "Custom Objects"
		File[] customFiles = GetFileList(customPath);
		for(int i = 0; i < customFiles.length; i++)
		{
			System.out.println("Loading " + customFiles[i].getName());
			LoadCustomObjects(customFiles[i]);
		}
		
		// load defaults
		loadDefaultArmorProperties();
		
		// Load Main Config File And this will go though changes
		File configFile = new File(configPath + "EnviroMine.cfg");
		System.out.println("Attempting to load config: " + configFile.getAbsolutePath());
		EM_Settings.loadGeneralConfig(configFile);
	}
	
	//#######################################
	//#          Get File List              #                 
	//#This Grabs Directory List for Custom #
	//#######################################
	public static File[] GetFileList(String path)
	{
		
		// Will be used Auto Load Custom Objects from ??? Dir 
		File f = new File(path);
		File[] list = f.listFiles();
		
		System.out.println("Found " + list.length + " custom configs");
		
		return list;
	}
	
	private static boolean isDatFile(String fileName)
	{
		//Matcher
		String patternString = "(.*\\.dat$)";
		
		Pattern pattern;
		Matcher matcher;
		// Make Sure its a .Dat File
		pattern = Pattern.compile(patternString);
		matcher = pattern.matcher(fileName);
		
		return matcher.matches();
	}
	
	//###################################
	//#           Check Dir             #                 
	//#  Checks for, or makes Directory #
	//###################################	
	public static void CheckDir(File Dir)
	{
		boolean dirFlag = false;
		
		// create File object
		
		try
		{
			dirFlag = Dir.mkdirs();
		} catch(SecurityException Se)
		{
			System.out.println("Error while creating config directory:\n" + Se);
		}
		
		if(!dirFlag)
		{
			System.out.println("Failed to create config directory!");
		}
	}
	
	//####################################
	//#   Load Custom Objects            #
	//# Used to Load Custom Blocks,Armor #                              
	//#   Entitys, & Items from Mods     #
	//####################################
	public static void LoadCustomObjects(File customFiles)
	{
		boolean datFile = isDatFile(customFiles.getName());
		
		// Check to make sure this is a Data File Before Editing
		if(datFile == true)
		{
			Configuration config;
			try
			{
				config = new Configuration(customFiles);
			} catch(NullPointerException e)
			{
				e.printStackTrace();
				System.out.println("FAILED TO LOAD CUSTOM CONFIG: " + customFiles.getName() + "\nNEW SETTINGS WILL BE IGNORED!");
				return;
			} catch(StringIndexOutOfBoundsException e)
			{
				e.printStackTrace();
				System.out.println("FAILED TO LOAD CUSTOM CONFIG: " + customFiles.getName() + "\nNEW SETTINGS WILL BE IGNORED!");
				return;
			}
			
			config.load();
			
			// Load Default Categories
			config.addCustomCategoryComment(armorCat, "Custom armor properties");
			config.addCustomCategoryComment(blockCat, "Custom block properties");
			config.addCustomCategoryComment(entityCat, "Custom entity properties");
			
			// 	Grab all Categories in File
			List<String> catagory = new ArrayList<String>();
			Set<String> nameList = config.getCategoryNames();
			Iterator<String> nameListData = nameList.iterator();
			
			// add Categories to a List 
			for(int x = 0; nameListData.hasNext(); x++)
			{
				catagory.add(nameListData.next());
				
				System.out.println("Searching Config for Found: " + catagory.get(x).toString());
			}
			
			// Now Read/Save Each Category And Add into Proper Hash Maps
			
			for(int x = 0; x <= (catagory.size() - 1); x++)
			{
				String CurCat = catagory.get(x).toString();
				
				if(!((String)CurCat).isEmpty() && ((String)CurCat).contains(Configuration.CATEGORY_SPLITTER))
				{
					String[] CatSplit = CurCat.toString().split("\\" + Configuration.CATEGORY_SPLITTER);
					String parent = (String)CatSplit[0];
					String child = CatSplit[1];
					
					if(parent.equals(blockCat))
					{
						System.out.println("Loading block " + child);
						
						LoadBlockProperty(config, catagory.get(x));
					} else if(parent.equals(armorCat))
					{
						System.out.println("Loading armor " + child);
						LoadArmorProperty(config, catagory.get(x));
					} else if(parent.equals(itemsCat))
					{
						System.out.println("Loading item " + child);
					} else if(parent.equals(entityCat))
					{
						LoadLivingProperty(config, catagory.get(x));
						System.out.println("Loading entity " + child);
					} else
					{
						System.out.println("Failed to load object " + CurCat);
					}
					
				}
			}
			
			config.save();
		}
	}
	
	private static void LoadBlockProperty(Configuration config, Object catagory)
	{
		config.addCustomCategoryComment((String)catagory, "");
		int id = config.get((String)catagory, "1.ID", 0).getInt(0);
		int metaData = config.get((String)catagory, "2.MetaID", 0).getInt(0);
		int dropID = config.get((String)catagory, "3.DropID", 0).getInt(0);
		int dropMeta = config.get((String)catagory, "4.DropMetaID", 0).getInt(0);
		double temprature = config.get((String)catagory, "5.Temprature", 0.00).getDouble(0.00);
		double airQuality = config.get((String)catagory, "6.AirQuality", 0.00).getDouble(0.00);
		String stability = config.get((String)catagory, "7.Stability", "loose").getString();
		
		// Change Temperatures to Floats
		float tempFloat = (float)temprature;
		float airQFloat = (float)airQuality;
		
		// Get Stability Options
		int minFall = 10;
		int maxFall = 15;
		int supportDist = 1;
		boolean hasPhys = true;
		
		switch(stability)
		{
			case "sand":
				minFall = -1;
				maxFall = -1;
				break;
			case "loose":
				minFall = 10;
				maxFall = 15;
				supportDist = 1;
				break;
			case "average":
				minFall = 15;
				maxFall = 22;
				supportDist = 3;
				break;
			case "strong":
				minFall = 22;
				maxFall = 25;
				supportDist = 5;
				break;
			case "off":
				hasPhys = false;
				break;
		}
		
		//INSERT NEW PROPERTY INSTANCE CODE
	}
	
	private static void LoadArmorProperty(Configuration config, Object catagory)
	{
		config.addCustomCategoryComment((String)catagory, "");
		int id = config.get((String)catagory, "1.ID", 0).getInt(0);
		double nightTemp = config.get((String)catagory, "2.nightTemp", 0.00).getDouble(0);
		double shadeTemp = config.get((String)catagory, "3.shadeTemp", 0.00).getDouble(0);
		double sunTemp = config.get((String)catagory, "4.sunTemp", -1.00).getDouble(0);
		double nightMult = config.get((String)catagory, "5.nightMult", 1).getDouble(1);
		double shadeMult = config.get((String)catagory, "6.shadeMult", 1).getDouble(1);
		double sunMult = config.get((String)catagory, "7.sunMult", 1).getDouble(1);
		
		//INSERT NEW PROPERTY INSTANCE CODE
	}
	
	private static void LoadLivingProperty(Configuration config, Object catagory)
	{
		config.addCustomCategoryComment((String)catagory, "");
		String name = config.get((String)catagory, "1.Entity Name", "").toString();
		Boolean dehydration = config.get((String)catagory, "2.Enable Dehydration", true).getBoolean(true);
		Boolean bodyTemp = config.get((String)catagory, "3.Enable BodyTemp", true).getBoolean(true);
		Boolean airQ = config.get((String)catagory, "4.Enable Air Quility", true).getBoolean(true);
		Boolean immuneToFrost = config.get((String)catagory, "5.ImmuneToFrost", false).getBoolean(false);
		
		//INSERT NEW PROPERTY INSTANCE CODE
	}
	
	// RIGHT NOW I AM JUST LOADING DEFAULT ARMOR INTO HASH MAPS
	// IF SOME CUSTOMIZED ARMOR>> THAN IT OVERIDES THIS FUNCTION
	public static void loadDefaultArmorProperties()
	{
		/*
		File customFile = new File(customPath + "ArmorDefaults.dat");
		
		Configuration custom;
		try	
			{ custom = new Configuration(customFile);} 
		catch(NullPointerException e)
			{e.printStackTrace();System.out.println("FAILED TO LOAD CONFIGS!\nBACKUP SETTINGS ARE NOW IN EFFECT!");	return;	} 
		catch(StringIndexOutOfBoundsException e)	
			{	e.printStackTrace();System.out.println("FAILED TO LOAD CONFIGS!\nBACKUP SETTINGS ARE NOW IN EFFECT!");	return;	}System.out.println("Loading EnviroMine Config: " + customFile.getAbsolutePath());
		custom.load();

		// Load Default Categories
		custom.addCustomCategoryComment(armorCat, "Add Custom Armor");
		custom.addCustomCategoryComment(blockCat, "Add Custom Blocks");
		custom.addCustomCategoryComment(entityCat, "Custom Entities");		
		*/
		
		ArmorDefaultSave("Leather Helmet", ItemArmor.helmetLeather.itemID, 2.0, 2.0, 1.0, 1);
		ArmorDefaultSave("Leather Chest", ItemArmor.plateLeather.itemID, 2.0, 2.0, 1.0, 1);
		ArmorDefaultSave("Leather Legs", ItemArmor.legsLeather.itemID, 2.0, 2.0, 1.0, 1);
		ArmorDefaultSave("Leather Boots", ItemArmor.bootsLeather.itemID, 2.0, 2.0, 1.0, 1);
		
		ArmorDefaultSave("Iron Helmet", ItemArmor.helmetIron.itemID, -1.0, 0.0, 5.0, 1.1);
		ArmorDefaultSave("Iron Chest", ItemArmor.plateIron.itemID, -1.0, 0.0, 5.0, 1.1);
		ArmorDefaultSave("Iron Legs", ItemArmor.legsIron.itemID, -1.0, 0.0, 5.0, 1.1);
		ArmorDefaultSave("Iron Boots", ItemArmor.bootsIron.itemID, -1.0, 0.0, 5.0, 1.1);
		
		ArmorDefaultSave("Gold Helmet", ItemArmor.helmetGold.itemID, 0.0, 0.0, 3.0, 1.1);
		ArmorDefaultSave("Gold Chest", ItemArmor.plateGold.itemID, 0.0, 0.0, 3.0, 1.1);
		ArmorDefaultSave("Gold Legs", ItemArmor.legsGold.itemID, 0.0, 0.0, 3.0, 1.1);
		ArmorDefaultSave("Gold Boots", ItemArmor.bootsGold.itemID, 0.0, 0.0, 3.0, 1.1);
		
		ArmorDefaultSave("Diamond Helmet", ItemArmor.helmetDiamond.itemID, 0.0, 0.0, 3.0, 1.1);
		ArmorDefaultSave("Diamond Chest", ItemArmor.plateDiamond.itemID, 0.0, 0.0, 3.0, 1.1);
		ArmorDefaultSave("Diamond Legs", ItemArmor.legsDiamond.itemID, 0.0, 0.0, 3.0, 1.1);
		ArmorDefaultSave("Diamond Boots", ItemArmor.bootsDiamond.itemID, 0.0, 0.0, 3.0, 1.1);
		
		//custom.save();	
	}
	
	private static void ArmorDefaultSave(String name, int id, double nightFloat, double shadeFloat, double sunFloat, double sunMult)
	{
		//INSERT NEW PROPERTY INSTANCE CODE
	}
	
	//Currently not used
	private static float convertToFarenheit(float num)
	{
		return((num * (9 / 5)) + 32F);
	}
	
	private static float convertToCelcius(float num)
	{
		return((num - 32F) * (5 / 9));
	}
	
} // End of Page
