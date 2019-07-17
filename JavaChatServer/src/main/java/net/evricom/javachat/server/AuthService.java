package net.evricom.javachat.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private static Connection connection;
    private static Statement statement;

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


//    SELECT nickname FROM main WHERE login='login1' AND password='pass1'
    public static String getNickByLoginAndPass(String login, String pass){
        String sql = String.format ( "SELECT nickname FROM main WHERE login='%s' AND password='%s'", login, pass );
        try {
            ResultSet resultSet = statement.executeQuery ( sql );
            if (resultSet.next ()){
                return resultSet.getString ( 1 );
            }

        } catch (SQLException e) {
            e.printStackTrace ();
        }
        return null;
    }

}

