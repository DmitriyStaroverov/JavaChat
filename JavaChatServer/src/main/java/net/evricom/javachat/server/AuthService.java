package net.evricom.javachat.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.Properties;

public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private static Connection connection;
    private static Statement statement;
    private static PreparedStatement prepStatAddHistory;
    private static PreparedStatement prepstatGetHistory;


    public static void connect() throws
            SQLException {

        InputStream inputStream = ClassLoader.getSystemClassLoader().getResourceAsStream("db.properties");
        Properties props = new Properties();
        try {
            props.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String driverDB = props.getProperty("db.driver");
        String addressDB = props.getProperty("db.address");
        //
        try {
            DriverManager.registerDriver((Driver) Class.forName(driverDB).newInstance());
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        //DriverManager.registerDriver(new org.sqlite.JDBC());
        //Class.forName( "org.sqlite.JDBC" );
        connection = DriverManager.getConnection(addressDB);
        statement = connection.createStatement();
        // ADD history
        String sqlAddHistory = "INSERT INTO history(date_msg,sender_id,receiver_id,msg) VALUES(" +
                "?," +
                "(SELECT main.id FROM main WHERE main.nickname=?)," +
                "(SELECT main.id FROM main WHERE main.nickname=?)," +
                "?" +
                ")";
        prepStatAddHistory = connection.prepareStatement(sqlAddHistory);
        // GET Privat history
        String sqlGetHistory = "SELECT history.date_msg, main.nickname AS sender, history.receiver_id, history.msg " +
                "FROM history INNER JOIN main ON history.sender_id = main.id " +
                "WHERE (receiver_id IN (SELECT id FROM main WHERE main.nickname = ?)) " +
                "OR " +
                "receiver_id ISNULL " +
                "ORDER BY history.date_msg;";
        prepstatGetHistory = connection.prepareStatement(sqlGetHistory);

    }

    public static void disconnect() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static String getHistory(String nickname, ArrayList<String> blacklist) {
        StringBuilder sb = new StringBuilder();
        java.util.Date dateMsg;
        boolean statusPrivat = false;
        try {
            prepstatGetHistory.setString(1, nickname);
            ResultSet resultSet = prepstatGetHistory.executeQuery();
            while (resultSet.next()) {
                String strNick = resultSet.getString("sender");
                statusPrivat = resultSet.getInt("receiver_id") != 0;
                // если в черном списке и неприватно, пропускаем сообщение
                if (blacklist.contains(strNick) & !statusPrivat) continue;
                dateMsg = resultSet.getDate("date_msg");
                String strMsg = resultSet.getString("msg");
                sb.append(ClientHandler.formatMsgStr(dateMsg, strNick, strMsg, statusPrivat));
                sb.append("\n");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sb.toString().trim();
    }

    public static boolean addHistory(java.util.Date datetime, String sender, String receiver, String msg) {
        int rez = 0;
        try {
            prepStatAddHistory.setDate(1, new java.sql.Date(datetime.getTime()));
            prepStatAddHistory.setString(2, sender);
            prepStatAddHistory.setString(3, receiver);
            prepStatAddHistory.setString(4, msg);
            rez = prepStatAddHistory.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return (rez == 1);
    }


    public static String getNickByLoginAndPass(String login, int hashPass) {
        // для безопасности (возможности использовать в поле логин-пароль SQL-иньекции )используем preparedStatement
        String sql = "SELECT nickname FROM main WHERE login=? AND password=?";
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, login);
            preparedStatement.setInt(2, hashPass);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
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
        return (rez == 1);
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
        return (rez == 1);
    }


    public static boolean regNewUser(String regLogin, int regPassHash, String regNick) {
        int rez = 0;
        // для безопасности (возможности использовать в поле логин-пароль SQL-иньекции )используем preparedStatement
        String sql = "INSERT INTO main (login, password, nickname) VALUES (?, ?, ?)";
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, regLogin);
            preparedStatement.setInt(2, regPassHash);
            preparedStatement.setString(3, regNick);
            rez = preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return (rez == 1);
    }
}

