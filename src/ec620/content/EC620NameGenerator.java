package ec620.content;

import arc.Core;
import arc.files.Fi;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.serialization.Json;
import arc.util.serialization.JsonReader;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import ec620.EC620JavaMod;
import mindustry.Vars;
import mindustry.game.Schematic;
import mindustry.game.Schematics;
import mindustry.game.SectorInfo;
import mindustry.io.JsonIO;
import mindustry.mod.Mods;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

import static arc.math.Mathf.absin;
import static arc.math.Mathf.rand;
class CharMap extends ObjectMap<String, Seq<String>>
{
}
public class EC620NameGenerator
{
    //static HashMap<Character,HashMap<Character, List<Character>>> nameMap=new HashMap();
    static ObjectMap<String, CharMap> nameMap;
    //static EC620Classes.NameHandler generatedNames=null;
    static Json json=new Json();
    static Fi file;
//    public EC620NameGenerator() throws IOException {
//
//        /*file=mod.root.child("GeneratedNames.json");
//        if(file.exists())
//        {
//            generatedNames=new Json().readValue(EC620Classes.NameHandler.class, (new JsonReader().parse(file)));
//            //Log.info("File existed");
//        }
//        else
//        {
//            generatedNames=new EC620Classes.NameHandler();
//            //if(file.exists()) Log.info("File finally existed");
//            //else Log.info("File not existed");
//        }*/
//    }
    public static void load()
    {
        Mods.LoadedMod mod = Vars.mods.getMod(EC620JavaMod.class);

        file = mod.root.child("NameMarkovChain.json");
        nameMap = json.readValue(ObjectMap.class, CharMap.class, new JsonReader().parse(file));
//        if(Core.settings.has("ec620.sectorInfos"))
//        {
//            EC620Vars.SectorInfos= Core.settings.getJson("ec620.sectorInfos", ObjectMap.class,EC620Classes.SectorData.class,ObjectMap::new);
////            Log.info("Sector Infos Loaded");
////            Log.info(EC620Vars.SectorInfos.toString("\n"));
//        }
    }
    public static String generate(int index,boolean reset)
    {
        //Gson gson = new Gson();

        // Define the type of the map using TypeToken
        //Type type = new TypeToken<HashMap<Character, HashMap<Character, List<Character>>>>() {}.getType();

        // Read the JSON file into a String
        //Fi file = Core.files.internal("NameMarkovChain.json");
//        FileReader reader = new FileReader(file.read());
//        char[] buffer = new char[(int) file.length()];
//        reader.read(buffer);
        //String json = new String(file.readString());
        //reader.close();

        //nameMap = gson.fromJson(file.readString(), type);

        //for(int i=0;i<10;i++)
        //{

        //Log.info(String.join("",name));
        //}
        String sectorName="ec620.sectorNames."+index;
        if(reset || !Core.settings.has(sectorName))
        {
            String name;
            if(index<0)
            {
                name = generateName();
                Core.settings.put(sectorName,name);
                return name;
            }
            else
            {
                name = generateName()+"-"+index;
                Core.settings.put(sectorName,name);
                Core.settings.put("ec620.sectorThreats."+index,index>0?rand.nextFloat():0);
                return name;
            }
        }
        else return Core.settings.getString(sectorName);
    }
    private static String generateName()
    {
        List<String> name = new ArrayList<>();
        Seq<String> chars=nameMap.keys().toSeq();
        int c=chars.size-1;
        String f="-";
        String o;

        while(Objects.equals(f, "-") || Objects.equals(f,"'"))
        {
            f=chars.get(rand.random(c));
        }
        name.add(f.toUpperCase());
        chars=nameMap.get(f).keys().toSeq();
        c=chars.size-1;
        String s=chars.get(rand.random(c));
        name.add(s);

        for(int j=0;j<rand.random(10);j++)
        {
            if(!nameMap.containsKey(f) || !nameMap.get(f).containsKey(s)) break;
            c=nameMap.get(f).get(s).size-1;
            o=nameMap.get(f).get(s).get(rand.random(c));
            name.add(o);
            f=s;
            s=o;
        }
        return String.join("",name);
    }
}