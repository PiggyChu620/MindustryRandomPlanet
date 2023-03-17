package ec620.content;

import arc.struct.Seq;
import mindustry.content.Blocks;
import mindustry.content.Items;
import mindustry.game.Objectives;
import mindustry.type.Item;
import mindustry.type.ItemSeq;
import mindustry.type.ItemStack;
import mindustry.type.SectorPreset;
import mindustry.world.Block;

public class EC620SectorPresents {
	public static SectorPreset newInvasion;
	
	public static void load(){
		newInvasion = new SectorPreset("new-invasion", EC620Planets.ec620, 0)
		{
			{

				difficulty = 0;
				alwaysUnlocked=true;
				addStartingItems=true;
				sector.info.resources=Seq.with(new Block[]{Blocks.oreCopper});
				Seq<ItemStack> is=new Seq<>();
				is.add(new ItemStack(Items.copper,1000000));
				is.add(new ItemStack(Items.lead,1000000));
				sector.info.items=new ItemSeq(is);
				sector.saveInfo();
			}
		};

	}
}
