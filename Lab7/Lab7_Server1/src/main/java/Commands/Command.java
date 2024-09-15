
package Commands;

import Collection.CollectionManager;
import ConnectionUtils.Request;
import ConnectionUtils.Response;
import Data.Movie;

public interface Command {
    Response execute(String login, Object args, Movie movie, CollectionManager collectionManager);
    String description();
    String getName();
}
