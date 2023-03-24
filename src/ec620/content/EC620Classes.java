package ec620.content;

import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.struct.Seq;
import arc.struct.StringMap;
import arc.util.Log;
import mindustry.content.Blocks;
import mindustry.game.Schematic;
import mindustry.world.Block;
import mindustry.world.blocks.defense.turrets.Turret;
import mindustry.world.blocks.storage.CoreBlock;

import java.util.ArrayList;

public class EC620Classes
{
    public static class LakeHandler
    {
        public Seq<Vec2> pos;
        public Block block;
        public LakeHandler(float x,float y,Block b)
        {
            pos=Seq.with(new Vec2(x,y));
            block=b;
        }
        public void Add(float x,float y)
        {
            pos.add(new Vec2(x,y));
        }
        public boolean Within(float x,float y)
        {
            for(Vec2 v:pos)
            {
                if(Math.pow(v.x-x,2)+Math.pow(v.y-y,2)<300) return true;
            }
            return false;
        }

    }
    public class NameHandler
    {
        public String planetName;
        public ArrayList<String> sectorNames;

        public NameHandler(){}
        public String Get(int i)
        {
            return sectorNames.get(i);
        }
    }
}
