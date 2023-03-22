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
	public static Item iron,aluminum,zinc,nickel,calcium,magnesium,potassium,phosphorus,tin;

	//Alloys/Intermetallic
	public static Item duralumin,brass,bronze,cupronickel,constantan,manganin,shibuichi,tumbaga,yAlloy;
	public static Item springCopper,cuTi,cuPb,alphaAlloy,betaAlloy,copperNitride,copperOxide,phosphorBronze;
	public static Item dutchMetal,devardaAlloy,brassicaNapus,tungstenCopper;
	//Ices
	public static Item ice,slagIce,cryofluidIce,arkyciteIce,neoplasmIce,oxygenIce,wax,galliumIce,hydrogenIce,nitrogenIce,cyanogenIce;

	public static void load()
	{
		iron = new Item("iron",Color.valueOf("A19D94"))
		{{
			hardness=3;
			localizedName="Iron";
		}};
		aluminum=new Item("aluminum",Color.valueOf("ADB2BD"))
		{{
				hardness=2;
				localizedName="Aluminum";
		}};
		zinc=new Item("zinc",Color.valueOf("92898A"))
		{{
			hardness=2;
			localizedName="Zinc";
		}};

//		cupb = new Item("cupb",Color.valueOf("B87333"))
//		{{	//4 Coppers + 1 Lead
//			hardness=3;
//			localizedName="CuPb";
//		}};
		//region Ices
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
		//endregion
		//region Alloys
		springCopper=new Item("spring-copper")
		{{
			localizedName="Spring Copper";
			description="https://en.wikipedia.org/wiki/Beryllium_copper";
		}};
//		cuTi=new Item("cuTi",Color.valueOf("AD9379"))
//		{{
//			localizedName="CuTi";
//			description="https://www.jx-nmm.com/english/products/alloy/copper/02ticu/";
//		}};
		alphaAlloy=new Item("alpha-alloy")
		{{	//Titanium + Ozone
			localizedName="Alpha Alloy";
			description="https://en.wikipedia.org/wiki/Titanium_alloys";
		}};
		betaAlloy=new Item("beta-alloy")
		{{	//Titanium + Silicon
			localizedName="Beta Alloy";
			description="https://en.wikipedia.org/wiki/Titanium_alloys";
		}};
		copperNitride=new Item("copper-nitride")
		{{	//3 Copper + Nitrogen, possible usages for electronic turrets or fireworks
			localizedName="Copper Nitride";

		}};
		duralumin=new Item("duralumin",Color.valueOf("ADB2BD"))
		{{
			hardness=2;
			localizedName="Duralumin";
		}};
		brass=new Item("brass",Color.valueOf("E1C16E"))
		{{	//3 Coppers + 1 Zinc
			hardness=3;
			localizedName="Brass";
		}};
		bronze=new Item("bronze")
		{{
			//Copper + Tin
			localizedName="Bronze";
		}};
		cupronickel=new Item("cupronickel")
		{{
			//7 Copper + 3 Nickel
			localizedName="Cupronickel";
		}};
		constantan=new Item("constantan")
		{{
			// 1 Copper + 1 Nickel
			localizedName="Constantan";
		}};
		manganin=new Item("manganin")
		{{
			//Copper + Manganese + Nickel
			localizedName="Manganin";
		}};
		shibuichi=new Item("shibuichi")
		{{
			//3 Copper + 1 Silver
			localizedName="Shibuichi";
		}};
		tumbaga=new Item("tumbaga")
		{{
			//Copper + Gold
			localizedName="Tumbaga";
		}};
		yAlloy=new Item("y-alloy")
		{{
			//Copper + Nickel + Aluminum
			localizedName="Y Alloy";
		}};
		copperOxide=new Item("copper-oxide")
		{{
			//Copper + Ozone: Possible usages for new solar panel
			localizedName="Copper Oxide";
		}};
		phosphorBronze=new Item("phosphor-bronze")
		{{
			//Copper + Tin + Phosphorus
			localizedName="Phosphor Bronze";
		}};
		dutchMetal=new Item("dutch-metal")
		{{
			//Copper + Zinc (+Tin)
			localizedName="Dutch Metal";
		}};
		devardaAlloy=new Item("devarda-alloy")
		{{
			//Copper + Aluminum + Zinc
			localizedName="Devarda's Alloy";
		}};
		brassicaNapus=new Item("brassica-napus")
		{{
			//Copper + Iron (+Phosphorus?)
			localizedName="Brassica Napus";
		}};
		tungstenCopper=new Item("tungsten-copper")
		{{
			//Copper + Tungsten
			localizedName="Tungsten Copper";
		}};
		//endregion
	}
	
}










