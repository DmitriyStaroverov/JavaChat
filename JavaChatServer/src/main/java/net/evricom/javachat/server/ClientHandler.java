package net.evricom.javachat.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.*;

public class ClientHandler implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(ClientHandler.class);

    private ServerMain serverMain;

    private Socket socket;

    private DataInputStream in;

    private DataOutputStream out;

    private ArrayList<String> blacklist;

    private String nick;

    String getNick() {
        return nick;
    }

    ClientHandler(ServerMain serverMain, Socket socket) throws IOException {
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
                    if (tokens.length < 3) {
                        sendMsg("Пустые логин/пароль");
                        continue;
                    }
                    String newNick = AuthService.getNickByLoginAndPass ( tokens[1], tokens[2] );
                    if (newNick != null) {
                        if (serverMain.getClientHandler ( newNick ) == null) {
                            sendMsg ( "/authok " + newNick );
                            this.nick = newNick;
                            serverMain.subscribe ( this );
                            updateBlackList();
                            break;
                        } else {
                            sendMsg("Пользователь " + newNick + " уже подключен!");
                        }
                    } else {
                        sendMsg("Неверный логин/пароль");
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
                        if (serverMain.getClientHandler(tokens[2]) == null){
                            sendMsg ("Пользователь " + tokens[2] + " не подключен" );
                        } if (tokens[1].equals("ADD")){
                            AuthService.addItemForBlackList(getNick(),tokens[2]);
                            sendMsg ("Вы добавили пользователя " + tokens[2] + " в черный список!" );
                         } if (tokens[1].equals("DEL")){
                            AuthService.deleteItemForBlackList(getNick(),tokens[2]);
                            sendMsg ("Вы удалили пользователя " + tokens[2] + " из черного списка!" );
                        }
                        updateBlackList();
                        continue;
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
                    log.debug( "Поток: " + Thread.currentThread ().getName () + "; строка: " + str );
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

    private void updateBlackList(){
        blacklist.clear();
        String s = AuthService.getBlackListForUser(nick);
        String[] tokens = s.split(" ");
        Collections.addAll(blacklist, tokens);
        sendMsg("/blacklist " + s);
    }

    boolean checkBlackList(String nick) {
        return blacklist.contains ( nick );
    }

    void sendMsg(String strMsg) {
        try {
            out.writeUTF ( strMsg );
        } catch (IOException e) {
            e.printStackTrace ();
        }
    }
}

