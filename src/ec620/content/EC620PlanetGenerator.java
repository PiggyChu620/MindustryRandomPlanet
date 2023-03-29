package ec620.content;

import arc.Core;
import arc.graphics.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import arc.util.noise.*;
import ec620.EC620JavaMod;
import mindustry.Vars;
import mindustry.ai.*;
import mindustry.content.*;
import mindustry.ctype.Content;
import mindustry.game.*;
import mindustry.graphics.g3d.*;
import mindustry.maps.generators.*;
import mindustry.type.*;
import mindustry.ui.dialogs.SchematicsDialog;
import mindustry.world.*;
import mindustry.world.blocks.defense.Wall;
import mindustry.world.blocks.defense.turrets.ItemTurret;
import mindustry.world.blocks.defense.turrets.LiquidTurret;
import mindustry.world.blocks.defense.turrets.Turret;
import mindustry.world.blocks.environment.*;
import mindustry.world.blocks.production.Drill;
import mindustry.world.blocks.sandbox.ItemSource;
import mindustry.world.blocks.storage.CoreBlock;
import mindustry.world.blocks.units.UnitFactory;

import java.lang.reflect.Field;
import java.lang.Class;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ec620.EC620JavaMod.*;
import static mindustry.Vars.*;
import static mindustry.content.Blocks.*;

import static ec620.content.EC620Classes.*;
/**
 * Planet generator thingy.
 * @author Anuke(n)
 * todo: remove ruin generation
 * todo: new weathers
 * todo: replace tiles and remove atmosphere
 */
public class EC620PlanetGenerator extends PlanetGenerator
{
    //alternate, less direct generation (wip)
    public static boolean alt = false;
    static final int seed = (int)(System.currentTimeMillis()%Integer.MAX_VALUE);
    final Color borudaMidColor = new Color();

    //BaseGenerator basegen = new BaseGenerator();
    EC620BaseGenerator basegen=new EC620BaseGenerator();
    float scl = 2.7f;
    float waterOffset = 0.07f;
    boolean genLakes = false;
    private boolean customCoreBasePartsAdded=false;
    private Rand pgRand=new Rand(seed);
    private Seq<Schematic> coreSchematics=new Seq<>();
    private Seq<Schematic> defensiveSchematics=new Seq<>();
    private Seq<Schematic> factorySchematics=new Seq<>();

    Seq<Block> liquids=Seq.with(Blocks.water,Blocks.tar,Blocks.slag, Blocks.cryofluid, Blocks.arkyciteFloor);
    //^ EQUATOR
    //                  <- LOW                   HIGH ->
    Block[][] arr =
            {
                    //{EC620Blocks.oreIron}
            };
    //v POLES

    //todo (how many todos are even here?
    /*ObjectMap<Block, Block> dec = ObjectMap.of(
            Blocks.sporeMoss, Blocks.sporeCluster,
            Blocks.moss, Blocks.sporeCluster
    );

    ObjectMap<Block, Block> tars = ObjectMap.of(
            Blocks.sporeMoss, Blocks.shale,
            Blocks.moss, Blocks.shale
    );

    ObjectMap<Block, Block> toMoss = ObjectMap.of(
            borudalite, twilightMoss,
            starryBorudaliteWater, starryMossWater
    );

    ObjectMap<Block, Block> toShallowMoss = ObjectMap.of(
            borudalite, mossyBorudalite,
            starryBorudaliteWater, starryMossWater
    );

    ObjectSet<Block> shallowBois = ObjectSet.with(
            starrySandWater, starryMossWater, starryBorudaliteWater
    );*/

    float water = 2f; // / arr[0].length;

    Vec3[] craters = new Vec3[4];
    float[] craterSize = new float[craters.length];
    void initCraters(){
        if(craters[0] != null) return;
        for(int i = 0; i < craters.length; i++){
            rand.setSeed(seed + i + 66);
            craters[i] = new Vec3(scl, 0, 0).setToRandomDirection(rand).setLength2(scl * scl);
            craterSize[i] = rand.nextFloat() * 0.15f + 0.03f;
        }
    }

    float rawHeight(Vec3 position){
        position = Tmp.v33.set(position).scl(scl);
        float tnoise = riverNoise(position);
        boolean isRiver = tnoise > 0.49f && tnoise < 0.65f;

        //do not touch tnoise here, tweak the riverNoise() instead
        float simp = isRiver ? 0f : Simplex.noise3d(seed, 5, 0.5f, 1f/8f, position.x, position.y, position.z) + craterNoise(position, true) * 1.4f;
        return (simp * 0.7f + waterOffset) / (1f + waterOffset);
    }

    @Override
    public void generateSector(Sector sector)
    {

        //these always have bases
        if(sector.id == 0)
        {
            sector.generateEnemyBase = true;
//            ItemSeq is=new ItemSeq();
//
//            is.add(Items.copper,100);
//            sector.info.items=is;
            //return;
        }
        //else sector.generateEnemyBase=rand.chance(.5);
        if(Core.settings.getBool("ec620.randomName"))
        {
            if(Objects.equals(sector.name(), sector.id + "")) sector.setName(EC620NameGenerator.generate(sector.id));
        }
        else sector.setName(sector.id+"");
        PlanetGrid.Ptile tile = sector.tile;

        boolean any = false;
        float poles = Math.abs(tile.v.y);
        float noise = Noise.snoise3(tile.v.x, tile.v.y, tile.v.z, 0.001f, 0.58f);

        if(noise + poles/7.1 > 0.12 && poles > 0.23){
            any = true;
        }

        if(noise < 0.16){
            for(PlanetGrid.Ptile other : tile.tiles){
                var osec = sector.planet.getSector(other);

                //no sectors near start sector!
                if(
                        osec.id == sector.planet.startSector || //near starting sector
                                osec.generateEnemyBase && poles < 0.85 || //near other base
                                (sector.preset != null && noise < 0.11) //near preset
                ){
                    return;
                }
            }
        }

        sector.generateEnemyBase = sector.id==0 || any;
    }

    @Override
    public float getHeight(Vec3 position){
        float height = rawHeight(position);
        return Math.max(water, height - 0.3f); //a bit more than the max crater noise is subtracted
        //return waterOffset / (1f + waterOffset); //eh fuck it
    }

    @Override
    public Color getColor(Vec3 position){
        Block block = getBlock(position);
        float tnoise = 1f;
        //float tnoise = Simplex.noise3d(seed, 8, 0.56, 1f/16f, position.x, position.y + 9999f, position.z);
        //tnoise = (1f - Mathf.clamp(tnoise)) * 0.3f + 0.7f;

        //if(block == mossyBorudalite) return Tmp.c1.set(borudaMidColor.set(borudalite.mapColor).lerp(twilightMoss.mapColor, 0.5f)).mul(tnoise).a(1f);
        return Tmp.c1.set(block.mapColor).mul(tnoise).a(1f - block.albedo);
    }

    //@Override
    //public boolean skip(Vec3 position){
    //    return Simplex.noise3d(seed, 8, 0.56, 1f/16f, position.x, position.y + 999f, position.z) > 0.72f; //test
    //}

    @Override
    public void genTile(Vec3 position, TileGen tile){
        tile.floor = getBlock(position);
        if(!generatedFloors.contains(tile.floor.name)) generatedFloors.add(tile.floor.name);
        try
        {
            tile.block = tile.floor.asFloor().wall;

            if (Ridged.noise3d(1, position.x, position.y, position.z, 2, 22) > 0.31) {
                tile.block = Blocks.air;
            }
        }
        catch(Exception e)
        {
            tile.block=Blocks.water;
        }
    }

    float riverNoise(Vec3 position){
        return Simplex.noise3d(seed, 7, 0.56, .1f, position.x, position.y + 999f, position.z) * 0.76f;
    }

    float craterNoise(Vec3 position, boolean smooth){
        initCraters();
        float d = scl * scl * 4f, s = 0.3f;
        for(int i = 0; i < craters.length; i++){
            float dt = craters[i].dst2(position);
            if(dt < d){
                d = dt;
                s = craterSize[i];
            }
        }

        d /= scl * scl; //d = [0 ~ 2]
        if(d <= s){
            //inside crater
            if(smooth) return d / s * 1.3f -0.7f;
            return -0.7f;
        }
        if(d >= s + 0.15f) return 0.2f; //not a crater
        float a = (d - s) / 0.15f;
        //outside crater
        return (1f - Mathf.sqrt(a)) * 0.38f + 0.22f;
    }
    /*ArrayList<Block> tempBlocks=new ArrayList<>(List.of(
            deepwater,
            Blocks.water,
            taintedWater,
            deepTaintedWater,
            darksandTaintedWater,
            sandWater,
            darksandWater,
            tar,
            cryofluid,
            slag,
            basalt,
            hotrock,
            magmarock,
            sand,
            darksand,
            dirt,
            mud,
            dacite,
            rhyolite,
            rhyoliteCrater,
            roughRhyolite,
            regolith,
            yellowStone,
            carbonStone,
            ferricStone,
            ferricCraters,
            beryllicStone,
            crystallineStone,
            crystalFloor,
            yellowStonePlates,
            redStone,
            denseRedStone,
            redIce,
            arkyciteFloor,
            arkyicStone,
            redmat,
            bluemat,
            grass,
            salt,
            snow,
            ice,
            iceSnow,
            shale,
            moss,
            sporeMoss,

            ));*/
    Seq<Floor> availableBlocks=null;
    Seq<SteamVent> availableVents=null;   //new ArrayList<>(List.of(rhyoliteVent,carbonVent,arkyicVent,yellowStoneVent,redStoneVent,crystallineVent,));
    Seq<String> generatedFloors=new Seq<>();
    Block getBlock(Vec3 position)
    {
        //todo refill poles with milksand, try reviving the arr usage
        float height = rawHeight(position);
        Tmp.v31.set(position);
        position = Tmp.v33.set(position).scl(scl);
        float rad = scl;
        float temp = Mathf.clamp(Math.abs(position.y * 2f) / (rad));
        float tnoise = riverNoise(position);
        float cnoise = craterNoise(position, false);
        //temp = Mathf.lerp(temp, tnoise, 0.5f);
        //height *= 1.2f;
        //height = Mathf.clamp(height);

        //float tar = Simplex.noise3d(seed, 4, 0.55f, 1f/2f, position.x, position.y + 999f, position.z) * 0.3f + Tmp.v31.dst(0, 0, 1f) * 0.2f;

        //Block res = arr[Mathf.clamp((int)(temp * arr.length), 0, arr[0].length - 1)][Mathf.clamp((int)(height * arr[0].length), 0, arr[0].length - 1)];

        //do not touch tnoise here, tweak the riverNoise() instead
        //Block res = (tnoise > 0.52f) ? (tnoise > 0.53f ? (tnoise > 0.57f ? (tnoise > 0.58f ? milksand : starrySandWater) : starryWater) : starryBorudaliteWater) : borudalite;
        if(availableBlocks==null)
        {
            Seq<Floor> tempBlocks=new Seq();
            availableBlocks = new Seq<>();
            availableVents=new Seq<>();
            for (Field f : Blocks.class.getDeclaredFields())
            {
                f.setAccessible(true);
                Object fv;
                try
                {
                    fv = f.get(Blocks.class);
                }
                catch (IllegalAccessException e)
                {
                    continue;
                }
                try
                {
                    if(fv instanceof SteamVent) availableVents.add((SteamVent)fv);
                    else if (fv instanceof Floor &&
                            fv != air &&
                            fv != empty &&
                            fv != coreZone &&
                            fv != spawn &&
                            fv != space &&
                            !((Floor) fv).name.contains("panel") &&
                            !(fv instanceof OreBlock) &&
                            !(fv instanceof OverlayFloor) &&
                            !((Floor)fv).isLiquid) tempBlocks.add((Floor) fv);
                }
                catch (Exception e)
                {

                }
            }
            while(tempBlocks.size>0)
            {
                int c=pgRand.nextInt(tempBlocks.size);

                availableBlocks.add(tempBlocks.get(c));
                tempBlocks.remove(c);
            }
        }
        if(pgRand.chance(.01)) return availableVents.getFrac(pgRand.nextFloat());
        Block res;
        if(sector.id==0)
        {
            Seq<Block> sands=Seq.with( sand, darksand);
            return sands.get((int)(tnoise*sands.size)%sands.size);
        }
        else res= availableBlocks.get((int)(tnoise*availableBlocks.size)%availableBlocks.size);
        //res=Blocks.sand;
        float moss = Ridged.noise3d(seed, position.x, position.y, position.z, 8, 0.29f);//freq = 1 / scl?
        if(cnoise < 0f || cnoise > 0.4f) return res; //inside of crater is safe from moss
        else if(cnoise > 0.2f) moss -= cnoise * 1.4f;
        if(moss > 0.45f){
            //return toMoss.get(res, res);
            return Blocks.sporeMoss;
        }
        else if(moss > 0.21f){
            //return toShallowMoss.get(res, res);
            return Blocks.moss;
        }
        return res;
    }
    private int s=0;
    private Block randomBlock(Block[] blocks)
    {
        return blocks[new Rand(s++).nextInt(blocks.length)];
    }
    private Block randomBlock(ArrayList blocks){return (Block)blocks.get(new Rand(s++).nextInt(blocks.size()));}
    @Override
    protected float noise(float x, float y, double octaves, double falloff, double scl, double mag){
        Vec3 v = sector.rect.project(x, y).scl(5f);
        return Simplex.noise3d(seed, octaves, falloff, 1f / scl, v.x, v.y, v.z) * (float)mag;
    }

