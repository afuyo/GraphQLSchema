package com.ogm.demo.gql;



import com.ogm.demo.gql.utility.FileHelper;
import com.ogm.demo.gql.utility.Printer;

import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.session.SessionFactory;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import org.yaml.snakeyaml.Yaml;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

//import com.sun.deploy.net.HttpResponse;
//import sun.net.www.http.HttpClient;

/**
 * OGM Intro for loading data
 */
public class Loader {

    /**
     * Session factory for connecting to Neo4j database
     */
    private final SessionFactory sessionFactory;
    private static String out= "" ;
    //  Configuration info for connecting to the Neo4J database
    static private final String SERVER_URI = "bolt://localhost:7687";
    static private final String SERVER_USERNAME = "neo4j";
    static private final String SERVER_PASSWORD = "wat#rMel0n";


    /**
     * Constructor
     */
    public Loader() {
        //  Define session factory for connecting to Neo4j database
        Configuration configuration = new Configuration.Builder().uri(SERVER_URI).credentials(SERVER_USERNAME, SERVER_PASSWORD).build();
        sessionFactory = new SessionFactory(configuration, "com.ogm.demo.gql.node", "com.ogm.demo.gql.relationship");
    }


    /**
     * Main method for starting Java program
     * @param args command line arguments
     */

    public static void main (String[] args) throws Exception {
        String schema="";
        String SCHEMA = "schema";
        String CONFIG = "config";


        OptionParser parser = new OptionParser() {
            {
                accepts(CONFIG, "Path to the Southpaw config file").withRequiredArg().required();
                accepts(SCHEMA, "Defines schema to be generated.");

            }
        };
        OptionSet options = parser.parse(args);

        Yaml yaml = new Yaml();
        Map<String, Object> config = yaml.load(FileHelper.getInputStream(new URI(options.valueOf(CONFIG).toString())));
      //  Map<String, Object> config = yaml.load(FileHelper.getInputStream(new URI("conf/conf.yaml")));
        // schema  = (String) options.valueOf(SCHEMA);
         schema=config.get("schema").toString();

        System.out.println(schema);
        /**
        if(args.length>0)
        {
            schema=args[0];
        }
        else
        {
            schema="PRISMA";
        }**/



         if (schema.equals("PRISMA")) {
             Printer printer = new Printer(schema,config);
             printer.printPrismaGraphQL();
             printer.writeFile();
         } else if (schema.equals("APPSYNC")) {
             Printer printer = new Printer(schema,config);
             printer.printAppSyncGraphQL();
             printer.writeFile();
         } else {
             throw new IllegalArgumentException();

         }

    }
}
