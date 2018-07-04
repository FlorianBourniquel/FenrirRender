package sample.model;

import com.google.gson.annotations.SerializedName;

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

    public CommitVersion(String name, String commit, long date, Map<String, List<AntiPatternInstance>> antiPatterns) {
        this.name = name;
        this.commit = commit;
        this.date = GregorianCalendar.getInstance();
        this.date.setTimeInMillis(date*1000);
        this.antiPatterns = antiPatterns;
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

    public Map<String, List<AntiPatternInstance>> getAntiPatterns() {
        return antiPatterns;
    }

    public Map<String, Map<String,Integer>> calculateOccurenceInSameClass(){
        List<String> alreadyProcessed = new LinkedList<>();
        Map<String, Map<String,Integer>> map = new HashMap<>();
        for (Map.Entry<String, List<AntiPatternInstance>> entry : antiPatterns.entrySet())
        {
            alreadyProcessed.add(entry.getKey());
            map.put(entry.getKey(),new HashMap<>());
            for (Map.Entry<String, List<AntiPatternInstance>> entryTmp : antiPatterns.entrySet()) {
                if (!alreadyProcessed.contains(entryTmp.getKey())){
                    for (AntiPatternInstance ap: entry.getValue()) {
                        Integer classOccurrence = 0;
                        for (AntiPatternInstance apSub: entryTmp.getValue()) {
                            if (ap.getLocation().isSameClass(apSub.getLocation()))
                                classOccurrence++;
                        }
                        map.get(entry.getKey()).put(entryTmp.getKey(),classOccurrence);
                    }
                }
            }
        }
        return map;
    }
}
