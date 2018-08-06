package fenrir.model;

public class PairAPDataLocation {
    private String name;
    private  Location location;

    public PairAPDataLocation(String name, Location location) {
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
