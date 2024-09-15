package DataBase;

import java.sql.*;
import java.time.LocalDate;

import Authorisation.PasswordManager;
import Authorisation.User;
import Collection.CollectionManager;
import ConnectionUtils.Response;
import Data.Movie;
import Utils.ServerLogger;
import org.springframework.security.crypto.scrypt.SCryptPasswordEncoder;

public class DataBaseManager {
    private static QueryMaker queryManager = new QueryMaker();
    public static Connection connection = null;

    public static Connection connect() {
        try {
            Class.forName("org.postgresql.Driver");
            // connection = DriverManager.getConnection("jdbc:postgresql://pg:5432/studs", "", ""); // для гелиоса
            connection = DriverManager.getConnection("jdbc:postgresql://localhost:5433/postgres", "postgres", ""); // для локалки
            ServerLogger.getLogger().info("Connection Established");
        } catch (SQLException | ClassNotFoundException e) {
            ServerLogger.getLogger().warning("Connection Failed " + e.getMessage());
            // System.exit(-1);
        }
        return connection;
    }

    public static Response addUser(String username, String password) {
        try (PreparedStatement pstmt = connection.prepareStatement(queryManager.addUser);) {
            pstmt.setString(1, username);
            pstmt.setString(2, PasswordManager.hashPassword(password));
            pstmt.executeUpdate();
            return new Response("Вы успешно зарегистрировались.");
        } catch (SQLException e) {
            return new Response(e.getMessage());
        }
    }

    public static boolean login(String username, String password) {
        try {
            PreparedStatement getId = connection.prepareStatement(queryManager.getPassword);
            getId.setString(1, username);
            ResultSet resultSet = getId.executeQuery();
            resultSet.next();
            return PasswordManager.checkPassword(password, resultSet.getString("password"));
        } catch (Exception e) {
            return false;
        }
    }

    private static int getOwnerId(String login) throws SQLException {
        PreparedStatement getId = connection.prepareStatement(queryManager.getUserId);
        getId.setString(1, login);
        ResultSet resultSet = getId.executeQuery();
        resultSet.next();
        return resultSet.getInt("id");
    }


    public static int addObject(Movie newMovie, String login) {
        try {
            String addMovie = """
                    INSERT INTO movies(name, coordinate_x, coordinate_y, creation_date, oscars_count, budget, genre, mpaaRating, operators_name, operators_weight, operators_eye_color, author, owner_id)
                    VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    RETURNING id;
                     """;
            PreparedStatement add = connection.prepareStatement(addMovie);
            //add.setInt(1, newMovie.getId());
            add.setString(1, newMovie.getName());
            add.setFloat(2, newMovie.getCoordinates().getX());
            add.setFloat(3, newMovie.getCoordinates().getY());
            add.setDate(4, Date.valueOf(newMovie.getCreationDate()));
            add.setInt(5, newMovie.getOscarsCount());
            add.setLong(6, newMovie.getBudget());
            add.setString(7, newMovie.getMovieGenre().toString());
            add.setString(8, newMovie.getMpaaRating().toString());
            add.setString(9, newMovie.getOperator().getName());
            add.setFloat(10, newMovie.getOperator().getWeight());
            add.setString(11, newMovie.getOperator().getEyeColor().toString());
            add.setString(12, newMovie.getUser_login());
            add.setInt(13, getOwnerId(login));
            ResultSet resultSet = add.executeQuery();
            resultSet.next();
            return resultSet.getInt("id");
        } catch (SQLException | NullPointerException e) {
            ServerLogger.getLogger().warning("Ошибка при подключении/выполнении запроса " + e.getMessage());
        }
        return -1;
    }

    public static boolean updateById(String login, int id, Movie newMovie) {
        try {
            String updateMovie = """
                    UPDATE movies SET(name, coordinate_x, coordinate_y, creation_date, oscars_count, budget, genre, mpaaRating, operators_name, operators_weight, operators_eye_color, author)
                    = (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    WHERE id = ? AND owner_id = ?
                    RETURNING id;
                     """;
            PreparedStatement update = connection.prepareStatement(updateMovie);
            update.setString(1, newMovie.getName());
            update.setFloat(2, newMovie.getCoordinates().getX());
            update.setFloat(3, newMovie.getCoordinates().getY());
            update.setDate(4, Date.valueOf(newMovie.getCreationDate()));
            update.setInt(5, newMovie.getOscarsCount());
            update.setLong(6, newMovie.getBudget());
            update.setString(7, newMovie.getMovieGenre().toString());
            update.setString(8, newMovie.getMpaaRating().toString());
            update.setString(9, newMovie.getOperator().getName());
            update.setFloat(10, newMovie.getOperator().getWeight());
            update.setString(11, newMovie.getOperator().getEyeColor().toString());
            update.setString(12, newMovie.getUser_login());
            update.setInt(13, id);
            update.setInt(14, getOwnerId(login));
            ResultSet resultSet = update.executeQuery();
            return resultSet.next();
        } catch (SQLException | NullPointerException e) {
            ServerLogger.getLogger().warning("Ошибка при подключении/выполнении запроса " + e.getMessage());
        }
        return false;
    }

