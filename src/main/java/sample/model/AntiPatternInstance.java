package sample.model;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Map;

public class AntiPatternInstance {

    @SerializedName("location")
    private Location location;

    @SerializedName("data")
    private Map<String,String> data;

    public AntiPatternInstance(Location location, Map<String, String> data) {
        this.location = location;
        this.data = data;
    }

    public Location getLocation() {
        return location;
    }

    public Map<String, String> getData() {
        return data;
    }

}
