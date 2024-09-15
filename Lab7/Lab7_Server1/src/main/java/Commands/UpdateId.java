package Commands;

import Collection.CollectionManager;
import Collection.CollectionUtil;
import ConnectionUtils.Request;
import ConnectionUtils.Response;
import Data.Movie;
import DataBase.DataBaseManager;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

public class UpdateId implements Command, Serializable {
    private static final long serialVersionUID = 101l;

    @Override
    public Response execute(String login, Object args, Movie movie, CollectionManager collectionManager) {
        if (args == null)
            throw new NullPointerException("enter argument - id");
        try {
            int id = Integer.parseInt((String) args);
            if (DataBaseManager.updateById(login, id, movie)){
                movie.setId(id);
                collectionManager.removeById(id);
                collectionManager.add(movie);
                return new Response("Элемент был обновлен");
            } return new Response("Элемент не был обновлен");
        } catch (Exception e) {
            throw new IllegalArgumentException("argument id must be integer");
        }
    }

    @Override
    public String description() {
        return "update {element}: update element in collection by its id";
    }

    @Override
    public String getName(){
        return "update";
    }
}