    public static boolean removeById(String login, int id) {
        try {
            String updateMovie = """
                    DELETE FROM movies
                    WHERE id = ? AND owner_id = ?
                    RETURNING id;
                     """;
            PreparedStatement update = connection.prepareStatement(updateMovie);
            update.setInt(1, id);
            update.setInt(2, getOwnerId(login));
            ResultSet resultSet = update.executeQuery();
            return resultSet.next();
        } catch (SQLException | NullPointerException e) {
            ServerLogger.getLogger().warning("Ошибка при подключении/выполнении запроса " + e.getMessage());
        }
        return false;
    }

    public static Response clearUserCollection(String login) {
        try {
            String clear = """
                    DELETE FROM movies
                    WHERE owner_id = ?;
                     """;
            PreparedStatement update = connection.prepareStatement(clear);
            update.setInt(1, getOwnerId(login));
            update.execute();
            return new Response("Коллекция очищена");
        } catch (SQLException | NullPointerException e) {
            ServerLogger.getLogger().warning("Ошибка при подключении/выполнении запроса " + e.getMessage());
        }
        return new Response("Возникла ошибка");
    }

    public boolean updateObject(Movie newMovie, String user) {
        try {
            Connection connection = connect();
            PreparedStatement update = connection.prepareStatement(queryManager.update);
            update.setString(1, newMovie.getName());
            update.setFloat(2, newMovie.getCoordinates().getX());
            update.setFloat(3, newMovie.getCoordinates().getY());
            update.setInt(4, newMovie.getOscarsCount());
            update.setLong(5, newMovie.getBudget());
            update.setString(6, newMovie.getMovieGenre().toString());
            update.setString(7, newMovie.getMpaaRating().toString());
            update.setString(8, newMovie.getOperator().getName());
            update.setFloat(9, newMovie.getOperator().getWeight());
            update.setString(10, newMovie.getOperator().getEyeColor().toString());
            update.setString(11, user);
            update.setInt(12, newMovie.getId());
            ResultSet resultSet = update.executeQuery();
            return (resultSet.next());
        } catch (SQLException | NullPointerException e) {
            ServerLogger.getLogger().warning("Ошибка при подключении/выполнении запроса");
        }
        return false;
    }

    public Response registration(User user) {
        try {
            Connection connection = connect();
            PreparedStatement findUser = connection.prepareStatement(queryManager.findUser);
            findUser.setString(1, user.getLogin());
            ResultSet resultSet = findUser.executeQuery();
            if (!resultSet.next()) {
                PasswordManager passwordManager = new PasswordManager();
                PreparedStatement addUser = connection.prepareStatement(queryManager.addUser);
                addUser.setString(1, user.getLogin());
                addUser.setString(2, user.getPassword());
                addUser.setString(3, passwordManager.hashPassword(user.getPassword()));
                addUser.execute();
                return new Response("Регистрация прошла успешно!");
            } else {
                return new Response("Пользователь с таким логином уже существует. Попробуй еще раз: ");
            }
        } catch (SQLException | NullPointerException e) {
            return new Response("Ошибка подключения к базе данных. Попробуй еще раз: ");
        }
    }

    public Response authorisation(User user) {
        try {
            Connection connection = connect();
            PreparedStatement findUser = connection.prepareStatement(queryManager.findUser);
            findUser.setString(1, user.getLogin());
            ResultSet resultSet = findUser.executeQuery();
            if (resultSet.next()) {
                if (resultSet.getString("password").equals(user.getPassword()))
                    return new Response("Вход выполнен успешно!");
                return new Response("Введен неверный пароль. Попробуй еще раз: ");
            } else {
                return new Response("Пользователь с таким логином не найден. Попробуй еще раз: ");
            }
        } catch (SQLException | NullPointerException e) {
            return new Response("Ошибка подключения к базе данных. Попробуй еще раз: ");
        }
    }
}


