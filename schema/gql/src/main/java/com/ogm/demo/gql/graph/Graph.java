package com.ogm.demo.gql.graph;

import com.ogm.demo.gql.node.AcordPhysicalProperty;
import com.ogm.demo.gql.node.AcordNodeType;
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

public class Graph {

    static Session session;
    //= sessionFactory.openSession();
    public Graph()
    {
        final SessionFactory sessionFactory;
        final String SERVER_URI = "bolt://localhost:7687";
        final String SERVER_USERNAME = "neo4j";
        final String SERVER_PASSWORD = "wat#rMel0n";

        Configuration configuration = new Configuration.Builder().uri(SERVER_URI).credentials(SERVER_USERNAME, SERVER_PASSWORD).build();
        sessionFactory = new SessionFactory(configuration, "com.ogm.demo.gql.node", "com.ogm.demo.gql.relationship");
        session = sessionFactory.openSession();
    }

    public Graph(Map<String,Object> config)
    {
        final SessionFactory sessionFactory;
        final String SERVER_URI = config.get("server_uri").toString();
        final String SERVER_USERNAME = config.get("server_username").toString();
        final String SERVER_PASSWORD = config.get("server_password").toString();

        Configuration configuration = new Configuration.Builder().uri(SERVER_URI).credentials(SERVER_USERNAME, SERVER_PASSWORD).build();
        sessionFactory = new SessionFactory(configuration, "com.ogm.demo.gql.node", "com.ogm.demo.gql.relationship");
        session = sessionFactory.openSession();
    }
    public static Iterable<AcordNodeType> queryNodesByCypher () {

        //  Create/load a map to hold the parameter
        Map<String, Object> params = new HashMap<>(1);

        String cypher = "MATCH (w:AcordNodeType) RETURN w order by w.Name";
        return session.query (AcordNodeType.class, cypher, params);
    }

    public static Iterable<AcordPhysicalProperty> queryPropertiesByCypher (String nodeName) {

        //  Create/load a map to hold the parameter
        Map<String, Object> params = new HashMap<>(1);
        params.put ("Name", nodeName);
        params.put("dataType",nodeName);

        //  Execute query and return the other side of the  relationship
        String cypher = "match(n:AcordNodeType{Name:$Name})-[:HAS_PROPERTY]->(m:AcordPhysicalProperty) return m;";
        return session.query (AcordPhysicalProperty.class, cypher, params);
    }
    public static Iterable<Map<String,Object>> queryRelationshipsByCypher() {


        String cypher = "match(n:AcordNodeType)-[r:HAS_RELATIONSHIP]->(rel:AcordPhysicalRelation)-[r2:HAS_RELATIONSHIP]->(m:AcordNodeType) where rel.cardinality=\"*\" return n.Name as one,m.Name as many;";
        return session.query(cypher, Collections.EMPTY_MAP);
    }

    public static Iterable<Map<String,Object>> queryRelationshipsNamesByCypher() {


        String cypher = "match(n:AcordNodeType)-[r:HAS_RELATIONSHIP]->(rel:AcordPhysicalRelation)-[r2:HAS_RELATIONSHIP]->(m:AcordNodeType) where rel.cardinality=\"*\" return n.Name as one, m.Name as many, rel.Name as relationName, rel.FBB as relationId ;";
        return session.query(cypher, Collections.EMPTY_MAP);
    }
    public static Iterable<Map<String,Object>> queryManyToManyRelationNamesByCypher()
    {
        // only many many are returned including many to many self referential

        //String cypher = "match(n:AcordNodeType)-[r:HAS_RELATIONSHIP]->(rel:AcordPhysicalRelation)-[r2:HAS_RELATIONSHIP]->(m:AcordNodeType)-[r3:HAS_RELATIONSHIP]->(rel2:AcordPhysicalRelation) where rel.association=rel2.association return n.Name as manyLhs,rel.Name as relationName,m.Name as manyRhs;";
        String cypher = "match(n:AcordNodeType)-[r:HAS_RELATIONSHIP]->(rel:AcordPhysicalRelation)-[r2:HAS_RELATIONSHIP]->(" +
                "m:AcordNodeType)-[r3:HAS_RELATIONSHIP]->(rel2:AcordPhysicalRelation) -[r4:HAS_RELATIONSHIP]->(o:AcordNodeType) " +
                "where rel.association=rel2.association and n.Name=o.Name return n.Name as manyLhs,rel.Name as relationName,m.Name as manyRhs;";

        //String cypher = "match(n:AcordNodeType)-[r:HAS_RELATIONSHIP]->(rel:AcordPhysicalRelation)-[r2:HAS_RELATIONSHIP]->(m:AcordNodeType) where n.Name=m.Name return n.Name as manyLhs,rel.Name as relationName,m.Name as manyRhs; ";
        return session.query(cypher, Collections.EMPTY_MAP);
    }

