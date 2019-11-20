package com.ogm.demo.gql.graph;

import com.ogm.demo.gql.node.AcordNodeType;
import com.ogm.demo.gql.utility.MultimapCollector;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.javatuples.Triplet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GraphTraversal2 {

    public static MultiValuedMap<String, Triplet<String,String,String>> manyToOneLHS;
    public static MultiValuedMap<String, Triplet<String,String,String>> manyToManyLHS;
    public static MultiValuedMap<String, Triplet<String,String,String>> manyToManyRHS ;
    public static MultiValuedMap<String, Triplet<String,String,String>> selfRefs ;
    public static MultiValuedMap<String, Triplet<String,String,String>> manyToOneRHS ;
    public static Map<String, Map<String,Integer>> parentrelationships ;
    public static Map<String,Map<String,Integer>> childrelationships ;

   public static ArrayList<AcordNodeType> nodeTypes = new ArrayList<>();
    // Map<String,String> propertyNames = new HashMap<>();
   public static  Map<String,Map<String,String>> propertyNamesAndDataTypes = new HashMap<>();

    public GraphTraversal2()
    {
        new Graph();
    }

    public GraphTraversal2(String schema)
    {
       this();

        manyToOneLHS= new HashSetValuedHashMap<>();
        manyToManyLHS = new HashSetValuedHashMap<>();
        manyToManyRHS = new HashSetValuedHashMap<>();
        manyToOneRHS =  new HashSetValuedHashMap<>();
        selfRefs= new HashSetValuedHashMap<>();
        parentrelationships = new HashMap<>();
        childrelationships = new HashMap<>();
        nodeTypes = new ArrayList<>();
        selfRefs= new HashSetValuedHashMap<>();

         this.initDictionary(schema);
         this.createRelationshipsDictionary();

    }

    private void createRelationshipsDictionary()
    {


        MultiValuedMap<String, Triplet<String,String,String>> allRelationships=getAllRelationships();
        MultiValuedMap<String, Triplet<String,String,String>> allRhsRelationships=Graph.getManyToOneTriplet3(getAllRelationships());
        allRelationships.putAll(allRhsRelationships);
        //TODO look over how relationships are counted, adjency matrix
        getAllRelationships().keySet().forEach(k->{
                    Map<String, Integer> accumulator = new HashMap<>();
                    getAllRelationships().get(k).forEach(v->
                            {
                                accumulator.merge(v.getValue0(),1,Math::addExact);

                            }
                    );
                    parentrelationships.put(k,accumulator);

                }
        );

        Graph.getManyToOneTriplet3(getAllRelationships()).keySet().forEach(k->{

                    Map<String, Integer> accumulator1 = new HashMap<>();
            Graph.getManyToOneTriplet3(getAllRelationships()).get(k).forEach(v->
                            {
                                accumulator1.merge(v.getValue0(),1,Math::addExact);
                            }
                    );
                    childrelationships.put(k,accumulator1);

                }
        );

    }

    public void initDictionary( String mode)
    {
       // Session session = sessionFactory.openSession();
        // session.purgeDatabase();
        if(mode.equals("PRISMA")) {
            prismaDict();
        } else
        {
            appSyncDict();
        }
    }

    private void prismaDict()
    {
        MultiValuedMap<String, Triplet<String,String,String>> tmpLHSRelationships = getAllRelationships();
        selfRefs = getAllSelfReferential();
        manyToManyLHS=getAllManyToMany();


       if(!manyToManyLHS.isEmpty())
           //retrieve all many to many and subtract from tmpLHSRelationships
           //tmpManyToManyLHS  for Address will also contain selfreferential e.g [Address,MunicipalityDelivery Address, Address] and [Place,MunicipalityDelivery Address,Address]
           // because it is how it is represented in the graph
           //manyToManyLHS for Address contains only [Place,MunicipalityDelivery Address,Address] it contains clean, printable many to many relatioships
           //so we want to remove tmpManyToManyLHS dirty many to many relationships

           //remove self-referencing relationships from tmpLHSRelationshps
           //remove many to many( from
           //remove many to many
       {

           MultiValuedMap<String, Triplet<String,String,String>>      tmpManyToManyLHS=
                   tmpLHSRelationships.entries().stream().filter(r->
                           manyToManyLHS.values().stream().anyMatch(e -> r.getValue().getValue1().equals(e.getValue1())))
                           .collect(MultimapCollector.toMultimap(Map.Entry::getKey,Map.Entry::getValue));

          tmpLHSRelationships.values().removeAll(tmpManyToManyLHS.values());
           tmpLHSRelationships.values().removeAll(selfRefs.values());
           selfRefs.values().removeAll(tmpManyToManyLHS.values());


       }

        manyToOneLHS=tmpLHSRelationships;
        //reverse many to one
        //TODO perhapse change name of this function
        manyToOneRHS =  Graph.getManyToOneTriplet3(manyToOneLHS);
        //reverse many to many
        manyToManyRHS= Graph.getManyToOneTriplet3(manyToManyLHS);
        //returns NodeTypes in alphabetic order
        nodeTypes= (ArrayList) Graph.queryNodesByCypher();

        nodeTypes.forEach(node->
                propertyNamesAndDataTypes.put(node.getNamn(), Graph.getPrismaPropNamesAndTypes(node.getNamn())));
    }

    private void appSyncDict()
    {
        Graph.queryRelationshipsNamesByCypher().forEach(rel->manyToOneLHS
                .put(rel.get("one").toString()
                        ,Triplet.with(rel.get("many").toString()
                                ,rel.get("relationName").toString()
                                ,rel.get("relationId").toString() )));

        manyToOneRHS =  Graph.getManyToOneTriplet2(manyToOneLHS);
        nodeTypes= (ArrayList) Graph.queryNodesByCypher();

        nodeTypes.forEach(node->
               // Graph.getAppSyncPropNamesAndTypes(node.getNamn()).containsKey("txPrimaryKey"));
                propertyNamesAndDataTypes.put(node.getNamn()
                        , Graph.getAppSyncPropNamesAndTypes(node.getNamn())
                       /** .entrySet()
                        .stream()
                        .filter(e->e.getKey()!="txPrimaryKey")
                        .collect(Collectors.toMap(Map.Entry::getKey
                                ,Map.Entry::getValue))**/

                ));
    }


    private MultiValuedMap<String,Triplet<String,String,String>> getAllRelationships()
    {
        MultiValuedMap<String, Triplet<String,String,String>> all = new HashSetValuedHashMap<>();
        Graph.queryRelationshipsNamesByCypher().forEach(rel->all
                .put(rel.get("one").toString()
                        ,Triplet.with(rel.get("many").toString()
                                ,rel.get("relationName").toString()
                                //,rel.get("relationId").toString()
                                ,rel.get("one").toString()
                        )));

        return all;
    }

    private MultiValuedMap<String,Triplet<String,String,String>> getAllSelfReferential()
    {
        MultiValuedMap<String, Triplet<String,String,String>> selfrefs = new HashSetValuedHashMap<>();
        Graph.queryAllSelfReferentialRelationNamesByCypher().forEach(rel->selfrefs
                .put(rel.get("manyLhs").toString()
                        ,Triplet.with(rel.get("manyRhs").toString()
                                ,rel.get("relationName").toString()
                                ,rel.get("manyLhs").toString()))
        );

        return selfrefs;
    }

    private MultiValuedMap<String,Triplet<String,String,String>> getAllManyToMany()
    {
        MultiValuedMap<String, Triplet<String,String,String>> m2m = new HashSetValuedHashMap<>();
        Graph.queryManyToManyRelationNamesByCypher().forEach(mm->m2m
                .put(mm.get("manyLhs").toString()
                        ,Triplet.with(mm.get("manyRhs").toString()
                                ,mm.get("relationName").toString()
                                ,mm.get("manyLhs").toString()))
        );
        return m2m;
    }
    public int getCountParentRelationships(String parent, String child)
    {
        return parentrelationships.get(parent).get(child);
    }
    public int getCountChildRelationships(String parent, String child)
    {
        return  childrelationships.get(parent).get(child);
    }
    public String getRelationColumnName(Triplet<String,String,String> v){

        String columnName = v.getValue1()
                .substring(0, 1)
                //.toLowerCase()
                .toUpperCase()
                + v.getValue1()
                .replaceAll("\\s+", "")
                .substring(1);
        return columnName;
    }
    public String getColumnNameAsPredicate(Triplet<String,String,String> v)
    {
        String
                columnName = v.getValue1()
                .substring(0, 1)
                //.toLowerCase()
                .toUpperCase()
                + v.getValue1()
                .replaceAll("\\s+", "")
                .substring(1);
        return columnName;
    }
    public String getColumnNameAsTable(Triplet<String,String,String> v){
        String  columnName = v.getValue0()
                .substring(0, 1)
                //.toLowerCase()
                .toUpperCase()
                + v.getValue0()
                .replaceAll("\\s+", "")
                .substring(1);
        return columnName;
    }

    public String getParentRelationColumnName(Triplet<String,String,String> v){

        if(getCountParentRelationships(v.getValue2(),v.getValue0())>1)
        {
            return getColumnNameAsPredicate(v);
        }
        else
        {
            return getColumnNameAsTable(v);

        }

    }

    public String getChildRelationColumnName(Triplet<String,String,String> v){
        if(getCountChildRelationships(v.getValue2(),v.getValue0())>1)
        {
            return getColumnNameAsPredicate(v);
        }
        else
        {
            return getColumnNameAsTable(v);

        }

    }

    /**
     * Return column name by relation.
     * Used when more than one relation exists.
     * @param v
     * @return
     */
    public String getParentRelationColumnNameTabByPred(Triplet<String,String,String> v)
    {
        // return getColumnNameAsPredicate(v);
        // return getColumnNameAsTable(v)+"By"+getColumnNameAsPredicate(v);
        if(getCountParentRelationships(v.getValue2(),v.getValue0())>1)
        {
            return getColumnNameAsTable(v)+"By"+getColumnNameAsPredicate(v);
        }
        else
        {
            return getColumnNameAsTable(v);

        }
    } /**
     * Return column name by relation.
     * Used when more than one relation exists.
     * @param v
     * @return
     */
    public String getChildRelationColumnNameTabByPred(Triplet<String,String,String> v)
    {
        if(getCountChildRelationships(v.getValue2(),v.getValue0())>1)
        {
            return getColumnNameAsTable(v)+"By"+ getColumnNameAsPredicate(v);
        }
        else
        {
            return getColumnNameAsTable(v);

        }
    }

    /**
     * Depracated.
     * @param v
     * @return
     */

    public String getSingleRelationColumnName(Triplet<String,String,String> v){

        String columnName = v.getValue0()
                .substring(0, 1)
                //.toLowerCase()
                .toUpperCase()
                + v.getValue0()
                .replaceAll("\\s+", "")
                .substring(1);
        return columnName;
    }
    public String getChildRhsString(Triplet<String,String,String> v)
    {
        String rhs = ": " + v.getValue0().replaceAll("\\s+", "") + " @relation(name:\""
                + v.getValue0().replaceAll("\\s+", "") + "_"
                + v.getValue1().replaceAll("\\s+", "")
                //+ v.getValue2().substring(0, 9)
                + "\")";
        return rhs;
    }
    public String getMNRhsString(Triplet<String,String,String> v)
    {
        String rhs= ": [" + v.getValue0().replaceAll("\\s+", "") + "]  @relation(name:\""
               // + v.getValue2().replaceAll("\\s+", "")
                +"M:N"
                + "_"
                + v.getValue1().replaceAll("\\s+", "")
                + "\")";
        return rhs;
    }
    public String getSelfLhsString(Triplet<String,String,String> v)
    {
        String rhs= ": [" + v.getValue0().replaceAll("\\s+", "") + "]  @relation(name:\""
                // + v.getValue2().replaceAll("\\s+", "")
                +"1:M"
                + "_"
                + v.getValue1().replaceAll("\\s+", "")
                + "\")";
        return rhs;
    }
    public String getParentRhsString(Triplet<String,String,String> v)
    {
        String rhs= ": [" + v.getValue0().replaceAll("\\s+", "") + "]  @relation(name:\""
                + v.getValue2().replaceAll("\\s+", "")
                + "_"
                + v.getValue1().replaceAll("\\s+", "")
                + "\")";
        return rhs;
    }




}
