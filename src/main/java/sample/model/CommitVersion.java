package sample.model;

import com.google.gson.annotations.SerializedName;
import com.sun.tools.javac.util.Pair;
import sample.utils.MyPredicate;

import java.util.*;
import java.util.function.Predicate;

public class CommitVersion {

    @SerializedName("name")
    private String name;

    @SerializedName("commit")
    private String commit;

    @SerializedName("date")
    private Calendar date;

    @SerializedName("antiPatterns")
    private Map<String, List<AntiPatternInstance>> antiPatterns;

    private Map<String, List<PairLocationOccurrence>> apLocationsAndOccurrences = new HashMap<>();

    public CommitVersion(String name, String commit, long date, Map<String, List<AntiPatternInstance>> antiPatterns) {
        this.name = name;
        this.commit = commit;
        this.date = GregorianCalendar.getInstance();
        this.date.setTimeInMillis(date*1000);
        this.antiPatterns = antiPatterns;
        initAntiPatternsLocationsAndOccurrences();
    }

    private void initAntiPatternsLocationsAndOccurrences() {
        for (Map.Entry<String, List<AntiPatternInstance>> entry : antiPatterns.entrySet()) {
            List<PairLocationOccurrence> pairLocationOccurrences = new LinkedList<>();
            myLabel: for (AntiPatternInstance antiPatternInstance: entry.getValue()) {
                for (PairLocationOccurrence pairLocationOccurrence: pairLocationOccurrences ) {
                    if (antiPatternInstance.getLocation() == pairLocationOccurrence.getLocation()) {
                        pairLocationOccurrence.addOne();
                        continue myLabel;
                    }
                }
                pairLocationOccurrences.add(new PairLocationOccurrence(antiPatternInstance.getLocation()));
            }
            apLocationsAndOccurrences.put(entry.getKey(),pairLocationOccurrences);
        }
    }

    public String getName() {
        return name;
    }

    public String getCommit() {
        return commit;
    }

    public Calendar getDate() {
        return date;
    }

    public Map<String, List<PairLocationOccurrence>> getApLocationsAndOccurrences() {
        return apLocationsAndOccurrences;
    }

    public Map<String, List<AntiPatternInstance>> getAntiPatterns() {
        return antiPatterns;
    }

    public Map<PairAPName, Map<String,List<PairAPNameLocation>>> calculateOccurrenceInSameClass(MyPredicate predicate){
        List<PairAPName> alreadyProcessed = new LinkedList<>();
        Map<PairAPName, Map<String,List<PairAPNameLocation>>> map = new HashMap<>();

        for (Map.Entry<String, List<AntiPatternInstance>> entry : antiPatterns.entrySet())
        {
            for (Map.Entry<String, List<AntiPatternInstance>> entryTmp : antiPatterns.entrySet()) {
                PairAPName pairAPName;
                if (entry.getKey().compareToIgnoreCase(entryTmp.getKey()) < 0)
                    pairAPName = new PairAPName(entry.getKey(),entryTmp.getKey());
                else
                    pairAPName = new PairAPName(entryTmp.getKey(),entry.getKey());
                if (!alreadyProcessed.contains(pairAPName)){
                    map.put(pairAPName,new HashMap<>());
                    myLabel: for (AntiPatternInstance ap: entry.getValue()) {
                        List<PairAPNameLocation> apNameLocations = new LinkedList<>();
                        for (AntiPatternInstance apSub: entryTmp.getValue()) {
                            if (map.get(pairAPName).containsKey(ap.getLocation().getClassLocation())) {
                                if (!entry.getKey().equals(entryTmp.getKey()))
                                    map.get(pairAPName).get(ap.getLocation().getClassLocation()).add(new PairAPNameLocation(entry.getKey(),ap.getLocation()));
                                continue myLabel;
                            }
                            if (predicate.testLocation(ap.getLocation(),apSub.getLocation())) {
                                apNameLocations.add(new PairAPNameLocation(entryTmp.getKey(), apSub.getLocation()));
                            }
                        }
                        if (!entry.getKey().equals(entryTmp.getKey()) && apNameLocations.size() > 0) {
                            apNameLocations.add(new PairAPNameLocation(entry.getKey(), ap.getLocation()));
                            map.get(pairAPName).put(ap.getLocation().getClassLocation(), apNameLocations);
                        } else if (apNameLocations.size() > 1) {
                            apNameLocations.add(new PairAPNameLocation(entry.getKey(), ap.getLocation()));
                            map.get(pairAPName).put(ap.getLocation().getClassLocation(), apNameLocations);
                        }
                    }
                }
                alreadyProcessed.add(pairAPName);
            }
        }
        return map;
    }

    private void findLocationAndAddOneOrCreateOne(List<PairLocationOccurrence> pairLocationOccurrences,Location location) {
        for (PairLocationOccurrence pairLocationOccurrence :pairLocationOccurrences) {
            if (pairLocationOccurrence.getLocation() == location){
                pairLocationOccurrence.addOne();
                return;
            }
        }
        pairLocationOccurrences.add(new PairLocationOccurrence(location));
    }

}
