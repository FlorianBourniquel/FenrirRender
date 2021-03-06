package fenrir.utils;

public class SVGRange {

    private int min;
    private int max;
    private int isFireHealOrNeutral;
    private String path;
    private boolean isClass;

    public SVGRange(int min, int max, int isFireHealOrNeutral, String path, boolean isClass) {
        this.min = min;
        this.max = max;
        this.isFireHealOrNeutral = isFireHealOrNeutral;
        this.path = path;
        this.isClass = isClass;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    public String getPath() {
        return path;
    }

    public int isFireHealOrNeutral() {
        return isFireHealOrNeutral;
    }

    public boolean isClass() {
        return isClass;
    }
}
