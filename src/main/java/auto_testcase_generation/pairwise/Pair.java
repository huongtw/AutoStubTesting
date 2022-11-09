package auto_testcase_generation.pairwise;

import java.util.Objects;

public class Pair<E, T> {
    private E first;
    private T second;

    public Pair(E first, T second) {
        this.first = first;
        this.second = second;
    }

    public E getFirst() {
        return first;
    }

    public T getSecond() {
        return second;
    }

    public void setSecond(T second) {
        this.second = second;
    }

    public String toString() {
        if(first instanceof String) {
            return Objects.toString(first, "_") + Objects.toString(second, "_");
        }
        return "(" + Objects.toString(first, "_") + ", " + Objects.toString(second, "_") + ")";
    }
}