package Data;

import java.util.Comparator;

public class MovieComparator implements Comparator<Movie> {

    @Override
    public int compare(Movie o1, Movie o2) {
        return (int) (o1.getBudget() - o2.getBudget());
    }
}
