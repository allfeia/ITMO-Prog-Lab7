package Commands;

import Collection.CollectionManager;
import ConnectionUtils.Request;
import ConnectionUtils.Response;
import Data.Movie;
import Data.MovieComparator;
import DataBase.DataBaseManager;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

public class AddIfMin implements Command, Serializable {
    private static final long serialVersionUID = 101l;

    @Override
    public Response execute(String login, Object args, Movie movie, CollectionManager collectionManager) {
        if (args != null)
            throw new IllegalArgumentException("no arguments except movie's parametrs");
        if (movie == null) throw new IllegalArgumentException("enter movie's parametrs");
        if (movie.getBudget() < collectionManager.getMovies().stream().max(new MovieComparator()).get().getBudget()){
            DataBaseManager.addObject(movie, login);
            collectionManager.add(movie);
            return new Response("Элемент был добавлен в коллекцию");
        };
        return new Response("Элемент больше минимального");
    }
    @Override
    public String description() {
        return "add_if_min {element}: add new movie if oscars count in it less then in any in collection";
    }

    @Override
    public String getName(){
        return "add_if_min";
    }
}
