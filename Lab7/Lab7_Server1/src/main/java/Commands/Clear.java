package Commands;

import Collection.CollectionManager;
import ConnectionUtils.Request;
import ConnectionUtils.Response;
import Data.Movie;
import DataBase.DataBaseManager;
import DataBase.DataBaseParser;

import java.io.Serial;
import java.io.Serializable;

public class Clear implements Command, Serializable {
    private static final long serialVersionUID = 101l;

    @Override
    public Response execute(String login, Object args, Movie movie, CollectionManager collectionManager) {
        if (args == null) throw new IllegalArgumentException("no arguments");
        Response response = DataBaseManager.clearUserCollection(login);
        collectionManager.setCollection(DataBaseParser.load());
        return response;
    }

    @Override
    public String description() {
        return "clear: clear collection";
    }

    @Override
    public String getName(){
        return "clear";
    }
}
