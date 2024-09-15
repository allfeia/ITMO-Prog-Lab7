package Commands;

import Collection.CollectionManager;
import ConnectionUtils.Response;
import Data.Movie;

import java.io.Serializable;

public class Reg implements Command, Serializable {
    private static final long serialVersionUID = 101l;
    @Override
    public Response execute(Object args, Movie movie, CollectionManager collectionManager) {
        return null;
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
