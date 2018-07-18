package sample.utils;

public class SVGRange {

    private int min;
    private int max;
    private boolean isFire;
    private String path;

    public SVGRange(int min, int max, boolean isFire, String path) {
        this.min = min;
        this.max = max;
        this.isFire = isFire;
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

    public boolean isFire() {
        return isFire;
    }
}
