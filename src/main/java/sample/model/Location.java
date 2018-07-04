package sample.model;

import com.google.gson.annotations.SerializedName;

public class Location {

    @SerializedName("classLocation")
    private String classLocation;

    @SerializedName("functionLocation")
    private String functionLocation;

    @SerializedName("lineLocation")
    private String lineLocation;

    public Location(String classLocation, String functionLocation, String lineLocation) {
        this.classLocation = classLocation;
        this.functionLocation = functionLocation;
        this.lineLocation = lineLocation;
    }

    public String getClassLocation() {
        return classLocation;
    }

    public String getFunctionLocation() {
        return functionLocation;
    }

    public String getLineLocation() {
        return lineLocation;
    }

    public boolean isSameClass(Location location) {
        return location.getClassLocation().equals(classLocation);
    }
}
