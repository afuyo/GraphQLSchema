# Graph Data Modeling Example 

## Prerequsites
1. Neo4j Community Server Edition 3.4.x installed and started (https://neo4j.com/download-center/#community)
2. APOC-Awesome Procedures for Neo4j 3.4.x installed (https://github.com/neo4j-contrib/neo4j-apoc-procedures)
3. JDK 8 installed.


## Repository structure

### ACORD folder
`ACORD.xml` - ACORD Data Model 2.6

### Scripts 
`load_xml00.txt` - deletes any existing nodes and loads ACORD from xml.
The XML file contains a lot of information but we only want to extract business concepts.  In this example we only work with small subset and these nodes will be named POC1Type. 

`customStoredProcedures.txt` - creates necessary stored procedures

`logical01.txt` - creates logical model. Creating logical model consists of series of steps, depending on what we wish to include. 

`physical02.txt` -create physical model. We can now iter-
ate, create, and recreate physical model based on the logical model.

`transform03.txt`- applies model transformations(e.g. splitting, merging, versioning etc) aka roll up and roll down

### Schema/gql 
Java program using Object Graph Mapper library to manage domain objects with Neo4j. Output is annotated schema file used by Amplify/Prisma in order to generate GrapQL API. 

## Setup

In `<NEO4J_HOME>/conf/neo4j.conf` add following properties

```
dbms.security.procedures.unrestricted=apoc.*
apoc.export.file.enabled=true
apoc.import.file.enabled=true
dbms.directories.plugins=plugins
```

## Graph Data Modelling Steps

### Load ACORD data model into Neo4j. 
All you need to do to load XML files into the database is to call APOC procedure and provide url to the file `call apoc.xml.import('file:///tmp/ACORD.xml');`

Most convenient and performant way is to exectue these commands from cypher-shell as scripts. In this first step execute `load_xml00.txt`.


```
 ./cypher-shell -u neo4j -p <password> --format plain < /edh-model/scripts/load_xml00.txt
```

### Create stored procedures

```
 ./cypher-shell -u neo4j -p <password> --format plain < /edh-model/scripts/customStoredProcedures.txt
```

### Create logical layer. 
Now let's execute `logical01.txt`
```
 ./cypher-shell -u neo4j -p <password> --format plain < /edh-model/scripts/logical01.txt

```

### Create physicla layer
Execute `physical02.txt`
```
./cypher-shell -u neo4j -p <password> --format plain < /edh-model/scripts/physical02.txt
```

### Model transformations
Let's apply transformations to the physicla model. 
```
./cypher-shell -u neo4j -p <password> --format plain < /edh-model/scripts/transform03.txt
```
Data model can now be accessed through Neo4j browser. 

## Running the gqlschema 

gqlschema.jar accepts command line arguments:

```
    Option (* = required)  Description                          
    ---------------------  -----------                          
   
    * --config             Path to the config file

```
### Generate graphQL annotated schema
Run java program generating schema file.
```
java -jar gqlschema.jar --config conf/conf.yaml
```
### Program Structure
You can read more about [GQLSCHEMA here](/schema/gql/README.md)
* conf - Configuration
* images - Images
* src - Java code
  * graph -  Graph traversals and application logic
  * node - OGM classes
  * util - utility code

### Config 
Config contains following parameters


* server_uri - Uri of the Neo4j server.
* server_username - username
* server_password - password
* schema - type of schema to be generated. "PRISMA" and "APPSYNC" are the valid options.

### Example
Example of a conf.yaml file

```
server_uri: "bolt://localhost:7687"
server_username:  "neo4j"
server_password: "password123"
schema: "PRISMA"
```


