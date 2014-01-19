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
		
		System.out.println("Loaded " + EM_Settings.armorProperties.size() + " armor properties");
		System.out.println("Loaded " + EM_Settings.blockProperties.size() + " block properties");
		System.out.println("Loaded " + EM_Settings.livingProperties.size() + " entity properties");
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
			}
			
			System.out.println("Found " + catagory.size() + " custom properties in " + customFiles.getName());
			
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
						LoadBlockProperty(config, catagory.get(x));
					} else if(parent.equals(armorCat))
					{
						LoadArmorProperty(config, catagory.get(x));
					} else if(parent.equals(itemsCat))
					{
						System.out.println("Loading item " + child + " (NOT YET SUPPORTED)");
					} else if(parent.equals(entityCat))
					{
						LoadLivingProperty(config, catagory.get(x));
					} else
					{
						System.out.println("Failed to load object " + CurCat);
					}
					
				}
			}
			
			config.save();
		}
	}
	
	private static void LoadBlockProperty(Configuration config, String catagory)
	{
		config.addCustomCategoryComment(catagory, "");
		int id = config.get(catagory, "1.ID", 0).getInt(0);
		boolean hasPhys = config.get(catagory, "2.EnablePhysics", false).getBoolean(false);
		int metaData = config.get(catagory, "3.MetaID", 0).getInt(0);
		int dropID = config.get(catagory, "4.DropID", 0).getInt(0);
		int dropMeta = config.get(catagory, "5.DropMetaID", 0).getInt(0);
		int dropNum = config.get(catagory, "6.DropNumber", 0).getInt(0);
		boolean enableTemp = config.get(catagory, "7.EnableTemperature", false).getBoolean(false);
		float temperature = (float)config.get(catagory, "8.Temprature", 0.00).getDouble(0.00);
		float airQuality = (float)config.get(catagory, "9.Air Quality", 0.00).getDouble(0.00);
		float sanity = (float)config.get(catagory, "10.AirQuality", 0.00).getDouble(0.00);
		String stability = config.get(catagory, "11.Stability", "loose").getString();
		boolean holdOther = config.get(catagory, "12.Holds Other Blocks", false).getBoolean(false);
		
		// Get Stability Options
		int minFall = 99;
		int maxFall = 99;
		int supportDist = 5;
		
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
			default:
				minFall = 99;
				maxFall = 99;
				supportDist = 5;
				hasPhys = false;
				break;
		}
		
		BlockProperties entry = new BlockProperties(id, metaData, hasPhys, minFall, maxFall, supportDist, dropID, dropMeta, dropNum, enableTemp, temperature, airQuality, sanity, holdOther);
		
		if(metaData < 0)
		{
			EM_Settings.blockProperties.put("" + id + "," + metaData, entry);
		} else
		{
			EM_Settings.blockProperties.put("" + id, entry);
		}
	}
	
	private static void LoadArmorProperty(Configuration config, String catagory)
	{
		config.addCustomCategoryComment(catagory, "");
		int id = config.get(catagory, "1.ID", 0).getInt(0);
		double nightTemp = config.get(catagory, "2.nightTemp", 0.00).getDouble(0);
		double shadeTemp = config.get(catagory, "3.shadeTemp", 0.00).getDouble(0);
		double sunTemp = config.get(catagory, "4.sunTemp", -1.00).getDouble(0);
		double nightMult = config.get(catagory, "5.nightMult", 1).getDouble(1);
		double shadeMult = config.get(catagory, "6.shadeMult", 1).getDouble(1);
		double sunMult = config.get(catagory, "7.sunMult", 1).getDouble(1);
		
		ArmorProperties entry = new ArmorProperties(id, (float)nightTemp, (float)shadeTemp, (float)sunTemp, (float)nightMult, (float)shadeMult, (float)sunMult);
		EM_Settings.armorProperties.put(id, entry);
	}
	
	private static void LoadLivingProperty(Configuration config, String catagory)
	{
		config.addCustomCategoryComment(catagory, "");
		String name = config.get(catagory, "1.Entity Name", "").getString();
		Boolean track = config.get(catagory, "2.Enable EnviroTracker", true).getBoolean(true);
		Boolean dehydration = config.get(catagory, "3.Enable Dehydration", true).getBoolean(true);
		Boolean bodyTemp = config.get(catagory, "4.Enable BodyTemp", true).getBoolean(true);
		Boolean airQ = config.get(catagory, "5.Enable Air Quility", true).getBoolean(true);
		Boolean immuneToFrost = config.get(catagory, "6.ImmuneToFrost", false).getBoolean(false);
		
		EntityProperties entry = new EntityProperties(name, track, dehydration, bodyTemp, airQ, immuneToFrost);
		EM_Settings.livingProperties.put(name, entry);
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
		
		ArmorDefaultSave(ItemArmor.helmetLeather.itemID	, 2.5F, 2.5F, 2.5F, 1.0F, 1.0F, 1.0F);
		ArmorDefaultSave(ItemArmor.plateLeather.itemID	, 2.5F, 2.5F, 2.5F, 1.0F, 1.0F, 1.0F);
		ArmorDefaultSave(ItemArmor.legsLeather.itemID	, 2.5F, 2.5F, 2.5F, 1.0F, 1.0F, 1.0F);
		ArmorDefaultSave(ItemArmor.bootsLeather.itemID	, 2.5F, 2.5F, 2.5F, 1.0F, 1.0F, 1.0F);
		
		ArmorDefaultSave(ItemArmor.helmetIron.itemID	, -1.0F, 0.0F, 2.5F, 1.0F, 1.0F, 1.1F);
		ArmorDefaultSave(ItemArmor.plateIron.itemID		, -1.0F, 0.0F, 2.5F, 1.0F, 1.0F, 1.1F);
		ArmorDefaultSave(ItemArmor.legsIron.itemID		, -1.0F, 0.0F, 2.5F, 1.0F, 1.0F, 1.1F);
		ArmorDefaultSave(ItemArmor.bootsIron.itemID		, -1.0F, 0.0F, 2.5F, 1.0F, 1.0F, 1.1F);
		
		ArmorDefaultSave(ItemArmor.helmetGold.itemID	, 0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.2F);
		ArmorDefaultSave(ItemArmor.plateGold.itemID		, 0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.2F);
		ArmorDefaultSave(ItemArmor.legsGold.itemID		, 0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.2F);
		ArmorDefaultSave(ItemArmor.bootsGold.itemID		, 0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.2F);
		
		ArmorDefaultSave(ItemArmor.helmetDiamond.itemID	, 0.0F, 0.0F, 0.0F, 0.9F, 0.9F, 0.9F);
		ArmorDefaultSave(ItemArmor.plateDiamond.itemID	, 0.0F, 0.0F, 0.0F, 0.9F, 0.9F, 0.9F);
		ArmorDefaultSave(ItemArmor.legsDiamond.itemID	, 0.0F, 0.0F, 0.0F, 0.9F, 0.9F, 0.9F);
		ArmorDefaultSave(ItemArmor.bootsDiamond.itemID	, 0.0F, 0.0F, 0.0F, 0.9F, 0.9F, 0.9F);
		
		//custom.save();	
	}
	
	private static void ArmorDefaultSave(int id, float nightTemp, float shadeTemp, float sunTemp, float nightMult, float shadeMult, float sunMult)
	{
		ArmorProperties entry = new ArmorProperties(id, nightTemp, shadeTemp, sunTemp, nightMult, shadeMult, sunMult);
		EM_Settings.armorProperties.put(id, entry);
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
