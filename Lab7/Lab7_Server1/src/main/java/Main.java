import Collection.CollectionManager;

import Data.Movie;
import DataBase.DataBaseManager;
import DataBase.DataBaseParser;
import Utils.*;

import java.sql.Connection;
import java.util.HashSet;
import java.util.concurrent.CopyOnWriteArraySet;

public class Main{
    private final static Integer serverPort = 6732;
    public static void main(String[] args) {

        DataBaseManager.connect();
        DataBaseParser dataBaseParser = new DataBaseParser();
        CopyOnWriteArraySet<Movie> collection = dataBaseParser.load();
        try {
            CollectionManager collectionManager = new CollectionManager();
            collectionManager.setCollection(collection);
            Server server = new Server(serverPort, collectionManager);
            server.run();
        } catch (Exception e){
            System.err.println(e.getMessage());
        }
    }


}
