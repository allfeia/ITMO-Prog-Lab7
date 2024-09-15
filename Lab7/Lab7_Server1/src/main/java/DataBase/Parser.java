package DataBase;

import Data.Movie;

import java.util.concurrent.CopyOnWriteArraySet;

public interface Parser {
    void save();
    CopyOnWriteArraySet<Movie> load();

}
