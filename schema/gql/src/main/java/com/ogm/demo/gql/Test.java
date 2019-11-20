package com.ogm.demo.gql;

import com.google.common.collect.Multimap;
import com.ogm.demo.gql.test.MultimapCollector;
import com.ogm.demo.gql.utility.MultimapCollector2;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.javatuples.Triplet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class Test {
  // public static Multimap<String, Triplet<String,String,String>> relationshipNames;
    public static MultiValuedMap<String, Triplet<String,String,String>> relationshipNames;
   public static MultiValuedMap<String, Triplet<String,String,String>> manyToManyLHS;
   // public static MultiValuedMap<String, Triplet<String,String,String>> manyToManyRHS ;
    public static MultiValuedMap<String, Triplet<String,String,String>> manyToOne ;
    public static Map<String, Map<String,Integer>> parentrelationships ;
    public static Map<String,Map<String,Integer>> childrelationships ;
    public static void main(String[] args) throws IOException
    {
           // relationshipNames= HashMultimap.create();
        relationshipNames= new HashSetValuedHashMap<>();
        MultiValuedMap<String, Triplet<String,String,String>>    manyToManyRHS = new HashSetValuedHashMap<>();
        manyToManyLHS = new HashSetValuedHashMap<>();
        manyToOne =  new HashSetValuedHashMap<>();
        parentrelationships = new HashMap<>();
        childrelationships = new HashMap<>();


        System.out.println("Hello");
        Map<String,String> map = new HashMap<>();
        map.put("one","one");
        map.put("two","two");
        map.put("three","three");

      //Map<String,String> map2= Stream.of(!map.containsKey("one")).map((k,v)->);
     Map<String,String> map2=   map.entrySet()
             .stream()
             .filter(x->x.getKey()=="one" || x.getKey()=="two")
             .collect(Collectors.toMap(Map.Entry::getKey
                ,Map.Entry::getValue));

        Multimap<String,Triplet<String,String,String>> relationshipNames2=
                relationshipNames.entries().stream().filter(r ->
                        r.getKey()=="one").collect((MultimapCollector.toMultimap(Map.Entry::getKey,Map.Entry::getValue)));

        MultiValuedMap<String,Triplet<String,String,String>> relationshipNames3=
                relationshipNames.entries().stream().filter(r->
                       // !manyToManyLHS.values().stream().anyMatch(e->e.getValue1()==r.getValue().getValue1()))
                        r.getValue().getValue1()!=manyToManyLHS.values().stream().findFirst().get().getValue1())
                        .collect(MultimapCollector2.toMultimap(Map.Entry::getKey,Map.Entry::getValue));

     System.out.println(map2);


    }
}
