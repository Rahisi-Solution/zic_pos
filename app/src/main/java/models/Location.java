package models;

public class Location {
    private String id;
    private String name;
    private String row_id;
    private boolean selected;

    public Location(String id, String name, String row_id) {
        this.id = id;
        this.name = name;
        this.row_id = row_id;
        selected = false;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRow_id() {
        return row_id;
    }

    public void setRow_id(String row_id) {
        this.row_id = row_id;
    }

    public boolean isSelected() {
        return selected;
    }

    public String getLocation(){
        return name;
    }
}
