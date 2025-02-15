package xiroc.doomcall.util;

import cpw.mods.fml.common.Loader;

public class ModHandler {
	
	public static boolean thermalExpansion = false;
	public static boolean thermalFoundation = false;
	public static boolean industrialcraft2 = false;
	public static boolean immersiveEngineering = false;
	public static boolean buildCraft = false;
	public static boolean minefactoryReloaded = false;
	
	public static void load(){
		thermalFoundation = Loader.isModLoaded("ThermalFoundation");
		thermalExpansion = Loader.isModLoaded("ThermalExpansion");
		industrialcraft2 = Loader.isModLoaded("IC2");
		immersiveEngineering = Loader.isModLoaded("ImmersiveEngineering");
		buildCraft = Loader.isModLoaded("BuildCraft");
		minefactoryReloaded = Loader.isModLoaded("MineFactoryReloaded");
	}


}
