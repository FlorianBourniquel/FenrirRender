package fenrir.utils;

public class PairSizeRange {

    private int min;

    private int max;

    private double size;

    public PairSizeRange(int min, int max, double size) {
        this.min = min;
        this.max = max;
        this.size = size;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    public double getSize() {
        return size;
    }
}
