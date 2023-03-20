package ec620.content;

import arc.graphics.Color;
import mindustry.content.Blocks;
import mindustry.content.Items;
import mindustry.content.SerpuloTechTree;
import mindustry.content.TechTree;
import mindustry.type.Item;
import mindustry.world.blocks.storage.CoreBlock;

public class EC620Items {
	
	//Load Mod Items

	//Raw Materials
	//public static Item iron,aluminum,zinc;

	//Alloys/Intermetallic
	//public static Item duralumin,brass;
	public static Item ice,slagIce,cryofluidIce,arkyciteIce,neoplasmIce,oxygenIce,wax,galliumIce,hydrogenIce,nitrogenIce,cyanogenIce;
	public static Item cupb;
	public static void load()
	{
//		iron = new Item("iron",Color.valueOf("A19D94"))
//		{{
//			hardness=3;
//			localizedName="Iron";
//		}};
//		aluminum=new Item("aluminum",Color.valueOf("ADB2BD"))
//		{{
//				hardness=2;
//				localizedName="Aluminum";
//		}};
//		zinc=new Item("zinc",Color.valueOf("92898A"))
//		{{
//			hardness=2;
//			localizedName="Zinc";
//		}};
//		duralumin=new Item("duralumin",Color.valueOf("ADB2BD"))
//		{{
//			hardness=2;
//			localizedName="Duralumin";
//		}};
//		brass=new Item("brass",Color.valueOf("E1C16E"))
//		{{	//3 Coppers + 1 Zinc
//			hardness=3;
//			localizedName="Brass";
//		}};
//		cupb = new Item("cupb",Color.valueOf("B87333"))
//		{{	//4 Coppers + 1 Lead
//			hardness=3;
//			localizedName="CuPb";
//		}};
		ice=new Item("ice",Color.valueOf("368BC1"))
		{{
			localizedName="Ice";

		}};
		slagIce=new Item("slag-ice",Color.valueOf("ffa166"))
		{{
			localizedName="Slag Ice";

		}};
		wax=new Item("wax",Color.valueOf("000000"))
		{{
			localizedName="Wax";
			flammability = 1.2f;
			explosiveness = 1.2f;
		}};
		cryofluidIce=new Item("cryofluid-ice",Color.valueOf("6ecdec"))
		{{
			localizedName="Cryofliud Ice";

		}};
		neoplasmIce=new Item("neoplasm-ice",Color.valueOf("c33e2b"))
		{{
			localizedName="Neoplasm Ice";
		}};
		arkyciteIce=new Item("arkycite-ice",Color.valueOf("84a94b"))
		{{
			localizedName="Arkycite Ice";
			flammability = 0.4f;
		}};

	}
	
}










