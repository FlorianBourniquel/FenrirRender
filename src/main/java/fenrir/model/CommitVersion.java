package fenrir.model;

import com.google.gson.annotations.SerializedName;
import fenrir.utils.MyPredicate;

import java.util.*;

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

    private Map<String, List<AntiPatternInstance>> apByClasses = null;

    private Map<String, List<AntiPatternInstance>> apByPackages = null;

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

    public static Map<PairAPName, Map<String,List<PairAPDataLocation>>> calculateOccurrence(MyPredicate predicate, List<CommitVersion> commitVersionList){
        Map<PairAPName, Map<String,List<PairAPDataLocation>>> map = new HashMap<>();
        for (CommitVersion commitVersion:commitVersionList) {
            Map<PairAPName, List<String>> locationAlreadyProcessed = new HashMap<>();
            List<PairAPName> apAlreadyProcessed = new LinkedList<>();
            for (Map.Entry<String, List<AntiPatternInstance>> entry : commitVersion.getAntiPatterns().entrySet()) {
                for (Map.Entry<String, List<AntiPatternInstance>> entryTmp : commitVersion.getAntiPatterns().entrySet()) {
                    PairAPName pairAPName;
                    if (entry.getKey().compareToIgnoreCase(entryTmp.getKey()) < 0)
                        pairAPName = new PairAPName(entry.getKey(),entryTmp.getKey());
                    else
                        pairAPName = new PairAPName(entryTmp.getKey(),entry.getKey());
                    if (!apAlreadyProcessed.contains(pairAPName)){
                        locationAlreadyProcessed.put(pairAPName,new LinkedList<>());
                        if (!map.containsKey(pairAPName))
                            map.put(pairAPName,new HashMap<>());
                        myLabel: for (AntiPatternInstance ap: entry.getValue()) {
                            List<PairAPDataLocation> apNameLocations = new LinkedList<>();
                            for (AntiPatternInstance apSub: entryTmp.getValue()) {
                                if (locationAlreadyProcessed.get(pairAPName).contains(ap.getLocation().getClassLocation())) {
                                    if (!entry.getKey().equals(entryTmp.getKey()))
                                        map.get(pairAPName).get(ap.getLocation().getClassLocation()).add(new PairAPDataLocation(commitVersion.getName() + " - "  + entry.getKey(),ap.getLocation()));
                                    continue myLabel;
                                }
                                if (predicate.testLocation(ap.getLocation(),apSub.getLocation())) {
                                    apNameLocations.add(new PairAPDataLocation(commitVersion.getName() + " - " + entryTmp.getKey(), apSub.getLocation()));
                                }
                            }

                            if (!entry.getKey().equals(entryTmp.getKey())  && apNameLocations.size() > 0) {
                                apNameLocations.add(new PairAPDataLocation(commitVersion.getName() + " - " + entry.getKey(), ap.getLocation()));
                                locationAlreadyProcessed.get(pairAPName).add(ap.getLocation().getClassLocation());
                                if (map.get(pairAPName).containsKey(ap.getLocation().getClassLocation()))
                                    map.get(pairAPName).get(ap.getLocation().getClassLocation()).addAll(apNameLocations);
                                else
                                    map.get(pairAPName).put(ap.getLocation().getClassLocation(), apNameLocations);
                            } else if (apNameLocations.size() > 1) {
                                locationAlreadyProcessed.get(pairAPName).add(ap.getLocation().getClassLocation());
                                apNameLocations.add(new PairAPDataLocation(commitVersion.getName() + " - " + entry.getKey(), ap.getLocation()));
                                if (map.get(pairAPName).containsKey(ap.getLocation().getClassLocation()))
                                    map.get(pairAPName).get(ap.getLocation().getClassLocation()).addAll(apNameLocations);
                                else
                                    map.get(pairAPName).put(ap.getLocation().getClassLocation(), apNameLocations);
                            }
                        }
                    }
                    apAlreadyProcessed.add(pairAPName);
                }
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

    public Map<String, List<AntiPatternInstance>> getApByClasses() {
        if (apByClasses == null) {
            apByClasses = new HashMap<>();
            for (Map.Entry<String, List<AntiPatternInstance>> entry : antiPatterns.entrySet()) {
                for (AntiPatternInstance antiPatternInstance: entry.getValue()) {
                    antiPatternInstance.setApName(entry.getKey());
                    if (apByClasses.containsKey(antiPatternInstance.getLocation().getClassLocation()))
                        apByClasses.get(antiPatternInstance.getLocation().getClassLocation()).add(antiPatternInstance);
                    else {
                        List<AntiPatternInstance> list = new LinkedList<>();
                        list.add(antiPatternInstance);
                        apByClasses.put(antiPatternInstance.getLocation().getClassLocation(),list);
                    }
                }
            }
            for (Map.Entry<String, List<AntiPatternInstance>> entry: apByClasses.entrySet()) {
                entry.getValue().sort(Comparator.comparing(AntiPatternInstance::getApName));
            }
        }
        return apByClasses;
    }

    public Map<String, List<AntiPatternInstance>> getApByPackages() {
        if (apByPackages == null) {
            apByPackages = new HashMap<>();
            for (Map.Entry<String, List<AntiPatternInstance>> entry : antiPatterns.entrySet()) {
                for (AntiPatternInstance antiPatternInstance: entry.getValue()) {
                    antiPatternInstance.setApName(entry.getKey());
                    if (apByPackages.containsKey(antiPatternInstance.getLocation().getClassLocation().substring(0,antiPatternInstance.getLocation().getClassLocation().lastIndexOf("."))))
                        apByPackages.get(antiPatternInstance.getLocation().getClassLocation().substring(0,antiPatternInstance.getLocation().getClassLocation().lastIndexOf("."))).add(antiPatternInstance);
                    else {
                        List<AntiPatternInstance> list = new LinkedList<>();
                        list.add(antiPatternInstance);
                        apByPackages.put(antiPatternInstance.getLocation().getClassLocation().substring(0,antiPatternInstance.getLocation().getClassLocation().lastIndexOf(".")),list);
                    }
                }
            }
            for (Map.Entry<String, List<AntiPatternInstance>> entry: apByPackages.entrySet()) {
                entry.getValue().sort(Comparator.comparing(AntiPatternInstance::getApName));
            }
        }
        return apByPackages;
    }
}
