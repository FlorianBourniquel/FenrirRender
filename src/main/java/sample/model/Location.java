package sample.model;

import com.google.gson.annotations.SerializedName;

import java.util.Objects;

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

    public static boolean isSameClass(Location location, Location location2) {
        return location.getClassLocation().equals(location2.getClassLocation());
    }

    public static boolean isSameClassAndFunction(Location location, Location location2) {
        return isSameClass(location,location2) && location.getFunctionLocation().equals(location2.getFunctionLocation());
    }

    public static boolean isSame(Location location, Location location2) {
        return isSameClassAndFunction(location,location2) && location.getLineLocation().equals(location2.getLineLocation());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Location location = (Location) o;
        return Objects.equals(classLocation, location.classLocation) &&
                Objects.equals(functionLocation, location.functionLocation) &&
                Objects.equals(lineLocation, location.lineLocation);
    }

    @Override
    public int hashCode() {

        return Objects.hash(classLocation, functionLocation, lineLocation);
    }

    @Override
    public String toString() {
        String res = "Class: " + classLocation;
        if (!functionLocation.equals(""))
            res = res + " Function: " + functionLocation;
        if (!lineLocation.equals(""))
            res = res + " Line: " + lineLocation;
        return res;
    }
}
