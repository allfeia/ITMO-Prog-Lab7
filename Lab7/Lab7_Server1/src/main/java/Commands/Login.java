package Commands;

import Collection.CollectionManager;
import ConnectionUtils.Request;
import ConnectionUtils.Response;
import Data.Movie;
import DataBase.DataBaseManager;

import java.io.Serializable;

public class Login implements Command, Serializable {
    private static final long serialVersionUID = 101l;
    @Override
    public Response execute(String login, Object args, Movie movie, CollectionManager collectionManager) {
        if (DataBaseManager.login(args.toString().split(" ")[0], args.toString().split(" ")[1])){
            return new Response("Вы успешно вошли в систему");
        } return new Response("Неверный логин или пароль");
    }

    @Override
    public String description() {
        return null;
    }

    @Override
    public String getName() {
        return "login";
    }
}
