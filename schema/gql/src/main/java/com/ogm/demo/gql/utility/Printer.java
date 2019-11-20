package com.ogm.demo.gql.utility;

import com.ogm.demo.gql.graph.GraphTraversal;
import com.ogm.demo.gql.graph.GraphTraversal2;
import com.ogm.demo.gql.node.AcordNodeType;
import org.apache.commons.collections4.MultiValuedMap;
import org.javatuples.Triplet;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Printer {

    GraphTraversal dict;
    MultiValuedMap<String, Triplet<String,String,String>> manyToOneLHS;
    MultiValuedMap<String, Triplet<String,String,String>> manyToOneRHS ;
    ArrayList<AcordNodeType> nodeTypes ;
    Map<String,Map<String,String>> propertyNamesAndDataTypes;
    MultiValuedMap<String, Triplet<String,String,String>> manyToManyLHS;
    MultiValuedMap<String, Triplet<String,String,String>> manyToManyRHS;
    MultiValuedMap<String, Triplet<String,String,String>> selfRefs;

    private static String out= "" ;

    public Printer()
    {

    }
    public  Printer(String schema)
    {
        dict = new GraphTraversal(schema);
        manyToOneLHS = dict.manyToOneLHS;
        manyToOneRHS =  dict.manyToOneRHS;
        nodeTypes = dict.nodeTypes;
        propertyNamesAndDataTypes = dict.propertyNamesAndDataTypes;
        manyToManyLHS= dict.manyToManyLHS;
        manyToManyRHS = dict.manyToManyRHS;
        selfRefs=dict.selfRefs;

    }
    public  Printer(String schema,Map<String, Object> config)
    {
        dict = new GraphTraversal(schema);
        manyToOneLHS = dict.manyToOneLHS;
        manyToOneRHS =  dict.manyToOneRHS;
        nodeTypes = dict.nodeTypes;
        propertyNamesAndDataTypes = dict.propertyNamesAndDataTypes;
        manyToManyLHS= dict.manyToManyLHS;
        manyToManyRHS = dict.manyToManyRHS;
        selfRefs=dict.selfRefs;

    }

    private void addStringLine(String s)
    {
        out+=s+System.lineSeparator();
    }
    public void writeFile() throws IOException
    {
        String fileName="schemaMany.txt";
        BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
        writer.write(out);

        writer.close();

        //second file with date
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        //Date date=new Date(System.currentTimeMillis());
        LocalDate localDate = LocalDate.now();

        String date = formatter.format(localDate);
        fileName="schema"+date+".txt";
        BufferedWriter writer1 = new BufferedWriter(new FileWriter(fileName));
        writer1.write(out);
        writer1.close();



    }

    public void printAppSyncGraphQL()
    {

        System.out.println ("Printing GraphQL.");

        nodeTypes.forEach(node->{
            Map<String,Integer> uniqueColumns = new HashMap<>();
            Map<String,Integer> manyToOneColumns = new HashMap<>();
            addStringLine("type "+node.getNamn().replaceAll("\\s+","") + "   @model");
            addStringLine("{");
            addStringLine("");


            propertyNamesAndDataTypes.get(node.getNamn()).forEach((k,v)->{

                if(v.equals("Time Period"))
                {
                    addStringLine(k.toString()
                            .substring(0,1)
                            .toLowerCase()+ k.toString()
                            .replaceAll("\\s+","")
                            .replaceAll("\\.+","")
                            .substring(1)+"Start "+": String");

                    addStringLine(k.toString()
                            .substring(0,1)
                            .toLowerCase()+ k.toString()
                            .replaceAll("\\s+","")
                            .replaceAll("\\.+","")
                            .substring(1)+"End "+": String");
                }
                else{
                    addStringLine(k.toString()
                            .substring(0,1)
                            .toLowerCase()+ k.toString()
                            .replaceAll("\\s+","")
                            .replaceAll("\\.+","")
                            .substring(1)+": String");
                }
            });


            if(manyToOneLHS.containsKey(node.getNamn()))
            {

                manyToOneLHS.get(node.getNamn()).forEach(v-> {
                    //TODO factor this out
                    String columnName = v.getValue1()
                            .substring(0, 1)
                            //.toLowerCase()
                            .toUpperCase()
                            + v.getValue1()
                            .replaceAll("\\s+", "")
                            .substring(1);

                    if(!uniqueColumns.keySet().contains(columnName))
                    {
                        uniqueColumns.put(columnName,1);

                    }else
                    {
                        Integer oldValue=uniqueColumns.get(columnName);

                        columnName+=oldValue+1;
                        uniqueColumns.replace(columnName,oldValue+1);
                    }
                    addStringLine(
                            columnName
                                    + ": [" + v.getValue0().replaceAll("\\s+", "") + "]  @connection(name:\""
                                    + node.getNamn().replaceAll("\\s+", "") + "_" + v.getValue1().replaceAll("\\s+", "")
                                    + v.getValue2().substring(0, 9) + "Connection\")");

                }  );


            }


            if(manyToOneRHS.containsKey(node.getNamn()))
            {

                manyToOneRHS.get(node.getNamn()).forEach(v-> {
                    //TODO factor this out
                    String columnName = v.getValue1()
                            .substring(0, 1)
                            //.toLowerCase()
                            .toUpperCase()
                            + v.getValue1()
                            .replaceAll("\\s+", "")
                            .substring(1);

                    if(!manyToOneColumns.keySet().contains(columnName))
                    {
                        manyToOneColumns.put(columnName,1);

                    }else
                    {
                        Integer oldValue=manyToOneColumns.get(columnName);

                        columnName+=oldValue+1;
                        manyToOneColumns.replace(columnName,oldValue+1);
                    }

                    addStringLine(columnName

                            + ": " + v.getValue0().replaceAll("\\s+", "") + " @connection(name:\""
                            + v.getValue0().replaceAll("\\s+", "") + "_"
                            + v.getValue1().replaceAll("\\s+", "")
                            + v.getValue2().substring(0, 9) + "Connection\")");
                });
            }


            addStringLine("}");
        });
    }

    public void printPrismaGraphQL()
    {
        System.out.println ("Printing Prisma GraphQL.");

        nodeTypes.forEach (node -> {

            addStringLine("type "+node.getNamn().replaceAll("\\s+","") + "  ");
            addStringLine("{");
            addStringLine("");


            propertyNamesAndDataTypes.get(node.getNamn())
                    .forEach((k,v)->{

                        if(v.equals("Time Period"))
                        {
                            addStringLine(k.toString()
                                    .substring(0,1)
                                    .toLowerCase()+ k.toString()
                                    .replaceAll("\\s+","")
                                    .replaceAll("\\.+","")
                                    .substring(1)+"Start"+": String");

                            addStringLine(k.toString()
                                    .substring(0,1)
                                    .toLowerCase()+ k.toString()
                                    .replaceAll("\\s+","")
                                    .replaceAll("\\.+","")
                                    .substring(1)+"End"+": String");
                        }
                        else{
                            addStringLine(k.toString()
                                    .substring(0,1)
                                    .toLowerCase()+ k.toString()
                                    .replaceAll("\\s+","")
                                    .replaceAll("\\.+","")
                                    .substring(1)+v);
                        }
                    });

            if(manyToOneLHS.containsKey(node.getNamn()))
            {
                manyToOneLHS.get(node.getNamn()).forEach(v-> {

                            String columnName = dict.getRelationColumnName(v);
                            String rhs = dict.getParentRhsString(v);
                            addStringLine(columnName+rhs);
                        }
                );
            }

            if(manyToOneRHS.containsKey(node.getNamn()))
            {
                //Integer count=  (int) (long) manyToOne.get(node.getNamn()).stream().count();
                manyToOneRHS.get(node.getNamn()).forEach(v-> {
                    String columnName = dict.getRelationColumnName(v);
                    String rhs = dict.getChildRhsString(v);
                    addStringLine(columnName+rhs);
                });
            }

            if(manyToManyLHS.containsKey(node.getNamn()))
            {
                manyToManyLHS.get(node.getNamn()).forEach(v->{
                    Triplet<String,String,String> t=v;
                    String columnName = dict.getRelationColumnName(v);
                    String rhs = dict.getMNRhsString(v);
                    addStringLine(columnName+rhs);
                });
            }

            if(selfRefs.containsKey(node.getNamn()))
            {
                selfRefs.get(node.getNamn()).forEach(v->{
                    Triplet<String,String,String> t=v;
                    String columnName = dict.getRelationColumnName(v);
                    String rhs = dict.getSelfLhsString(v);
                    addStringLine(columnName+rhs);
                });
            }

            addStringLine("}");
        });
    }


}