    @Override
    protected void generate()
    {

        class Room
        {
            int x, y, radius;
            ObjectSet<Room> connected = new ObjectSet<>();

            Room(int x, int y, int radius)
            {
                this.x = x;
                this.y = y;
                this.radius = radius;
                connected.add(this);
            }

            void join(int x1, int y1, int x2, int y2)
            {
                float nscl = pgRand.random(100f, 140f) * 6f;
                int stroke = pgRand.random(3, 9);
                brush(pathfind(x1, y1, x2, y2, tile -> (tile.solid() ? 50f : 0f) + noise(tile.x, tile.y, 2, 0.4f, 1f / nscl) * 500, Astar.manhattan), stroke);
            }

            void connect(Room to)
            {
                if(!connected.add(to) || to == this) return;

                Vec2 midpoint = Tmp.v1.set(to.x, to.y).add(x, y).scl(0.5f);
                pgRand.nextFloat();

                if(alt)
                {
                    midpoint.add(Tmp.v2.set(1, 0f).setAngle(Angles.angle(to.x, to.y, x, y) + 90f * (rand.chance(0.5) ? 1f : -1f)).scl(Tmp.v1.dst(x, y) * 2f));
                }
                else
                {
                    //add randomized offset to avoid straight lines
                    midpoint.add(Tmp.v2.setToRandomDirection(pgRand).scl(Tmp.v1.dst(x, y)));
                }

                midpoint.sub(width/2f, height/2f).limit(width / 2f / Mathf.sqrt3).add(width/2f, height/2f);

                int mx = (int)midpoint.x, my = (int)midpoint.y;

                join(x, y, mx, my);
                join(mx, my, to.x, to.y);
            }

            void joinLiquid(int x1, int y1, int x2, int y2)
            {
                float nscl = pgRand.random(100f, 140f) * 6f;
                int rad = pgRand.random(5, 10);
                int avoid = 2 + rad;
                var path = pathfind(x1, y1, x2, y2, tile -> (tile.solid() || !tile.floor().isLiquid ? 70f : 0f) + noise(tile.x, tile.y, 2, 0.4f, 1f / nscl) * 500, Astar.manhattan);
                path.each(t -> {
                    //don't place liquid paths near the core
                    if(Mathf.dst2(t.x, t.y, x2, y2) <= avoid * avoid)
                    {
                        return;
                    }

                    for(int x = -rad; x <= rad; x++)
                    {
                        for(int y = -rad; y <= rad; y++)
                        {
                            int wx = t.x + x, wy = t.y + y;
                            if(Structs.inBounds(wx, wy, width, height) && Mathf.within(x, y, rad))
                            {
                                Tile other = tiles.getn(wx, wy);
                                other.setBlock(Blocks.air);
                                if(Mathf.within(x, y, rad - 1) && !other.floor().isLiquid)
                                {
                                    Floor floor = other.floor();
                                    //TODO does not respect tainted floors
                                    other.setFloor((Floor)(floor == Blocks.sand || floor == Blocks.salt ? Blocks.sandWater : Blocks.darksandTaintedWater));
                                }
                            }
                        }
                    }
                });
            }

            void connectLiquid(Room to)
            {
                if(to == this) return;

                Vec2 midpoint = Tmp.v1.set(to.x, to.y).add(x, y).scl(0.5f);
                pgRand.nextFloat();

                //add randomized offset to avoid straight lines
                midpoint.add(Tmp.v2.setToRandomDirection(pgRand).scl(Tmp.v1.dst(x, y)));
                midpoint.sub(width/2f, height/2f).limit(width / 2f / Mathf.sqrt3).add(width/2f, height/2f);

                int mx = (int)midpoint.x, my = (int)midpoint.y;

                joinLiquid(x, y, mx, my);
                joinLiquid(mx, my, to.x, to.y);
            }
        }

        cells(4);
        distort(10f, 12f);

        float constraint = 1.3f;
        float radius = width / 2f / Mathf.sqrt3;
        int rooms = pgRand.random(2, 5);
        Seq<Room> roomseq = new Seq<>();


        for(int i = 0; i < rooms; i++)
        {
            Tmp.v1.trns(pgRand.random(360f), pgRand.random(radius / constraint));
            float rx = (width/2f + Tmp.v1.x);
            float ry = (height/2f + Tmp.v1.y);
            float maxrad = radius - Tmp.v1.len();
            float rrad = Math.min(pgRand.random(9f, maxrad / 2f), 30f);
            roomseq.add(new Room((int)rx, (int)ry, (int)rrad));
        }

        //check positions on the map to place the player spawn. this needs to be in the corner of the map
        Room spawn = null;
        Seq<Room> enemies = new Seq<>();
        int enemySpawns = pgRand.random(1, Math.max((int)(sector.threat * 4), 1));
        int offset = pgRand.nextInt(360);
        float length = width/2.55f - pgRand.random(13, 23);
        int angleStep = 5;
        int waterCheckRad = 5;
        for(int i = 0; i < 360; i+= angleStep)
        {
            int angle = offset + i;
            int cx = (int)(width/2 + Angles.trnsx(angle, length));
            int cy = (int)(height/2 + Angles.trnsy(angle, length));

            int waterTiles = 0;

            //check for water presence
            for(int rx = -waterCheckRad; rx <= waterCheckRad; rx++)
            {
                for(int ry = -waterCheckRad; ry <= waterCheckRad; ry++)
                {
                    Tile tile = tiles.get(cx + rx, cy + ry);
                    if(tile == null || tile.floor().liquidDrop != null)
                    {
                        waterTiles ++;
                    }
                }
            }

            if(waterTiles <= 4 || (i + angleStep >= 360))
            {
                roomseq.add(spawn = new Room(cx, cy, pgRand.random(8, 15)));

                for(int j = 0; j < enemySpawns; j++)
                {
                    float enemyOffset = pgRand.range(60f);
                    Tmp.v1.set(cx - width/2, cy - height/2).rotate(180f + enemyOffset).add(width/2, height/2);
                    Room espawn = new Room((int)Tmp.v1.x, (int)Tmp.v1.y, pgRand.random(8, 16));
                    roomseq.add(espawn);
                    enemies.add(espawn);
                }

                break;
            }
        }

        //clear radius around each room
        for(Room room : roomseq)
        {
            erase(room.x, room.y, room.radius);
        }

        //randomly connect rooms together
        int connections = pgRand.random(Math.max(rooms - 1, 1), rooms + 3);
        for(int i = 0; i < connections; i++)
        {
            roomseq.random(pgRand).connect(roomseq.random(pgRand));
        }

        for(Room room : roomseq)
        {
            spawn.connect(room);
        }

        Room fspawn = spawn;

        cells(1);

        int tlen = tiles.width * tiles.height;
        int total = 0, waters = 0;

        for(int i = 0; i < tlen; i++)
        {
            Tile tile = tiles.geti(i);
            if(tile.block() == Blocks.air)
            {
                total ++;
                if(tile.floor().liquidDrop == Liquids.water){
                    waters ++;
                }
            }
        }

        boolean naval = (float)waters / total >= 0.26f;

        //create water pathway if the map is flooded
        if(naval)
        {
            for(Room room : enemies)
            {
                room.connectLiquid(spawn);
            }
        }

        distort(10f, 6f);

        boolean isWater=pgRand.chance(.5f) || sector.id==0;
        Seq<LakeHandler> lakeSeq=new Seq<>();
        Seq<Vec2> lakeVec=new Seq<>();
        //rivers
        pass((x, y) ->
        {
            //if(block.solid) return;

            Vec3 v = sector.rect.project(x, y);

            float rr = Simplex.noise2d(seed, (float)2, 0.6f, 1f / 7f, x, y) * 0.1f;
//            float value = Ridged.noise3d(seed, v.x, v.y, v.z, 1, 1f / 53f) + rr - rawHeight(v) * 0f;
            float rrscl = rr * 44 - 2;
            float value=noise(x,y,5,.8,80);

            //Log.info(value);
            if(value > .65f && !Mathf.within(x, y, fspawn.x, fspawn.y, 12 + rrscl))
            {
                boolean deep = !Mathf.within(x, y, fspawn.x, fspawn.y, 15 + rrscl);
                boolean spore = floor != Blocks.sand && floor != Blocks.salt;
                //do not place rivers on ice, they're frozen
                //ignore pre-existing liquids

                if(!(floor == Blocks.ice || floor == Blocks.iceSnow || floor == Blocks.snow || floor.asFloor().isLiquid))
                {
                    if(isWater)
                    {
                        floor = spore ?
                                (deep ? Blocks.taintedWater : Blocks.darksandTaintedWater) :
                                (deep ? Blocks.water :
                                        (floor == Blocks.sand || floor == Blocks.salt ? Blocks.sandWater : Blocks.darksandWater));
                    }
                    else
                    {
                        lakeVec.add(new Vec2(x,y));
                    }
//                    {
//                        if(lakeSeq.size==0)
//                        {
//                            floor=liquids.get(pgRand.nextInt(liquids.size));
//                            lakeSeq.add(new LakeHandler(x,y,floor));
//                            //Log.info("Initializing ("+x+", "+y+", "+floor+")");
//                        }
//                        else
//                        {
//                            floor=null;
//                            for(LakeHandler lh:lakeSeq)
//                            {
//                                if(lh.Within(x,y))
//                                {
//                                    lh.Add(x,y);
//                                    floor=lh.block;
//                                    break;
//                                }
//                            }
//                            if(floor==null)
//                            {
//                                floor=liquids.get(pgRand.nextInt(liquids.size));
//                                lakeSeq.add(new LakeHandler(x,y,floor));
//                                //Log.info("Adding ("+x+", "+y+", "+floor+")");
//                            }
//                        }
//                    }
                }
            }
        });
        Seq<Block> availableLiquids=Seq.with(Blocks.water, cryofluid, slag, tar, arkyciteFloor);
        int n=pgRand.nextInt(5)+1;
        Seq<Block> liquids=new Seq<>();
        while(lakeVec.size>0)
        {
            Vec2 vec=lakeVec.get(0);
            Seq<Vec2> seq=lakeVec.select(a->Mathf.within(a.x,a.y,vec.x,vec.y,10));
            lakeVec.removeAll(seq);
            for(int i=0;i<seq.size;i++)
            {
                Vec2 v=seq.get(i);
                Seq<Vec2> s=lakeVec.select(a->Mathf.within(a.x,a.y,v.x,v.y,10));
                lakeVec.removeAll(s);
                seq.addAll(s);
            }
            Block b;
            if(liquids.size<n)
            {
                b=availableLiquids.getFrac(pgRand.nextFloat());
                liquids.addUnique(b);
            }
            else b=liquids.getFrac(pgRand.nextFloat());
            lakeSeq.add(new LakeHandler(seq,b));
        }

        for(LakeHandler lh:lakeSeq)
        {
            for(Vec2 v:lh.pos)
            {

                tiles.getn((int) v.x, (int) v.y).setBlock(lh.block);
            }
        }
        //shoreline setup
        pass((x, y) ->
        {
            int deepRadius = 3;

            if(floor.asFloor().isLiquid && floor.asFloor()==Blocks.water)
            {

                for(int cx = -deepRadius; cx <= deepRadius; cx++)
                {
                    for(int cy = -deepRadius; cy <= deepRadius; cy++)
                    {
                        if((cx) * (cx) + (cy) * (cy) <= deepRadius * deepRadius)
                        {
                            int wx = cx + x, wy = cy + y;

                            Tile tile = tiles.get(wx, wy);
                            if(tile != null && (!tile.floor().isLiquid || tile.block() != Blocks.air))
                            {
                                //found something solid, skip replacing anything
                                return;
                            }
                        }
                    }
                }

                floor = deepwater;
            }
        });

        /*
        if(naval){
            int deepRadius = 2;

            pass((x, y) -> {
                if(floor.asFloor().isLiquid && !floor.asFloor().isDeep() && !floor.asFloor().shallow){

                    for(int cx = -deepRadius; cx <= deepRadius; cx++){
                        for(int cy = -deepRadius; cy <= deepRadius; cy++){
                            if((cx) * (cx) + (cy) * (cy) <= deepRadius * deepRadius){
                                int wx = cx + x, wy = cy + y;

                                Tile tile = tiles.get(wx, wy);
                                if(tile != null && (tile.floor().shallow || !tile.floor().isLiquid)){
                                    //found something shallow, skip replacing anything
                                    return;
                                }
                            }
                        }
                    }

                    floor = floor == Blocks.water ? Blocks.deepwater : Blocks.deepTaintedWater;
                }
            });
        }*/

        Block[] availableOres=new Block[]{oreLead,oreCoal,oreTitanium,oreBeryllium,oreTungsten,oreThorium,oreCrystalThorium,oreScrap,wallOreBeryllium, wallOreTungsten};
        Seq<Block> ores = new Seq<Block>();
        float poles = Math.abs(sector.tile.v.y);
        float nmag = 0.5f;
        float scl = 1f;
        float addscl = 1.3f;
        int l= availableOres.length;

        ores.add(oreCopper);
        if(sector.id==0)
        {
            ores.add(oreLead);
            ores.add(oreCoal);
            ores.add(oreTitanium);
        }
        else
        {
            for(int i=0;i<l;i++)
            {
                if(Simplex.noise3d(seed, 2, 0.5, scl, sector.tile.v.x+i, sector.tile.v.y, sector.tile.v.z)*nmag + poles > (i+1)*addscl/(l+1))
                {
                    ores.add(availableOres[i]);
                }
            }
            for(int i=ores.size;i<Math.min(pgRand.nextInt(l),5);i++)
            {
                ores.add(randomBlock((Arrays.stream(availableOres).filter(x->!ores.contains(x)).toArray(Block[]::new))));
            }
            if(ores.size==1) ores.add(randomBlock((Arrays.stream(availableOres).filter(x->!ores.contains(x)).toArray(Block[]::new))));
        }



        FloatSeq frequencies = new FloatSeq();
        for(int i = 0; i < ores.size; i++){
            frequencies.add(pgRand.random(-0.1f, 0.01f) - i * 0.01f + poles * 0.04f);
        }

        pass((x, y) -> {
            if(!floor.asFloor().hasSurface()) return;

            int offsetX = x - 4, offsetY = y + 23;
            for(int i = ores.size - 1; i >= 0; i--)
            {
                Block entry = ores.get(i);
                float freq = frequencies.get(i);
                if(Math.abs(0.5f - noise(offsetX, offsetY + i*999, 2, 0.7, (40 + i * 2))) > 0.22f + i*0.01 &&
                        Math.abs(0.5f - noise(offsetX, offsetY - i*999, 1, 1, (30 + i * 4))) > 0.37f + freq)
                {
                    ore = entry;
                    break;
                }
            }

            if(ore == Blocks.oreScrap && pgRand.chance(0.33)){
                floor = Blocks.metalFloorDamaged; //todo
            }
        });

        trimDark();

        median(2);

        inverseFloodFill(tiles.getn(spawn.x, spawn.y));

        tech();

        pass((x, y) ->
        {
            //random moss
            /*if(floor == twilightMoss){
                if(Math.abs(0.5f - noise(x - 90, y, 4, 0.8, 65)) > 0.02){
                    floor = starryMoss;
                }
            }*/

            //tar
            if(floor == Blocks.darksand){
                if(Math.abs(0.5f - noise(x - 40, y, 2, 0.7, 80)) > 0.25f &&
                        Math.abs(0.5f - noise(x, y + sector.id*10, 1, 1, 60)) > 0.41f && !(roomseq.contains(r -> Mathf.within(x, y, r.x, r.y, 15)))){
                    floor = Blocks.tar;
                }
            }

            //todo below
            //hotrock tweaks
            if(floor == Blocks.hotrock || floor== magmarock)
            {
                if(pgRand.chance(.1f)) floor=availableVents.getFrac(pgRand.nextFloat());
                else if(Math.abs(0.5f - noise(x - 90, y, 4, 0.8, 80)) > 0.035)
                {
                    floor = Blocks.basalt;
                }
                else
                {
                    ore = Blocks.air;
                    boolean all = true;
                    for(Point2 p : Geometry.d4)
                    {
                        int px=x + p.x;
                        int py=y + p.y;
                        Tile other = tiles.get(px, py);
                        if(other == null || (other.floor() != Blocks.hotrock && other.floor() != Blocks.magmarock))
                        {
                            all = false;
                        }
//                        else if(other.floor().isLiquid && other.floor()!= slag)
//                        {
//                            for(LakeHandler lh:lakeSeq)
//                            {
//                                if(lh.Within(px,py))
//                                {
//                                    lh.block=slag;
//                                    for(Vec2 v:lh.pos)
//                                    {
//                                        Tile t=tiles.get((int)v.x,(int)v.y);
//                                        t.setBlock(slag);
//                                    }
//                                    Log.info("Lake replaced");
//                                    break;
//                                }
//                            }
//                        }
                    }
                    if(all)
                    {
                        floor = Blocks.magmarock;
                    }
                }
            }
            else if(genLakes && floor != Blocks.basalt && floor != Blocks.ice && floor.asFloor().hasSurface())
            {
                float noise = noise(x + 782, y, 5, 0.75f, 260f, 1f);
                if(noise > 0.67f && !roomseq.contains(e -> Mathf.within(x, y, e.x, e.y, 14)))
                {
                    if(noise > 0.72f)
                    {
                        floor = noise > 0.78f ? Blocks.taintedWater : (floor == Blocks.sand ? Blocks.sandWater : Blocks.darksandTaintedWater);
                    }
                    else
                    {
                        floor = (floor == Blocks.sand ? floor : Blocks.darksand);
                    }
                }
            }

            if(pgRand.chance(0.0075))
            {
                //random spore trees
                boolean any = false;
                boolean all = true;
                for(Point2 p : Geometry.d4){
                    Tile other = tiles.get(x + p.x, y + p.y);
                    if(other != null && other.block() == Blocks.air){
                        any = true;
                    }else{
                        all = false;
                    }
                }
                if(any && ((block == Blocks.snowWall || block == Blocks.iceWall) || (all && block == Blocks.air && floor == Blocks.snow && pgRand.chance(0.03))))
                {
                    block = pgRand.chance(0.5) ? Blocks.whiteTree : Blocks.whiteTreeDead;//todo starry large tree
                }
            }

            //random stuff
            /*dec: {
                for(int i = 0; i < 4; i++)
                {
                    Tile near = world.tile(x + Geometry.d4[i].x, y + Geometry.d4[i].y);
                    if(near != null && near.block() != Blocks.air)
                    {
                        break dec;
                    }
                }

                if(rand.chance(0.01) && floor.asFloor().hasSurface() && block == Blocks.air)
                {
                    block = randomBlock(new Block[]{sporePine, basalt, stone, arkyciteFloor, arkyicStone}); //Add more stuff
                }
            }*/
        });

        float difficulty = sector.threat;
        ints.clear();
        ints.ensureCapacity(width * height / 4);

        int ruinCount = pgRand.random(-2, -1); //disabled ruins

        if(ruinCount > 0){
            int padding = 25;

            //create list of potential positions
            for(int x = padding; x < width - padding; x++){
                for(int y = padding; y < height - padding; y++){
                    Tile tile = tiles.getn(x, y);
                    if(!tile.solid() && (tile.drop() != null || tile.floor().liquidDrop != null)){
                        ints.add(tile.pos());
                    }
                }
            }

            ints.shuffle(rand);

            int placed = 0;
            float diffRange = 0.4f;
            //try each position
            for(int i = 0; i < ints.size && placed < ruinCount; i++){
                int val = ints.items[i];
                int x = Point2.x(val), y = Point2.y(val);

                //do not overwrite player spawn
                if(Mathf.within(x, y, spawn.x, spawn.y, 18f)){
                    continue;
                }

                float range = difficulty + rand.random(diffRange);

                Tile tile = tiles.getn(x, y);
                BaseRegistry.BasePart part = null;
                if(tile.overlay().itemDrop != null){
                    part = bases.forResource(tile.drop()).getFrac(range);
                }else if(tile.floor().liquidDrop != null && rand.chance(0.05)){
                    part = bases.forResource(tile.floor().liquidDrop).getFrac(range);
                }else if(rand.chance(0.05)){ //ore-less parts are less likely to occur.
                    part = bases.parts.getFrac(range);
                }

                //actually place the part
                if(part != null && BaseGenerator.tryPlace(part, x, y, Team.derelict, (cx, cy) -> {
                    Tile other = tiles.getn(cx, cy);
                    if(other.floor().hasSurface()){
                        other.setOverlay(Blocks.oreScrap);
                        for(int j = 1; j <= 2; j++){
                            for(Point2 p : Geometry.d8){
                                Tile t = tiles.get(cx + p.x*j, cy + p.y*j);
                                if(t != null && t.floor().hasSurface() && rand.chance(j == 1 ? 0.4 : 0.2)){
                                    t.setOverlay(Blocks.oreScrap);
                                }
                            }
                        }
                    }
                })){
                    placed ++;

                    int debrisRadius = Math.max(part.schematic.width, part.schematic.height)/2 + 3;
                    Geometry.circle(x, y, tiles.width, tiles.height, debrisRadius, (cx, cy) -> {
                        float dst = Mathf.dst(cx, cy, x, y);
                        float removeChance = Mathf.lerp(0.05f, 0.5f, dst / debrisRadius);

                        Tile other = tiles.getn(cx, cy);
                        if(other.build != null && other.isCenter()){
                            if(other.team() == Team.derelict && rand.chance(removeChance)){
                                other.remove();
                            }else if(rand.chance(0.5)){
                                other.build.health = other.build.health - rand.random(other.build.health * 0.9f);
                            }
                        }
                    });
                }
            }
        }



        //remove invalid ores
        for(Tile tile : tiles)
        {
            if(tile.overlay().needsSurface && !tile.floor().hasSurface())
            {
                tile.setOverlay(Blocks.air);
            }
        }

        Schematics.placeLaunchLoadout(spawn.x, spawn.y);

        for(Room espawn : enemies)
        {
            tiles.getn(espawn.x, espawn.y).setOverlay(Blocks.spawn);
        }

        if(sector.hasEnemyBase())
        {
            if(!customCoreBasePartsAdded)
            {
                customCoreBasePartsAdded=true;

                /*addCorePart(3,"bXNjaAF4nC2QzW6EMAyEB8KGn5D90R77DBx67LmPUVUVZbMVEgtVWLrq23fs9EA+x3bGZnDG2aCY+1uAfV1i+HiGu4R1iOP3fVxmAHbqP8O0In97t3iKYZyvSxzCpVu3+BW6Rz9N3dQzRHHd1oByffTxFiJO9/Hez+N264Zl/gm/S0S1zdPSX1hsBw7r5m2YwrZyygs/VMgFjSCDRZ7BoJZbDpfgBUZqQMFDwSDn4VggvNx2bDGGqNKtlpq84s2yyRREtQNKjRyjvZFSmxr3omxxFJTcKS8Iq10iVsuCokm5ivIUqdCqnGHkkUn5IK8rnAS1iOREK3s16U2jkqWRf9YuR1mFKrvU5VRZ1nNUNlI+yEItyv98yylGEo3KSULN8Mkan8zwnAJJqoleRDIeavA+GXxQUwk1+JiSJ/66gp1/ctIv+A==");
                addCorePart(-1,"bXNjaAF4nF1W224bRwzlzkp7v18k25tfEIp+T9EH1d62LhTJkGL4uwv0oY5jbUie6WRqB6vD5XLIQw6HGfpEn0JaHfefZwp+pvxhvtyfH5++PJ6ORBQd9r/NhwuZX37NqHg6vczn3eX0fL6faTrPj8ffTyw+7C7P5z/m3cv+cNgd9ixSrR92T+fTX/P9l9OZqs/z8cF7j+R9PlMDn8fTw2yXZrx0vvy5fzi9UPbjKyXPx8NpL2uqe7bY7e/Pp6fT4fFCFPxE8pcEtKKVETFIVEOGdfyrcmh1K9EtV5XXVhd5drHVJZ5danWZZ5dbXeHZld73yn6vE2O/Q99Yfeut66yu99YPVjd+WL+x+q23/sbqbhMKBe8sTqpnu0R+5Z+Rn5ACgTUgBqSAHFACakAL6AEjYAu4Ff8BhwopXL7Rmh+zvIKuhjeWRijvgUYnY2SvRBdSRIFAAsgABaACNIAOMFAgy7ekqdzCN8dgHittA0OQJyevbey1xF4xcFCBGJAAUkAGyAEFoARUgBrQAFpAB+gBA2BEWGW5tiwjZmP+29JAKhAKq0hoyltERhgmSb1cuZy0vFPAj+En5Ge1vC8Lf9E/1iysWYTb8sbSG9u98bsxEaeXk0INj5145C1alm/8+F6MeJLTQXKKpLyh7hAXdPmbLf/h56sRcgOobkB1q8eH8/qRDx8hW+0Y1Y4lL4ENabtpLWJbi0R2TNYG0gJah0TsI96NWMinXFBVasQEERPZW13Fh9NGSxEtRbQU0VJES220zPWHdJpGy8Q+ZsiShGJb4gBlZFnwVfqZf688VzLK2b5QqXBS6aTKSbWTGpUkRptoVD18hp+QN+OVS/vKe/BKklGk9TQUBUJNc86Qc+b1c25zzpFzjgbPKZNIJuVuLaFoRUFioeXIUY7clqP40IgFSlJgA0rZgITf4FUkuJNPPQyVYAGCxYc2KC3JEiRLkCydu9KxK8GuBLvSsqu81qjArIKPyvmonI8KXCpwqbwGqd2m60ATPzX81M5P7fzU4FKDSy05Yaipr4YwqBrwaVCpFpVqnLvGVapFpRqwa8CusRm2LkOdsWLYoh1bbUeJ/M6n+SonmlsxYtsAzSJNw83Voh1DkQonlU6qnFQ7SdsxlBhtEi//6lxZYy5IPI6yyHzUFmxRjRbVaL2qdrYSHSrRoRI9Jk4vE0eUmnWHrDubde/tR4+sexzaHtF6ROu9hh9stAHRBozKAaNykFGp//sYPVcywOSUGR1r3JJcrHf95aHnxinmrgw6oz468aHr/7dWy2N4YMrgveqoXblBGao1U1muRugMILcBOc16sFmP3l6PyHpE1iOyHpH16NV4o1kbdrQSw40wFogBCSAFZIAcUABKQAWoAQ2gBXSAHjAARgolrGawsRlsvX3bSgYhAzMSSAAZoABUgAbQATiALNdct94O32iugoh269VL7hihQAhYA2JACsgBJaAGtIAeMAK2AKVw65X7zlK4sxQmeTeCuFBO3oVyshfKybtQTvZCOXkXysleKCfvQjnZC+XkXSgne6Gc+EIp3fbV2ZaeTWVt9FJJxtk0Vt96MTqr6731g9WNH9ZvrH7rrb+xOlwqJ75UhnIKhJuMH9VxncLvFrFk0w==");

                addPart(Liquids.cryofluid,"bXNjaAF4nCWOW27DIBBFL8PDTpqfLCBL8EfXU/WDOqiyhA3CsbL6yp0ZZJkzcB+ACy4WbotrgvnExzPtc1vqaykbgJDjT8o76Ovb4z6XWlOb3jHnKcf2m3Ct5c0nW3km2Nhm3Ja1HnlPUz3WiluX93K0OXHdg38QjMB1BIFMZFih8cprd5FOlj8xOA2QkvjMGVIqXIeHEUsY2X6eRkoGaAePYmaT1QYruwCNkmhOdvIoDfge8F3zohleaDTnH+SuIO8SuA4PKw1hlLtCLxk4YwW2gz3/X54lVA==");
                addPart(Liquids.water,"bXNjaAF4nCWOXW7DIBCEh+XHTpqXHCBH8EPPU/WBOqiyhA3CsXL6yt1dhOBbmNlhccHFwm1xTTCf+HimfW5LfS1lAxBy/El5B319e9znUmtq0zvmPOXYfhOutbz5ZSvPBBvbjNuy1iPvaarHWnHr8l6ONieOe/AGwQhcRxBIRYYVGq98dhdpZXmJwWkDKYnfnCGlwnV4GLGEke3naSRkgGZwKWY2WU2wcgvQVhLNyU2G0gbfG3zXvGiGDxrN+Qf5K8hcAtfhYSUhjDJz6CED91iB7WDPP1+BJVM=");
                addPart(Items.copper,"bXNjaAF4nCVM0QrDIBCLnpbCHkY/xId9z9iDa2UIVxXb0t+fp4QjJLkEM2aCSX4PUC88tnCsNZYz5gRgYv8NfEC/P4RlzaWE6m7P7NjXXwBtV8azpHDt/oyr22pkbr2lHagBqqGTBSloMYWMmCRPEmuMZCg7ypMoI5lQN62M9alBfcV2Le+gPwnfGJM=");
                addPart(Items.coal,"bXNjaAF4nDWPzW6DMBCExz/YmEbtoU/RA4c8T9SDC1ZqyRBkjPL62WUVAfrM7sx4FxdcDOwalwR1xcec9qnmreXHCsCV+JfKDn37tfhuucU1H8v4jKWMJdZ7gpmPBz7vNW7/uaVxq2nf8bWt6Vhiy9M411wKJf3Qhw4aUDACz9B00PRSzzAcdQmBi4YfhoXRhA6K/7wUe1ZaSiWlJYvWrINieFZaSgFLBr6N79accaZ00nNid2J3Yndid2/J8B6WJF7sXno9L6POSU5YgRecA/aiDCIJsmaQNQMrQflUfAFLpyDT");
                addPart(Items.thorium,"bXNjaAF4nCVQy26DMBAcP7AN9FSpn8Eh31P1QIPTIhkc2UH5+ah012uEh52ZtYfFiNHA7vMWoS4Yl1ivZb0/1rwDcGn+jqlCf355vD9+c1mPbXrOKU1pLj8RHyWu+y2Xa1ymehDTRNjbUSPcFvclFgz3/Ixl2vMSMaa50vdSVrK9iVDzQQfQdRPa0rJpRWBhGDoBB8OaF0sPTS7SlPRow10wDEbACxlY19xAPKuWDcG0Cyxw0tLM+dBq6POP2Nd5QrWWwGms5OroVR34UVwFKNYGUI6OLQytclI5rjh1i9jCWwIT9PliA93NEek4R7QPdDyRLLE7SG/L3v6DwQh4gSDTaFPo2wwIvEDPyQeZ4yBzHHiO/7hMPac=");
                addPart(Items.titanium,"bXNjaAF4nC1Qy26EMBBzHhAevVX9DA77PVUP6ZKtkFhYBdD+/Kp0nCmRcDx2ZqxBj97BL/GeYC7ox7Rd8/TYp3UBUM/xO80b7OdXwPs+7XGZjvvwjPM8zDH/JHzkNC23NV/TOGyHVIoIfzu2hPqeljFldI/1mfKwrGNCP8dN7mOexPamwrYe0kDmDSif4c/CED0coVKo4agFtbQwVnwVmeU7B3JLcApBi41aWrJikrZwjSsDPHDKZ1gLUvPsfb7OUxLQ3jCJ10wVc1XgMWT/WsfQlWavlNXKajImLvECk3kB19jzxZrMZTxpV0tZxp+/copUxgcRLd+X7Lw7glMICuRiKJtrmZAQFFom7HSPne6x4x7/AG0bQAM=");
                addPart(Items.copper,"bXNjaAF4nCWM3Q7CIAxGP8aPy7wwxufYhc9jvEBGDEk3CJv6+raQUE779QAmTBpm82uEuuO8xD3UVI6UNwCO/CvSjuHx1LiGXEqs888TzeTrO8Lunr4Zl7LFz+qPFOalJiJ+eeOCwQAovhqcgA8GiQwnjFE2mhslcBIaUUQwfWqK7Yrtiuu/uK44UYCTKIKmjLL7AxE/GY8=");
                addPart(Items.lead,"bXNjaAF4nCWM0Q7CIAxFLxtjy3wwxu/Yg99jfEDWGJJuI4Dx921HQjnt7QHMmHvY3W8E88BlpRJyTDUeOwDH/k1c0D1fPW7hSIny8vPMC/v8IYwl+Fop45p2+m6+xrCsOTLL27sULDrAyHXCKeSg08hKIph000tjFE5Dq4oKtk2nMjRlaIprv7imOFWAURXFqUy6+wO2rxpi");
                addPart(Items.scrap,"bXNjaAF4nCWM0Q7CIAxFLxtjy3wwxu/Yg99jfEDWGJJuI4Dx921HQjnt7QHMmHvY3W8E88BlpRJyTDUeOwDH/k1c0D1fPW7hSIny8vPMC/v8IYwl+Fop45p2+m6+xrCsOTLL27sULDrAyHXCKeSg08hKIph000tjFE5Dq4oKtk2nMjRlaIprv7imOFWAURXFqUy6+wO2rxpi");
                addPart(Items.coal,"bXNjaAF4nCVMywoDIRCLr2Whh7If4qHfU3qwrrTCrIru0t+vo4QhJJkEK1YFndwRIB647aH5GssZcwKwkHsHapDPl8Lmcymh2p8jsuTqJ2BpPlf/xb2kcB3ujN7uNRL16tYPqgOiY5CBEpBsMmk2FT9xLDGTqcwsL6w0Z0zDNDw2piaNFTM0v0P9Ae8yGdA=");
                addPart(Items.copper,"bXNjaAF4nDWOWwrCMBBFb95SP8QduIF+uB7xI7ZBArENaYvbdyaDJOHA5J6ZwYDBwC7xk6DuOM9pm1que14XAL7EVyob9ONpcJ3WWlMbv7GUscT2TjDzseJSl3R84p6ncW65FPJu9GCgGI6h+qEboBQ0/zGswAk8RwwpWhE0NOeMwEkxSOMTt7IcYQQZ0z0nnhPPief+ke558bwUA2/Ge9EohhU4ged9Qg+RzNsQ6O8HTR0ZXw==");
                addPart(Items.coal,"bXNjaAF4nDWQzW6EIBSFr4Cgo7GLPoeL7vsmTRd0JFMSdIw/6ev3Hk8mIl+AwwcXGWSw4pY4J6k+pJvSft/yeuTnIiK+xJ9UdjFf307ej3zEJZ/z+BdLGUvcHknsdD5leGxx/c1HGtct7bu8rUs653jk+zhtuRQ1feovtVRAACqxYoAWI6Od0YYI4IlANIx0GFn9LjipjKIWCwSxmGy5dsMGp0ep0+lOYxQWSacRi8kON3HS4wo1IlCpDLhkNSOeFk+Lp8XT4l+RyxJoCbQEWgIjDS0NLQ0tDS3NK9LzOa5Iy8JaWlpGUJaObtgOOKIlMK1dh9o7RjpYAE8EAucrrpfvEQF07R/hMyJm");
                addPart(Items.coal,"bXNjaAF4nDWOQW7DIBBFv4GA7Ubtoqfowouep+qCJihBwq6FiXr9zmcUYevZzPszgzPOFm6La8LwiZdrOi417y3/bgB8iT+pHDBf3w7vLbe45ce6/MVSlhLrLcHdYy54vdW433NLy17TceBt39JjjS1flmvNpUirD3lxggEGWEUgjHwYeaQ2EF7+BRMvLQ/hYI2gK1ZyHSNNJ13FdBIxhh4GoitOuoDKzGmcbdijdzlpzWvca9xr3GvcP5X5uawoQeNBa1xBFhx1z5HzicDAKKcr3ZxUmSTewVGCQMy8/AfEeCEc");
                addPart(Items.coal,"bXNjaAF4nDWQwW6EIBRFn4Ago7GLfoeL7vsnTRd0hsyQoDXqpL/fd72ZiJwAlwMPGWW04pY0Z2k+pL/l/bqV9Si/i4j4mn5y3cV8fTt5P8qRlvKcp79U61TTds/iHqlUGe9bWh/lyNO65X2Xt3XJzzkd5TrdtlKrqj71l1YaIACNWDFAxMhoZ7QhAngiEB0jPUZWvxNOGqNoxQJBLCYj1y7Y4PQodTrdaYzCIuk0YjHZ4yZOBlyhRQQqlQGnrGXE0+Jp8bR4WvwrcloCLYGWQEtgpKOlo6WjpaOle0UGPscZiSws0hIZQVk6umA74IhIYFq7HrX3jPSwAJ4IBM5XnC8/IALo2j+PhSK9");
                addPart(Liquids.cryofluid,"bXNjaAF4nFWQSW7DMAxFvzVZbtJVT9ADeNHzFF24jlAYkAd4QO6eJlU5dFMY9KfIr0faOOFk4aZuTKjecLqkrV+HZR/mCUDI3WfKG8z7R8DLPuzdNBxje+1ybnO3fiU8/yviaZmvaW2n+ZL48tSnFedhXI68pXY5xgVndWzzsfaJRrxSwKBicSpBJbJwZioyGMjLxga2FFQU2rVUdfJ4yvxfRasigX2GcDKogTgotdIVbyQqE5nsyoPiG6bcgHIrpdwpfuj8kBmerzvUurDgvOJ4PvWC9oL2ODeGxMSm3CHTiWZlQoEnqqfc8ddQnQE178fiefPIVE9SR1g+Rv05Qqe12Uz/RMWrEOEXxmlRDA==");
                addPart(Liquids.water,"bXNjaAF4nFVQSW7DMAwca7PcpKe+oA/woe8penAdoTAgL/CC/D1NqnLppTDoocjRDEWccLJwUzcmVG84XdLWr8OyD/MEIOTuM+UN5v0j4GUf9m4ajrG9djm3uVu/Ep7/FfG0zNe0ttN8SXx56tOK8zAuR95SuxzjgrMytvlY+0QWrxQwqBicQlCIDJyZiggG8rOxgS0FFYV2LVWdfJ4y/1fRqkBgniE5MWogDEqtdIUbSZUVWdmVB8U3TLkB5VZKuVP80PkhHp6vO9Q6sMh5lWN/6gXtBe1xbgyBiU25Q9xJzYpDgSdVT7nj11CdBWqej8Hz5JFVPUFNC+Fj1OWIOo3NZNqJglcghV/GE1EK");
                addPart(Items.copper,"bXNjaAF4nCWPQQ6DIBRER0FEa9L0Ar2Bi56n6YIqaUioErTt9ct3FuSF4fEHMGBQ0It7e1Q3nGa/TTmkPawLABPd08cN9f2hcJnWlHwefy7GMbr88mg2F78rzmnxn7fbwzTOOcRYbl7LgoYSGEGFmrCCusRKIk0Yhr3slJiCw9RiiqcJw/AwG5oNTUPTlNNaYKTF0GxptjQt2y3bLWdaMYGO7+xo9vxDL8of/zoafg==");
                addPart(Items.coal,"bXNjaAF4nDWPyw7CIBBFb+kL26Yu/AaXXfg9xgUqURJaG0D9fZneGBJOZsK5zGDEWKJazGxRnNDfbbwFtyb3WgA03lytj1DnS4VDcsks7j1PX+P95E14WNTR+M8L4yOY9emSndZgY8R+Xex7Nsndpntw3uesIyQQCihQEVqg5BT5alh1gjK/zc1SXgo0m8NfViqjYdVJcC2Cyqgko87C1twECVZlRsNqE1oKLYWWQktB8wdNQVPYcaSdCALN5iDouEMnglTbDj3X7Dn8IGP8APFJIZM=");
                addPart(Items.coal,"bXNjaAF4nDXQW26DMBQE0IvNwxhKInUdfETqbqp+uImVWjIEAWm23zuMiiVGfpzrh5zlbKWcwxSluEh3i9t1TcueHrOI1Dl8x7yJ+fwq5X1Pe5jTcxpfIecxh/UepdpC/n3IcF/D8pP2OC5r3DY5LXN8TmFP1/G2ppy11oegoBQIhyikFIPw6Bm0Qn+6BOE42COsEp3Tc2LOElgZ/msURqMWi3BiMXi4Cs5olJir1FkMHq6GsxrqEIer6Rq6hq6ha+gcnaNzdI6upWvpWrqWzvOcnuf0dJ6u4/063q/j/To40QUGK3u+S8936bXp94aNEB69AaURWvoPLy0jBw==");
                addPart(Items.thorium,"bXNjaAF4nCWMSw7CMAwFX5pPi7JjxyG64DyIRaARRHIpSgpcHztWFI/1PDYiooV7pTXDnBGX3O61vPeyvQAESrdMDcPlanHcn1stn3X+JaKZUn1k+JbouyFSarnOSy1EvHbiDwcjCIpJIF3HACPFKboyqGJFMQxWBnh+nDqMMnM4CLwqXq8EnQWdSc/hyAc6vISTKpMqUjqsgpU/ME0Ysg==");
                addPart(Items.thorium,"bXNjaAF4nCWPSw7CMAxEpw0NIa1YcAh2XXAexCJABJUMRSmf6+OJFVUvtd+4LrbYOqye6ZHRHNBf83Ip0+s9zU8AXtI5y4L2eHLYve9zmT6P8ZdERknlltEtSb4zeklLLuO1TCIa2+uDFRrCGwLRoNWjiHzTG1qW1CS8IVhvoOcYIKJN1EaLTg+IYMVqdmZ2ZnrU8d56a/Y4uOYCcyxG26uawcyNTdlYL9qCUb/hCG8Iuo9ioNJrwBH1jwaahDeo+QdJGRnU");
                addPart(Items.lead,"bXNjaAF4nCXNUQ6DIAwG4F9BZM5k2QW8gQ87z7IHhmQhQSXIsuuvtSHkh/LRYsSooDe3BjQPXJdw+BJzjfsGwCT3DulA+3wp3P2ecyjzz6U0J1c+Af3hXa2h4Ja38F1djX5eSkyJ/k60odFwGI4GLS0Kyzc6oeWShuIwUFwcGChxiuTZgx7YUTOOs5kW2YnsRBqRRqQRaUT2InuRVqZbmW5lumUJXEgqDssxMOEg8gfPEBsi");
                addPart(Items.scrap,"bXNjaAF4nFVUW27bMBBcPSxRFEXSTtBb+KPnKfqhOkIhVJYMWU7QHqyXaxp3h9MUKGB7uPQ+ZrkPeZQPhZRzfx4k+yjt03A9reNlG5dZRKqp/zJMV8k/ffbysI1bP4+38/Gln6bj1K9fB6mvp37bhlWqdbkB9/+0Tsv8PHxfVun+M5Ty2zjN0o7zaZyHtd9Uo7ouK4ztZXkZ1uO8PA16vk3Pwzr+0PvmOlz6v6rnYYJquMzD7dxv4+n4tI7q1tH2utzW06Dcf+pXcskAJaEiGIIlOIInRMIBAONcP1JIAdgRakJDaAVa0hECYc/YD/BS4pgp5JLnCirrg8MZpIpSTclQaihZ+CgRAZfKE1JHyVMKlCKlPaKXSj6l/Ij/doiXKWg8BK0JRgpcNYSWlx3BI7Gdui5gvufDpRwq5lAxh0qdZQBD6GBQwRwqiUtFLhW51LBDrjtJxlqOWqFWMcuMnoyx6d2gzKLt+Oz6hbPd/U3k/qa/WdGovtIHWEJLcFLAV2e0aqmYOSwKRPIpEkIHyUAkktaeXZFyNMzRkKsBV6ugL9AqGPSL0cAJWl52kkPF0yAiA8P8DfM3zL+hzwY+WwVDUHNcRqrs2ZqJiwWXQqGEiiUXSy6WXCy5WHKx5GLJxdKnVZ/J2YGuHyW1beLSkktLLi25tMyhVbsMzhMXRy6OXBxr6FhDxxrquzvUEFV6f/f773Qu7q96fk3n8n7XSv5KNXSsoWMNHWvoUMNOf1DDNIao+P21QKT3GjrW0DFHxxwdc3TMsWPvd+igQiG1a4d2Ra83hJaXHVVS73cYK5in/D3z98zfM3/PQfIYJEBH8PDiycWTiyeXwJkJnPvAuQ+c+8C5D5z7wLkPZBY490EjJMlTCpQipcQTm0vJR26ryG0Vua0it1XktorcVpHbKrLlom6rtPiw7hRKQkUwBEtwBE+IhIMUfwDzMXjZ");
                addPart(Items.scrap,"bXNjaAF4nCXNUQ6DIAwG4F9BZM5k2QW8gQ87z7IHhmQhQSXIsuuvtSHkh/LRYsSooDe3BjQPXJdw+BJzjfsGwCT3DulA+3wp3P2ecyjzz6U0J1c+Af3hXa2h4Ja38F1djX5eSkyJ/k60odFwGI4GLS0Kyzc6oeWShuIwUFwcGChxiuTZgx7YUTOOs5kW2YnsRBqRRqQRaUT2InuRVqZbmW5lumUJXEgqDssxMOEg8gfPEBsi");
                addPart(Items.coal,"bXNjaAF4nDWOUQ7CIAyGf1ZgZj4Yb+AF9uB5jA/IiC7BjbAZr29LY4B8pe3XFAMGgl3CO8FccZzSFutc9nldAPgcHilv6G53wjmupaQ6fkPOYw71meC3uNb4wqks6fMO+xzHqc45s3rhB4IROIHh0wl6kESksAqn8JyWi84wuL31gQSthVhvgw/yszrRatKp59Rz6jn13L+leV49r8meQ2p7KazCKdpKfauKrODaD728Gq0=");
                addPart(Liquids.water,"bXNjaAF4nEWQwW7DIAyGHaCBpr3ssMfIYc8z7ZAlbIpEAoJGffmqzD+uNHL4Ymz//jFd6KLJ7NPmqfugy+LLnNd0W+NORH2Yvn0opD6/LL3NMSWfx/sUwhim/OvpPft1/4l59stYDr5pSbK3crDiSv3m98VnGlK8c+ceF0/XdUtHKH5Mx5boKpkSDxbhiSP9H0XUMQxp4CToSSNnpeSMKsU5KSdOIlaAFljqAActhQYtRQY/jt/ehEzlo3FnHQvVR63ch1Ju7KQIcxSM4GuRg0NDA9A8AC3qJeoRvdyyNFwZBuZi5gNFPBf2Tnib5fG6PusTFri/a11ONJp3Bw1AC6xcutc2BEpgBWc4GWSPg+xxwB7/ADmIQPA=");
                addPart(Liquids.slag,"bXNjaAF4nEWQy26DMBREx49gQrLpop/BIt9TdUHBrZB4WBCUn4/q3vGNVHtxuM8ZjAsuDn7p5ghzw2WIe7+N6T6uC4Bq6r7itMN+fAa89WtKcWsf3TS1U7f9RLxvcVy+162PQ7sfkilFhPt+yMYR1RyXIW5o0vqQyWUdIq7jnI5pj2065oSrVvb1kCWi2OL/WMAIPBxxUlRwrAVtObPLSk3bIUXGlnCKAEPU3GU54LTJ86N2RcADWY5hLkjOc1V+5gxj2C7DRpPUsjTDW6KaLj0aovggSlRpVDF6OZb1dOYFrrb5yQbRpb0T/y1QPv/KLaUiHyjB+eK95jzhFEGT9es1FFYRFGduavQdG33Hhu/4ByjIQOs=");
                addPart(Liquids.oil,"bXNjaAF4nEVQy26DMBAc2wQ7JJce+hkc+j1VDxTcComHZYLy81HojjdSzWFY7+zMeHHBxaFaujnCfOAyxK3PY7qN6wKgnrrvOG2wn18eb/2aUsztvZumduryb8R7juPys+Y+Du22y01pwt+2XRRH1HNchpjRpPUuk8s6RFzHOe3TFtu0zwlX7WzrLiLi2OL/WMAIVHCEk0INx55XypksKz2lQ5qsLcEpeBhCoJblgFNSxZ8gby9C1SGHVvBBhI6HVKZQZdAoiT6WQfiVKjBhhYZQMhBKVWtVs3qlFWmmqgToS88HSeLEeCe+zYu9O57HkxFk3hhOBdUo2QM1CE7B62V4bUPBKniFM5M0usdG99hwj38zFUDo");
                addPart(Liquids.cryofluid,"bXNjaAF4nC1Qy26DMBAc2wQbklvVz+DQ76l6oMGtLPESYOXno9Adb43EeB8zs15ccXWo5n6KMB+4DnG/b2k90jIDqMf+O4477OeXx9uRjn5Oeeoe/Th2Y7/9RrxvMc0/y3aPQ7dnyZQi/LFn0UyopzgPcUO7Lo+4dfMyRNzStOZxj92apxU3rexLFhHx7FCO1Z81AhUc4aJQw7HmwQANjHRJ7Z9jHVkKTkE6CYEES4JDaRJZuCCvL4bVKafkfBCh83meIs1WIRptoo/lIPxKFADWWkKZgVCiWqOaEacFnEgLyVQC9KXnk03iy/EufJsXe3e+zhdHEL4prKAaDco4lk8Ici3gNRl0GwwErILXZMNJWt1jq3tsucc/9rlB4A==");
                addPart(Items.coal,"bXNjaAF4nG2RwW6DMBBEJxhCiBtsRe2p38Chf9D/qHqgiZsiEYJMov5+d5hrbaFne3aW9RpHPDuUU39N2LzBn9NyysN8H24TgO3Yf6VxQfHxWeE1p2H6vuVTOnfLI19S99uPYzf2tsTLvyK2lmseE9pL7uef4Z66OadlQZin9Lj29+HUnfNggcC7fahREI3giQ0q4YBiYysHR5RwXO9QEF5akBYJx8m4iiElWoY402yUFlQQkajspHCGWmgEL+1Ie21OO9wyGXHQ4WqvaS8NtdAIXtqRhp18O/p4O9C+Z87SdrXQCF5aVORq38u+l92rXK9yvcr1+p+nAXji/QoLr4RWh5G7Vj1r1bPWigARiaBdsM7YCOp8UOeDzVVbOxj1VFFPFZnsD1xYMMg=");
                addPart(Items.coal,"bXNjaAF4nDWS3XKjMAyFhX+AEMAk07s+Axf7PDt7QRNvlx0CjEmm794m8Uo+2zDiM/KxpEimF3rRZObh4in7Qfuz305hXK/jMhNRPg1vftpI/fxV0Gvw4/x7CSd/7rdbePf9xzBN/TTwkvKLn88+ULGt/nQNnlxS9mtY/rJjCdS+h2H9M17F57eN6nX58KHflhvryK2zv12G63jqz2GcJk7+RumXyUsBBsiBEqiAGmiBDjgKlATIGArQpAUGsKTFVVDa2UtCxVHSngM64CCRFMdkiZZg3Dh+EpyE1pAY5DPIZ76d6ZyF08KZi1Jxbi17OT/K8KugTLAD9pTJXgOlQwM4mOWvvCzjIz75uTPv8StGNvmS9YPtrkXXiI5UfJJhmvhFGZuK97TWbEZLQ0kKKKWOnFGSFlSkxVmj50f0PBW+l8INf6UDFQ5UOFDJAZE4HDhiSOyzjLzccdpnKkGz2RjZpDxZP+InCy0Xb1hbUMIO4JwSoJEAaaY8Rzb7v7kmrTUCpH9Vo/ONFClopPMtZtRijE6GJeNOt8DJLRCng+SAe5WiOMzPwdkhSgdn9+1MShmwFmjAABZoAQd0wAFXVpIzFGCAHCiBCqiBFugADvAP0I99uQ==");
                addPart(Items.thorium,"bXNjaAF4nC2R3W6DMAyFHUIo4ydFvd0zcLE32HtMu2Alm5hSQKFVn33q2swnXkX9kcjHxzZ0oIOmfB5OjtQL1aPbjmFaz9MyE1Hhhw/nN8re3gt6Dm6aP5dwdGO/XcKX66+D970f+JXsyc1jv4bl2x3PS6ACZxdot618ERw163J1od+WCxeg2g8bn8Ywec8+r5R+mQQF5IJCUAoqQSOwgg6ANFMMliPkpAFDWpOmHSmgJoUUK4IOQSOTHzKljjHe+Zgxo0YHnKBRAiVzlmlgL911CIYsy+5IjL8xsgss99I4Xxp2Zm+gQgs7auFdQI2hksETDHDiS8N5SVDJrJWk1KKrRNfgMmNkaL0hXUrTD8rjDYvj9jUrDCxbrq+gSeO20ABYB0+UlmaxNMzXlOZ/lAf/b/GHS6o0lGUJITPtzkqxPToBWvkMhJpdWiojFxSCUlAJGoEVdKT/ABqTUDQ=");
                addPart(Items.beryllium,"bXNjaAF4nCWQ226EMAxEJyHLJay2L/0NpPZ7qj4EiFqkcFFYtOrP79aDieBge2yNgxtuBdwS5gjziXaM+5Cn7T6tC4AyhT6mHfbr+4L3Pua/lKZj7h4hpS6F/BNRznEZY8Z1Wx8xd/t65EGyfY5h+MXbtsRjDvdp6MY8pSQzP3A+Vj+GcIpSUSu84kpQbI3AoiAKFPx3tdReTyOhNFuiUV2r06+QlJNXkoUmHQvSirY2ryc43VHncJFjiYqjSjFg6QmMKs5gdCorVVZU0u8pabShpsTSR00jEHNeWixxyr3u4vUGPOVGrFmcthuuJHWmPJdt5RS8BiiswilKRa3wCtH+AwoTKQI=");
                addPart(Items.tungsten,"bXNjaAF4nCWQ0Y7CIBBFp1AJYsXG/Y4+7Pv+yWYfaiWuBltDa/z1NdGwM9w25TBlzg1Ae9prqsf+Gqj6pM0xzEM635bzNBKRif0hxJnU94+hj+U+nuYljN2jj7GLfToF8tcwHrtbmi5hWKZERurAPKTQD7/U3KZHSN083dMQaHOI/bx0x3SOkeO/qDwKQyWoAQNYwAEN4IFWIKqqGPzKUJPSpEUXrAH2pMVLt2KPu/jItuKRJa5IrJWUNVeeV1R+KUnbUWlgZ8WhHCtwEms4r2y0bMJIi+b9aqksEtdosdAddIfTOLRsIDjoDYQGmY38VLSVs9UMbVV+k85P0mx62bDAApJUdL4OD8XT1ur8ym/+nvkvv8tSuQaP7F1Jl7nYLc8LasAAFnBAA3igJf0Prxc8Sw==");
                addPart(Items.coal,"bXNjaAF4nC1QW3KjMBBsSRiEIdiVe/CRK+QaW/tBjJIlhYESduXi+/5YpUezDKjRzHTPA494dCiW4RpgntCMYb/EabtN6wKgnIeXMO+wX756nPd7fAv9xzDP/TzwF+U1LGOIqMbp9fW+B5y39SPEflnH8D+lk5R+i+t7uNzWiFYz9vUeL4y+xWH7Nt0CM8K+47Qt4X4dbtOlH+M0z2zhmR8cjc9BoVKoFRoBgwdYgZOAFTM8CjjLcKlAgqOz8R4u/YVJfwB+R9Ja+o/0iUqTfhtkbWo40eDLiBMn9W3ug85Cax808yBuOUoPUxLZpkAtxUt0mnqGLSFuCDQ6DkTUi2gFMStHLZNUSvBK8EKwjGSxWniSVylkQi0Emxtj9aPyjlpIDitFK+TatYi1KtYIr+AtD9FySTCyllaCD9yQKfIKOEsnrQo0GiPR8XbwVfqXhyEVPv3icn+klH6iTN9TolqnS+xo7PQEo+BkeyeNnVnfClSMEmqFRoF9fAJc1FAr");
                addPart(Liquids.water,"bXNjaAF4nCWSXW6DMBCE1zb+wRBF6j146DF6hqoPJLEqJBMQBEXt1ZOG7nqE4o/N7ngGAx3paKi69mMi9U7NJa3nZZhvw3QlIpf7U8or6c8vR2/nfjkNl9Td+5y73C/fidp5uqelW6dtOSdyY7pe0kL1up3yMPY3Hkg5nW/LlH9+udEO47zlNXXzNs68/wf/SJMSVIADAhCBViB3WvG4DnWRaWmR4Qv6qtDy/1ZWJROGCirAAg7wQABqIAINKbFq2arY7C/Y7H+w2Z+8mfBBdn8piXNAGr4VW97ElExaFo8qotdSiaSlqqSS9AeEL3KLnsWkk0r2qMhYhkdVkjrIHeQecg+5hzygCpgMmKwxWSNnRM4oG0gV0SvyCHmEPELeQN6g10hP8aJDkAYfVjkLPiw5qPJ8fFDlATlzK+9MUAEWcIAHAlADEWjIiE0bIiz2l1jIW4HN/oTN/uDOQy4lH0+JfOCERmAAC3igBtjgH1AYVXc=");
                addPart(Items.tungsten,"bXNjaAF4nCWRYW6DMAyFHQIZBRpS1mvwY5fYJab9oG1UMaVQhVQ9fFUxmxeRfGDnOXmGjnTUlE/DzZP6ovril3Mc72mcJyIyYTj5sFD282voMz2m65L81D+HEPowxKsne/PTpb/H+c+f0xzJyLePVKQxDRM19/npY7/Mj3j2VJ/CsKT+EscQuPo3bSPDogQ5YIASqIAGsIADOoFUyBSDq8iSkxYUpDVp+gBqBC22tKRE4CDvZNGi44eKMudXKWNWjLeSuC15y/peV1I8MyWSVgrkcr6W8+SogoNagh2scK7gS/A1BLXkDHIldDvodtCVyFXQ1dDV0FXINdDtodtD1yBnkWthu4XOIufQJ4c+Ofh14nd9UcbTbM5o8+vE7/pa3+xQbUKRtERSYOubQ98O6PdB/Assgi1+D8n53dZeRg4YoAQqoAEs4AAu8A/TX0WF");
                addPart(Liquids.slag,"bXNjaAF4nF2U627aQBCFx15f1uBg8C2N+gz86PNU/eGCm1AZjGxQnqmvmJLQOXswUiNkvrU9c2bnspYneTISHJp9K943mW/bcTPsjqddfxCRqGt+tt0o/vcfiXwd2t3hVz9s2u16PA/P7fq16bp11+hSZsf+tR3Wh37bSrRvD9t2kMwZr49D/7vdnPpB5c7jaWjlS3Pa9+PxpR12m/WmP2zaw2loYJFSZuzP6inzsWue1y9tc1K1dLc/nrtR9c77o+7tj17iiwcERERYYkakxIJYEjlREjWAle+pom9jVRIJnbCIcXEYLZDAqIU+83x36xASERETlkjEg+jMRmqtMte/el30etfrQ4LrxRiVS2m2sFM0c4+VUWhJrIicKIiSqChS2+QmwQSQyKeY4l8/DAI9ypQhklIRFxdyBkXA2ooBMjF4mHNrJU1qFsWHewAHICdqdsZFCBkhpGUIS/ylvHMOIcUimkQqboCACMVEisgmunlNQP/NbfWhKYm73m8Jigfb3EaT7fR8ytWDZEHlkqiImnPkNh1z0zF2hDIEKIRlPSzqATiVmHu3sIy15S4vSzFLsYRiCdNL6DDDnaeote/+9Trt02B6ndscbhgPnbhQESFAqv7GKlJsMsWE4F1OywqjnNL9gVEXTCFjChlSiBWpuL5mfKiZeGpZ21BLevlvgjClOEJOM6NmxkwyZrLk3ZI5L2m5ouUK79DllHfOIadDzj7n7HPOPufa57mbPXTv3XXwcn3T2rxphXR075u79d0LdPeuLwWygUJuLbo/qSDa/SzfVh4CFoxbEhVR8xvhkiiYRMHDUfJwFJz1gseh5HEomFnJzEqYGFVM8a7kkJeULildUbqiQ0XNipYVLD39862dNs5kb2cZ35E3t8ZnomYda9axRh2BiIgJSyRodK2fpGCqH2Twc88X1p2qe9ApQEbvJbEicqIgSqJigFoP4P3gfQqEj66rwaMmaQBDhERMJMSceCAyYkUUhMb8Bz4B0y8=");

                addPart(null,"bXNjaAF4nDWM0Q6CMAxF77oNUNQ/4cHvMT4Aq2YJbDggxn83YsGYZe3JPW1hkWuYUPcMdUbpeGyTHyYfA4CsqxvuRtDlmiPrOThO2A/xyakK0TEOPx7jnFpG2cy+c9W0Zij9xP3fnO4pzsFVt7qdYnrh2PnH7N1fA6V8EEhJ04UCLR8iYVMQzLKIMkpLFW+kaAvsyEpg1w0oI7iGigz0RpYyGSVrZFAJydXlvXl54jP6AqBmMCk=");
                addPart(null,"bXNjaAF4nDWMUQ6CMBBEp9sWVNSb8ON1jB9AV9MEWiwQ492NuGBIs+3LvNnCItcwoeoY6oLC8dAk348+BgBZW9XcDqDrLUfWcXCccOjji1MZomMc/zzEKTWMop5868pxyVD4kbvNnB8pTsGV96oZY3rj1Prn5N2mgUIGBFLy6J0CzV8iYSNs5llpkeKMXNoCe7IS2KWtlBFcQkUGeiVLmVTJGinK/vLj/Fm9HPEZ/QDpFjAk");
                addPart(null,"bXNjaAF4nDWMUQ6CMBBEp9sWVNSb8OVxjB9AV9MEWiwQ492NuGBIs+3LvNnCItcwoeoY6oLC8dAk348+BgBZW9XcDqDrLUfWcXCccOjji1MZomMc/zzEKTWMop5868pxyVD4kbvNnB8pTsGV96oZY3rj1Prn5N2mgUIGBFLy6J0CzV8iYSNs5llpkeKMXNoCe7IS2KVNygguoSIDvZKlTKpkjRRlf/lx/qxejviMfuo3MCY=");
                addPart(null,"bXNjaAF4nD2NwVLEIBBEewbIxl3d4/5FDn6P5SEbRouqBFYCWv67ZRxi6QF40930wOFoYOO4COgRJy/rlMOthBQBdPN4lXkFPz336BaJXjKOt/QheYjJC+5/eU01T4JTKLL8D9caZj+UFsD5Naca/fAyTiXlT1xG70MJ7zJkmVJcS67NwMMc3mrwfx3AWQ8YTPqYnsDbN7Oy7VWEhd02MjofHHBHVjVNGr1ME9ip4NpfUKfYQ8O8e8RGc40cH7SIndU8Kema7Wv3tV79bqe2wNAPIxc5mQ==");
                addPart(null,"bXNjaAF4nD2NwVLEIBBEewbIxl3d4/5FDn6P5SEbRouqBFYCWv67ZRxi6QF40930wOFoYOO4COgRJy/rlMOthBQBdPN4lXkFPz336BaJXjKOt/QheYjJC+5/eU01T4JTKLL8D9caZj+UFsD5Naca/fAyTiXlT1xG70MJ7zJkmVJcS67NwMMc3mrwfx3AWQ8YTPqYnsDbN7Oy7VWEhd02MjofHHBHVjVNGr1ME9ip4Npfok6xh4Z594iN5ho5PmgRO6t5UtI129fua7363U5tgaEfI145mg==");
                addPart(null,"bXNjaAF4nD2NwVLEIBBEewbIxl3d4/5FDn6P5SEbRouqBFYCWv67ZRxi6QF40930wOFoYOO4COgRJy/rlMOthBQBdPN4lXkFPz336BaJXjKOt/QheYjJC+5/eU01T4JTKLL8D9caZj+UFsD5Naca/fAyTiXlT1xG70MJ7zJkmVJcS67NwMMc3mrwfx3AWQ8YTPqYnsDbN7Oy7VWEhd02MjofHHBHVjVNGr1ME9ip4Npfpk6xh4Z594iN5ho5PmgRO6t5UtI129fua7363U5tgaEfI6U5mw==");
                addPart(null,"bXNjaAF4nG1Py07EMAycPLe7ZREnTpy498D3IA7dJqBIfSxpC+LX0UKxHe2NRo7diT3jwQ53BnZshwj1hDrEucvpvKRpBOD79hT7Gfr5ZY/DefqMuRmnEOGHOIaYcVOweVpzF1GnJQ7Xn4dh7Zd07lPXLukjNjl20zgvee2WKaM+rakPzcLjuH3L0zqG5rXlty/ctyGkf4aOfXpfU7gqAI8UqCsNtf1SfCsFxZCSY/jSJRkqKNnKQ1MvJH4oLjR3IeyiiQWustRotw2eaKQSTl9YdjCGUuWAveD7AhwEsKRyhGJN+lzR5MvysyYO+LIe8dCo4rXALUZalKYFC2AFcAS44scVP479VMztLFGyiq0qNiFjEGm3bYRAQmxqQ32OmLwQ+sIkZpyYMYqBfQHYjFe0K2qqtPoDymtgpQ==");
                addPart(null,"bXNjaAF4nG1Py07EMAycPLe7ZREnTpy498D3IA7dJqBIfSxpC+LX0UKxHe2NRo7diT3jwQ53BnZshwj1hDrEucvpvKRpBOD79hT7Gfr5ZY/DefqMuRmnEOGHOIaYcVOweVpzF1GnJQ7Xn4dh7Zd07lPXLukjNjl20zgvee2WKaM+rakPzcLjuH3L0zqG5rXlty/ctyGkf4aOfXpfU7gqAI8UqCsNtf1SfCsFxZCSY/jSJRkqKNnKQ1MvJH4oLjR3IeyiiQWustRotw2eaKQSTl9YdjCGUuWAveD7AhwEsKRyhGJN+lzR5MvysyYOeF5PKeKhUcVrgVuMtChNCxbACuAIcMWPK34c+6mY21miZBVbVWxCxiDSbtsIgYTY1Ib6HDF5IfSFScw4MWMUA/sCsBmvaFfUVGn1B8saYKY=");
                addPart(null,"bXNjaAF4nG2Py07EMAxFb57TmTKIFStW7LvgexCLThNQpD6GtAXx62ig2I5mRyMn6Yl97Ysd7gzs2A4R6gl1iHOX03lJ0wjA9+0p9jP088seh/P0GXMzTiHCD3EMMeOmsHlacxdRpyUO15+HYe2XdO5T1y7pIzY5dtM4L3ntlimjPq2pD83C5bh9y9M6hua15bcv3LchpH+Kjn16X1O4dgAeKVBXGmr7pfhWCoqRkmV40+UwdKHDVh6aciHxQ3GhuguxiyYVuMpSot02eJKRm2j6orKDMXRUDtgL3xdwEGCpyxGKe9LnSk/eLD9r0oDn8bQiHSpVPBY4xUiK0jRgAVaAI+CKH1f8OPZTsbazJMldbFWxCSmDtHbbRgQSYlMbynOk5EXQFyUx48SMUQz2BbAZr2hW1I6H/QPLyWCn");
                addPart(null,"bXNjaAF4nG1R0Y7UMBBzkjabbtk7JCQQ0v3BqQ98D+Kh1wYU1G1Ktr2DX0fLFk8CLwhV0aSe2B4naPDOoJr7s4f6gHb0lyGFZQ1xBmCn/slPF+iPn1rcnf08dkuKX/2wxoTjEl986uY4erRh9efuErc0eLz335c4+3kN/dQlP8T5sqYtc6xo+IRXhfuH8HDepjUsUxj6NTz7fzjt0xamsVuFgbsvKW4c43MvvR94249j+A/pNIVvWxj/OgCPXDjBKJZ7p1HxU/vVaP6/roGjUlBonSF62/fc/ak0sXt2G8VLwhuoCkKAFAUjRZdioBVL5Sz0fgPy+sV1FRtiV12zXzs5XNHAUibvsqYtKgcYUXTZUoCmAMcMWPqeZAYjMxzyFNm6krZ2BKzkg2q4beRkhWKTjyhdoS5AnYGagC15bMljRe8o49RUYCR6OicheMpkKmn7TgR55ZhaAtQUtFnQFqUcxuYwRgnQFEDCWCVhW+7owd1JWod815Yv4fhGvLb9xlkc34eR1W/+/npr");
                addPart(null,"bXNjaAF4nG1R0Y7TMBCctRPXaegdEhII6f7glAe+B/GQSwwySuPgJnfw66g07K7hBaHIWmfWM7Njo8E7i2ruzwH0Ae0YLkOOyxrTDMBN/VOYLjAfP7W4O4d57JacvoZhTRnHJb2E3M1pDGjjGs7dJW15CHgfvi9pDvMa+6nLYUjzZc2bcpxohIxXhfuH8HDepjUuUxz6NT6Hfzjt0xansVuFgbsvOW08xudeej/wth/H+B/SaYrftjj+dQAeeeEES1zuvUHFH+1Xa/j/dQ0ciUBovWX0tu/a/UmGsXvuNsSXhDegCkKAFIKVYkqxMMSl8g5mvwG6fvG6ig1jV1Nzv/ZyuGIDxzK6U01XVA6woujVUoCmAEcFHPueZAYrMxx0CrWupG08A07yETW8beRkhWKjR8hUqAtQK1Az4EoeV/I40TvKODUrcCT29F5C8CmrVKbtOyPQpTGNBKhZ0KmgK0oaxmkYSwI0BZAwjiRsyzv24N1JWge9a8cv4fmN+Nr2G8/i+X04Mv0G/9R6bA==");
                addPart(null,"bXNjaAF4nG1Ry27cMAwcSZZWXneTAgVaFMgfBD7ke4oeHFstVHgtV2vn8evBdh2SSi5BYRCUh5whR0KNbwbV1B0D1B2aIZz6HOclpgmAG7v7MJ6gf/xscHUM09DOOf0J/ZIy9nN6DLmd0hDQxCUc21Nacx/wPTzNaQrTEruxzaFP02nJq3Aca4SMT4X7Rrg5ruMS5zH23RIfwgdOc7/GcWgXZuDqd04rrfGr49ozvnbDEP9DOozx7xqH9wnALQUOMIrStdeo6FPb2Wj6/2yBvVJQaLwh9LJtUn1RmrBrqtaKLglfoCowAZwUDCddkoFWlCrvoLcLIPGP4sxjCDtrS3XrubmiAY5k5CSarqjsYFjRy0gG6gLsBXA098A7GN5hJ1vI6IrL2hPg2J9WNR1r7qxQxkiL0hVsAawAlgBX/Ljix7HentexpECWaKb3bIK6jFCJtm2EQEJsajZgSdCJoCtKYsaJGaMYqAvAZpxis43lZdnKgUs7uWtHL+Hpjejatgvt4ul9yLJ6BQC5em0=");
                addPart(null,"bXNjaAF4nG1Sy5LTMBBsPSy/kl0KqOxScOAHcuDGv1AcvLGgRDl2cGwevw4hpmfMXqg9yDOemW51S0KNtw6+b44R5h3qNp4PYzpNaegBhK55iN0Z9sPHDW6OsW/3p3H4Eg/TMCLIfxxRpyke9+dhHg8R1Wn4Hsd9P7QRd1OcxmZK3+J+jIehP0/jrMhX8cdp6GM/pab7r7VZ8f/Y3hznbkqnLh2eoqkf5tS1+0kQuPk8DjP1fWqk9xO7pm3TE6Btl77OqX3cAXjPhedwEl7CGYYdnGW4y4CNkcZ94ZAtv4HlwnXljMEWzjHcsuXhl4UMZrkQaPCMwMpItmNWGo8Mr0Emi7qQsSvHPeMvDlnc6lDGoRcwZlUiwYgaAWlgIh9fBNjlKjK4/qgk7svaxQb2s8ITIXoCaTRTzrCy5GLNodAtpVCuhUoLOTVsRYMXDQX/rHLAS9uWLASj8iqmpUxm0G0yHTHW6LYsBC1kLOSrn3z1k4ufWnRknpSiyBeFmOCUUyhhy6InfXm0aS3nMhIGJQwrk5rJ1YwzUijXgpgJPNoCNTMimG2lVehZByO2eXHLRdjVgqOmkhfn9QWU64Mo5UFYhp1eqEG1Po1Kn0Ztamb3CvkLxpKONA==");
                addPart(null,"bXNjaAF4nG1Sy5LTMBBsPSy/kl0KqOxScOAHcuDGv1AcvLGgRDl2cGwevw4hpmfMXqg9yDOemW51S0KNtw6+b44R5h3qNp4PYzpNaegBhK55iN0Z9sPHDW6OsW/3p3H4Eg/TMCLIfxxRpyke9+dhHg8R1Wn4Hsd9P7QRd1OcxmZK3+J+jIehP0/jrMhX8cdp6GM/pab7r7VZ8f/Y3hznbkqnLh2eoqkf5tS1+0kQuPk8DjP1fWqk9xO7pm3TE6Btl77OqX3cAXjPhedwEl7CGYYdnGW4y4CNkcZ94ZAtv4HlwnXljMEWzjHcsuXhl4UMZrkQaPCMwMpItmNWGo8Mr0Emi7qQsSvHPeMvDlnc6lDGoRcwZlUiwYgaAWlgIh9fBNjlKjK4/qgk7svaxQb2s8ITIXoCaTRTzrCy5GLNodAtpVCuhUoLOTVsRYMXDQX/rHLAS9uWLAQ5IGMqpqVMZtBtMh0x1ui2LAQtZCzkq5989ZOLn1p0ZJ6UosgXhZjglFMoYcuiJ315tGkt5zISBiUMK5OaydWMM1Io14KYCTzaAjUzIphtpVXoWQcjtnlxy0XY1YKjppIX5/UFlOuDKOVBWIadXqhBtT6NSp9GbWpm9wr5C8ejjjU=");
                addPart(null,"bXNjaAF4nG1Sy5KUQBDMftLAzK6hxuwaevAHOHjzXwwPLLRGGwwgAz5+XXGwqnAvxh6aKqoqszO7GyXeGti+PkeodyjbeGmmNM5p6AH4rn6I3QX6w8cDbs6xb6txGr7EZh4meP6PE8o0x3N1GZapiSjG4Xucqn5oI+7mOE/1nL7FaorN0F/maRHkq/hjHPrYz6nu/msddvw/tjfnpZvT2KXmKZryYUldW82MwM3naVhI36eaez9xqts2PQE6dunrktrHHYD3tPAchsNLGEXhBKMp3DngoLhxHwzc9hvYVlpXmlE4whgKt9SysNtGDGpbCajwjICF4uxEWa4sHF6DmDTKwGNXGrcUf9GQxq0MORp6AaV2JRwUq2GQBEr4Y4OH3q4sg9YfkUT7Um3VnvouWEKwHk80kgmn31kytmYQZEsu5HuhkEJGGo6swbKGQH9aOGC5rXMqeD4grQpKc550kG2cjCitZFsqeCk4KmS7n2z3k7GfknU4S5SsyIbAJmjKCJRg2yYnvT7a1JrmHBF6IfQ7k5jJxIxRXMj3ApvxdLQBpWOxbOXIrSBn7RXbpovbVmYXC4Y05XRxVl5Avj+InB+EpnCSC1Uo9qdRyNMoVUnZvUD+Asi0jjY=");
                addPart(null,"bXNjaAF4nEVOy27EIAwcHCBp0977Ezn0e1Y9ZAOVkJKwSxJV++/bUhv6AFmDPZ5hYNA10Ou4eKhX9M5vUwqXPcQVgJ3Hs5830OmthV386nzC4yV++DSs0Xk81fcWjzR59OcjzG7YZYY+7H75Y8aQhvdx2mO64XkO1yO4Xw54wf9RaASoQgNSDLqzoPwFlPrkukNxUb4TMW86Khqds6zDVnUroH58+EJzpw3wQIZbq+qHwghHhVOEqtYMMjBFiDoq8WS1sbJgNHuxJydsJROqlnLmDqVKYuINGDazxcxWF873DQOAQyQ=");
                addPart(null,"bXNjaAF4nEVOy27EIAwcHCBp0977Ezn0e1Y9ZAOVkJKwSxJV++/bUhv6AFmDPZ5hYNA10Ou4eKhX9M5vUwqXPcQVgJ3Hs5830OmthV386nzC4yV++DSs0Xk81fcWjzR59OcjzG7YZYY+7H75Y8aQhvdx2mO64XkO1yO4Xw54wf9RaASoQgNSDLqzoPwFlPrkukNxUb4TMW86Khqds6zDVnUroH58+EJzpw3wQIZbK5RSIpQ/RM+cIlS1ZpCBKULUUYknq42VBaPZiz05YSuZULWUM3coVRITb8CwmS1mtrpwvm8D/0Ml");
                addPart(null,"bXNjaAF4nEWPzU7EMAyEJ7/bpXDlxiP0wPMgDt0mSJG67W6agnj3hWA7AipZTsbjL1M49AZ2Gc8R6hl9iNuU06WkdQHg5/EU5w365bWDP8clxIy7y/oR87CsIeK+nbd1z1NEn0o8/11Oe5rDUNiAfkx5eBunsuZPPI4hpJLe45DjtC5byTsP8DCn657CLwB4wv+nYLjp1gy0omY7D12/AakvqhsUla43rWnuOjLCyp6tlVfgG+EAw71zwFH0I+tysuTW/IaC5bF2dPUyUvQeORUDWwyxKLGLYERwGvKs0CQ6Q82BmnGWkISm9B3nRduVvLWSAin5I004OAJ6AfpGkuxWshvFAmX/AY/rTMI=");
                addPart(null,"bXNjaAF4nEWPzU7EMAyEJ7/bpXDlxiP0wPMgDt0mSJG67W6agnj3hWA7AipZTsbjL1M49AZ2Gc8R6hl9iNuU06WkdQHg5/EU5w365bWDP8clxIy7y/oR87CsIeK+nbd1z1NEn0o8/11Oe5rDUNiAfkx5eBunsuZPPI4hpJLe45DjtC5byTsP8DCn657CLwB4wv+nYLjp1gy0omY7D12/AakvqhsUla43rWnuOjLCyp6tlVfgG+EAw71zwFH0I+tysuTW/IaC5bF2dPU8UoreI6diYIshFiV2EYwITkOeFZpEZ6g5UDPOEpLQlL7jvGi7krdWUiAlf6QJB0dAL0DfSJLdSnajWKDsP5CGTMM=");
                addPart(null,"bXNjaAF4nG1Qy06EQBCsGWAWFtd48uTRKwe/x3hgYUzG8HIAjb9uVrG6yd4Maaanqru6a1DgLkE61L2HeULZ+rmJYVrCOABwXX323Qz7/HLEbe+Htpri+OabZYw4TuOnj9Uwth5OOB9xs2PzuMbGowyL76+Xh37tljB1oamX8OGr6JtxmJe4qlh5XkPXVou0o6xDrF5rIb5wX7dt+Kfj1IX3NbRXeeCRgRMSA4MytzDbL+PbWFhh5DBIrCZ6JLCGR5o7WNZC44dxYd+F2MWyBFmeStW2wakMM9V0u8oBifB5BhSKFztwVCBDyp2MDJP3hDD88cmFtgfmzghlcqaFVKZyTeVjibFcYAcyBTJrdQ9dQP04sVLIOhkVaIlT8lxMsCrRSrZtGxFoqE3Lwcgo6FTQ7UpqxqmZxAhQ7ICYcSYlXzLjDGZ86j8Mm2V2");
                addPart(null,"bXNjaAF4nG1Qy07DMBAcO4mbNBRx4sSRaw58D+KQJkYyygsnAfHrqBBmN+oNVVtvZmbHO0aBuwTpUPce5gll6+cmhmkJ4wDAdfXZdzPs88sRt70f2mqK45tvljHiOI2fPlbD2Ho44XzEzY7N4xobjzIsvr9+PPRrt4SpC029hA9fRd+Mw7zEVc3K8xq6tlpkHGUdYvVaC/GF+7ptwz8Tpy68r6G92gOPLJyQGBiUuYXZflnfxsIKI4dBYrXRI4E1PNLcwVILrR/WhXMXYhdLCbI8FdW2wakNO/V0u8sBifB5BhSKFztwVCBDyp2MXCbvCWH4xycX2h7YO0PGmJxtIcpUlKn8KDGWC+xApkBmre6hC2geJ1EKWSejAyPxljyXEFQlquTYthGBlsa0vBgZDZ0aut1JwzgNkxgBih2QMM6k5Et2vIMdn/oPDVFldw==");
                addPart(null,"bXNjaAF4nG1R266UQBCsuTAMi+sxR6Mx8ReI8XuMDxwYkzlhAVnw8utmPVjd6IvxodNN9VR1F40abxz82F4SzAfUfbp2S57XPI0AwtA+pOEK+/FTjeeXNPbNvEyPqVunBad5+paWZpz6hDqv6dJcp23pEt6m7/M0pnHN7dAsqZvG67psygmikRY8O7h/CO8u27Dmechdu+av6R9O/bDloW9WYaBu89J8bqXxA6/bvs//YZyH/GXL/V954D0D93CSXkkyOMMZprtI9/D7TtTsN2eJvSiAkzGwqKOgT+x65p/GErtjtzIOBV7C+ENTkoGTZI/EggB8DLD7E6Dxi3GTMcRutmC/iJ4MGR8oo5VqhkOlBBdyiDpSgOoATgoE7nCWHbzsUPLLqga8tG0kEIy0TMWykpcFdEyhT4z1OpZAUKAgUB5+ysNPKX5OskfhKWlY+RjFBF85pZK270SgoTatlW0oGFQwHEpqplQzzghQHYCYCfy1ETUrzmB1llbUfx14iUrutN9EXS047lTxTl6OyEpPW8lpfwOoenu8");
                addPart(null,"bXNjaAF4nG1R266UQBCsuTAMi+sxR6Mx8ReI8XuMDxwYkzlhAVnw8utmPVjd6IvxodNN9VR1F40abxz82F4SzAfUfbp2S57XPI0AwtA+pOEK+/FTjeeXNPbNvEyPqVunBad5+paWZpz6hDqv6dJcp23pEt6m7/M0pnHN7dAsqZvG67psygmikRY8O7h/CO8u27Dmechdu+av6R9O/bDloW9WYaBu89J8bqXxA6/bvs//YZyH/GXL/V954D0D93CSXkkyOMMZprtI9/D7TtTsN2eJvSiAkzGwqKOgT+x65p/GErtjtzIOBV7C+ENTkoGTZI/EggB8DLD7E6Dxi3GTMcRutmC/iJ4MGR8oo5VqhkOlBBdyiDpSgOoATgoE7nCWHbzsUPLLqga8tG0kEAw7xlQsK3lZQMcU+sRYr2MJBAUKAuXhpzz8lOLnJHsUnpKGlY9RTPCVUypp+04EGmrTWtmGgkEFw6GkZko144wA1QGImcBfG1Gz4gxWZ2lF/deBl6jkTvtN1NWC404V7+TliKz0tJWc9jepZnu9");
                addPart(null,"bXNjaAF4nG1Sy5LTMBBsPSy/EpZaqOxScOAHfODGv1AcvLG2SpRjB8fh8esQYnrG7IXagzzjmelWtyTUeO/gh/YQYT6g7uJpP6XjnMYBQOjbh9ifYD993uDFIQ5dc5zGL3E/jxOC/McJdZrjoTmN52kfUR3H73FqhrGLuJvjPLVz+habKe7H4TRPZ0W+iT+O4xCHObX9f63Niv/H9u5w7ud07NP+OZr64Zz6rpkFgbpNU/PYSuMndm3XpWcQ2z59PafuiR74yIVbOAmv4QzDDs4y3GXAxkjjvnDIlt/AcuG6csZgC+cYbtjy8MtCBrNcCDR4SWBlJNsxK41Hhrcgk0VdyNiV457xF4csbnQo49ArGLMqkWBEjYA0MJGPLwLschUZXH9UEvdl7WID+1nhiRA9gTSaKWdYWXKx5lDollIo10KlhZwatqLBi4aCf1Y54KVtSxaCUXkV01ImM+g2mY4Ya3RbFoIWMhby1U+++snFTy06Mk9KUeSLQkxwyimUsGXRk7482bSWcxkJgxKGlUnN5GrGGSmUa0HMBB5tgZoZEcy20ir0rIMR27y45SLsasFRU8mL8/oCyvVBlPIgLMNOL9SgWp9GpU+jNjWze4X8Baf1jN4=");
                addPart(null,"bXNjaAF4nG1Sy5LTMBBsPSy/EpZaqOxScOAHfODGv1AcvLG2SpRjB8fh8esQYnrG7IXagzzjmelWtyTUeO/gh/YQYT6g7uJpP6XjnMYBQOjbh9ifYD993uDFIQ5dc5zGL3E/jxOC/McJdZrjoTmN52kfUR3H73FqhrGLuJvjPLVz+habKe7H4TRPZ0W+iT+O4xCHObX9f63Niv/H9u5w7ud07NP+OZr64Zz6rpkFgbpNU/PYSuMndm3XpWcQ2z59PafuiR74yIVbOAmv4QzDDs4y3GXAxkjjvnDIlt/AcuG6csZgC+cYbtjy8MtCBrNcCDR4SWBlJNsxK41Hhrcgk0VdyNiV457xF4csbnQo49ArGLMqkWBEjYA0MJGPLwLschUZXH9UEvdl7WID+1nhiRA9gTSaKWdYWXKx5lDollIo10KlhZwatqLBi4aCf1Y54KVtSxaCHJAxFdNSJjPoNpmOGGt0WxaCFjIW8tVPvvrJxU8tOjJPSlHki0JMcMoplLBl0ZO+PNm0lnMZCYMShpVJzeRqxhkplGtBzAQebYGaGRHMttIq9KyDEdu8uOUi7GrBUVPJi/P6Asr1QZTyICzDTi/UoFqfRqVPozY1s3uF/AWpBozf");
                addPart(null,"bXNjaAF4nEVOy04EIRAsmseMjnv3J+bg9xgP7AwmJAzszkPjv69iA26EVJruqi4KGr2EinZxEC8YZrdNq7/sPkUAJtizCxvo9a2DWVyc3YrHS/p06xjT7PDU3ls61slhOB8+zONeZhj87pY7c4r2w4bx3U57Wr9wCv56+PnOAs/4PwKyFGpFggQX1RtQ/gEqvhk3CAblGxHzuqe6o3Iucpi23ZUi/nz4QnGnNPBAmlsj2oeFKRxVrhjKNpB1oOsi2qjGK1JpikAr9mJPTtiVTGi7lDN3qKiJiRXQbGaqmWkunO8XLjJD/Q==");
                addPart(null,"bXNjaAF4nEVOW07EMAycOI8Wyv5ziX5wHsRHtw1SpDTd7QPE3ReCnbAikeXY88jAotUwaZg91Au6yW/jGi57WBIAF4ezjxvo9a2Bm32a/IrHy/Lp1z4tk8dTfW/LsY4e3fkIcep32aELu5/vyCkNH0Ps34dxX9YvnGK4HmG6o8Az/o+Clka1aZDiZloHyj9AqW+uGxQX5RsR47alojE5Cx2uqhtp6s+HLwxPxgIPZHl0AiklQvlD9IyJoa4LXRa2CFFXJZ5QtROCNezFnpywkUyoWsqZJ5QqiYkZsGzmipmrLpzvFy6xQ/4=");
                addPart(null,"bXNjaAF4nEWPzU7DMBCEx+ufpoReufEIOfA8iEMaG8lSYrdOUsS7F8J6IyDSau3Z2c8TWLQaJvVTgHpB68M8lHhZYk4A3NifwziDXt8auCkkHwoeLvkjlC5lH/C4n+e8liGgjUuY/i7nNY6+W6oBp9Tf+rF774cll0889d7HJd5CV8KQ07yUtQ5wGuN1jf4XATzj/1PQtdHeNEhxM40Dbd+A1BfXHYqLtjsRz23DRhjZM9tWV+B2wgG69sYCR9GPVZeTYTfVNxRMHZPlq5OR4vfYqSpwjyEWErsIWgRLkGeFJtErVB+4aWsYyWhO39S82Hcl77axAin5I2IcLAOdAN1OkuxGsmtVBc7+A/hSTZs=");
                addPart(null,"bXNjaAF4nEWPzU7DMBCEx79NCb1y4xFy4HkQhzQ2kqX8tI5TxLsXwu5aQKTV2rOznydwaA3s3E8R6gVtiOuQ06WkZQbgx/4cxxX69a2Bn+IcYsbDZfmIuZuXEPFYz+uy5SGiTSVOf5fzlsbQFTbgNPe3fuze+6Es+RNPfQippFvschyWeS154wFOY7puKfwigGf8fwqGm67NQCtqtvHQ+zcg9UV1h6LS+11rmruGjLCyZ/edV+Ar4QDDvXHAUfQj63Ky5Nb8hoLlsXZ09TxSit4jp2JgjSEWLXYRjAhOQ54VmkRnqDlQM84SktCUvuG8qLuSd99JgZT8kSYcHAG9AH0lSXYr2Y1igbL/APjtTZw=");
                addPart(null,"bXNjaAF4nG1Qy07EMAycpE023bKIEyeOXHvgexCHbBOkoL5I20X8OlootituqHLjzNhjT1DhrkA5+D5CPaEOcW5zmpY0DgBs58+xm6GfX4647eMQmimPb7FdxozjNH7E3AxjiLDMxYybHZvHNbcRdVpi/3d56NduSVOXWr+kS2xybMdhXvIqYvV5TV1oFm7HafAX3zWvnqlP3PsQ0j89py69ryn8DQAeKXBCoaBQOw21/VB8KQ3NDB8KhZZEjgJa0VE6C021kPimuFLflbCrphIYV3LVtsGKDGWiaXeVAwrmnQEqwasdOApgUNJOiofxi4IZ+tGjM60PlFvFlHKUVlxZ8rXkj0q0pgV2wAhgtJY9ZAHxY9lKxesYUiBLNMU5NkFVhVRS27YRAgmxqWkwDAlaEbS7kpixYqZQDFQ7wGasKomveSlSPvBT/wLULWZP");
                addPart(null,"bXNjaAF4nG1Qy07DMBAcO7HrNBRx4sSRaw58D+LgxkYyygsnKeLXUSHsbsQNVVtvZmbHO0aFuwLl4PsI9YQ6xLnNaVrSOACwnT/HboZ+fjnito9DaKY8vsV2GTOO0/gRczOMIcIyFzNudmwe19xG1GmJ/d/HQ792S5q61PolXWKTYzsO85JXMavPa+pCs/A4ToO/+K559Ux94t6HkP6ZOXXpfU3h7wLgkQonFAoKtdNQ2w/Vl9LQzPChUGhp5CigFR2ls9CkhdQ31ZXmroRdNUlgXMmqbYMVG+rE0+4uBxTMOwNUglc7cBTAoKSdFF/GLwpm6I8enWl9oN4qYpRy1FasLFlZ8o8kWtMCO2AEMFrLHrKA5LEcpeJ1DDlQJLrFOQ5BqkKUNLZthEBKYmq6GIYMrRja3UnCWAlTKAaqHeAwVpXE17wUOR/4qX8B1ONmUA==");
                addPart(null,"bXNjaAF4nG1RXY+UQBCs+WAYFtc1d0Zj4l/YGH+P8YGDMRnDArJw6l8362F1oy8XHybdVHdVd9Go8dbBD80lwXxE3aVrO+dpyeMAIPTNQ+qvsJ8+13h5SUN3nubxa2qXccZhGr+n+TyMXUKdl3Q5X8d1bhPepR/TOKRhyU1/nlM7DtdlXpUTRCPNeLFz/xLeX9Z+yVOf22bJj+kZp35Yc9+dF2HgODSPVP3SSOkn3jRdl//DOfb525q7fwOAD3y4g5PwWoLBEc4wnCL9w28bUbPdnCX2qgAOxsCijoI+seoZfxlL7MRqZRwK3MP4XVOCgZNg98CEAHwMsNsToO83303GELvZgvUiejJkfKCMZqoZdpUSXMgh6kgBqh04KBC4w1F28LJDyS+rGvBStpFAMFIyFdNKOgvomEJbrPU6lkBQoCBQ7n7K3U8pfg6yR+EpaZj5GMUEu5xSSds2ItCnNq2VbSgYVDDsSmqmVDPOCFDtgJgJ/LURtSxF5SjXEeCkJcNO3mm7ibpacNyp4p28HJGZnraS0/4B6eF8lQ==");
                addPart(null,"bXNjaAF4nG1RXY+UQBCs+WAYFtc1d0Zj4l/YGH+P8YGDMRnDArJw6l8362F1oy8XHybdVHdVd9Go8dbBD80lwXxE3aVrO+dpyeMAIPTNQ+qvsJ8+13h5SUN3nubxa2qXccZhGr+n+TyMXUKdl3Q5X8d1bhPepR/TOKRhyU1/nlM7DtdlXpUTRCPNeLFz/xLeX9Z+yVOf22bJj+kZp35Yc9+dF2HgODSPVP3SSOkn3jRdl//DOfb525q7fwOAD3y4g5PwWoLBEc4wnCL9w28bUbPdnCX2qgAOxsCijoI+seoZfxlL7MRqZRwK3MP4XVOCgZNg98CEAHwMsNsToO83303GELvZgvUiejJkfKCMZqoZdpUSXMgh6kgBqh04KBC4w1F28LJDyS+rGvBStpFAMKwYUzGtpLOAjim0xVqvYwkEBQoC5e6n3P2U4ucgexSekoaZj1FMsMsplbRtIwJ9atNa2YaCQQXDrqRmSjXjjADVDoiZwF8bUctSVI5yHQFOWjLs5J22m6irBcedKt7JyxGZ6WkrOe0f6s18lg==");
                addPart(null,"bXNjaAF4nG1Sy5LTMBBsPSy/YpYCKrsUHPiBHLjxL9QevLGoEuXYwXEW+HUIMT1j9kLtQZ7xzHSrWxJqfHDwQ3uIMB9Rd/G0n9JxTuMAIPTtQ+xPsJ/vN3hxiEO3O07j17ifxwlB/uOEOs3xsDuN52kfUR3H73HaDWMXcTvHeWrn9Bh3U9yPw2mezop8G38cxyEOc2r7/1qbFf+P7f3h3M/p2Kf9czT1wzn13W4WBJqhfSTbl1ZaP7Ftuy49g2n69O2cuqcNgE9ceAUn4Q2cYdjCWYbbDNgYadwVDtnyG1guXFfOGDRwjuGGLQ+/LGQwy4VAg5cEVkayLbPSeGR4BzJZ1IWMXTnuGX9xyOJGhzIOvYYxqxIJRtQISAMT+fgiwC5XkcH1RyVxX9YuNrCfFZ4I0RNIo5lyhpUlF2sOhW4phXItVFrIqaERDV40FPyzygEvbVuyEIzKq5iWMplBt8l0xFqj27IQtJCxkK9+8tVPLn5q0ZF5UooiXxRiglNOoYQti5705cmmtZzLSBiUMKxMaiZXM85IoVwLYibwaAvUIsqIlUZahZ51MGKbF7dchF0tOGoqeXFeX0C5PohSHoRl2OqFGlTr06j0adSmZnankL9J3o23");
                addPart(null,"bXNjaAF4nG1Sy5LTMBBsPSy/YpYCKrsUHPiBHLjxL9QevLGoEuXYwXEW+HUIMT1j9kLtQZ7xzHSrWxJqfHDwQ3uIMB9Rd/G0n9JxTuMAIPTtQ+xPsJ/vN3hxiEO3O07j17ifxwlB/uOEOs3xsDuN52kfUR3H73HaDWMXcTvHeWrn9Bh3U9yPw2mezop8G38cxyEOc2r7/1qbFf+P7f3h3M/p2Kf9czT1wzn13W4WBJqhfSTbl1ZaP7Ftuy49g2n69O2cuqcNgE9ceAUn4Q2cYdjCWYbbDNgYadwVDtnyG1guXFfOGDRwjuGGLQ+/LGQwy4VAg5cEVkayLbPSeGR4BzJZ1IWMXTnuGX9xyOJGhzIOvYYxqxIJRtQISAMT+fgiwC5XkcH1RyVxX9YuNrCfFZ4I0RNIo5lyhpUlF2sOhW4phXItVFrIqaERDV40FPyzygEvbVuyEOSAjKmYljKZQbfJdMRao9uyELSQsZCvfvLVTy5+atGReVKKIl8UYoJTTqGELYue9OXJprWcy0gYlDCsTGomVzPOSKFcC2Im8GgL1CLKiJVGWoWedTBimxe3XIRdLThqKnlxXl9AuT6IUh6EZdjqhRpU69Oo9GnUpmZ2p5C/Su+NuA==");
                addPart(null,"bXNjaAF4nEVOXU/EIBAcPq9ajffon+iDv8f4QAsmxJaetI1//hSXxYuQzbIzO8PAoFPQyS0B4gW9D9uU42WPawJgZzeGeYN8fTvBLiH5kHF/Wb9CHtLqAx7ae1uPPAX04xFnP+wVQx/3sNyYp92lj+HdjTlObl8zHuf4eUR/44Fn/B8BVZtsTUEKarqzkOUH4PqmukJQyXKVknjTaVLoUmBZS68qo4ldTrWJPz+60DRpA9xJQ6MV7ePKVE4yV41VAxQDZxaiQRyzripbF4wmL/KkpF3NhqblbKUQAi5OL2kLhgwtG9rmRBl/AWeURt0=");
                addPart(null,"bXNjaAF4nE1PW07EMAyc5rUpBcEegCP0g/MgPvoIUkQfS9qKyy8E29EKIlm2x+PxBBaNhlm6OaB6QTOGbUjxssd1AeCmrg/TBvX65uHmsIwh4e6yfoXULusYcF/qbT3SEPAwxc8jjre26Y84je3OFDRxD/Nt8rh3y0f73vUpDt2+JpwFSOEfBDzj71XQnFRJGqqiZLyDyj+AxDfFFRWFylelaG69zhlO9kzOvEKdKJygOXtr4AWvGZfKEJu2LXXG0kDRETgZVScqa6axPFGUUPiYLoAW4EmxDIqaWGdRumig6WJN7g259+wXZVf85kwIJORHyhLPkqATQVeUyDvjnvBzxQB5/wVnzk42");
                addPart(null,"bXNjaAF4nFVQy07DMBAcP+MkFFq+gWsOfA/ikCZGMuSFm4p/R6Vm11EliLQa7+x4vBOU2CvoqR09xDPq3p+6GJY1zBMAO7RHP5wgX14r3I9+6pslzu++W+eIapm/fGymufewPPMRdxt3ms+x89gN4fMc+ltbh9WPt+ZxiWH0TfRv7TGGrmXH+ngOQ9+s7IGHtZ0+mj/TQyb+XQCeqLCDEhConYZI15TA+A2VrkJCsoJBQMl8yKAgBYF2FjJdgVw/VBe6eyHuIkkC4xT52WyhU8p+dnMooDSBMxou8yWUIagMUApLS+wg+CH6CrAUhjrNY+mIsIJHoqRjyUrNrSFgiZT0+EaYTOylzHvkBXIWyzE0r0MrlBSH3nSOA5BKZSVdT4kY5MoRpSGdIUObDe3mVPDulsIAB8FEuVlXbC0MzWsaFYKj0O/+BcPBZHE=");
                addPart(null,"bXNjaAF4nDVQSVLEMBCTHS/JhGF5AE/IgfdQHLKYKhdZBicp/sl7BoJsD4e2utRSt8qocS6g5nZyEC+oB7f2wV82v8wAzNh2blwhX98q1P0yr1vY+20JqP3mpmZd9tA7mMnNgws4XZYvF5p5GRzucn9T3G/t/NG06+qmbqSy7nY/Ds0WNXhIw/e2C75v4/Lz6D93P/ybgWdAoNIC30QJq4EnKVFwhAgCRQSZgU18VGkgj18g1Q/rCsGSx1UqznWpkl0dB19NlRLRbvIWi0JD4wSSUUheU8dbCooBKmlJGBFHlOiEhDiTDGcyYRLxSMLmtDantTFtma4o7or3VGljxKg6DnZIlcPzgBTUaC4zaZnJdkv7SQiUt19hsPxT8g+lWFQM");
                addPart(null,"bXNjaAF4nE1RW3LVMAw9VpzEefYWaLuKMMN6GD7ycDspedw6ybBUNsJnIUg2TPuhSDmSjnQsPOAugl7a2UJ9QTHYrXfjdR/XBUAytZ2dNtDXbzXuu3Yb+6bdNjt3k3XNvA7HZFHPdhmaq1ufbb+vDvrx2CyKfl223R0eKsbdzs22Hq63uFzXH9y9rINtptY9WSTCYB3ytwzKEP/rqfd2+f42GkV3jNPQ7FKDG598bDs39q2Mu/WAs++gahpfjnH4zwd8BggfAIUYtcQpDFTErowVfkJzlMfARxVzVBswYjySqgQZ5HXYKUTiKLgIlLLTJgGdfwBvv9leodjofCXD+dhEkJH6POXr+ZLAkCLS7AzPyUgAHiGUuRFC4jJSGQpU4K5ctsj5j2S7HNp3FQwkSlKq5DCTykJ+NTspISYuA1B64MJAFRRVQVEliip2UczrsKhKRIkMWfw8OYI3L5A052MmSjxRElpTbs2VpETNLQmWMWaURLnPEm7k2TWfIDxtxIA/AMlZaiOK9fmLV9WcCQdRuMjJgE98QPoLwSd3DA==");
                addPart(null,"bXNjaAF4nDVQSVLEMBCTHS+ZCdsHeEIOvIfikMUUrsoy2EnxTR40EGR7OLTVpZa6VUaD+wpq6WYH8YJmdHEI/rL5dQFgpq53U4R8fTuhGdYlbmEftjWg8Zub27juYXAws1tGF3C+rF8utMs6OtyV/qZ4iB/+0nYxurmfqGz63U9juyUNHvPwveuDH7q0/H7yn7sf/83AMyBw0gLfRAmrgScpUXGEBAJVAlmATXpUbSCPXyDXD+sKwZLHVSrOda2yXR0HX02VEsluyhaLSkPjDJJJSF5Tx1sKigFO0pIwIo0o0RkJaSYZzhTCZKIiYUtaW9LalLbOVxR3pXuqtiliUh0HO+Qq4XlACmo0l5m8zBS7pf0sBOrbrzBY+Sn5B6yIVAU=");
                addPart(null,"bXNjaAF4nE1RWZLVMAxsL0mcdWZYTxGqOA/FRxYPmMrysJPiqFyEz4Eg2VAzH4qUVmtpC+/xVkFvw2ohPqKebZi8ux1u3wDkyzDaJUB++tzh3TgEN/VDCHYdF+v7dZ/PxaJb7Tb3N79/s9Oxe+jHM1jU076Fw58Rqt1h1z7sp58s7m/7D6re9tn2y+C/WOTcwXpUzxk0Kf5X04Wv7vY8GvV4umXuD+bgLiYfh9G7aeBxDxHw9gXULu776eb//YAPgMQrQCBDx3EBA6HINZnAT2iKqgx4LTKKOgNCTEQKkaMEvw45AcVOJqcgC3La5JDXHyDab7InCDJ5PUlD+cwo8Eh9XfyN/fLUoYDS5AzNKSUDNIJbVoYbSqJJUaJGC6qqeIuK/iRvV0HHqpqAXHBKNBSWzKz5V5NjiqTGTQKaCCgC2qSoTYpaVtSSUxmtQ6JaFsUyePHrogjRokDJzIyYJpbmqbQgoBKaIlbzEEklkwRHVcxK3PGzazpBelpFQDyA5LN0hhXr6xetqimTDiJwzycD3tAB5V/jC3ch");
                addPart(null,"bXNjaAF4nDVQSVLEMBCTHS/JhGF5AE/IgfdQHLKYwlVZBicp/sl7BoJsD4e2utRSt8qocS6g5nZyEC+oB7f2wV82v8wAzNh2blwhX98q1P0yr1vY+20JqP3mpmZd9tA7mMnNgws4XZYvF5p5GRzucn9T3E+u/2jadXVTN1JZd7sfh2aLGjyk4XvbBd+3cfl59J+7H/7NwDMgUGmBb6KE1cCTlCg4QgSBIoLMwCY+qjSQxy+Q6od1hWDJ4yoV57pUya6Og6+mSoloN3mLRaGhcQLJKCSvqeMtBcUAlbQkjIgjSnRCQpxJhjOZMIl4JGFzWpvT2pi2TFcUd8V7qrQxYlQdBzukyuF5QApqNJeZtMxku6X9JATK268wWP4p+Qd2NVPq");
                addPart(null,"bXNjaAF4nE1RWXLVMBBsjWVbXvMCJDmFqeI8FB9elMSUlxfZLo7KRfgMmBkJKvmY6lHP2ho84C6CXtrZQn1BMditd+N1H9cFQDK1nZ020NdvNe67dhv7pt02O3eTdc28DsdkUc92GZqrW7/bfl8d9OOxWRT9umy7OzxVjLudm209XG9xua4/uHpZB9tMrXuySKSDdcjfIiiD/6+GZ/TPb6NRdMc4Dc0uObjxwce2c2PfyrhbTzj7jqqm8eUYh//9gM8A4QOgEKMWP4WBihjKWOEnNHt5DHxUMXu1ATPGM6lKkEF+h0EhEqAAEShl0CYBnX8Ab7/ZXqHY6Hwlw/HYEGSkPk/fKwnVKSLNYHhGRkJwe2mXm4gbEaeRtFAZClTgyly2yPlFsl0O7SsLJhIlIVWym0lmIU/NICnEzctAlJ64MFEFRVVQVImiiiGKeSUWVYkokSGLnyd78OYFkmTGnGl8aRJKUyZypdkTRbc+KZMkJV7uo4Qb+XbNJwhfGzHhD0ByltqIan3+4lU1R8JBFC5yMuATH5D+AoOEeOA=");


                bases.cores.sort(b -> b.tier);
                bases.parts.sort();
                bases.reqParts.each((key, arr) -> arr.sort());*/
                for(Schematic sch: Vars.schematics.all())
                {
                    if(sch.hasCore()) coreSchematics.add(sch);
                    else if(sch.tiles.contains(s -> s.block instanceof Turret)) defensiveSchematics.add(sch);
                    else if(sch.tiles.contains(s->s.block instanceof UnitFactory)) factorySchematics.add(sch);

                }

                for(Schematic s: coreSchematics)
                {
                    addCorePart(s);
                }
                if(Core.settings.getBool("ec620.schematics"))
                {
                    addCorePart(Schematics.readBase64("bXNjaAF4nF1W227jRgylRrbu94udRPsLRtHvKfrg2mrXhWMHdoJ8d4E+NJuNVZJnqp0mC/lQFIc85HC4Q1/oy4IWp+3jSN7PFF6fx+3jYU/Jdfd1fNw+H3ZXSvfjdXc5PD0fziciCo7b38bjlcwvvyaUPZ1fx8vmen657EYaLuPh9PuZxf3m+nL5Y9y8bo/HzXHLIpX6YfN0Of857p7PFyoex9PeeQ/kfbxQBZ+n8360SxNeOl6/bvfnV0p+fKXo5XQ8b2VNsWOLzXZ3OT+dj4crkfcTyV/kEadnRPQi1ZBhHf+q7FvdQnTTTeWl1QWOXWh1kWMXW13i2KVWlzl2ufO9sN/LyNjv0FdWXzvrGqtrnfWd1fWf1q+sfu2sv7O6+4h8wQeLg+rZLpJf+WfkxydPYAkIATEgBeSAElADWkAPWAPuxb/HoXzyp++05MdMb6Cr4Y2l4cu7p9HJGNkr0fkUkCcQARJABigAFaABdOTJ8jVpKvfwzTGYx0LbwBDkYZaXNvZSYi8YOKhACIgAMSABpIAMkAMKQAmoADWgAbSADtAjrLJcWpYBszH/baknFfCFVSA05S0gIwyjqJxuXE6aPsjjx/Dj87OYPqaJv+gfaybWTMJtemfpne3e+d2YgNNLSaGEx0Y88hZN03d+XC9GPMnpIDlFUl5fd4gLOv3Fln/z880IuQ5UV6C61uPDef3Ih4+QrXaIaoeSl8CKtN20FqGtRSQ7Jms9aQGtQyT2Ae9GKORjLqgqNWKEiJHsra7iw2mjxYgWI1qMaDGixTZaMveHdJpGS8Q+ZEiiiEJbYg9lZFnwTfqZf288VxJK2T5TKZulfJaKWSpnqVJJYtSRRtXDZ/jxeTPeuLRvvAdvJBkFWk9DgSfUNOcEOSdOP6c25xQ5p2jwlBKJZGLu1hyKWhQkFlqOFOVIbTmyT42YoSQZNiCXDYj4DV5Fgjv51MJQCWYgmH1qg9ySzEEyB8l8dpfP7HKwy8Eut+wKpzUKMCvgo5h9FLOPAlwKcCmcBinnTdeBJn5K+ClnP+XspwSXElxKyQlDTX1VhEFVgU+FStWoVDW7q+ZK1ahUBXYV2FU2w3rOUGesGNZox1rbUSJ/8Gm+yYnmVgzY1kOzSNNwc9VoR1+kbJbyWSpmqZwlbUdfYtRROP2jc2WJuSDxOMok81FbsEY1alSjdqra2Eo0qESDSrSYOK1MHFFq1g2ybmzWrbMfLbJucWhbRGsRrXUavrPROkTrMCo7jMpORqX+72P0XMkAk1NmdKxxS3KxPvSXh948TjF3ZdAZ9dGID13/v7VaHsMDUwbvTUftYh6UvlozlelmhE4HciuQ06w7m3Xv7HWPrHtk3SPrHln3To1XmrVhRwsxXAljgRAQAWJAAkgBGSAHFIASUAFqQANoAR2gJ1/CagYrm8Ha2be1ZOAzMCOBCJAAMkABqAANgAPIcs117ezwneYqiGj3Tr3kjuEL+IAlIATEgBSQA0pADWgBPWANUAr3TrkfLIUHS2GQdyOIC+XgXCgHe6EcnAvlYC+Ug3OhHOyFcnAulIO9UA7OhXKwF8qBL5TSbd9m29yxKayNXirJzDaV1ddOjMbqWmd9Z3X9p/Urq1876++sDpfKgS+VvpwC4SbjR3VcJ/9fYtFr8A=="));
                    addCorePart(Schematics.readBase64("bXNjaAF4nC2QyU7EMBBEK3HI5mQWzZFvyIEjZz4DIWQyHoiUBcUJI/6e6jaHuHrzc6dwwSVDNrvJI39ZVv/+hCJs3k3DFXXov/zktqEPsFcf+nX43oZlBpCP7sOPAenrW47H1Q/zbVl7f+3Cvn767u7GsRsdQ2S3PXgy726d/IrzNmxuHvap65f5x/8uK8p9Hhd3ZbPpuUE37/3o98BXnvmhRCpSiyTIkSYwqCRLYaO0IkZ6QMZDhUHKw7JBaSV74IgxlDJmlfTkFrOcQyajlA9AoZFldDDSauLgQcg5TiIFd0ozSq5TAqtkQWESVxJPSIlGcYZRi0TaR7ld4ixSCSSlNLJXHe/UiiyM/LNOWWJVlGzjlFWyrGdJNtI+ykINiv96w1eMFGrFSUHNaKM1bTSj5SuQoprYCiThoQYfosFHNZWiBp9i8cxfV+HkHxwqNxU="));

                }

                for(Schematic s: defensiveSchematics)
                {
                    addReqPart(s);
                }
                if(Core.settings.getBool("ec620.schematics"))
                {
                    addReqPart(Schematics.readBase64("bXNjaAF4nDWQa2qFMBCFR5OMYgul3Yc/7hK6jlJKGkMrxAdRuZvXa+dhE5gPJnPOGQINNBbs6IcI7j2HrxtUyxr90HfQLOE3Dn7twwJPXVxC7ue1n0YAwOS/Y1qg/Ph08BqmeY65vfuU2uTzTwTjc4DnebpTe5m2HKjVbRO8zGPcxLLtcp8SWd1ATqmlYFgFKmoGP5cFoVQYKBhW4RSoqFRQA9GwgMcN2VAlhWE4MNzFmiWGJBIrGZYlMsgtviJDtrP/k2LudNLxJAO1WenyYoaaj1c+aj5qPlL+GxQnHRLKPR/nQ3icB9Wdrc6dLp+dO1elOaqHvEktOOyKlu0q/iaGUViFU6Ci0h/m/QmlwipQQa9/oJxY4g=="));
                    addReqPart(Schematics.readBase64("bXNjaAF4nCWPfW6EIBDFR6CKQDbpQfxjr7DXaJoGkXRN8CNgs717s2tnGA385M1z5gEXuChQq18imFuOPty/rr9X6MoR/TJPYEq4x8Ufcyhgp1hCnvdj3lYAaJMfYyogPj7f4D1s+x7z8PApDcnn7wjtEtcpZnD79sBK2X5yQHWsU8COyZdjmPKcEjYboD6Ct4agGC1DMwzDEcgsGoQASZAg6VtpXFQ8Xw1K2IAq0AOQyfIER6LChaJkUVGBfrXYQJx/54uKeHa0tdynxQQ1FHoldNSATo4jVrFniyZR4ElocT45EeYxNIfQ8XXqFUxNj3A00pKVkvaaYjyrh2RD97T4SqgTKwRDMVqGZhgGev8Bm3w4Jg=="));
                    addReqPart(Schematics.readBase64("bXNjaAF4nDWQYXKCMBCFlxAiRYjWe/jHI3iNTqeDkKl0ojCBjr262kp380Zmwifr27ebRxvaaNLn+uSo2AdXN8eP3c+OFuPk6lPXUjE2R3eqp64Zadm6sQndMHX9mYiMrw/Oj6Te3g29Nv0wuLC91N5vfR0+HZmTO7cukDlEX1oefD1O2zZ03pOVP7dD6L9cM/WByqG/cPvYf4fGsfme4qPwSgQaMEAOFEAJWGAtkFaVMJT8TqVdsAAKoKJEJCuMWRM3ptIgWGFodNEo6mdxLXLDojRlGFKCgpQULbaMGxhRyrL8pRnRJRe7jFHlXJn/5sd8ZV7nG7/vfH5Zl8UBKnZZeLBVRi+k84z0/KCMj+HeG5/7/FByqbhJIZskrKxkXiGzFS1lWYFFbGvEFrcscXORs8QiKouoLKKyiKpCABapWNzHPovRZYXiCkUZkwoUoAED5EABlIAFuPMf66RQHQ=="));
                    addReqPart(Schematics.readBase64("bXNjaAF4nD2SYZKbMAyFBYYAtiGU2Wvkx56hx+h0Oix4N3RIyAKZHH43pU9W4mTIZz9kWXoRvdBLQsm5PTnKfh5du/55pWxZXXsaetJLd3Sndh26hUzvlm4eLuswnYloN7Zvblwo/vU7px/ddLm4+XBrx/EwtvOHo+rkzv3hMk9/XbdOM+n3aXbLse2nG5Xj8Hkd+sMyXefOkRlWd3pu7GW6IdNjp/rrROZtbJf10M/DOOLmD/KfWH4iRiLYCXKBFlhBJagFDYMzxBEQk2IkpLCijCKGoYjFSt7VUIBGbsMyphSSx17ERkrAOwXsUk6XYJWlKEKxlgetCJoOmgmaDVrJWsxpG74iQ1ruKMUqy9Pt3/a9fRNtX9sdzxd0UrgQDngUAi0wpPic5XOkcHLDiQRP7M9x46W/mB30HRbcocJOmkEDiPF5C/I7y0Xljyo5fi/++2KLR7Holo8xEK+A5zVazNIhv5ZALYl1aF9LRhMyGgk0ktGEjFYKL6Vw+0icYpXlGVrlaKIIzd9hXLLdvQFEKRyyYpsV26zYZsU2C9tynLl7s3ECK4UZUjByQ2X8jVWCuKcXpXhhpfJSKkdNJeZB/t0S8wAtYq0Img6aCZoN2rPPSvrcwwbFMNxXJSNYy1jXMtY1jzWjEvhBriWyET8ajmQkgp0gF2iBFVSCWoAE/wEM7HOE"));
                    addReqPart(Schematics.readBase64("bXNjaAF4nD2UfXKbMBDFFxAf5tvGk1v4n16hx+h0OsSmNTM4doFMDtEbJ3HoWz0se8xPvKyeVqtV5EmejJiX9tJJ/P3ctfOvbxJPc9de+pOk0/HcXdq5P06SnbrpOPa3ub++iEg0tM/dMIn/42ci2+P1duvGw1s7DIehHf90El26l1M3Svr7OnbTuT1d3yTr5+5ymK6v47GTYuj/vvanx2t+u77BYX2Lp2M7z5iePQ/tNB9OYz8MWPWf2I/Ph6cwREQkRErkREnUxI7YK9TI9wBfAkVAGCIkIgn0L7BWwFPFiqiJLbEjGlrvBQx0DXU0Ot1wQoAQm7pd3WiIYhXtvJBiSDFiZKRiAMQhNhcYjJIQyfqqbZyWOi1zWu60wmml1dR2XWSvqSaacyCxRCErGOPrhXgkSWr34y0fy6fIcl8+MfrC764KRu/LsrzDKvSQo2zEIuPsItlgzofuFLHLcpcAbz5+SrN8SbQsEnm6IE7MAJVNUFPa8nCRnwEiq2PziLEoxB5QpRn7GrhXYbNuIUY/pOq4WR21QaxV6qxSWqW0Sp1VyqLkLEq2OhqMHsnlTC6nY+4cczrmdMydY87kitUKB4K8AkUmOJbC5VjSsVwdPYzsQZQ4CMxAMb9sAT8lRPFCFDTE2McBeOshaGyRpCj3XUuPKBihyNrH7KYIHWa00bXhPF0CpQ/weGRbsh0rvSPa7kbrWKEB2U4VGhCBVts4LXVa5rTcaYXTSquprb0WFfuwZrPXXHKrDanI9TbWjxCb1VZDFA0vtp23o7h7iDay4Q1veMMb3vCGN7zRG66oiJrYEjui4f8MPTnAJwwREQmREjlREjWxI+DzHzV6jlk="));
                    addReqPart(Schematics.readBase64("bXNjaAF4nD1S7XKbMBA8EJ+SsIknr8GvPkIfo5PpEFBqOmAokPHDxw7d42zZAyut7la3x9GJXiOKLvXgKP15dvX6+wely+rqoWtJL83ZDfXaNQuZ1i3N3E1rN16IKOnrd9cvFP56y+ilGafJzdW17vuqr+c/jg6Du7TVNI9/XbOOM+mPcXbLuW7HKxV99++za6tl/JwbR6Zb3fDc2Gm8QumxS5ZmnJszmfe+Xtaqnbu+x+VvtP9CeQUMkUAikAloAStwECgFTgysEAaAkBRDRAorSilgMBQweRAoJeEkt2EZUoygHY58FslZwmcKkMQsF2GVxihCMZcxFzCXe057znjOeq5gLmTZE1+RQpYdxVilWbx9b7ftRrR9bXc8X+ApwIXcAYZcQAvAEOdZ5N22bfsmhYwQT7TnsfGCL947uDvM2aHCTszAAGJC1s25pAyVIDl7VMnxR+n/Xmz+KBZuOY0B8QrwvEZLs7TX1xKoRVh7+1oUjVc0EmhE0XhFK4UXUrh9CMdYpVkKq7RHBWjAHY2LtjseZmN0yKJwxZALaAFDivNtliHnvjcbGVgpfHaFJm6ojP8hvpj1vSikF1YqL6RyNLnAPMhkFJiHJ5d7TnvOeM56bvcZ8jTvPo9og2Iw7OsgI1jKWJcy1iWPNcNBoOTIkiP/Ax1uc3c="));
                    addReqPart(Schematics.readBase64("bXNjaAF4nD2TfZLaMAzF5dhJTD4IZGePwX+9QY/R6XRS8JbMJCSbQDn87tZ9ssAzwM95tqUnRdArvRoyl250lH8/u+766xvl69V1Y3+iYj2e3dhd++NK5cmtx6Wfr/10IaJs6H67YaXkx09L++M0z2453LthOAzd8sdRNrrLyS1UvE2LW8/dabpTPfTvt/50WKfbcnRU9lc3Ph+qebojwuPJ3Lu/0Ppxvg2rO8y3cUbOd3wpIcUwgkxgBYWgEmwFO0HL4FWiECMhzdACI0hJs5STYpSChhTvtZL7haF5GW5xMM17Ck+t+ApHUkmUsqjhMktxOjFY5diqFGuWNcXaJmpF1MqoVVGro7YNWgKvO2lDyJqLMcvGDJ4yPg8bOdeU4sfajPdI+Q//he+n98+VgjXaUEDJNedUk+IoW46Ca/ZRBZxDTVKgtpn3pHH/n/+gxH9S6r9Qdeq94gvBZng/wd+G/RlAjOV4YQVp1I/4xPozVRFTFZyKBQmVwNtOXnWIWErFlVRcPgKjexKxjBGrR8QUq9ymYX4MvBI8wz/WHyi94t4xYIvvPP1Xkq0W/3VsbC2NramyFm3kJmDEuB1IIBPCs2B4tBQfDy2to6utuNIGqzAYmjUbtU3UiqiVUauiVkctOMYYN9KmrRhvZBwbeMJnx8YYVlDx+O94j/8t4cJeLuxZVBjiBxo+2eJQwF7QcpWt3Hsh/kMAWpAKcsFGUApqQSNAsP97c3S1"));
                    addReqPart(Schematics.readBase64("bXNjaAF4nD2TaXKbQBCFm31YxCJTKl9CP3OCHCOVSmEYW6SQUACXDpBbR7bJ62k0Uknf8OiZ92ajZ3r2yb80Z03R95Null/fKJoX3Zz7jpK5Pelzs/TtTGmn53bqr0s/XogoHJoXPczk/viZUNWO16uejrdmGI5DM71pys/60h2v0/hbt8s4UfI6Tno+Nd14o+Q63lB9GTtNab/o83Ee36dW027o/7z33eMx5DH0RJnUb6r/+j6j38vQzMuxm/phQJy/ZD6u/DkMXxAKlCARZIJcUAr2glpwYPB4rgO45DF88tCiiBxGKijI4Xd7KamhAAeJgKZLASSDJxEPkgvvPCBUzvqxfkAPKQqQzQnQUgG8HNZiqyVWS62WWW1ntdxqhdVKo7FfpVy4wZOD4vnAkSIKudRFBHw5CVbMtGMV4j0WYv3C9x+ifqGru97Xu6NQhEVgYCm5U0mYZkQVu5lVNwsQ8wIEeApZdxUERfBSXO8DFZsTFz7JZplQ8RYKIahEDDb9QhBJ7vFuINCK+cXWMWHHABCrGPlMwhTbhA7JZsWFxiPdPBRExT1TO1Qm4XcSPttG9NF6xN1J3EyG2tmhcjYLIDyGyiVVvo0RoRWpaP2UlUUppoOVXfG7Y3rh+onIBa8bIyPPR49H8lzsCrtlBW9ZADzsSkleSfJyc8Uh4ELepFyFMPrcLgufTwcBOI7vcFlBHqNk49JOt5Lpluzv4Qknl2Ojf4WTCwtUVzi5qPZYi62WWC21Wma1ndVyqxVWK43GfhX7mQnuZYJPmIzHSAUFv9zL/arl6tZydWs+LIy9wNzSWioP5jABrsAXhAIlSASZIBeUgr2gFhzI+w+16Igd"));
                    addReqPart(Schematics.readBase64("bXNjaAF4nD2TaY6bQBCFi71ZzGIjay7hv7lAjhFFEQOdmAgbBxj5ALl1PDPkVRduW/bXPKr7vd7ohV588q/NRVP09ayb5ccXiuZFN5e+o2Ruz/rSLH07U9rpuZ3629KPVyIKh+ZVDzO5374nVLXj7aan070ZhtPQTL805Rd97U63afyt22WcKPk5Tno+N914p+Q23lF9HTtNab/oy2ke36ZW027o/7z13fMx5DH0RJnUP9XXSTftmdLXoZmXUzf1w4BAf8l8XPlzGL4gFChBIsgEuaAU7AW14Mjg8VwHcMlj+OShRRE5jFRQkMPv9lJSQwGOEgFNlwJIBgcRj5IL7zwgVM76vr5DDykKkM0J0FIBvBzWYqslVkutllltZ7XcaoXVSqOxX6VcuMGTg+L5yJEiCrnURQR8OQlWzLRjFeI9FmL9xPcfon6iq7s+1oejUIRFYGApuVNJmGZEFbuZVTcLEPMCBHgKWXcVBEXwUlzvAxWbExceZLNMqHgLhRBUIgabfiKIJPd4NxBoxfxi65iwYwCIVYx8JmGKbUKHZLPiQuORbh4KouKeqR0qk/A7CZ9tI/poPePuJG4mQ+3sUDmbBRCeQ+WSKt/GiNCKVLR+yMqiFNPByq74PTC9cP1A5ILXjZGR56PHM3kudoXdsoK3LACedqUkryR5ubniEHAhb1KuQhh9bJeFz6eDABzHd7isII9RsnFpp1vJdEv29/CEk8ux0b/CyYUFqiucXFR7rMVWS6yWWi2z2s5qudUKq5VGY7+K/cwE9zLBAybjMVJBwS/3cr9qubq1XN2aDwtjLzC3tJbKozlMgCvwBaFACRJBJsgFpWAvqAVH8v4DcWKI0g=="));
                    addReqPart(Schematics.readBase64("bXNjaAF4nD2R/06DMBDHby2FUrBO34M/9g4mPoQxpoNuw8AgLct8903xrmccaz/0fvcLbOE5g+zsRg/VS384XKL/2H3toIiLd2PfgYntyY9u6dsIVedjG/p56aczAOSD2/shgnh7L+CpnebZh+bqhqEZXDh6sKM/d80cpk/fLlOAouMGkJPDB6jn6YopcbqEFsOPwc2nfvGY4iN22w8uLk0X+mHAbq+QfoK3DSFj5AzNMIyaYRlbAqWKDUKAxD8owE1AAUBGDZJOhuIkpiejxVfElnpJKLT66ylhs95x3dZ1vSWf0RnA+o1ehStbV7RCWkIiBFWnOEklCkbJRst4pF4Z9RJ4JXxX6UlbxUbL18VDjjXShXNKUHhSjIqlwBBB9Smv5DzNkSVHlhxpKFIh0kiGUwxFSgxIU1c0tUBFFBWrSRoyWvalqWtWtqaEDFGQGCBRnHsSCHCRW7PbaIXK/ZBeqJP614pCLNdLAzzQtyEY/pR0QAhGxsgZmmEYNcMysNgvhfRbUg=="));
                    addReqPart(Schematics.readBase64("bXNjaAF4nCWRYW6bQBCFh12CMSaGrlBu4T85Q0/RqqoIrBMqbNBClB49lezQeftA7CeGeW+YGXmSp1TSa3vxcvg+nM/vi//9/PdZdsvq28vQS7F0b/7SrkO3yKH3SxeGeR2mq4hkY/vix0XMz187+dZN8+zD6aMdx9PYhlcv1XkKnT/NYfrju3UKsutZQbKLv/Y+SDlPH6pZpndNlONraOe3YYXEL1ruZWyX9dSHYRy13A+Jl+GRACmRETlRECVxJGrCEQ0AI5OIxW30yIg9URKVWKQ4fVTQsGjUpngzUImx8qC3TfXIcsjM9rXdLYI7SYA9cSAeJUFuhdztvn2JtbCoUTPTYgY9qbvaaTiiYrDBkaNVIPaYI9Pgv5GZU5BDgGA0y/nnBT0LphT0LNhHiQ4zRbQuYY2g4yijrqSuVF0MNpxvrF5B/qBvMeWIFAQdUxp8q3QWFtgTB+IRyhouVhGnWOsU7XbTKX6qi9FJIlbl6XbTad22f9vndtcvmFqNqcFAayRxTzpDx2U6LtNxmY7LdFym4xYdu8BhAUOkREbkREGUxJGoCUeoz3+ablkR"));
                    addReqPart(Schematics.readBase64("bXNjaAF4nD2QUW6DMBBEN8QQxxiUHoSPfPUCvUVVVQ5YLZEJyKZK7x5VdNdTFcl+2KuZnTWd6KRI3dzkyb6MafEx+ffz95kOafVuGgcyqf/0k1vHPlE9+NTHcVnH+UZEVXAXHxIVr28VPfXzwvLu7kLogosfnqrJ3wYfqRV2S5yvvl/nSHr460R2me+sSfNX7D3Vl+DS2g1xDIHtnyl/BbadQAEVoAEDWKAViKbYMQrK255ITiXx354OqBnUatQatGnltBedYpSaXye7ldu2/RDxknuj2XF7bJs48xJJg4BswjqxENS4bJGaayV3P0jECpcagiMERwg0aga1mgX51GLMPJrFaFYCKEYpiWRATrjLKS2nVJxRUj/+U4okT2wR2GLiRuwEJWCABi8qYkYBKKACNGAAC7DkFyQvSNM="));
                    addReqPart(Schematics.readBase64("bXNjaAF4nC2RUW6DMAyGHQg0BUqh6jV46RmmXWKaJgbRxhQKIkzd1aeqymz+8ZCPxPmMbehMZ0362o6WiqfBz3bx9u3yc6GdX207Dj1lvvu0Y7sOnae8t75bhnkdpisRpa59t85T9PKaUt1NM+vNrXWuce3yYSkd7bW3C5XCZl6mL9ut00Km//8SFfN0Y8dP30tnKX93rV+bfhmc4/TPtD0RFiXQQAoYIAMKoAQq4CSQDJFiRLQtMZHsEuK3mHakBDkpOSxx5YgrtdyNOAvvYtE1IzE8Mt5oSkMID6LwUHJemph0eIQgEsVKlBpVcxL2JIXgiMMTWuFYwkVwGYJcCk4RM/D28PbwDGIZvBxeDi9DrIB3gHeAVyBWInZkLxbkUmyJWIU5VZhTJRHNSEwc7nLGvaqt14p71eHOvd7Dr/Qq41GibHOr0HaFudWiChKZcC0VCGr8HpEZEaCBFDBABhRACVQAJ/gDFQ5PCg=="));
                    addReqPart(Schematics.readBase64("bXNjaAF4nC2MXQ7CIBCEp4X+RB9MD9IH7+AtjDEIGyXZFgJt9PiyaAg7MN/MYsSooVezEIbLHu7nzxlD3sgs3uGQ7YsWs3mbcXSUbfJx82EF0LN5EGe015vCZEOMlOa3YZ7ZpCdBuT3gFFfaa392yTOX3lQulIymnCqdjLaYjYgW+x9Q5d8KqUxJUlgvTAsTqWYnLVlVsiJaVnQ/1sv7C7NgIcw="));
                    addReqPart(Schematics.readBase64("bXNjaAF4nDWOb8rCMAyHf1ubVeYH8QZeYF88g7cQkbqV9y10f1g39PgmDdKWJ03yhKBFa2EnPwa42z4/r58rXN6CH+OANvf/YfRb7DOOQ8j9GpctzhOAJvlXSBn1/WFw7udlCWv39il1ya9/AWbYZ5yWKezF74Y1psTehR8MKgEJKj61wKGSyMAIrIIUDafloq4Y3F76FCSyYb0MPsjP6kSrSVKP1CP1SD36tRSvUa/RpJO5spdRWAUpykquVFk2Cq59AdNVIrE="));
                    addReqPart(Schematics.readBase64("bXNjaAF4nDVPW27DIBAcYwwmjfo4Rj/8kTtU6iGqKqI2SpCwjcBWe/zuZhP5MezOzO6AI44aevFzwOvHvp4/i8/XuIXz6e8EW7fg5zjhUMdrmP0Wx4qnKdSxxLzFdQFgkv8JqUJ9fWu8jWvOoQy/PqUh+XIJaKd9xfPlPnbIJdSKl7yE/TZvmEpMiea804cOCmjQClgGRQdFL3EtgyGWwHGz5YdBo1UEnZgtGoaeCmLASk0WpVjHZk0ScNPxUo0DV7xb8YxOqhtnxG7Eft9txG4eksMjLEms2K1wHIGy9JKz5/0MVoDb9HOc09GZlE6u6WSV46uA5ndo/gEzQSzM"));
                    addReqPart(Schematics.readBase64("bXNjaAF4nDWQQW6DMBBFJ8bYEKK0PQibrLus1ENUVeSC1SA5AWGi9vid798I8NMM329ky1GOVuwtXKM8v93n8/salsu0xfPp9yQ+bzFcp1H2ebjEa9imIUs3xjys07JN801EXApfMWUxH59WXoZ5WeLa/4SU+hTW7yjVeJ/l+P2v7Zc15ixPyy3ei68f1ykl9bzqJ7XsAA/spBIDtKiMLkZfRABHeKJhpENV6VNgZWcUNeHZbIk9NlgdpU6rO41RVESLoVZlgsgBVY0IVLUUo2ezRBwtjhZHi6PFPSLF4mnxtHhaPCMNLQ0tDS0NLc0jcuB1lEirlgrwUqFZIjiWVntcAmCJlkBblw5n7xjpYAEc4QnMV5SbPyAC6L8//nQuVw=="));
                    addReqPart(Schematics.readBase64("bXNjaAF4nDVQ226DMAw9JCFNyvqwD+lDf2I/MU1TBlmHFC4iVN3HozI77oTwsePLOTZOOBmYMQwR7u2W4+fl94JDXmMY+g7H3P7EIax9m9F0MbdLP6/9NAKwKXzFlKHePyxe22me43K+h5TOKSzXSDPidYjjCku2iwvMN43HyzzdqTBPt6WNaFLIFHVLnxLNvNAPhYrBCFgBJ3BkYE9VVKdQjAE0fVYixwUKXkY1DJpdRWD4SXMJd5HLQL10ARhXc56Z98f+AMRyBs5RZt8orsrbP6+XCUdmMMJVc5LFaPYtq+LIyy6l5CBynAhwIsAVAUy+kWUaxbWEmpotz3eko35q22jJ6qkDZWsvw4oWJ0ReDuXlUF7W93Ihz1lF5UbAyXlLX0N5zaAFaoGDABH9AX2YR9g="));
                    addReqPart(Schematics.readBase64("bXNjaAF4nCWQ3U7DMAyF3R+ytsmm8SC72RNwxUsghEIboFK6Vk2n8e5IGz45i+RP9nzcY8tRjrXUFz8FaV6vKXycf8+yS1vw0zhIl/qfMPlt7JPYIaR+HZdtnC8iYqL/DDFJ+fZu5LmflyWsp5uP8RT9+h10RviewmUTo3EIq9RfOl7cMt+0Mc3XtQ9io0+aDesYo858kfwrGQqgJgzREB3hiAMATVkoVIdQi1T6VAe0LFoRtDgKDggVOgHLr2kCsVYA1ep1pG5QKR/3x12EsUDdop7zArVsATJH9V71ioNkI5WUTwp1BLQwZrQzL5f9G3RiR81gOQtaCloKWgoadnbUddzE0rSlaQvTaqrM1vJFH3ed4OAEaNllmx0W0PenL0e9TpEnq6rAQMe5eR3HOzve2eFUQL6s42Ud/ez5356XheEKKImaMERDdIQjVPIPedVThQ=="));
                    addReqPart(Schematics.readBase64("bXNjaAF4nDWPW26EMAxFLyEkQEetuox+8DG76B6qqkrBGiIFJkqo2uXXxhrxOPHjXju44GJh97AR+vcQ09f17wpfDwpbXDDWeaUtHHGueFqoziXmI953AC6Fb0oV5uPT4nW+50xl+g0pTSmUG8Gu7IbnWwl5jQdNuVCteMk7/ZyG01JiSmz0xh86GKBBq/ACwwfDL9cageOYMUiy5acRWDSG0WnSa7KXTsuunLQsMYbRKrz4W3aBtIwSyWwjHuegTmtO5U7lTuVO5e7RMj6W5Zpn5RmdNVmBzXrZVGAVXiFp/g2y56BXGVjeCmQUw0ttlJX+AXHSKd0="));
                    addReqPart(Schematics.readBase64("bXNjaAF4nDWQUW7DIBBENxgDjqNUPYh/8t0D9A5VVVEHJUgksYyr9vjdYRTZ5mmX4a2MHOVoxd7jLUl4j7l8nf5O4uuW4i2fZV/na7rFLc9VxnOq85qXLT/uIuJK/E6livn4tPI6P5YlrdNvLGUqcb0ksVe1yfGyxuWatzQta6pVXpZ7+mnC6bzmUlT0pp/0sgM8sJNODDCgMroYfREBHOGJwMiIqtOnwcrOKHrCszkQexywOkqdVk8ao+iIAUOtygSRA6oeEah6aUbPZos4WhwtjhZHi3tGmsXT4mnxtHhGAi2BlkBLoCU8IwdeR4sMaukALx2aLYLf0mqPSwAsMRBo69KSIyMjLIAjPIH5igHJAyKA7v0DucArZw=="));
                    addReqPart(Schematics.readBase64("bXNjaAF4nCVRW26EMAx0AoRsiLbqQfjoVy/RG1RVlYaopQoPBVZ7drTdTe0YRCZ+jMfYcIZzDfXspgD6zc0+pM8XaLc9uGkcwGz+J0xuH/0G3RA2n8Z1H5cZAFR0XyFuIN8/Wnj2y7qG1F9djH106TvAeQrz0K9p+Q1+XxLYdblixrZckg+gKBoSVSFNVPRu3/HytM7hUhT7IY0xotIrlKeiQzLUDIpBMxgGSyDpJhAkCDpq3RS2zI9CF/ghX1Toa0DUCAokWZoJJ02GKKkKhKBaBu8IlvWRQ6BJq+amqIxsEBTFaiQUNxIaILck0Nx5iSgiKKC3IstwrBBaJrRM0EzQrKCpAFmGY6WlE7d04pYMT8DQX1AnKEVW+QdDBAkdjkXmW37kIz9wFB2NgqDMoMMZ6JzzPd+Jk/8w75ZvINB35APxng9B8wYee9mN5d1Y3o3l3VjejaXcf/PKWLU="));
                    addReqPart(Schematics.readBase64("bXNjaAF4nC2Q607EIBCFT6H31mx8kP7YB/EJjDG1Ra2BQqCb9dU1ujgDpiEfzOWcmeKEU4lyn43C+KD0sdrr/nz+PKMJh5rNtqIPy7sy87EtAcOqwuI3d2x2B1Dr+UXpAPH4VON+sc4pP11nrSc9+zeF06v1i5qctx9qOaxHbdS+Ko/W/FthdPZKTcFeqBLjZtxFB2q5GEcGEx0IFIwyo85oM/qMkcE3UUDSVwjqqiD43kBycOC8wB2XS64UBCqh7VG1RbylcJkLKi6Q5EUClEbfJnHJogOrV2xZkXaDgi16bq5zc5NmpgnTq8teHYUkIyl2KFkx3qSk1ponHHjskl5dW8bfeIs/8Tt+0VgkldJpgT6LDll0YHVBo3T5J6TcHXhdgsyoMpqMLoPc/gBKZEUh"));
                    addReqPart(Schematics.readBase64("bXNjaAF4nC2RfW6DMAzFHQiQUaCs9+CfnmAX2AmmaWI02pj4iICqu3rVdZmdNxD+keD3HBs60EGTntrRUvFsh+00X6a34/eRsnWz7difKF+7Tzu2W9+ttDvZtVt6t/XzRETp0L7bYaXo5TWlx252zi7NpR2GZmiXD0vpaKeTXagSNm6Zv2y3zQuZ8b8SFW6+sGadz0vHJ+hHdx5W27jz6Nj/icIVISiBBlLAADlQABVQC0QaKQbfEmJSskpIxfyeATtsVsAeupqYseg0IzFxqEr+7r2/K3EKdbQU4O/iLKtaQkKVifxdstlT8TqYppKbcFWuK8ilfEYlvtVoKNg9wM7IZsJ5QZCjzxwpO+hy6Ar0WciYNCM22t9I+yvvy8lj77lWicZL9leiqaXREqOtxEEzCpP5H//Lz41bvbKF4vgbjHgiSgZVyqgq7iqMO4xqL9MVxEAhKXuk1DhFHX4AQwMpYIAcKIAKYOUfDUVYnA=="));
                    addReqPart(Schematics.readBase64("bXNjaAF4nC2Q3U7DMAyFvaxLs7Qa4kF6wRWvwT1CKLQRdEp/lHYarz62EewcWilfHPv4JKYDHQoqRjd4si8uuhDc9/sTlcvq3dB3ZJf2yw9u7duFqs4vbezntZ9GItLBffiwkHp9K+mxnebZx+bMHZrg4qenw+DHrpnjdPTtOkXSEvtI9TyduXKZTrH1ZOZ/V/Zs3bpywcM8+lP2bLrYh8Bez5Q/hWUjKAANGMACtUCK1YahKC8F0ZaxM5xLdw4Vi5UcGbPNFZt0I9Qq2hOJ1sKxFr+taAQG/iSRtKEdQyOyyNVyKMdKYHDTLNAi0PnPkUUuC0oISggMBAYORhpIZJHLV9rjSnsUWbzYyl5uohFZTKaWh1QYQ8VjUOmS7inxKCQ2puSdSr/plq7Me/pJF+aVmfJEKpmJzFYaMBRQABowgAW49g8VzVhN"));
                    addReqPart(Schematics.readBase64("bXNjaAF4nC1Q226DMAw1AcJlEe0+hIc+7T/2Ok1TBlmbKZAooer+fWqZHY8gH9k+Jz4xHOFYQbXqxUD/akNw5uP0c4ImbUYvdoY+TRez6M1OCZ5mk6Zow2b9CgDS6U/jEoi39waeJx+CieNNOzc6Hc8GDl8+TmYM0X+bafMRZMwDQC5mnU0EFfwNJclfkQfDOepwsRspTEpwCKu55snjHK1zOPEF8ic4FAQVg2RoGXoGxTAQkEYUUOIpBAZJpRI6ACoqpgx8Y2ZXlAnklYB/jQdLNciWEvrK/bHfc6MBQa2uRdl+3x95So0usl6xP0xQjeUMHRcHCg1dQN6RIrCDA2vMMrMlJhUVP2+gXoeC/EpslQjZWp+tka1fqqM1qv17ym8las/CgZ6teBeKd6F4F4p3oXgJildCoSQQDBWDZGgZegbFgJI/SahJKQ=="));
                    addReqPart(Schematics.readBase64("bXNjaAF4nE2S3W6bQBCFhx8DBorXVl7DN3mU3lZVRfE2ocIGgaP02d0k9Jw9qWRb6289O3NmdnbswR5SSy/t2Vv5tZ+mwf94/PNo+XL17bk/Wbl0z/7cXvtuserkl27up2s/XswsG9qfflgs/va9sH03TpOfj6/tMByHdn7y1pz95XSc5vG3767jbLtf49z5O0M2h4SW0dHPVk/jKySW8QV+1jzN7fTcXxnhl8V208W/hEqOp7kfBlRwsvCJ9RMRqZAJhVAKtdAITjgQVIgjILaESIVMKIRSqIVGcJYw/KDc2Ma2oSkBUAR6i3CkSG1Lh9QqnX2xiJ5OcQjf4FsUufHY1r8Wr7f1Hbv39QPrDesW0aeiD0+wR4XYbbASrAg+a8S7o4wMwO2JcPuMOVLLedUEJynPcphjopZxx4Jz26t1UGHxCMgBbIhSxk+XoLmlSwYEsS3FaNyr80GlVNqKaXP8C2IlzTSGtOX/gKBZSbOSZiXNSi61NGu51HweAs9D46fLwfRGTNvwJYjQ9EZNb6iyAe6ajlbfNAvrB9YbHyGK4LNlt5v73sMBpYU3SMLsxaH3jcrfsX1EpUkL0+U0XU7T5TRdTtPlNF1O0+U0XU7T5TRdB1V5CBmBVMiEQiiFWmgEJ0DgH5nidfc="));
                    addReqPart(Schematics.readBase64("bXNjaAF4nCWM3Q6CMAyFjzAG4oUxPgcXPIuXxpg5Gl1SYNnw5/Fdt6TtSXu+HvToFdRiZsL+Yviz3sffiDZuZGY3oY/2RbPZnI04TBRtcH5z6wJAs3kQR1TXW42TXb2nMHwN88AmPAlNlDgc/ULvnDBMwTGnz3NqKFTALo0sWiQVKjkpASp04tQFqBOSv5IhgCpbRpqCNAXRJUUXRAsCtDk8SUY68f4ndiO3"));
                    addReqPart(Schematics.readBase64("bXNjaAF4nCXNWQrCMBAG4L/bdLEgXsAb9KVX8VFEYhs0kC4kdTm+Mx1C+JPJlxm0aHPks5ks6ovxn+Xe/3qUcbNmciOaOLzsZDY3RBxGG4fg1s0tMwDy5mF9RHq9ZTgNy7ra0H2N95034WlRRGmH4zrb996hG4Pznn+eeSNHIkESCVJeHJXc+IRUSkwkSIuNgEykRKU9+EFcjp2TFndZqCxUkkpSSSpJZamyVFnp9IplJkE8jKORt5plJrHLRogEkz8UYiR+"));
                    addReqPart(Schematics.readBase64("bXNjaAF4nDWPQW6DMBBFP8YBhyCq9g5dssgpcoAuqypywUosGbBsmvb4nfG0IPE04/8+gAGDhl7t4vDyZsNju16SjXe/u+v554w2784ufkaXp7tb7O6njNPs8pR83P22AmiC/XQhQ71/aDxPW4wujd82hDHYdHM4ZO7FcPsrHmNyOeMpru6rNI5z8iFQ0yu4DgqooAWGofiu6NHI1DFqytKy5iTDyLL/l5UiNHymSaDrwIIiaIGRZRG4WNWEBhVPRWhFaEVoRWhFMPIGQ9kyFeEon3QkAQwjy57Ryf90LPBUhBMnGSXZ89kvAX0ueg=="));
                    addReqPart(Schematics.readBase64("bXNjaAF4nDXQXW6DMBBG0QEcMIaStPvgJeoeuoA+VlXkEitBMmDZ9Gf5nc/TEomrTHxGEDrRSZFa7eLo6dX6r+3yEm24z7u7nH/O1KTd2WW+kknT3S12n6dE3dWlKc5hn7eViGpvP5xPVL69K3qcthBcHL+t96O38ebokLCXhtvf4jFElxIdw+o+88bxGmfvedMzYR0ViEYKUlQiBt9KfAq+8RFEy7BHKib8WwWAGBkO/zvKksMO0eCKHV8HuJKjJEaG2dVwFafGqppdHmbXiGvENeIacVqcFqfFaXGtuFZcK64VZ+Q5DbsK0VRhmF0n79exqxAjwwHp2fGwh0M0/o1e3AMAksGAIwgf+QV6KC/d"));
                    addReqPart(Schematics.readBase64("bXNjaAF4nCWMQQrDIBBFf40xIVmU0nNkkbNkWUqxZmgFk4ha2uN3VFA/43/zMGCQkLveCONidEoUHvNvRhcT6c2uGKJ506aTNRHjStEE65M9dgDK6Se5CHG7N7iYw3sK01c7NzkdXsSOKsTZ7/QpjmkN1jnevfKFhABO/JRQOfhA5C+ZAYE+N00FGkbKFhcZkHUqSFuRtiKqWlRFVEaArsg5CtLn7g+ryiVd"));
                    addReqPart(Schematics.readBase64("bXNjaAF4nCXNYW6DMAyG4Q9IDaWVpl2gN+BPr7KfU1WlwdoiBYqSTO3xZ2Mh9ELyxMEZZwe3+oVx+gq+Vs736/uKvlT2S5wxlvDLi68xFJxmLiHHrcbnCoCSf3AqaL9vHT7Dc9s4Ty+f0pR8/mGZYQPxsa38t8+Y5hxTkrMXeeHQaEjToJVHMuiffKHVJSEassVRQadSM9gM2VDnLKRnncmDyYNJMkkmySSZ7E32Jge7fRDZaUguk4y6dxTZaXY5KtEI+Qca+yYs"));
                    addReqPart(Schematics.readBase64("bXNjaAF4nCWMzQrCMBCEp/mppR6kD9KDz+JRRGK62EDahqSij292y7IMO7PzoUNnYFa3EPqb37Kfn9ffFaeyk1vChL74mRa3B19wnqj4HNIethVAG92LYoG6PzQGv6VEefy6GMfo8pvQFuHhklb6CGKccoixVoe60HXQ1BGx0A0UmyyGTc1PHCsoSdDwZY9yy5nhjEVMyzBBHSIUKze/Q/8B15gkYg=="));
                    addReqPart(Schematics.readBase64("bXNjaAF4nDWO24rDIBRFd7yG9KH0D/oDecmv9LEMxRppBXNBU2Y+fzweisjystdWDBgU1OqWgOHmt+zfj+lvgi1HcEucMRT/Dos7oi84zaH4HPcjbisAk9wzpAJx/5G4+G3fQx5/XUpjcvkVYErrw3lfw6dVjHOOKVX1WickOoImdHUIgkVHK8lQDM0wlJRVEV2FgKBc3RM0ybLqrbinnaIIwfIzzdPsafY0e/obaZ5hz/ChpV76l2QohmYY6rGc7Pk3Pd39A/JZJSg="));
                    addReqPart(Schematics.readBase64("bXNjaAF4nCWQwW6DMBBE12AIAROlH8Ihp35Ij1VVEbBaKhOQoUr/PYroDsOBZ+9qZmctZzlbsbd29FK9zb5bo/+8/F3ksKy+HYdeyqX79mO7Dt0iVe+XLg7zOkw3EclDe/VhkeT9I5eXbppnH5t7G0IT2vjlJR/9rfdRTmAzx+lH/aeo3hwkbp7uKlmm39jp/Gtol7Xp4xCCur/K/iX8GcASOVEQJeGIEwBNYhSJ7L9UDG6ZmFTPB95Uh17FW80xJ1F9Cp1VZIW+ze6Wbdv2FNmeBvWySPX82DY9i6QGkpoB1UR1mAhUSGPhitTay3T6AcWcxYKCIwVHCgr2SvYqCkoWHVdzXM1BZxUZEulATShmT+k0pdWMSP1ASixmINk3dgzsuHENOyBDr8bbADVfFGJFQlgiJwqiJByhkn9Lh0gL"));
                    addReqPart(Schematics.readBase64("bXNjaAF4nCWQUW6DMAyGHQiUAqUB9Rq89BC7wB6naaI02phCQYGpu/pUVZnNz0M+YudzYtOJTpr0rRstFa+z7VdvP86/Z9otq+3G4Ur50n/ZsVuHfqHiapfeD/M6TDciSl13sW6h6O09pbqf5tn69t4517rOf1pKR3u7Wk+VsJ399M31J8+1cRGV83RnZZl+fM/3X1y3rO3VD85x9RfavgiLEmggBTIgB0qgAgzQCKRCpBgRbUtMJLuE+C+mHSlBQUqCFY4ccaTG3Y3sYtE1I8l4YLzRlIYQnkThqSReZTHp8AxBLqBYiVLj1VyEPSkhOCLYoBXOJfyIHZGgQLBBf5u3h7eHlyGXwyvgFfBy5Ep4B3gHeCVyFXJH9mJBIY+tkDOYk8GcjGQ0I8ni8JAY96q2Xg33qsODe32EP+lVxqNE2eZm0LbB3GpRBQlQyaBrHGk2mREBGkiBDMiBEqgAA3CBf9KRTi8="));
                    addReqPart(Schematics.readBase64("bXNjaAF4nDWS3Y6bMBCFBwwkIcQhUV4jF/sUfYBeVlXEEneXigQEVNt3XxJ6Zk4Xa/jwcDw/tuUkp0SSe3ULcvreh3oawuXbUPXvzRQuL39fZDVOobo1V8nH+j3cqqmpR9lew1gPTT813V1EsrZ6De0o8Y+fKznUXd+H4fxRte25rYa3IPtf3VCHcz90v5GhGyS7hfs1DAjOlFL03QfWjN0fCMW//a8AS8KIdK9tNU7n69C0LdJdxJ6Yr0iREBmxJnKiIDxREkeFRogjcRhRjFkq9r0Sp9iKU6cXUUnJBUd9OVUqPHNjEgPmTL6cpkxVpnBEQqT854m9Zk81g+KgiTKNaZXoggzDJXit1hv8SJd5eS4PmI7F7AGbYZ/4ml0KLTZBgU3QlcU6F7c8RKCYJcZXvDxhi5mDJbAU/sQS7nQPVijNaTHoJcUsk1hRcI9RYSYbjEhnJtlQsqEkZxNbbUJhwXIqt1RuqdQXZgXiOEWuyoJbuGPzu6/mUegTBT+tgcjsAZthn/DPpkW72A5rF+WlsMROMZLYhrOTUmq7O21X7OBiPXA7M8+99zwzr0fp0IJdjj0vh1eRYk+UxIHBjnpxSt6tvS5XeDrtNpXMV/JfyYtTsusDnQc64dKYR6sYSIiMWBM5URCeKAkE+AfqeoyK"));
                    addReqPart(Schematics.readBase64("bXNjaAF4nC2SXW6jQBCEm+HHgAEDRlYu4ZdcYg+wj6tVluBRQoQNGoiSo2eVjdguKpaGD6a6q9vTI3dyF0hwa69Wmp+T7RZnH364dnruF/tw/34vu3mx7bW/SDp3z/baLn03y/5i587109KPNxGJhvbRDrOYX793UnXjNFl3fmuH4Ty07slKcbW3y3ly44sWGJ16spJEEKyTbBrfNGUeX12n4U/fDWiKnbXa49DOy/ni+mHQan9k+xk+PCAgIiImUiIjCqIkaqIhTgD8jKcw4gOB+PomO/GAPXEQD1rNkEZ3FCe2oK9GQt3ygCM8A2oRNF+hDQb62MW+hpn13/ql69MLdU+bBlIiIwrGl4hHrAS6jAerCv4R/EPtMRIDlPz7WycJOokUiRggR6sJ+4qZlzAvYV7KQ02p7antqWX0zOmZ0zOnZ07PjHk583LmFfQsqB2oHaiV9KzgGepXLD6QEhlRYAoVK5Rw8fVrO8ZKj3E7jvUDI1u/dH1u+2Ucfh/t3/UD+9Al8JC6HVvNykcO98jhHjFcaCfejO02NLwNDW4DUGP+De9Nw0g8fMAQARERMZESGVEQJVETDaF2/wH5AWVt"));
                    addReqPart(Schematics.readBase64("bXNjaAF4nDVQ0W6DMAy8hhASaB/2IX3ov/RtmiZGow4JCiKg7uOrdr54U0jOXGzfxTjgYGFv7RjRnNMmQf95+jmhSmuU+II6dd9xbNe+S2guMXVLP6/9dAPghvYrDgnm/cPhrZvmOS7HezsMx6FdrlF6xOsYbyucnJe4oFpVAft5uktumrali9j347wNKR7nbZyl70k2DHYEq+AUvEJNYGR2kieLhwUKWU7/PBMMgmz5GlYVLDACllTBFFZJSJBaGQSst1R9PV9PQE9D3vuSdcI9hNuRz+1YGrRDTQWrWiUvaaaAKQUcsrVsx2lKpXa8GvBqwP8byBImCz6lUeC7eO29+/P2eD3AR+Y55GeyS9Bm2YtXoaCDCjqooM8POqFA2kh6JmuSHG+ua+S+IBQKpUKlIEK/XFpLJg=="));
                    addReqPart(Schematics.readBase64("bXNjaAF4nCWQXW6DQAyEzU83wDpRepC85AQ9RN+qqqJklSJBQCwovXulpJ6drOSPdTzeseUox1LKWzsG8e9xs4/+6/x7ll1cg31fpIndTxjbte+i+EuI3dLPaz/dRMQN7XcYouQfn05eu2mew3K6t8NwGtrlGqxHuI7htoqzeAmL7Fa+IDpPd6uN07Z0QbQf522I4TRv42x93yT9coYMKAlHVERDKHEAoMkzg+kQSpHCjumAmkkvghKl4IBQoBLwfM0uEFsGMK0tScoKmfz5eD5EGDPkPfLpniGXLECmVO9NbzhIMlJI/mIwR0ANY45WHGd2qMSMdoPlJKgpqCmoKahY2VDXcBJP056mPUybqTxZSxt9PqyDciWKfqjy1Q4D2Pmzk6K9kKXOpsrQUNk3jaPcs3LPyu0pN6t0p/Sz5397bhaGCyAnSsIRFdEQSpjkH5EJVrU="));
                    addReqPart(Schematics.readBase64("bXNjaAF4nCWPUU6FMBBFh7aUPt/7cQkugI+3Gj+MMRUmStJCQ0Fcu8bUuTRDc8rcmXuBrnQ1ZGYfmdyz/+K3+/eduryxj9NID3n45Oi3ach0HTkP65S2aZmJyAb/ziGTenlt6XFYUuK1P3wIffDrB9MtLYd08rKvA5ONPI+8kjkkg25TTHvI3Kc9JvF6kkOKGsBU2AoH4KYaGVBOE6olNC068iaPwhogaxh0TuRSTtcLfDSu+pxssOMQ1ErJlqEOEaZOwlsjQkGzVbNV6+CiBAqOnZwGaBHq5Oua8kNnPCbRck6XP6lfolJExA+dTheRNaAr2gqx/AcFtDgt"));
                    addReqPart(Schematics.readBase64("bXNjaAF4nEWQ0W6FIAyGiyKHo+QkexBvfJldLMvCtNlMQIno3LOf5OSspRejCR/Svy2/cIObBr34iGBf/Q9+DL8DXPKOPs4TtHn8xuj3eczQTZjHbU77vC4AYIL/xJChentv4GVcU8KtP30IffDbF4JL60k3eT22EcFEXCbcQJ80A9wc0xEy9umIiXoN8L8qUAwtMAIraAVOdLQUobIKaqgVnYiqJmiBEVAx56iY5R3Lnw/FHagRVXIPRitzy9ZQFFj5LspGlI0ojbzXSO7COU3yUme5ji9beT0IivIqXa6SKxvP1lAzjMDSHcGxxY4sVs8HvfpebHZsk1EcdeSI/JS4U7AzJwNd+SMELTACK2gFpP0DDXI9jA=="));

                }
                //addReqPart(Schematics.readBase64(""));

                for(Schematic s:factorySchematics)
                {
                    addPart(s);
                }
                if(Core.settings.getBool("ec620.schematics"))
                {
                    addPart(Schematics.readBase64("bXNjaAF4nDXKQQ7CIBCF4TeUtmqNN+mi5zEuEEYlKdAAjfHuJgo1Tt7iy+RHi0ZCeuUYNKFPmZWzBoekH+xUtjphMJx0tEu2wQPoZnXlOUGcLxKDzezGFNaoGZ1jbzjidI9h9Wa8KZ1DfOG4hCfHfwX0qNeWUQUV7alS1AnQ7w3Ireo21YqoKRI7+rxL+QUWqCTv"));
                    addPart(Schematics.readBase64("bXNjaAF4nDXKQQ7CIBCF4TeUtmqNN+nC8xgXCKOSFGiAxnh3EwUSJy+Tb/GjRychvXIMOmNMmZWzBoekn+xUtjphMpx0tGu2wQMYFnXjJUFcrhKTzezmFLaoGYNjbzji9Ihh82a+K51DfOO4hhfHfwWMqNeXUQUV7alS1Ak01ydbNTTViqgrEjv6fkr5AxbKJPA="));
                    addPart(Schematics.readBase64("bXNjaAF4nDXKQQ7CIBCF4TeUtmqNN+mi5zEuEEYlKdAAjfHuJgo1Tl4m3+JHi0ZCeuUYNKFPmZWzBoekH+xUtjphMJx0tEu2wQPoZnXlOUGcLxKDzezGFNaoGZ1jbzjidI9h9Wa8KZ1DfOG4hCfHfwX0qNeWUQUV7alS1An8XJ7cqm5TrYiaIrGjz7sEXxbsJPE="));
                    addPart(Schematics.readBase64("bXNjaAF4nDWPQQ7CIBBFP5Sitsalt+jC8xgXCKOSCDRANd7dWKHGySzezJ+3GLSQAsIrR2AHrFIm5axBl/SNnMpWJ/SGko52zDZ4APKuznRP4MdTi95mckMKU9QE6cgbithdY5i8GS5K5xBf2CtjbLYPGiLp4FOOUw2wHcOT4t8GOtRqS7MKrNCGVeRLgKYuOC8j+x00BQUKi0WTC1WNsfIU+JrPb2D+oEhlrn7DvwXLME8="));
                    addPart(Schematics.readBase64("bXNjaAF4nDWPQQ7CIBBFP5RWbY1Lb9GF5zEuEEYlKdAA1Xh3YwUSJ7N4/0/eYtCiExBOWgI7YRMTSWs0+qgeZGUyKmLQFFUwczLeAegmeaUpgp8vLQaTyI7RL0EROktOU8DhHvzi9HiTKvnwxlFqbZJ50hhIeRdTWMoB+9m/KPxtoEeZNi8rwDLtWEFeD2hKwXmOtWWsySiQWVStq1Q0xvJT4Fu+foD1iyzlXPyG/wAGAjBQ"));
                    addPart(Schematics.readBase64("bXNjaAF4nDWPQQ7CIBBFP5Sitsalt+jC8xgXCKOSCDRANd7dWKHGySze/5O3GLSQAsIrR2AHrFIm5axBl/SNnMpWJ/SGko52zDZ4APKuznRP4MdTi95mckMKU9QE6cgbithdY5i8GS5K5xBf2CtjbLYPGiLp4FOOUz1gO4Ynxb8NdKjTlmUVWKENq8iXA5pacF7ir2VNQYHCYtHkQlVjrDwFvubzG5g/KFLJ1W/4FwY5MFE="));
                    addPart(Schematics.readBase64("bXNjaAF4nG2OwU7FIBBFL9DS+mpM3LjyF7rwe4wLhFFJCjRANf67eXXoe+4kMNyZ3HsAA6YOXTSBIJ4wlEomeIdTsR8UTPW2YHJUbPZr9SkC0It5paVAPr8MOK3pi/IckyNMvlKYS9qyJehA0VHGY9iW6tfFW4Z90pzJplhq3mxNGXfvOW3RzW+mtd94MM75f3y3l2eubOAe7SOjgNjPgivQ8z6UYHUjJCuJ1kK1gWyDDoejPxwKCiNExxdPeXFQXSz9gdOHajghNCs5dvsPsJ8h+WhOSNkcja+Y3/+F+2tYQ7OS4hcScETM"));
                    addPart(Schematics.readBase64("bXNjaAF4nG2OzU7FIBSEB2hpvTUmblz5Cl34PMYFwlFJCjRANb67ufXQe92Z8DNn8s0ABkwdumgCQTxhKJVM8A6nYj8omOptweSo2OzX6lMEoBfzSkuBfH4ZcFrTF+U5JkeYfKUwl7RlS9CBoqOMx7At1a+Lt1z2SXMmm2KpebM1Zdy957RFN7+ZNn7jwTjn/+FuL89cu4F7tI+MAmI/Cz6BntehBKsbIVlJtBGqGbIZHQ6iPwgFhRGi44tdRjmoLkh/1OlDtTohNCs5dvsPsJ8heWuOSNmI1q+4v/8L99ewhmYlxS8SykTN"));
                    addPart(Schematics.readBase64("bXNjaAF4nG2OzU7FIBSEB1povTUmblz5Cl34PMYFwlFJCjRANb67ufXQe92Z8DNnMvMBBkw9+mgCQTxhKJVM8A6nYj8omOptweSo2OzX6lMEoBfzSkuBfH4ZcFrTF+U5JkeYfKUwl7RlS9CBoqOMx7At1a+Ltwz7pDmTTbHUvNmaMu7ec9qim99MG7/xYJzz/+RuL89c2cA92kdGAbGfBZ+A4nUowepGSFYSbUTXDNmMHkdCHYkOHUaIni92AcnF7hJRB04fquGE0Kzk2O8/wH6G5K1bRbZE43fMV39ldS1raNW4vxMkRM4="));
                    addPart(Schematics.readBase64("bXNjaAF4nG1Qy07EMAycJE3SB+yBAxLS/kIPfA/iUJoAkdqkpCmPf0csTrN7QSiybI9H9kzQ4KZC5YfZgt1Dr8kOszNo1/HVzkNy44rO2HWMbkkueABqGp7stII/PDa4ntzb5ky/hi2OFu0SPmzsfTAWarbe2IjOJTtfCHf2cwne+uSGqY92DH5NcRtTiDjO25TcMrmRzr7bP9PDSwybN/3zkNsv3A7GuH94V0XB+RxwpEAnKwjOwKDqCuz0A4GacEHBGSdc05xSXVIDISi1EmgYfQ8OYAIcIEDuFdtHHPkhAyIDPLdV4cqdIamtwVRGWZbCNKpCUfs6tVd5HWM1VbxWp2+AJHIKlQVm2RSSZ2a+I+iOuiyR5yUaShY3urjRxY0ubvTuJhNr+g6gZb8oalYG"));
                    addPart(Schematics.readBase64("bXNjaAF4nG1Qy07EMAycJE3SB+yBAxLS/kIPfA/iUJoAkdqkpCmPf0csTrN7QSiybI9HHk/Q4KZC5YfZgt1Dr8kOszNo1/HVzkNy44rO2HWMbkkueABqGp7stII/PDa4ntzb5ky/hi2OFu0SPmzsfTAWarbe2IjOJTtfCHf2cwne+uSGqY92DH5NcRtTiDjO25TcMrmRZN/tn+nhJYbNm/55yO0Xbgdj3D+8q3LBWQ44UqCTFQRnYFB1BXb6gUBNuKDgjBOuaU6pLqmBEJRaCTSMvgcHMAEOECD3iu0jjvyQAZEBntuqcOXOkNTWYCqjjNQY06gKRe3r1F7ldYzVVPFanb4BOpFTqHxgPptC8szMOoJ01GWJPC/RULK40cWNLm50caN3N5lY03cALfsFKPVWBw=="));
                    addPart(Schematics.readBase64("bXNjaAF4nG1Qy07EMAycJE3SB+yBAxLS/kIPfA/iUJoAkdqkpCmPf0csTrN7QaiybI+nHk/Q4KZC5YfZgt1Dr8kOszNo1/HVzkNy44rO2HWMbkkueABqGp7stII/PDa4ntzb5ky/hi2OFu0SPmzsfTAWarbe2IjOJTtfCHf2cwne+uSGqY92DH5NcRtTiDjO25TcMrmRZN/tn+nhJYbNm/55yO0Xbgdj3D+8q3LBWQ44UqCTFQRnYFB1BXb6gUBNuKDgjBOuaU6pLqmBEJRaCTSMngcHMAEOECD3iu0jjvwhAyIDPLdV4cqdIamtwVRG2a6mURWK2tepvcrrGKup4rU6fQN0IqdQ+Zd8NoXkmZl1BOmoyxJ5XqKhZHGjixtd3OjiRu9uMrGm5wBa9gspgFYI"));
                    addPart(Schematics.readBase64("bXNjaAF4nG1QTU+EMBB9LbQUWDfRTdYYvXnm4O8xHhCqNoGCpfjx29eI07J7MXuYzJuZN28+UOI2RWrrXoM9IJu8rnvTopiaN93X3jQTylZPjTOjN4MFILv6WXcT+ONTgdJ43VfTMLtG46Iz77NpT+G1196RxIeunG4GO3k3N35wKMbhU7vKDq2G7LVttcON/hoHq603dfePftfPnTdjZ5pzYttXN8y2rV7qEH5jX7etOcPbrEOPuwH3ZLgSQMEYoZ1IkRBiKAPiIHRJ1ZxxCOzBEnBIlYItv0igqCUh4yylfIYkOLW6PFQ4itgtqHtLWiFHCRERiyUaQ7QolIQED4l05YrIkNSiwDJycUkwRTBSsignIwpyjOWEuJLLD0ArcjIZFiSyJBM8MMOchOZkJxFxFFGQYr1Grdeo9Rq1XqPiNYGY03/CzwIK/ykJFfGPG9qgwE6x5bAc+B9Qf2Tk"));
                    addPart(Schematics.readBase64("bXNjaAF4nG1QTU+EMBB9LbQUWDfRTdYYvXnm4O8xHhCqNoGCpfjx29eI07J7MXuYzJuZN28+UOI2RWrrXoM9IJu8rnvTopiaN93X3jQTylZPjTOjN4MFILv6WXcT+ONTgdJ43VfTMLtG46Iz77NpT+G1196RxIeunG4GO3k3N35wKMbhU7vKDq2G7LVttcON/hoHq603dfePftfPnTdjZ5pzYttXN8y2rV7qEH5jX7etOcPbrEOPuwH3ZLgSQMEYoZ1IkRBiKAPiIHRJ1ZxxCOzBEnBIlYItv0igqCUh4yylfIYkOLW6PFQ4itgtqHtLWiFHCRERiyUaQ7QolIQED4l05YrIkNSiwDJycUnGFMFIyaKcjCjIMZYT4kouPwCtyMlkWJDIkkzwwAxzEpqTnUTEUURBivUatV6j1mvUeo2K1wRiTv8JPwso/KckVMQ/bmiDAjvFlsNy4H9RKmTl"));
                    addPart(Schematics.readBase64("bXNjaAF4nG1QTU+EMBB9LbQUWDfRTdYYvXnm4O8xHhCqNoGCpfjx29eI07J7MXuYzJuZN28+UOI2RWrrXoM9IJu8rnvTopiaN93X3jQTylZPjTOjN4MFILv6WXcT+ONTgdJ43VfTMLtG46Iz77NpT+G1196RxIeunG4GO3k3N35wKMbhU7vKDq2G7LVttcON/hoHq603dfePftfPnTdjZ5pzYttXN8y2rV7qEH5jX7etOcPbrEOPuwH3ZLgSQMEYoZ1IkRBiKAPiIHRJ1ZxxCOzBEnBIlYItv0igqCUh4yylfIYkOLW6PFQ4itgtqHtLWiFHCRERiyUaQ7QolIQED4l05YrIkNSiwDJycUnOFMFIyaKcjCjIMZYT4kouPwCtyMlkaCGyJBM8MMOchOZkJxFxFFGQYr1Grdeo9Rq1XqPiNYGY03/CzwIK/ykJFfGPG9qgwE6x5bAc+B9R1WTm"));
                    addPart(Schematics.readBase64("bXNjaAF4nDWLUQ7CIBBEZylQFa/SD89j/EC6RpJSGsAY726sUONmNvuyeQOFTkLONjDohD4XtsGPOGR352CLdxlm5OySX4qPMwA92StPGeJ8kTC+cBhyfCTH0IHnkROM9Wm4WVdieuG4xCenvwL0aKNqqAFV2lND0SJAvzcgN0tv1CyirpLY6fUNrB+IunrrUb0EJb5awSev"));
                    addPart(Schematics.readBase64("bXNjaAF4nDWLQQ7CIBBF/1BK1XqVLjxP4wJhjCSlNIAx3t1YgcTJnz9v8QY9Ogm5as+gC4aUWXtncUrmwV5nZxJGy8lEt2UXVgBq0TdeEsR8lRhdZj+l8IyGoTyvliNG7eJ01yaH+MZ5Cy+OfwUYUKcvoQpU6EgVRY1A41qyWapRtYi6QuKg9g+wfyHKqvZH5RJ68QNa9yew"));
                    addPart(Schematics.readBase64("bXNjaAF4nDWP0W4CIRBF77CAttv46F/sg9/T+EBhmpLIYgBr/HejZWgkkDmZe+dOgIHV0KtLDDpgUxu7FAPeq//h5Fr0FXPg6ks8t5hXAPbkvvhUoT6PBnNsnJaaL8UzbOI1cMHsYlm+nW+53LB3IcQWf3kp7PNaW7mIgI9zvnJ5jQI7yDH9kgB1eiNBNQRM0lDS0BgOMxyq6/Q/MXUcmh45dpDkEPUvQm3t8w48H1D92ZFMvRKMEqcsmPoC/QoxI+QPOEUxTA=="));
                    addPart(Schematics.readBase64("bXNjaAF4nDWPzW4DIQyExyzQn6167FvsIc8T9UDBVZHCEgFp1HevmmJLQUaMPMNnGQ7ewu6hMOiAhz44lJzw3OMXlzBy7FgT99jyeeS6A/Cn8MGnDnN8d1jz4LL1emmR4QvviRvWkNv2GeKo7QdvIaU88jdvjWPd+2gXMfByrldu96/AK+S4WSSCpnoikUYNLNIw0rDQhNOEmb7GiJYp1bPK8aqEQzRXhHn0t1/g9gczr1cyzZfgjCRlwDIH2DvEKeQfOJ0xTQ=="));
                    addPart(Schematics.readBase64("bXNjaAF4nG1PzU7EIBgcoHytrWcTk32FHnwe4wHpZyTpzwZYje9uXD9gvXmgM50MMwx6TB263W0M9YQ+ZXZbWDAm/86by8EnTAsnH8M5h2MHQKt75TVBP7/0GM/HJ8d5PxYGbbwvHDGFzNucjkv0jNN2WXM4r8FL2AfPkf2xpxwvPh9idSHOb67wLzy4ZQn/mO5bxy0QeER5xdBBXX9gMMifkaOVkm8PU2BocAejBUYrVGmIJMxWpqomCHEUwRRBF0GSi2Crw9QK1QnUgnLRNIutcVRZiVOKhOmBrt+AvE3LIbR0KoG6OEuPkR77F2JvIQSybQa1GdRmUJtBdQapXy+3RDk="));
                    addPart(Schematics.readBase64("bXNjaAF4nG1PzU7EIBgcoHytrWcTk32FHnwe4wHpZyTpzwZYje9uXD9gvXmgM50MMwx6TB263W0M9YQ+ZXZbWDAm/86by8EnTAsnH8M5h2MHQKt75TVBP7/0GM/HJ8d5PxYGbbwvHDGFzNucjkv0jNN2WXM4r8FL2AfPkf2xpxwvPh9idSHOb67wLzy4ZQn/mO5bxy0QeER5xdBBXX9gMMifkaOVkm8PU2BocAejBUYrVGmIJMxWpqomCHEUwRRBF0GSi2Crw9QK1QnUAiUXTbPYGkeVlTilSJge6PoNyNu0HEJLpxKoi7P0GOmxfyH2FkIg22ZQm0FtBrUZVGeQ+gUwOEQ6"));
                    addPart(Schematics.readBase64("bXNjaAF4nG1Q207FIBCchQLtqZrog4mJ8Q/64PcYH7DFSNKblHr5d+NxgeOLkWSzs8Owwy4aXFWoZjs50D3MFp2d/IDD1r+4yUbfb2gHt/XBr9EvMwA92ic3bhAPjw3OR/+6+6Hblj30Dq2PbvotDuvy7kI3L4ODntw8uIAb97Eus5ujt2MXXL/MWwx7H5eA22kfo19H37Ptm/tz21ofumeb8Ceu7TD4f0RnxfHkD9xxoFUVpCBGlwpoSICg6wp0/IZEzbzkECSZN5Ap1SU1kMTpkJ/xlnABIgiACZUR5SvJiNlEyESIRFRFq7JCcVmDdGH5kEFVCp3b6YxSO6Kakaj18QvgLwoOnT6IrIASKScfyT76t4k6NTHQqkxjyjSmTGPKNCZPk4Q1LwY4ZJQW09IPHlxU8A=="));
                    addPart(Schematics.readBase64("bXNjaAF4nG1QS07FMAwcJ03SvgISLJCQEDfogvMgFqENIlJ/pCmfuyMeTvLYIBaWx5OJxzYaXFWoZjs50D3MFp2d/IDD1r+4yUbfb2gHt/XBr9EvMwA92ic3bhAPjw3OR/+6+6Hblj30Dq2PbvotDuvy7kI3L4ODntw8uIAb97Eus5ujt2MXXL/MWwx7H5eA22kfo19H37Ptm/vz2lofumeb8Ceu7TD4f0RnxfHkD9xxoFUVpCBGlwpoSICg6wp0/IZEzbzkECSZN5Ap1SU1kMTpkL/xlXABIgiACZUR5SfJiNlEyESIRFRFq7JCcVmDdGFZSgZVKXRupzNK7YhqRqLWxy+ARxQcOg2IrIASKScfyT76t4k6NTHQqmxjyjambGPKNiZvk4Q1HwY4ZJQO09IPHvFU8Q=="));
                    addPart(Schematics.readBase64("bXNjaAF4nG1Qy07EMAycpE2atgsSrLQIwY1zD3wP4lBaIyL1RZry+PZFLE66e0F7sDxxxuOxUeIuRTrUPUE8Ips91b1tUczNG/W1t82MsqW5cXbydhwA6K5+oW6GfHouUFpPfTWPi2sIF519X2x7et548o4lPqhy1IzD7N3S+NGhmMZPctUwtgTd09CSwy19TeNAg7d1949+3y+dt1Nnm3NiZW1d9VoH/I1d3bb2DGmzTjwaAx44cK2AQghGW5UiYSRQBiTB6Ip/cyGhsINIIKFNCnH4RQLDLQmHFCnXMyQhmTXl4UeiiN2Kuy9ZK9S4oCIS8YvHMC0KJaEgQyFduSoyNLcYiIxTNAlhGEZKFuV0REFOiJyRNPrwA7BFyaGDQSZrDiUDM8xJeE52ElFHEQOt1m3Muo1ZtzHrNiZuE4g53yfcLKBwn5JREe+4YQcFtkYc9oe9/ANuxWOO"));
                    addPart(Schematics.readBase64("bXNjaAF4nG1Qy07EMAycpE2atgsSrLQIwY1zD3wP4lBaIyL1RZry+PZFLE66e0F7sDxxxuOxUeIuRTrUPUE8Ips91b1tUczNG/W1t82MsqW5cXbydhwA6K5+oW6GfHouUFpPfTWPi2sIF519X2x7et548o4lPqhy1IzD7N3S+NGhmMZPctUwtgTd09CSwy19TeNAg7d1949+3y+dt1Nnm3NiZW1d9VoH/I1d3bb2DGmzTjwaAx44cK2AQghGW5UiYSRQBiTB6Ip/cyGhsINIIKFNCnH4RQLDLQmHFCnXMyQhmTXl4UeiiN2Kuy9ZK9S4oCIS8YvHMC0KJaEgQyFduSoyNLcYiIxTNCmEYRgpWZTTEQU5IXJG0ujDD8AWJYcOBpmsOZQMzDAn4TnZSUQdRQy0Wrcx6zZm3cas25i4TSDmfJ9ws4DCfUpGRbzjhh0U2Bpx2B/28g9vcGOP"));
                    addPart(Schematics.readBase64("bXNjaAF4nDWLUQrCMBBEZ7dNqlaP0g/PI37EdMVA05QkKl5eTSouM/Bg3kKhadHOxgvoiC5lMd6N2CV7E2+yswn9KMlGt2QXZgB6MheZEvh0btC7LH5I4R6t4DCbh5mGq7E5xBf2S3hK/I9Ah3qqhCpQoe1KXIiJwKDfArSrqFeqIhEX4o36vMGlur4VUZcq/gK4CSQO"));
                    addPart(Schematics.readBase64("bXNjaAF4nDWLUQ7CMAxDnWzdgMFR+OA8iI/QBVFpXae2gLg80E4icpIX2YFB06KdxSvohD5lFe9G7JK9q5fsbMIwarLRLdmFGUA3yVWnBD5fGgwuqz+m8IhWcZjlKdPxJjaH+MZ+CS+NfxPoUcsUUQUqtF2JCzERGFTvOto12K1Ug0RciDfm+wGX7uobqGyC4R+4OyQP"));
                    addPart(Schematics.readBase64("bXNjaAF4nDWP0U5DIQyGfziA06mXvsW52PMsXjCokeQAC7AtvrxauozQ5Ev790sKC2dgis8EdcBTH+Rzinjp4ZuyHyl07CP10NJ5pFoAuM2faOvQx0+LfRqU114vLRBcphKp4a34q9/WLx9GbT/48DGmka60Ngq19NEuc4DXc71ReywD75jP8lcTFNOzkGbSQsvsCRpIw0pIQ0PdlxZGmRlROaGpUorvhN7Zv19oLjfFHHRcVs/UlC8sNw+BFcE/hYcwLw=="));
                    addPart(Schematics.readBase64("bXNjaAF4nDWPQW4DIQxFPwzQNmm7zC1m0fNEXVBwVKQBIiCJevkmxlIQlh7295OAhTMwxWeC+sJLH+Rzitj18EvZjxQ69pF6aOk8Ui0A3OZ/aOvQx2+LfRqU114vLRBcphKp4aP4q9/Wkw+jtj8cfIxppCutjUItfbTLHOD9XG/UnsvAJ+axfNUExfQmpJm00DJ7ggbSsBLS0FDzrdTCKDMjKic0VUrxP6Ff7f0fmstNMQcdl9UzNeULy81TYEXwAIXbMDA="));
                    addPart(Schematics.readBase64("bXNjaAF4nG2PzU7FIBCFD1CmtdWtiYmv0IXPY1wgxUjSnxvgXuPLqzNwdeWCzmE6fGcOekwdut1tAeoJfS7BbXHBmP172FyJPmNaQvYpnko8dgC0utewZujnlx7j6fgIad6PJYC2sC8hYYolbHM+zskHPG7ntcTTGj3DLmFOwR97Lunsy5Fwt7uLW+c3J7dP3Ltlif+M3TaXKxJ4gOwxdFDfXzAY+Gb4aKX428NIGVq5gdFcRstSaXCLla1K/fW0lceijPQ0/wPDpWHrkKkuquNSPeStaSO2EqkqISpFrPRgeTXNh2QxBpHAtEyJh2EP+wuwVwCBbEtBLQW1FNRSUE1B6geSGEMe"));
                    addPart(Schematics.readBase64("bXNjaAF4nG2PzU7FIBCFD1CmtdWtiYmv0IXPY1wgHSNJf26Ae40vrw5wdeWCzmE6fGcOekwdut1tDPWEPmV2W1gwJv/Om8vBJ0wLJx/DKYdjB0Cre+U1QT+/9BhPxwfHeT8WBm28LxwxhczbnI5z9IzH7bzmcFqDF9iF58j+2FOOZ5+PiLvdXdw6v7ly+8S9W5bwz9htc7kigQeUPYYO6vsLBoPcjBytlHx7mFKGVm5gtJTRilQa0hJlq1J/PW3L46JM6Wn5B4GXhq1DprqoTkr1UPLWtBFbiVRVISpFovRgZTUth8piAqIC02WqeBjxsL8AewUQyLYU1FJQS0EtBdUUpH4AkpVDHw=="));
                    addPart(Schematics.readBase64("bXNjaAF4nG1QQU7EMAycJE3SdlkOHJCQ9gs98B7EIbRGRGrTkqQLfB5w2t0L4mB5Mh6P7aDBXYUquIkgHmFTJjf5AW3q32hy2fcJh4FSH/2S/RwAmNG90Jggn54bHEf/vvqhS/Mae0K7zB8UuzAPBDNRGCji4DNNV8EDfS5zoJC9G7tI/RxSjmuf54jTtI7ZL6PveeyZ/lSPwZ255dWV1xfu3TD4f2Q3+wKXacCJAwddQUkBAVNXED/fUKiZVxxSSOYt1znVe2qgFKdWA43g38EthIIEmNAbElupIKmLR0GqcJIZbtgIvYk0P2sIU1hRthEW1S4xm6PZUHEUomYka80bSg5T9itbc2hZVGWG4hnmaqAvBhZG78fY/Ri7H2P3Y+x2TBHW/BtAK34BNTRTlQ=="));
                    addPart(Schematics.readBase64("bXNjaAF4nG1QQU7EMAwcJ03SdlkOHJCQ9gs98B7EIbRGRGrT0qYLfB5w2t0L4mB5Mh6P7aDCXYEi+oFBj3BLYj+EDvXSvvHgU2gXHDpe2jlMKYwRgO39C/cL1NNzhWMf3tfQNcu4zi2jnsYPnps4dgw7cOx4xiEkHq6CB/6cxsgxBd83M7djXNK8tmmccRrWPoWpD62MPfOf6jH6s7S8+vz6wr3vuvCP7GZf4DINOEngYApoRSDYsgD9fEOjFF5LKFLCO6lLKvdUQWtJtQEqkt/BLUhDAUKYDdFWykiZ7JGRzpwSRho2wmwiI88SZDNLMpDIodgldnO0G8qORKUgVRrZUEnYvF/eWsKorMoztMywVwNzMXCwZj/G7ce4/Ri3H+O2Y7KwlN8AavoFNbtTlg=="));
                    addPart(Schematics.readBase64("bXNjaAF4nG1QTU/DMAx9SZs0bTckmDSE4MZ5B34P4hDaICL1izQb8NsnMex0uyAOlp/t5+cP1LjPkQ+2dxBPKObobO9bVHPz7nobfTOjbt3cBD9FPw4AdGdfXTdDPr9UqH10/W4e96FxWHf+Y+/bS3gbXQwkcXC74JpxmGPYN3EMqKbx04XdMLYOundD6wLu3Nc0Dm6I3nZ/6A/9vot+6nzzn9h6sAdqebMcfWNr29b/Q1stM8+rAY9kuFFAJQShjcqRERKoGUkQuqZqKSQUthAZJLTJIU4/yGCoJSOTIqd8gYydWVzJFYkqdSvqviItzlFCJSRSiZFUrMEo45yk0cgXukokTV0GoiCX9oQwBBOlSIo6IVYUoiQkjaINJZnm/YioyZRkFs/IaEZxEVBnAQOtlmPMcoxZjjHLMSYdw8SS3sMvY8TvqQlV6Y0rml5hY8TpeDrKX+RsYm4="));
                    addPart(Schematics.readBase64("bXNjaAF4nG1QTU/DMAx9SZs0bTckmDSE4MZ5B34P4hDaICL1izQb8NsnMex0uyAOlp/t5+cP1LjPkQ+2dxBPKObobO9bVHPz7nobfTOjbt3cBD9FPw4AdGdfXTdDPr9UqH10/W4e96FxWHf+Y+/bS3gbXQwkcXC74JpxmGPYN3EMqKbx04XdMLYOundD6wLu3Nc0Dm6I3nZ/6A/9vot+6nzzn9h6sAdqebMcfWNr29b/Q1stM8+rAY9kuFFAJQShjcqRERKoGUkQuqZqKSQUthAZJLTJIU4/yGCoJSOTIqd8gYydWVzJFYkqdSvqviItzlFCJSRSiZFUrMEo45yk0cgXukokTV0GoiCX9hTCEEyUIinqhFhRiJKQNIo2lGSa9yOiJlOSWTwjoxnFRUCdBQy0Wo4xyzFmOcYsx5h0DBNLeg+/jBG/pyZUpTeuaHqFjRGn4+kofwHlE2Jv"));
                    addPart(Schematics.readBase64("bXNjaAF4nDWKXQrCMBCEJ2lafyroRfrQ84gP22TFxSYpScTLC5oqMszwze6gRWNgAnmGGrHJhcmLwz7bG3sqYjN6x9kmWYrEAKCbaeI5Q58vBr0U9kOOj2QZnefgOOFYKNyHK01JLJWYcFjik9N/BrTAL09aVajWVWrltZjvb6ebSmqr3q96/gCMaSUi")); //Stell
                    addPart(Schematics.readBase64("bXNjaAF4nE2MQQ6CMBBFf0tBQI1yEBaex7goZYyNtGBb4uWNSJuQOJt572f+IEchIKw0BHbBzgeSRveovXqQkUErj31PXjk9BT1aAMUgOxo8+PVWYK8DmdaPs1OEwpDtyeEUpH22d9k5rWQYHZoUOPqLDtP4Jrc1j4N+zbrfFKgRJwfOnK3AkiJbg4ZFEWAcHInBshVFPBKpU3ERtcyWD7B8EXeWSvFBxfKVRC5Qsh/d0zW/"));
                    addPart(Schematics.readBase64("bXNjaAF4nFWN4U7EIBCEZwuU1mrO8z36w+cx/uDaNRJLW4GLL288F/QShYQZvszOwmLQ0KsLDHqETZld8DNu0vTKwWU/JQwzpyn6PfttBdAu7sRLQvP0bHG7bx8cx7Sd48QYfOZw/bSB15kjHvboA4+RX9wp+snlLeKQ3fo2/gHHCv5l7hb/fvbztQ44oGzvQAQCDHDfNKi+PErAkQrQPwkjwJKCgoxoEaFyyIitEVNL+qYIdfryCVy+oESVqC0VtbWnVpw2Gl2dMr9MrrCevgF5Bj+j"));
                    addPart(Schematics.readBase64("bXNjaAF4nDVOW1LEMAxT0mT7YgscpB+ch+EjTc0Qtm1Kki735DgMQ3HK7I8tybItnFEpqMXMBPGEMiYysxvRRPtGs0nORrQjRRvcmpxfAJwmM9AUIZ9fanQzLWO/Bv9ONvmA1volprD9E5do7qPfgiWcspMCumSWS29ipHmYmN8f/NUMwVmTt+5W/0nhtnae3Mfmxhvt/JXCGNyV+tFzZuARORMKAQmlBb6YyixIFJA6G3gAMHqQBSMB5MZjxTYNobkJkc8Iiepw1vLESFTl/gPsv1D7NwrG+UnBVXPVKNkjtULDan38aouKmeIpC0eG5tBrFtos/AHmpkh4"));
                    addPart(Schematics.readBase64("bXNjaAF4nE1QW1KEMBDsyWOBxX2Vln55BD48j+VHgFhGgawJrEf1LpaKEywfVAV6enp60uAKewU1mN6CbpDF0ZretVjH5sH2ZnRNRNna2AR3HJ0fAKw6U9suQtzelbisTXRNZWK0fd3ZUPW+nTqLs6N/4Sr6KTQW294ObXUM/tE2ow8oGz/EMUzfhRtt/6NcJaUN2HTueXLtr8Fohqe/Ldgt9b2pg2tMMjksRLD/qK0/2dAGd7JV6zkecA0IbACCRp7T/AEtBOMCUiJDpgmvUIy0Br8FH24p5Atxzq0CLMdepBnCMiqSgmXMF7RipDRPEDHSuZg/oeZ3KMpQsg/l/OEeP1QwVCCVbpSGBTGiXLIcfJJIYsWcYMM1ki4tOVDSqZwhB+DtSxwJPb9BUtLrpGff7XLtUq6x4yRSMLHE2S18wcQ+EYRD+hvJQgAXaeEX69BgPw=="));
                    addPart(Schematics.readBase64("bXNjaAF4nDXHSw7CMAxF0RcnfIvESjpgPYiBmxrVUtNEcRDbJx0wuToXAT4gbJwE7oGTNeGkM64WF0ncNBqGWSxWLU3zBuC48iSrgZ4vj7stWsY3T1Ujt1wxaJM0Wv7UKLiV/JX6PyAADs517fXAAfC0y3VdiLroDPoBlhkgnA==")); //Elude
                    addPart(Schematics.readBase64("bXNjaAF4nE3LwQ6CMBAE0FkoCqIxfAgHv8d4KGUNmwDFtsS/V0sTEvf0ZrKDAgcFNeuJQTccfWA9SY+TNwNPOojxqHv2xskSxM4ADqPuePTI7o8CtQSeWm9XZxhXP8jSPnXnxOhgHZpUOP6rzot9s9sXl1Feq/R7BEpsVwB5Qh5V0SYV1RAhA6VMWaRCtEr/VVzEWNL3k6TiVqGkHxNVLys="));
                    addPart(Schematics.readBase64("bXNjaAF4nFWNUWrDMBBER5ZkO3VLce/hj56n9EORt2TBsh1JIZcPTVcqgVYgdnZ4O4MOg4FZXSCod3Qpkws84yn5EwWX2ScMMyUfec+8rQDaxR1pSWg+Pju8LHy+8Dyl7RI9YeBM4bG0gdaZIt72yIGmSF/uGNm7vEW8phPv0x9jrMY/5nnfrhQfacCI0m4NDkpBARbQaFC0LqOBrlr8X8KIGquy1dPC91BGhrjylBVpymJr3AGtDNXr+zf0/QYlH5JrS3YhtLT3qkCmbwQytbacl4ZONRLQQv8AaZE/tw=="));
                    addPart(Schematics.readBase64("bXNjaAF4nD2OUU7EMAxEx026bbcscJF+cB7ER5oYbaSmKUm6XJPzIERxK+DHHj+PR0aPVkPPJjDoCU0ubIJ3OGd75WCKtxm942yTX4qPM4DTZEaeMqrnlxa9jXMuabUlJvS+cBhyXJNlnALPjhMuk39bvfvD9/nql8HkzGGcZP1wzK9mTN6aPeRuie+c/u3xxsklf+PBRfkRuAAEXRM+pFeoauCRCAoQpYQo4UrtstY4Ky28Bh2F5H6vzWHucBJFrd6+gO0bevv8DaiESye0R35HjZj1ftkdoKcf5UhAUg=="));
                    addPart(Schematics.readBase64("bXNjaAF4nE1PW27DIBAcbBzbcZ2HKuUW/uh5qn5g2CpIxqRgp0ftXaoq6YIUtUKI2ZndmQVHHCTkrBxBvKCOCylnDbZRn8mpxeqIzlDUwV4W62cAm0mNNEUUr29bnEYVrR5UjOTGicLgvFknQqf9HJew6sUHdHYhN0S/Bk3YOJoNBfST/VitedC7eLaXPx/sc/2uxmC1SibHTAT6Rz1d/CdHPhz8lYIJ9kqD8fwd4AQU6AGBGnUl8MVljaoCalGgyeiZUQswKvm03FtKfopKYlsmqWSpzU2S0VEktUoqz3RsIjb8CMFxEDVDCZFD0xjLPURT3H+A+y0n9LwDJ/TsK9GI1CCbMpH3b743nk/jjyV3GbWixZ5XZ2KfiU78Am/UU5I="));
                    addPart(Schematics.readBase64("bXNjaAF4nDWKyw7CIBRE5wLFR030R7rwe4wLCteUpEADGH/eWEmNm8mZM4MOUkFFExh0xa5UNsE7HIudOJjqbUHvuNjsl+pTBKBnM/JcIG53hd5XDkNJz2wZOnB0nHEObKfhYcbsrakp47SkF+f/DeiAX16IGhBIQGDjVqC27UCyEe3l+m5aQK8fSPoCuMYnGw==")); //Merui
                    addPart(Schematics.readBase64("bXNjaAF4nE2M3W7CMAxGPzdOKD/T4EG44HmmXYTUiEhNC0kQ746mdUkkpPnC+nyObWgYBk82COiEVcpigx+wSe4qwWbvEraDJBf9Lft5AmBGe5Yxofv6Ntj6LOGY5kd0AhNkGiTiY/T3hx/e+DOIux4v9hy9s3mOODQQ5R/a3eanxPcFsEEtDeyJSiDUDlXAmrqSWDN6UujQPIhL5LrF7WxNuo69Wn6K7WCWX6hm649Ds9wrKOjlBVUs0x9BbTe7"));
                    addPart(Schematics.readBase64("bXNjaAF4nFWNwU7DMBBEZ2M7SQmIlv/Iod+DOLjOolqKk2C76s8jytpVJdjLzDyNdtBi0NCLDQw6okuZbfATnpI7c7DZu4Rh4uSi37JfFwDtbE88JzTvHx1eZv918dOY1kt0jMFnDo/QBl4mjnjbog88Rv60p+idzWvEa2B3Hv+AQwX/Os/beuX4+AbsUdaNxk4RCDDAjhoUX6VB5apyiNPS7akwU5hSUOhBWkSoHBmxugRT3+2pFaHe3H7Q3b4BUSU9JZ5kw9w3TN04VKDvoAx06hdzoT+E"));
                    addPart(Schematics.readBase64("bXNjaAF4nE1M2w6CMAw9hYLgJeqH+OD3GB/mqHEJA9xG/HmjbjMm9qE9PTdUqBk8KCugIxY+iLKmw9Lrm1gVjPZYdeK1M1Mw4wCg7tVFeo/idK6xMkHswY+z04LaytCJw6Y399l0P3prRd8OV3VxRqswOuwz4eSPWk/jQ9wvAayRpgJ2RBEQ8imQNsrIt1RExBWjoTIJWSGOkJOLc7qlKr1N+X4C7xfSpWjlbxXnqn02cQP6ALGBNbU="));
                    addPart(Schematics.readBase64("bXNjaAF4nE1QW07DMBAcx86b0hYkxCXywXkQH46zVY2SuNhJOSp3QYiyNuLxEXl2Zj0zDm6wU1CzngjiAWVYSE92QBPMkSa9WBPQDhSMt6fFuhlAMeqexoDs8anF9UTz0J28eyazOI/WuDksfv0e7EJTF9zqDaGIm+SxGe3Laocfmg3MsdMh0NSPLG/TfNC9t0ZHk30iPP2jrk7ulfyvgzuTH7w9Uzc4fsZdr4M1f5bd5IZ1JG5+zx9qSIEMZS7wxmeGPAdKFrIoZZCJuOVRAYx2QjISEJGQTNSCfxhUrlAllDNqeLNABVHwIUTMESVDFa9VyacWVVyo5OUjhRWXT24S1Wi6T6qqZMy/vEOyqji54qsiNoulWllzUskrTKSyTeJrJtpINNhig+wLyA9Y4g=="));

                }
                bases.cores.sort(b -> b.tier);
                bases.parts.sort();
                bases.reqParts.each((key, arr) -> arr.sort());
            }
            basegen.generate(tiles, enemies.map(r -> tiles.getn(r.x, r.y)), tiles.get(spawn.x, spawn.y), state.rules.waveTeam, sector, difficulty);
            //Log.info("Core size in PlanetGenerator: "+bases.cores.size);
            //Log.info("Core schematic name: "+bases.cores.getFrac(difficulty).schematic.name());
            state.rules.attackMode = sector.info.attack = true;
        }
        else
        {
            state.rules.winWave = sector.info.winWave = (int)(20f*Mathf.pow(10,difficulty));
//            Log.info("Difficulty: "+difficulty);
//            Log.info("Rules Win Waves: "+state.rules.winWave);
//            Log.info("Info Win Waves: "+sector.info.winWave);
        }

        float waveTimeDec = 0.4f;

        state.rules.waveSpacing = Mathf.lerp(60 * 65 * 2, 60f * 60f * 1f, Math.max(difficulty - waveTimeDec, 0f));
        state.rules.waves = sector.info.waves = true;
        state.rules.env = EC620Planets.ec620.defaultEnv;
        state.rules.enemyCoreBuildRadius = 600f;

        //spawn air only when spawn is blocked
        state.rules.spawns = Waves.generate(difficulty, pgRand, state.rules.attackMode, state.rules.attackMode && spawner.countGroundSpawns() == 0);
        //state.rules.spawns = Waves.generate(difficulty, new Rand(sector.id), state.rules.attackMode, state.rules.attackMode && spawner.countGroundSpawns() == 0, naval);

    }
    //private void addPart(Content c, String schematic)
    private void addPart(Schematic schem)
    {
        //Schematic schem=Schematics.readBase64(schematic);
        BaseRegistry.BasePart part=new BaseRegistry.BasePart(schem);

        part.tier = schem.tiles.sumf(s -> Mathf.pow(s.block.buildCost / s.block.buildCostMultiplier, 1.4f));

        bases.parts.add(part);
    }
    private Seq<Item> minable=Seq.with(Items.copper,Items.lead,Items.coal,Items.sand,Items.titanium,Items.thorium,Items.beryllium,Items.tungsten,Items.scrap);
    private void addReqPart(Schematic schem)
    {
        BaseRegistry.BasePart part=new BaseRegistry.BasePart(schem);
        Seq<Content> requiredItems=new Seq<>();
        boolean isGraphite=schem.tiles.contains(s->s.block.name=="graphite-press");
        int drillTier=0;

        for(Schematic.Stile tile:schem.tiles)
        {
            if(tile.block instanceof Drill)
            {
                int tier=((Drill)tile.block).tier;

                if(tier>drillTier) drillTier=tier;
            }
        }
        for(Schematic.Stile tile : schem.tiles)
        {
            /*if (tile.block instanceof ItemSource)
            {
                Item config = (Item) tile.config;
                if (config != null)
                {
                    part.required = config;
                    bases.reqParts.get(config, Seq::new).add(part);
                    break;
                }
            }*/

            if(tile.block instanceof ItemTurret)
            {
                if(isGraphite) requiredItems.addUnique(Items.coal);
                else
                {
                    for(Item item:((ItemTurret) tile.block).ammoTypes.keys())
                    {
                        if(minable.contains(item))
                        {
                            if(item==Items.thorium && drillTier>3) requiredItems.addUnique(item);
                            else if(item==Items.tungsten && drillTier>4) requiredItems.addUnique(item);
                            else requiredItems.addUnique(item);
                        }
                    }
                }

            }
            else if(tile.block instanceof LiquidTurret)
            {
                for(Liquid liquid:((LiquidTurret)tile.block).ammoTypes.keys())
                {
                    requiredItems.addUnique(liquid);
                }
            }
//            else if(tile.block instanceof Wall)
//            {
//                tile.block.alwaysReplace=true;
//            }
        }
        for(Content c: requiredItems)
        {
            part=new BaseRegistry.BasePart(schem);
            part.tier = schem.tiles.sumf(s -> Mathf.pow(s.block.buildCost / s.block.buildCostMultiplier, 1.4f));
            part.required=c;
            bases.reqParts.get(c,Seq::new).add(part);
        }
    }
    //private void addCorePart(int index,String schematic)
    private void addCorePart(Schematic schem)
    {
        //Schematic schem=Schematics.readBase64(schematic);
        BaseRegistry.BasePart part=new BaseRegistry.BasePart(schem);

        for(Schematic.Stile tile:schem.tiles)
        {
            if(tile.block instanceof CoreBlock)
            {
                part.core=tile.block;
                break;
            }
        }
        if(part.core==null) throw new IllegalArgumentException("There is no core in the schematic!");
        part.tier = schem.tiles.sumf(s -> Mathf.pow(s.block.buildCost / s.block.buildCostMultiplier, 1.4f));
        bases.cores.add(part);
        //else bases.cores.insert(index,part);
    }
    @Override
    public void postGenerate(Tiles tiles){
        if(sector.hasEnemyBase()){
            basegen.postGenerate();
        }
    }

    private Block nearLiquid(int x,int y,int r)
    {
        for(Block b:liquids)
        {
            if(near(x,y,r,b)) return b;
        }
        return null;
    }

}