package models;

public class RepatriationTypeModel {
    public String id;
    public String name;
    public String rowId;

    public RepatriationTypeModel(String id, String name, String rowId) {
        this.id = id;
        this.name = name;
        this.rowId = rowId;
    }

    @Override
    public String toString() {
        return name; // this will be shown in the Spinner
    }
}
