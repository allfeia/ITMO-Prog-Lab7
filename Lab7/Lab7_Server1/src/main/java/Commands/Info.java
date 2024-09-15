package Commands;

import Collection.CollectionManager;
import ConnectionUtils.Request;
import ConnectionUtils.Response;
import Data.Movie;

import java.io.Serial;
import java.io.Serializable;

public class Info implements Command, Serializable {
    private static final long serialVersionUID = 101l;


    @Override
    public Response execute(String login, Object args, Movie movie, CollectionManager collectionManager) {
        if (args == null) throw new IllegalArgumentException("no arguments");
        return collectionManager.info();
    }

    @Override
    public String description() {
        return "info: get information about collection (type, data of initialization, count of elements)";
    }

    @Override
    public String getName(){
        return "info";
    }
}

