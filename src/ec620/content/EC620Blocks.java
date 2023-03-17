package ec620.content;

import arc.Core;
import arc.graphics.Blending;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.Rand;
import arc.util.Eachable;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.content.*;
import mindustry.entities.Effect;
import mindustry.entities.Lightning;
import mindustry.entities.UnitSorts;
import mindustry.entities.Units;
import mindustry.entities.bullet.BasicBulletType;
import mindustry.entities.bullet.BulletType;
import mindustry.entities.part.DrawPart;
import mindustry.entities.part.RegionPart;
import mindustry.entities.pattern.*;
import mindustry.entities.units.BuildPlan;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.gen.Bullet;
import mindustry.gen.Hitboxc;
import mindustry.gen.Sounds;
import mindustry.graphics.CacheLayer;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.type.LiquidStack;
import mindustry.world.Block;
import mindustry.world.blocks.defense.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.blocks.distribution.*;
import mindustry.world.blocks.environment.*;
import mindustry.world.blocks.liquid.Conduit;
import mindustry.world.blocks.liquid.LiquidBridge;
import mindustry.world.blocks.liquid.LiquidRouter;
import mindustry.world.blocks.power.Battery;
import mindustry.world.blocks.power.ConsumeGenerator;
import mindustry.world.blocks.power.PowerNode;
import mindustry.world.blocks.production.AttributeCrafter;
import mindustry.world.blocks.production.GenericCrafter;
import mindustry.world.blocks.production.SolidPump;
import mindustry.world.blocks.sandbox.ItemSource;
import mindustry.world.blocks.sandbox.LiquidSource;
import mindustry.world.blocks.sandbox.PowerVoid;
import mindustry.world.blocks.storage.StorageBlock;
import mindustry.world.consumers.ConsumeCoolant;
import mindustry.world.consumers.ConsumeLiquid;
import mindustry.world.draw.*;
import mindustry.world.meta.*;

import static arc.graphics.g2d.Draw.color;
import static arc.graphics.g2d.Lines.lineAngle;
import static arc.math.Angles.randLenVectors;
import static mindustry.Vars.*;
import static mindustry.type.ItemStack.with;

public class EC620Blocks {

	//Load Mod Factories


	//Ores
	public static Block oreIron,oreAluminum;

	//Crafters
	public static Block duraluminFactory;

	//Walls
	//public static Block copperWallHuge;

	public static void load()
	{

		//region Ores
		oreIron = new OreBlock("iron-ore")
		{{
			variants = 3;
			oreThreshold = 0.95F;
			oreScale = 20.380953F;
			itemDrop=EC620Items.iron;
			oreDefault=true;
			localizedName="Iron";
		}};
		oreAluminum=new OreBlock("aluminum-ore")
		{{
			variants = 3;
			oreThreshold = 0.95F;
			oreScale = 20.380953F;
			itemDrop=EC620Items.aluminum;
			oreDefault=true;
			localizedName="Aluminum";
		}};
		//endregion

		//region Factories
		duraluminFactory = new GenericCrafter("duralumin-factory"){{
			requirements(Category.crafting, with(Items.copper, 5, EC620Items.aluminum,95));
			craftEffect = Fx.smeltsmoke;
			outputItem = new ItemStack(EC620Items.duralumin, 1);
			craftTime = 40f;
			size = 2;
			hasPower = true;
			hasLiquids = false;
			drawer = new DrawMulti(new DrawDefault(), new DrawFlame(Color.valueOf("ffef99")));
			ambientSound = Sounds.smelter;
			ambientSoundVolume = 0.07f;
			localizedName="Duralumin Factory";

			consumeItems(with(Items.copper, 1, EC620Items.aluminum, 2));
			consumePower(0.50f);
		}};
		//endregion

		//region Walls

//		copperWallHuge = new Wall("copper-wall-huge"){{
//			requirements(Category.defense, ItemStack.mult(Blocks.copperWall.requirements, 9));
//			health = 320*9;
//			size = 3;
//			envDisabled |= Env.scorching;
//			localizedName="Huge Copper Wall";
//		}};
		//endregion
	}


}
