package sample.model;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class AntiPatternInstance {

    @SerializedName("location")
    private Location location;

    @SerializedName("data")
    private Map<String,String> data;

    private String apName;

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

    public String getApName() {
        return apName;
    }

    public void setApName(String apName) {
        this.apName = apName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AntiPatternInstance that = (AntiPatternInstance) o;
        return Objects.equals(location, that.location) &&
                Objects.equals(data, that.data) &&
                Objects.equals(apName, that.apName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(location, data, apName);
    }
}
