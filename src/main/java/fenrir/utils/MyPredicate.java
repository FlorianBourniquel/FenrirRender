package fenrir.utils;

import fenrir.model.Location;

public interface MyPredicate {
    boolean testLocation(Location location1, Location location2);
}
