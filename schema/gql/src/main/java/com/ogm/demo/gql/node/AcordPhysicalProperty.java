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
public class AcordPhysicalProperty {

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



    /**
     * Acord data type
     */
    private String dataType;

    public AcordPhysicalProperty(){}

    public AcordPhysicalProperty(Long id, String EAID, String modelNavn, String namn) {
        this.id = id;
        this.FBB = FBB;
        ModelName = modelNavn;
        Name = namn;
    }

    public AcordPhysicalProperty(Long id, String EAID, String modelName, String namn, String dataType) {
        this.id = id;
        this.FBB = FBB;
        ModelName = modelName;
        Name = namn;
        this.dataType=dataType;
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

    public String getDataType() {
        return dataType;
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



    public void setDataType(String dataType) {
        this.dataType = dataType;
    }
}
