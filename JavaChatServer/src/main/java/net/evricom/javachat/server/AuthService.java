package net.evricom.javachat.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private static Connection connection;
    private static Statement statement;
    private static PreparedStatement preparedStatement;

    public static void connect() throws SQLException {
        DriverManager.registerDriver(new org.sqlite.JDBC ());
        //Class.forName ( "org.sqlite.JDBC" );
        connection = DriverManager.getConnection ( "jdbc:sqlite:userDB.db" );
        statement = connection.createStatement ();
    }

    public static void disconnect(){
        try {
            connection.close ();
        } catch (SQLException e) {
            e.printStackTrace ();
        }
    }


    public static String getNickByLoginAndPass(String login, String pass){
        // для безопасности (возможности использовать в поле логин-пароль SQL-иньекции )используем preparedStatement
        String sql = "SELECT nickname FROM main WHERE login=? AND password=?";
        try {
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1,login);
            preparedStatement.setString(2,pass);
            ResultSet resultSet = preparedStatement.executeQuery ();
            if (resultSet.next ()){
                return resultSet.getString ( 1 );
            }

        } catch (SQLException e) {
            e.printStackTrace ();
        }
        return null;
    }

    public static String getBlackListForUser(String username) {
        StringBuilder sb = new StringBuilder();
        String sql = String.format(
                "SELECT " +
                        "main.nickname " +
                        "FROM blacklist " +
                        "INNER JOIN main " +
                        "ON blacklist.id_nick_blocked = main.id " +
                        "WHERE blacklist.id_nick_owner " +
                        "IN (SELECT main.id FROM main WHERE main.nickname = '%s')",
                username);

        try {
            ResultSet resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                sb.append(resultSet.getString(1));
                sb.append(" ");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sb.toString().trim();
    }

    public static boolean deleteItemForBlackList(String nickOwner, String nickForBlocking) {
        int rez = 0;
        String sql = String.format(
                "DELETE FROM blacklist " +
                        "WHERE blacklist.id_nick_owner IN " +
                        "(SELECT main.id FROM main WHERE main.nickname = '%s') " +
                        "AND blacklist.id_nick_blocked IN " +
                        "(SELECT main.id FROM main WHERE main.nickname = '%s')",
                nickOwner, nickForBlocking);
        try {
            rez = statement.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return (rez==1);
    }

    public static boolean addItemForBlackList(String nickOwner, String nickForBlocking) {
        int rez = 0;
        String sql = String.format(
                "INSERT INTO blacklist (id_nick_owner, id_nick_blocked) VALUES (" +
                        "(SELECT main.id FROM main WHERE main.nickname = '%s'), " +
                        "(SELECT main.id FROM main WHERE main.nickname = '%s')" +
                        ")", nickOwner, nickForBlocking);
        try {
            rez = statement.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return (rez==1);
    }



}

