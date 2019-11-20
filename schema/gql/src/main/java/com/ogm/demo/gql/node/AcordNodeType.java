package com.ogm.demo.gql.node;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.ArrayList;
import java.util.List;

/**
 * Neo4j node representing entity
 */
@NodeEntity
public class AcordNodeType {

    @Id
    @GeneratedValue
    private Long id;

    /**
     * Unique id
     */
    private String FBB;

    /**
     * Model name
     */
    private String ModelName;

    /**
     * Object name
     */
    private String Name;

    /**@Relationship(type = "HAS_PROPERTY")
    private List<> children = null;**/

    /**
     * Constructor
     */
    public AcordNodeType() {}

    public AcordNodeType(Long id, String EAID, String modelName, String name) {
        this.id = id;
        this.FBB = FBB;
        ModelName = modelName;
        Name = name;
    }

    public Long getId() {
        return id;
    }

    public String getFBB() {
        return FBB;
    }

    public String getModelNavn() {
        return ModelName;
    }

    public String getNamn() {
        return Name;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setFBB(String FBB) {
        this.FBB = FBB;
    }

    public void setModelNavn(String modelNavn) {
        ModelName = modelNavn;
    }

    public void setNamn(String namn) {
        Name = namn;
    }
}
