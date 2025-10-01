package models;

public class ComplainModel {
    public String id;
    public String name;
    public String rowId;

    public ComplainModel(String id, String name, String rowId) {
        this.id = id;
        this.name = name;
        this.rowId = rowId;
    }

    @Override
    public String toString() {
        return name; // this will be shown in the Spinner
    }
}