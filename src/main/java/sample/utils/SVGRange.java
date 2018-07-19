package sample.utils;

public class SVGRange {

    private int min;
    private int max;
    private int isFireHealOrNeutral;
    private String path;

    public SVGRange(int min, int max, int isFireHealOrNeutral, String path) {
        this.min = min;
        this.max = max;
        this.isFireHealOrNeutral = isFireHealOrNeutral;
        this.path = path;
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
}
