package Commands;

import Collection.CollectionManager;
import Collection.CollectionUtil;
import ConnectionUtils.Request;
import ConnectionUtils.Response;
import Data.Movie;
import DataBase.DataBaseManager;

import java.io.Serial;
import java.io.Serializable;

public class RemoveById implements Command, Serializable {
    private static final long serialVersionUID = 101l;

    @Override
    public Response execute(String login, Object args, Movie movie, CollectionManager collectionManager) {
        if (args == null)
            throw new NullPointerException("enter argument - id");

        try {
            int id = Integer.parseInt((String) args);
            if (DataBaseManager.removeById(login, id)){
                collectionManager.removeById(id);
                return new Response("Элемент был удален");
            } return new Response("Элемент не был удален");

        } catch (Exception e) {
            throw new IllegalArgumentException("argument id must be integer");
        }
    }

    @Override
    public String description() {
        return "remove_by_id {id}: remove element from collection by its id";
    }

    @Override
    public String getName(){
        return "remove_by_id";
    }
}

