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
	public static Item iron,aluminum;

	//Alloys/Intermetallic
	public static Item duralumin;
	public static void load(){
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
		duralumin=new Item("duralumin",Color.valueOf("ADB2BD"))
		{{
			hardness=2;
			localizedName="Duralumin";
		}};
	}
	
}










