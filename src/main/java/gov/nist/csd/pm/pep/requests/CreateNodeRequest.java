package gov.nist.csd.pm.pep.requests;

import java.util.HashMap;

public class CreateNodeRequest {
    private long                    parentID;
    private long                    id;
    private String                  name;
    private String                  type;
    private HashMap<String, String> properties;

    public long getParentID() {
        return parentID;
    }

    public void setParentID(long parentID) {
        this.parentID = parentID;
    }

    public long getID() {
        return id;
    }

    public void setID(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public HashMap<String, String> getProperties() {
        return properties;
    }

    public void setProperties(HashMap<String, String> properties) {
        this.properties = properties;
    }

    public CreateNodeRequest parentID(long parentID) {
        this.parentID = parentID;
        return this;
    }

    public CreateNodeRequest id(long id) {
        this.id = id;
        return this;
    }

    public CreateNodeRequest name(String name) {
        this.name = name;
        return this;
    }

    public CreateNodeRequest type(String type) {
        this.type = type;
        return this;
    }

    public CreateNodeRequest properties(HashMap<String, String> properties) {
        this.properties = properties;
        return this;
    }

    public CreateNodeRequest property(String key, String value) {
        if(this.properties == null) {
            this.properties = new HashMap<>();
        }
        this.properties.put(key, value);
        return this;
    }
}
