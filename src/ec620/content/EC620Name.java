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
import mindustry.io.JsonIO;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

import static arc.math.Mathf.rand;

public class EC620Name
{
    //static HashMap<Character,HashMap<Character, List<Character>>> nameMap=new HashMap();


    public static String generate()
    {
        class CharMap extends ObjectMap<String, Seq<String>>
        {

        }

        var mod = Vars.mods.getMod(EC620JavaMod.class);
        var file = mod.root.child("NameMarkovChain.json");

        ObjectMap<String, CharMap> nameMap = new Json().readValue(ObjectMap.class, CharMap.class, new JsonReader().parse(file));

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
            List<String> name = new ArrayList<>();
            Seq<String> chars=nameMap.keys().toSeq();
            int c=chars.size-1;
            String f="-";
            String o;

            while(Objects.equals(f, "-"))
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
                if(!nameMap.get(f).containsKey(s)) break;
                c=nameMap.get(f).get(s).size-1;
                o=nameMap.get(f).get(s).get(rand.random(c));
                name.add(o);
                f=s;
                s=o;
            }
            return String.join("",name);
            //Log.info(String.join("",name));
        //}
    }
}
