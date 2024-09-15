package Commands;

import Collection.CollectionManager;
import ConnectionUtils.Request;
import ConnectionUtils.Response;
import Data.Movie;
import DataBase.DataBaseManager;

import java.io.Serializable;

public class Reg implements Command, Serializable {
    private static final long serialVersionUID = 101l;
    @Override
    public Response execute(String login, Object args, Movie movie, CollectionManager collectionManager) {
        return DataBaseManager.addUser(args.toString().split(" ")[0], args.toString().split(" ")[1]);
    }

    @Override
    public String description() {
        return null;
    }

    @Override
    public String getName() {
        return "reg";
    }
}
