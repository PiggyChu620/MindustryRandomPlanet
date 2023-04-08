package ec620.content;

import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.struct.ObjectMap;
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

        public LakeHandler(Seq<Vec2> v,Block b)
        {
            pos=v.copy();
            block=b;
        }
    }
    public static class LakeGroup
    {
        public Seq<Integer> seq;
        public int min,max;
        public LakeGroup(int x)
        {
            seq=Seq.with(x);
            min=x;
            max=x;
        }
        public boolean add(int n)
        {
            if(n<max+10)
            {
                seq.add(n);
                max=n;
                return true;
            }
            return false;
        }
    }
    /*public static class NameHandler
    {
        public String planetName;
        public ObjectMap<Integer,String> sectorNames;

        public NameHandler()
        {
            sectorNames=new ObjectMap<>();
        }
        public String get(int i)
        {
            return sectorNames.get(i);
        }
        public boolean contains(int i){return sectorNames.containsKey(i);}
        public void add(int i,String s)
        {
            sectorNames.put(i,s);
        }
    }*/
    public static class SectorData
    {
        public String name;
        public float threat;
        public SectorData(String n, float t)
        {
            name=n;
            threat=t;
        }
    }
}
