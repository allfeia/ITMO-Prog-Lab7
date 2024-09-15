package Utils;

import Commands.Command;
import ConnectionUtils.Request;
import ConnectionUtils.Response;
import Data.Movie;
import Collection.CollectionManager;

/**
 * Класс инициатора
 * инициатор - сущность запускающая команды
 */

public class CommandManager {

    public static Response runCommand(Request request, CollectionManager collectionManager, String login) {
        Command command = request.getCommand();
        String args = request.getArgs();
        Movie movie = request.getObject();
        try {
            return command.execute(login, args, movie, collectionManager);
        } catch (Exception e) {
            return new Response("Error with executing command " + e.getMessage());
        }
    }

}
