package com.ogm.demo.gql.graph;

import com.ogm.demo.gql.graph.Graph;
import com.ogm.demo.gql.node.AcordNodeType;
import com.ogm.demo.gql.utility.MultimapCollector;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.javatuples.Pair;
import org.javatuples.Triplet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GraphTraversal {

    public static MultiValuedMap<String, Triplet<String,String,String>> manyToOneLHS;
    public static MultiValuedMap<String, Triplet<String,String,String>> manyToManyLHS;
    public static MultiValuedMap<String, Triplet<String,String,String>> manyToManyRHS ;
    public static MultiValuedMap<String, Triplet<String,String,String>> selfRefs ;
    public static MultiValuedMap<String, Triplet<String,String,String>> manyToOneRHS ;
    public static MultiValuedMap<String,Pair<String,Integer>> reletionsMatrix;

    public static ArrayList<AcordNodeType> nodeTypes = new ArrayList<>();
    public static  Map<String,Map<String,String>> propertyNamesAndDataTypes = new HashMap<>();

    public GraphTraversal()
    {
      // new Graph();
    }

    public GraphTraversal(String schema)
    {
       // this(); Map<String, Object> config
        new Graph();
        manyToOneLHS= new HashSetValuedHashMap<>();
        manyToManyLHS = new HashSetValuedHashMap<>();
        manyToManyRHS = new HashSetValuedHashMap<>();
        manyToOneRHS =  new HashSetValuedHashMap<>();
        selfRefs= new HashSetValuedHashMap<>();
        nodeTypes = new ArrayList<>();
        selfRefs= new HashSetValuedHashMap<>();

        this.initDictionary(schema);
        this.createRelationshipsDictionary();

    }

    public GraphTraversal(String schema, Map<String, Object> config)
    {
        // this(); Map<String, Object> config
        new Graph(config);
       manyToOneLHS= new HashSetValuedHashMap<>();
        manyToManyLHS = new HashSetValuedHashMap<>();
        manyToManyRHS = new HashSetValuedHashMap<>();
        manyToOneRHS =  new HashSetValuedHashMap<>();
        selfRefs= new HashSetValuedHashMap<>();
        nodeTypes = new ArrayList<>();
        selfRefs= new HashSetValuedHashMap<>();
        reletionsMatrix= new HashSetValuedHashMap<>();


        this.initDictionary(schema);
        this.createRelationshipsDictionary();

    }

    private void createRelationshipsDictionary()
    {


        MultiValuedMap<String, Triplet<String,String,String>> allRelationships=getAllRelationships();
        MultiValuedMap<String, Triplet<String,String,String>> allRhsRelationships=Graph.getManyToOneTriplet3(getAllRelationships());
        allRelationships.putAll(allRhsRelationships);

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

    /**
     * Create prisma dictionary
     */
    private void prismaDict()
    {


        MultiValuedMap<String, Triplet<String,String,String>> tmpLHSRelationships = getAllRelationships();
        selfRefs = getAllSelfReferential();
        manyToManyLHS=getAllManyToMany();
       reletionsMatrix=getRelationshipsAdjacenyMatrix();



        /**
         * Collect all relationships - subtract many to many
         * Also subtract many to many from self referencing relationships.
         */

        if(!manyToManyLHS.isEmpty())

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
        //TODO perhapse change name of this functon
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

                ));
    }


    private MultiValuedMap<String,Triplet<String,String,String>> getAllRelationships()
    {
        MultiValuedMap<String, Triplet<String,String,String>> all = new HashSetValuedHashMap<>();
        Graph.queryRelationshipsNamesByCypher().forEach(rel->all
                .put(rel.get("one").toString()
                        ,Triplet.with(rel.get("many").toString()
                                ,rel.get("relationName").toString()
                                ,rel.get("one").toString()
                        )));

        return all;
    }
    private MultiValuedMap<String, Pair<String,Integer>> getRelationshipsAdjacenyMatrix()
    {
        MultiValuedMap<String,Pair<String,Integer>> mat = new HashSetValuedHashMap<>();
        Graph.queryRelationshipsAdjacencyMatrix().forEach(rel->mat
        .put(rel.get("lhs").toString()
                ,Pair.with(rel.get("rhs").toString()
                        ,Integer.valueOf(rel.get("overlap").toString())))
        );
        return mat;
    }

    private MultiValuedMap<String, Pair<String,String>> getRelationshipsNamesMatrix()
    {
        MultiValuedMap<String,Pair<String,String>> mat = new HashSetValuedHashMap<>();
        Graph.queryRelationshipNamesMatrix().forEach(rel->mat
                .put(rel.get("lhs").toString()
                        ,Pair.with(rel.get("rhs").toString()
                                ,rel.get("relation").toString()
                        ))
        );
        return mat;
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

    public int getRelationshipsCount(String parent, String child)
    {
        return reletionsMatrix.get(parent).stream().filter(e->e.getValue0()
                .equals(child))
                .findFirst()
                .get()
                .getValue1();
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

    public String getRelationColumnName(Triplet<String,String,String> v){

        if(getRelationshipsCount(v.getValue2(),v.getValue0())>1)
        {
            return getColumnNameAsPredicate(v);
        }
        else
        {
            return getColumnNameAsTable(v);

        }

    }

    public String getChildRhsString(Triplet<String,String,String> v)
    {
        String full =
                 v.getValue0().replaceAll("\\s+", "") + "_"
                        + v.getValue1().replaceAll("\\s+", "");
       String shrt="A"+String.valueOf(full.hashCode() & 0xfffffff);
        String rhs = ": " + v.getValue0().replaceAll("\\s+", "") + " @relation(name:\""

                + shrt
               // +full
                + "\")";
        return rhs;
    }
    public String getMNRhsString(Triplet<String,String,String> v)
    {
        String shrt = "A"+String.valueOf(v.getValue1().replaceAll("\\s+", "").hashCode() & 0xfffffff);
        String full = v.getValue1().replaceAll("\\s+", "");
        String rhs= ": [" + v.getValue0().replaceAll("\\s+", "") + "]  @relation(name:\""
                // + v.getValue2().replaceAll("\\s+", "")
                +"MN"
                + "_"
                + shrt
               // + full
                + "\")";
        return rhs;
    }
    public String getSelfLhsString(Triplet<String,String,String> v)
    {
        String shrt =  "A"+String.valueOf(v.getValue1().replaceAll("\\s+", "").hashCode() & 0xfffffff);
        String full = v.getValue1().replaceAll("\\s+", "");
        String rhs= ": [" + v.getValue0().replaceAll("\\s+", "") + "]  @relation(name:\""
                // + v.getValue2().replaceAll("\\s+", "")
                +"IM"
                + "_"
                + shrt
               // + full
                + "\")";
        return rhs;
    }
    //TODO add debug parameter and print full name of relationships.
    public String getParentRhsString(Triplet<String,String,String> v)
    {
        String full =  v.getValue2().replaceAll("\\s+", "")
                + "_"
                + v.getValue1().replaceAll("\\s+", "");
        String  shrt="A"+String.valueOf(full.hashCode() & 0xfffffff);
        String rhs= ": [" + v.getValue0().replaceAll("\\s+", "") + "]  @relation(name:\""
                +shrt
                //+full
                + "\")";
        return rhs;
    }



}
