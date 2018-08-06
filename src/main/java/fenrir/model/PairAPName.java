package fenrir.model;

import java.util.Objects;

public class PairAPName {
    private String name1;
    private String name2;

    public PairAPName(String name1, String name2) {
        this.name1 = name1;
        this.name2 = name2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PairAPName that = (PairAPName) o;
        return (Objects.equals(name1, that.name1) && Objects.equals(name2, that.name2)) ||
                (Objects.equals(name2, that.name1) && Objects.equals(name1, that.name2));
    }

    @Override
    public int hashCode() {

        return Objects.hash(name1, name2);
    }

    public String getName1() {
        return name1;
    }

    public String getName2() {
        return name2;
    }

    @Override
    public String toString() {
        return name1 + '-' + name2;
    }
}
