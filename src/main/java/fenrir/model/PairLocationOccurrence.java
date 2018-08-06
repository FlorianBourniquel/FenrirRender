package fenrir.model;

public class PairLocationOccurrence {
    private Location location;
    private int occurrence;

    public PairLocationOccurrence(Location location) {
        this.location = location;
        occurrence = 1;
    }

    public PairLocationOccurrence(Location location, int occurrence) {
        this.location = location;
        this.occurrence = occurrence;
    }

    public int getOccurrence() {
        return occurrence;
    }

    public Location getLocation() {
        return location;
    }

    public void addOne() {
        occurrence++;
    }




}
