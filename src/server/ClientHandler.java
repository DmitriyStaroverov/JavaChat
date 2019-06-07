package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ClientHandler implements Runnable {

    private ServerMain serverMain;

    private Socket socket;

    private DataInputStream in;

    private DataOutputStream out;

    private ArrayList<String> blacklist;

    private String nick;

    public String getNick() {
        return nick;
    }

    public ClientHandler(ServerMain serverMain, Socket socket) throws IOException {
        this.serverMain = serverMain;
        this.socket = socket;
        this.in = new DataInputStream ( socket.getInputStream () );
        this.out = new DataOutputStream ( socket.getOutputStream () );
        this.blacklist = new ArrayList<> ();
    }

    @Override
    public void run() {

        try {
            while (true) {
                String str = in.readUTF ();
                if (str.startsWith ( "/auth" )) {
                    String[] tokens = str.split ( " " );
                    String newNick = AuthService.getNickByLoginAndPass ( tokens[1], tokens[2] );
                    if (newNick != null) {
                        if (serverMain.getClientHandler ( newNick ) == null) {
                            sendMsg ( "/authok " + newNick );
                            this.nick = newNick;
                            serverMain.subscribe ( this );
                            break;
                        } else {
                            sendMsg ( "Пользователь " + newNick + " уже подключен!" );
                        }
                    } else {
                        sendMsg ( "Неверный логин/пароль" );
                    }
                }
            }
            SimpleDateFormat timeFormat = new SimpleDateFormat ( "[H:mm:ss]" );
            while (true) {
                String str = in.readUTF ();
                // блок служебных сообщений
                if (str.indexOf ( "/" ) > -1) {
                    if (str.indexOf ( "/end" ) > -1) {
                        out.writeUTF ( "/serverClosed" );
                        break;
                    }
                    if (str.indexOf ( "/blacklist" ) > -1) {
                        String[] tokens = str.split ( " " );
                        if (serverMain.getClientHandler ( tokens[1] ) != null) {
                            blacklist.add ( tokens[1] );
                            sendMsg ( "Вы добавили пользователя " + tokens[1] + " в черный список!" );
                        } else {
                            sendMsg ( "Пользователь " + tokens[1] + " не подключен" );
                        }
                    }
                    //отправка личных сообщений
                    if (str.indexOf ( "/w " ) > -1) {
                        String[] tokens = str.split ( " ", 3 );
                        if (tokens.length < 3) {
                            sendMsg ( "Ошибка личного личного сообщения" );
                            continue;
                        }
                        ClientHandler clientToPrivatMsg = serverMain.getClientHandler ( tokens[1] );
                        if (clientToPrivatMsg != null) {
                            clientToPrivatMsg.sendMsg ( timeFormat.format ( new Date () ) + " " + getNick () + "(privat): " + tokens[2] );
                            sendMsg ( timeFormat.format ( new Date () ) + " " + getNick () + "(privat): " + tokens[2] );
                        } else {
                            sendMsg ( "Пользователь с ником " + tokens[1] + " не подключен" );
                        }
                    }
                } else {
                    serverMain.broadcastMsg ( this, timeFormat.format ( new Date () ) + " " + getNick () + ": " + str );
                    System.out.println ( "Поток: " + Thread.currentThread ().getName () + "; строка: " + str );
                }
            }

        } catch (IOException e) {
            e.printStackTrace ();
        } finally {

            try {
                in.close ();
            } catch (IOException e) {
                e.printStackTrace ();
            }
            try {
                out.close ();
            } catch (IOException e) {
                e.printStackTrace ();
            }
            try {
                socket.close ();
            } catch (IOException e) {
                e.printStackTrace ();
            }
            serverMain.unsubscribe ( this );

        }

    }

    public boolean checkBlackList(String nick) {
        return blacklist.contains ( nick );
    }

    public void sendMsg(String strMsg) {
        try {
            out.writeUTF ( strMsg );
        } catch (IOException e) {
            e.printStackTrace ();
        }
    }
}
