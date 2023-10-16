package ec620.content;

import arc.graphics.Color;
import mindustry.content.*;
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


	public static void load()
	{
		//region Unlock hidden items
		Items.fissileMatter.hidden=false;
		Liquids.gallium.hidden=false;
		//endregion
		iron = new Item("iron",Color.valueOf("A19D94"))
		{{
			hardness=3;
			localizedName="Iron";
			hidden=true;
		}};
		aluminum=new Item("aluminum",Color.valueOf("ADB2BD"))
		{{
				hardness=2;
				localizedName="Aluminum";
			hidden=true;
		}};
		zinc=new Item("zinc",Color.valueOf("92898A"))
		{{
			hardness=2;
			localizedName="Zinc";
			hidden=true;
		}};

//		cupb = new Item("cupb",Color.valueOf("B87333"))
//		{{	//4 Coppers + 1 Lead
//			hardness=3;
//			localizedName="CuPb";
//		}};

		//region Alloys
		springCopper=new Item("spring-copper")
		{{
			localizedName="Spring Copper";
			description="https://en.wikipedia.org/wiki/Beryllium_copper";
			hidden=true;
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
			hidden=true;
		}};
		betaAlloy=new Item("beta-alloy")
		{{	//Titanium + Silicon
			localizedName="Beta Alloy";
			description="https://en.wikipedia.org/wiki/Titanium_alloys";
			hidden=true;
		}};
		copperNitride=new Item("copper-nitride")
		{{	//3 Copper + Nitrogen, possible usages for electronic turrets or fireworks
			localizedName="Copper Nitride";
			hidden=true;

		}};
		duralumin=new Item("duralumin",Color.valueOf("ADB2BD"))
		{{
			hardness=2;
			localizedName="Duralumin";
			hidden=true;
		}};
		brass=new Item("brass",Color.valueOf("E1C16E"))
		{{	//3 Coppers + 1 Zinc
			hardness=3;
			localizedName="Brass";
			hidden=true;
		}};
		bronze=new Item("bronze")
		{{
			//Copper + Tin
			localizedName="Bronze";
			hidden=true;
		}};
		cupronickel=new Item("cupronickel")
		{{
			//7 Copper + 3 Nickel
			localizedName="Cupronickel";
			hidden=true;
		}};
		constantan=new Item("constantan")
		{{
			// 1 Copper + 1 Nickel
			localizedName="Constantan";
			hidden=true;
		}};
		manganin=new Item("manganin")
		{{
			//Copper + Manganese + Nickel
			localizedName="Manganin";
			hidden=true;
		}};
		shibuichi=new Item("shibuichi")
		{{
			//3 Copper + 1 Silver
			localizedName="Shibuichi";
			hidden=true;
		}};
		tumbaga=new Item("tumbaga")
		{{
			//Copper + Gold
			localizedName="Tumbaga";
			hidden=true;
		}};
		yAlloy=new Item("y-alloy")
		{{
			//Copper + Nickel + Aluminum
			localizedName="Y Alloy";
			hidden=true;
		}};
		copperOxide=new Item("copper-oxide")
		{{
			//Copper + Ozone: Possible usages for new solar panel
			localizedName="Copper Oxide";
			hidden=true;
		}};
		phosphorBronze=new Item("phosphor-bronze")
		{{
			//Copper + Tin + Phosphorus
			localizedName="Phosphor Bronze";
			hidden=true;
		}};
		dutchMetal=new Item("dutch-metal")
		{{
			//Copper + Zinc (+Tin)
			localizedName="Dutch Metal";
			hidden=true;
		}};
		devardaAlloy=new Item("devarda-alloy")
		{{
			//Copper + Aluminum + Zinc
			localizedName="Devarda's Alloy";
			hidden=true;
		}};
		brassicaNapus=new Item("brassica-napus")
		{{
			//Copper + Iron (+Phosphorus?)
			localizedName="Brassica Napus";
			hidden=true;
		}};
		tungstenCopper=new Item("tungsten-copper")
		{{
			//Copper + Tungsten
			localizedName="Tungsten Copper";
			hidden=true;
		}};
		//endregion
	}
	
}










