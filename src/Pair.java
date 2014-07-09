
public class Pair<S,T> {

    public S first;
    public T second;
    
    public Pair () {
        first = null;
        second = null;
    }
    
    public Pair (S first, T second) {
        this.first = first;
        this.second = second;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Pair) {
            return ((Pair<?,?>) obj).second.equals(second) &&
                   ((Pair<?,?>) obj).second.equals(second);
        }
        return false;
    }
}