    /**
     * Cartesian product of AcordNodeTypes and how many relationships there are between the nodes
     * returns adjacency matrix
     * @return
     */
    public static Iterable<Map<String,Object>>queryRelationshipsAdjacencyMatrix()
    {
       // String cypher = "MATCH (g1:AcordNodeType), (g2:AcordNodeType) OPTIONAL MATCH path = (g1)-[:HAS_RELATIONSHIP]-()-[:HAS_RELATIONSHIP]-(g2)WITH g1, g2, CASE WHEN path is null THEN 0 ELSE COUNT(path) END AS overlap RETURN g1.Name as lhs, g2.Name rhs, overlap ORDER BY g1.Name, g2.Name;";
        String cypher="MATCH (g1:AcordNodeType), (g2:AcordNodeType)\n"+
        "OPTIONAL MATCH path = (g1)-[:HAS_RELATIONSHIP]-(r)-[:HAS_RELATIONSHIP]-(g2)\n"+
                "WITH g1, g2, CASE WHEN path is null THEN 0 ELSE COUNT(distinct r) END AS overlap\n"+
        "RETURN g1.Name as lhs, g2.Name as rhs, overlap\n"+
        "ORDER BY g1.Name, g2.Name\n";

        return session.query(cypher, Collections.EMPTY_MAP);
    }

    public static Iterable<Map<String,Object>>queryRelationshipNamesMatrix()
    {
        // String cypher = "MATCH (g1:AcordNodeType), (g2:AcordNodeType) OPTIONAL MATCH path = (g1)-[:HAS_RELATIONSHIP]-()-[:HAS_RELATIONSHIP]-(g2)WITH g1, g2, CASE WHEN path is null THEN 0 ELSE COUNT(path) END AS overlap RETURN g1.Name as lhs, g2.Name rhs, overlap ORDER BY g1.Name, g2.Name;";
        String cypher="MATCH (g1:AcordNodeType), (g2:AcordNodeType)\n"+
                "OPTIONAL MATCH path = (g1)-[:HAS_RELATIONSHIP]-(r)-[:HAS_RELATIONSHIP]-(g2)\n"+
                "WITH g1, g2, CASE WHEN path is null THEN 'NULL' ELSE r.Name END AS relation\n"+
                "RETURN g1.Name as lhs, g2.Name as rhs, relation\n"+
                "ORDER BY g1.Name, g2.Name\n";

        return session.query(cypher, Collections.EMPTY_MAP);
    }

    public static Iterable<Map<String,Object>> queryAllSelfReferentialRelationNamesByCypher()
    {

        //It will return all self referential and all many to many relationships
        //In order to only keep self referential(one to many) we need subtract many to many


        String cypher = "match(n:AcordNodeType)-[r:HAS_RELATIONSHIP]->(rel:AcordPhysicalRelation)-[r2:HAS_RELATIONSHIP]->(m:AcordNodeType) where n.Name=m.Name return n.Name as manyLhs,rel.Name as relationName,m.Name as manyRhs; ";
        return session.query(cypher,Collections.EMPTY_MAP);

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
    //AppSync
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
    //PRISMA
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

     public static Map<String,String> getAppSyncPropNamesAndTypes (String nodeName){

     String defaultDataType = "String";
     Map<String,String> hs = new HashMap<>();
     queryPropertiesByCypher(nodeName)
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

    public static Map<String,String> getPrismaPropNamesAndTypes (String nodeName){

        String defaultDataType = ": String";
        String defaultUniqueDataType = ": String @unique";
        String defaultIdDataType = ": ID! @id";
        Map<String,String> hs = new HashMap<>();
        queryPropertiesByCypher(nodeName)
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
  /**
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
**/
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
