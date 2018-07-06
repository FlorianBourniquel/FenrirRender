package sample.model;

public class PairAPNameLocation {
    private String name;
    private  Location location;

    public PairAPNameLocation(String name, Location location) {
        this.name = name;
        this.location = location;
    }

    public String getName() {
        return name;
    }

    public Location getLocation() {
        return location;
    }
}
