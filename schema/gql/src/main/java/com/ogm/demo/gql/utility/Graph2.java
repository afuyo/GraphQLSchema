package com.ogm.demo.gql.utility;

import com.ogm.demo.gql.node.AcordNodeType;
import com.ogm.demo.gql.node.AcordPhysicalProperty;
import org.apache.commons.collections4.MapIterator;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.javatuples.Triplet;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Graph2 {
    private final SessionFactory sessionFactory;
    static private final String SERVER_URI = "bolt://localhost:7687";
    static private final String SERVER_USERNAME = "neo4j";
    static private final String SERVER_PASSWORD = "wat#rMel0n";
    static Session session;
    //= sessionFactory.openSession();
    public Graph2()
    {

        Configuration configuration = new Configuration.Builder().uri(SERVER_URI).credentials(SERVER_USERNAME, SERVER_PASSWORD).build();
        sessionFactory = new SessionFactory(configuration, "com.ogm.demo.gql.node", "com.ogm.demo.gql.relationship");
        session = sessionFactory.openSession();
    }
    public static Iterable<AcordNodeType> queryNodesByCypher (Session session) {

        //  Create/load a map to hold the parameter
        Map<String, Object> params = new HashMap<>(1);

        String cypher = "MATCH (w:AcordNodeType) RETURN w";
        return session.query (AcordNodeType.class, cypher, params);
    }

    public static Iterable<AcordPhysicalProperty> queryPropertiesByCypher (String nodeName, Session session) {

        //  Create/load a map to hold the parameter
        Map<String, Object> params = new HashMap<>(1);
        params.put ("Name", nodeName);
        params.put("dataType",nodeName);

        //  Execute query and return the other side of the  relationship
        String cypher = "match(n:AcordNodeType{Name:$Name})-[:HAS_PROPERTY]->(m:AcordPhysicalProperty) return m;";
        return session.query (AcordPhysicalProperty.class, cypher, params);
    }
    public static Iterable<Map<String,Object>> queryRelationshipsByCypher(Session session) {


        String cypher = "match(n:AcordNodeType)-[r:HAS_RELATIONSHIP]->(rel:AcordPhysicalRelation)-[r2:HAS_RELATIONSHIP]->(m:AcordNodeType) where rel.cardinality=\"*\" return n.Name as one,m.Name as many;";
        return session.query(cypher, Collections.EMPTY_MAP);
    }

    public static Iterable<Map<String,Object>> queryRelationshipsNamesByCypher(Session session) {


        String cypher = "match(n:AcordNodeType)-[r:HAS_RELATIONSHIP]->(rel:AcordPhysicalRelation)-[r2:HAS_RELATIONSHIP]->(m:AcordNodeType) where rel.cardinality=\"*\" return n.Name as one, m.Name as many, rel.Name as relationName, rel.FBB as relationId ;";
        return session.query(cypher, Collections.EMPTY_MAP);
    }


    /**
     * Get other side of relationships
     * @param map conatining many to one relationships
     * @return one to many relationsips
     */
    public static MultiValuedMap getManyToOne (MultiValuedMap map)
    {
        MapIterator<String,String> it = map.mapIterator();
        MultiValuedMap map1 = new HashSetValuedHashMap();

        while(it.hasNext())
        {
            String one = it.next();
            String many = it.getValue();

            map1.put(many,one);


        }

        return map1;
    }

    /**
     * Get other side of relationships
     *
     * @param map of triples containing  many to one relationships with name and unique id
     * @return many  to one realtionsips
     */
    public static MultiValuedMap getManyToOneTriplet (MultiValuedMap map)
    {
        MapIterator<String, Triplet<String,String,String>> it = map.mapIterator();
        MultiValuedMap map1 = new HashSetValuedHashMap();
        MultiValuedMap<String,Triplet<String,String,String>> manyToOne = new HashSetValuedHashMap<>();


        while(it.hasNext())
        {
            String one = it.next();
            Triplet<String,String,String> many = it.getValue();
            String manyTableName = many.getValue0();

            map1.put(many,one);
            manyToOne.put(manyTableName,Triplet.with(one,many.getValue1(),many.getValue2()));


        }

        return map1;
    }
    public static MultiValuedMap<String,Triplet<String,String,String>> getManyToOneTriplet2 (MultiValuedMap map)
    {
        MapIterator<String,Triplet<String,String,String>> it = map.mapIterator();
        MultiValuedMap map1 = new HashSetValuedHashMap();
        MultiValuedMap<String,Triplet<String,String,String>> manyToOne = new HashSetValuedHashMap<>();

        //<String,Triplet<String,String,String>>
        while(it.hasNext())
        {
            String one = it.next();
            Triplet<String,String,String> many = it.getValue();
            String manyTableName = many.getValue0();

            map1.put(many,one);
            manyToOne.put(manyTableName,Triplet.with(one,many.getValue1(),many.getValue2()));


        }

        return manyToOne;
    }
    public static MultiValuedMap<String,Triplet<String,String,String>> getManyToOneTriplet3 (MultiValuedMap map)
    {
        MapIterator<String,Triplet<String,String,String>> it = map.mapIterator();
        MultiValuedMap map1 = new HashSetValuedHashMap();
        MultiValuedMap<String,Triplet<String,String,String>> manyToOne = new HashSetValuedHashMap<>();

        //<String,Triplet<String,String,String>>
        while(it.hasNext())
        {
            String one = it.next();
            Triplet<String,String,String> many = it.getValue();
            String manyTableName = many.getValue0();

            map1.put(many,one);
            manyToOne.put(manyTableName,Triplet.with(one,many.getValue1(),many.getValue0()));


        }

        return manyToOne;
    }

    //TODO Index id added manually this should be handled by amplify
     public static Map<String,String> getAppSyncPropNamesAndTypes (String nodeName, Session session){

     String defaultDataType = "String";
     Map<String,String> hs = new HashMap<>();
     queryPropertiesByCypher(nodeName,session)
     .forEach(prop->{
     hs.put(prop.getNamn(),prop.getDataType());
     });

     if (!nodeName.equals("Person") && (!nodeName.equals("Organization")) ) {



     hs.put("basicDataCompleteCode",defaultDataType);
     hs.put("typeName",defaultDataType);
     hs.put("creationDateTime",defaultDataType);
     hs.put("informationModelObjectKind",defaultDataType);
     hs.put("txGlobalTypeName",defaultDataType);
     hs.put("txTypeCode",defaultDataType);
     }
     if(nodeName.equals("Person"))
     {
     hs.put("txGlobalGenderCode",defaultDataType);
     }
     hs.put("id",defaultDataType);
     hs.put("txMetaSourceKey",defaultDataType);
     hs.put("txMetaStartEventTime",defaultDataType);
     hs.put("txMetaLoadTime",defaultDataType);
     hs.put("txMetaSource",defaultDataType);
     hs.put("txMetaCorrection",defaultDataType);
     hs.put("txMetaChange",defaultDataType);

     return hs;
     }

    public static Map<String,String> getPrismaPropNamesAndTypes (String nodeName, Session session){

        String defaultDataType = ": String";
        String defaultUniqueDataType = ": String @unique";
        String defaultIdDataType = ": ID! @id";
        Map<String,String> hs = new HashMap<>();
        queryPropertiesByCypher(nodeName,session)
                .forEach(prop->{
                    //TODO look into Acord data types mapping
                    // for now we use strings for all
                   // hs.put(prop.getNamn(),prop.getDataType());
                   String s =  prop.getDataType();
                   if(!s.equals("Time Period")) {
                       hs.put(prop.getNamn(), defaultDataType);
                   }
                   else {
                       hs.put(prop.getNamn(),prop.getDataType());
                   }
                });

        if (!nodeName.equals("Person") && (!nodeName.equals("Organization")) ) {



            hs.put("basicDataCompleteCode",defaultDataType);
            hs.put("typeName",defaultDataType);
            hs.put("creationDateTime",defaultDataType);
            hs.put("informationModelObjectKind",defaultDataType);
            hs.put("txGlobalTypeName",defaultDataType);
            hs.put("txTypeCode",defaultDataType);
        }
        if(nodeName.equals("Person"))
        {
            hs.put("txGlobalGenderCode",defaultDataType);
        }
        hs.put("id",defaultIdDataType);
        hs.put("txPrimaryKey",defaultUniqueDataType);
        hs.put("txMetaSourceKey",defaultDataType);
        hs.put("txMetaStartEventTime",defaultDataType);
        hs.put("txMetaLoadTime",defaultDataType);
        hs.put("txMetaSource",defaultDataType);
        hs.put("txMetaCorrection",defaultDataType);
        hs.put("txMetaChange",defaultDataType);

        return hs;
    }


}
