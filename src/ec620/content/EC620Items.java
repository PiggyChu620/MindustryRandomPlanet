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
	
	public static Item iron;
	public static void load(){
		iron = new Item("iron",Color.valueOf("A19D94")){{
			hardness=3;
			localizedName="Iron";
		}};

	}
	
}










