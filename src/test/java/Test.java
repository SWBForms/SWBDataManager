
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author javiersolis
 */
public class Test {
    public static void main2(String[] args) {
        System.out.println(String.format("%04d", 30));
    }
    
    public static void main(String[] args) {
        LinkedHashMap<String,Integer> map=new LinkedHashMap<String,Integer>();
        map.put("Javier", 43);
        map.put("Karen", 27);
        map.put("Ivonne", 41);
        map.put("Michelle", 15);
        map.put("Alexander", 13);
        map.put("Adrian", 41);
        map.put("Alfredo", 46);
        map.put("Juan", 50);
        
        map.entrySet().stream().sorted((Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) -> {
            System.out.println("compare:"+o1+" "+o2);
            return o1.getValue().compareTo(o2.getValue());
        }).forEach((Map.Entry<String, Integer> t) -> {
            map.remove(t.getKey());
            map.put(t.getKey(), t.getValue());
        });
        
        
        Iterator<Map.Entry<String,Integer>> it=map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Integer> next = it.next();
            System.out.println(next.getKey()+" "+next.getValue());
        }
    }
}
