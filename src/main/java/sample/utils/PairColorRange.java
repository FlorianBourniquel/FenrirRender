package sample.utils;

public class PairColorRange {

    private int min;

    private int max;

    private String color;

    public PairColorRange(int min, int max, String color) {
        this.min = min;
        this.max = max;
        this.color = color;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    public String getColor() {
        return color;
    }
}
