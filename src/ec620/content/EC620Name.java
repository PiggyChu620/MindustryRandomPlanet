package ec620.content;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class EC620Name
{
    static HashMap<Character,HashMap<Character, List<Character>>> nameMap=new HashMap();


    public static void load()
    {
        nameMap.put('a',new HashMap<>());
            nameMap.get('a').put('a',new ArrayList<>());

    }
}
