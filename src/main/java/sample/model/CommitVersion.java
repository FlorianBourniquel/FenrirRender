package sample.model;

import com.google.gson.annotations.SerializedName;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

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
}
