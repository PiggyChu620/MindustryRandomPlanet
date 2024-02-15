package ec620.content;

import arc.Core;
import arc.Graphics;
import arc.graphics.g2d.Draw;
import arc.math.Mathf;
import arc.math.geom.Geometry;
import arc.math.geom.Vec2;
import arc.scene.ui.layout.Table;
import arc.struct.EnumSet;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.struct.StringMap;
import arc.util.Log;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.content.Fx;
import mindustry.content.Items;
import mindustry.entities.Effect;
import mindustry.game.Schematic;
import mindustry.gen.Building;
import mindustry.gen.Icon;
import mindustry.gen.LaunchPayload;
import mindustry.graphics.Pal;
import mindustry.logic.LAccess;
import mindustry.type.Category;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.type.Sector;
import mindustry.ui.Styles;
import mindustry.world.Block;
import mindustry.world.blocks.campaign.LaunchPad;
import mindustry.world.blocks.defense.turrets.Turret;
import mindustry.world.blocks.storage.CoreBlock;
import mindustry.world.meta.BlockFlag;

import java.util.ArrayList;

import static mindustry.type.ItemStack.with;

public class EC620Classes
{
    public static class LakeHandler
    {
        public Seq<Vec2> pos;
        public Vec2 center;
        public float radius;
        public Block block;

        public LakeHandler(Seq<Vec2> v,Block b)
        {
            pos=v.copy();
            center=new Vec2(pos.sumf(a->a.x)/ pos.size,pos.sumf(a->a.y)/pos.size);
            radius=pos.max(p->Mathf.dst2(p.x,p.y,center.x,center.y)).len()+5;
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

        @Override
        public String toString()
        {
            return name+": "+threat;
        }
    }
    public static class DVec
    {
        public double x,y;
        public DVec()
        {

        }
        public DVec(double x,double y)
        {
            this.x=x;
            this.y=y;
        }
        public void Add(DVec v)
        {
            x+=v.x;
            y+=v.y;
        }
        public void Add(double s,DVec v)
        {
            x+=s*v.x;
            y+=s*v.y;
        }
        public double DistanceFrom(DVec v)
        {
            return Math.sqrt(Math.pow(x-v.x,2)+Math.pow(y-v.y,2));
        }
    }

    public static class LauchPadController extends LaunchPad
    {
        public LauchPadController(String name)
        {
            super(name);
            size=7;
            localizedName="Launch Pad Controller";
            itemCapacity=2147483647;
            launchTime=60;
			requirements(Category.crafting, with(Items.copper, 100));
        }
        public class LaunchPadControllerBuild extends LaunchPadBuild
        {
            public float launchCounter;

            public LaunchPadControllerBuild()
            {
                super();
            }

            @Override
            public void buildConfiguration(Table table)
            {
                if (Vars.state.isCampaign() && !Vars.net.client())
                {
                    table.button(Icon.upOpen, Styles.cleari, () ->
                    {
                        Vars.ui.planet.showSelect(Vars.state.rules.sector, (other) ->
                        {
                            if (Vars.state.isCampaign() && other.planet == Vars.state.rules.sector.planet)
                            {
                                EC620Planets.ec620.sectors.select(s->s.info.wasCaptured).forEach(s->s.info.destination=other);
                                //Vars.state.rules.sector.info.destination = other;
                            }

                        });
                        this.deselect();
                    }).size(40.0F);
                }
                else
                {
                    this.deselect();
                }
            }

            @Override
            public void updateTile() {
                if (Vars.state.isCampaign()) {
                    if ((this.launchCounter += this.edelta()) >= launchTime)
                    {
                        this.consume();
                        launchSound.at(this.x, this.y);
                        LaunchPayload entity = LaunchPayload.create();
                        this.items.each((item, amount) -> {
                            entity.stacks.add(new ItemStack(item, amount));
                        });
                        entity.set(this);
                        entity.lifetime(120.0F);
                        entity.team(this.team);
                        entity.add();
                        Fx.launchPod.at(this);
                        this.items.clear();
                        Effect.shake(3.0F, 3.0F, this);
                        this.launchCounter = 0.0F;
                    }

                }
            }
        }
    }
}
