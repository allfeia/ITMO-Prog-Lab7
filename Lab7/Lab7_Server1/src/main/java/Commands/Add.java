package Commands;

import Collection.CollectionManager;
import ConnectionUtils.Request;
import ConnectionUtils.Response;
import Data.Movie;
import DataBase.DataBaseManager;

import java.io.Serial;
import java.io.Serializable;

public class Add implements Command, Serializable {

    private static final long serialVersionUID = 101l;
    @Override
    public Response execute(String login, Object args, Movie movie, CollectionManager collectionManager) {
        if (args == null)
            throw new IllegalArgumentException("no arguments except movie's parametrs");
        if (movie == null) throw new IllegalArgumentException("enter movie's parametrs");
        int id = DataBaseManager.addObject(movie, login);
        // DataBaseManager dataBaseManager = new DataBaseManager();
        // Integer id = dataBaseManager.addObject(movie);
        if (id == -1){
            return  new Response("Error with added object to db");
        } else{
            movie.setId(id);
            return collectionManager.add(movie);
        }

    }
    @Override
    public String description() {
        return "add: add movie into collection";
    }

    @Override
    public String getName(){
        return "add";
    }
}


