package sample.utils;

import sample.model.Location;

public interface MyPredicate {
    boolean testLocation(Location location1, Location location2);
}
