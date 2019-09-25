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

    private static final long TIMEOUT = 120000;

    private static final SimpleDateFormat timeFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss");

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
        this.in = new DataInputStream(socket.getInputStream());
        this.out = new DataOutputStream(socket.getOutputStream());
        this.blacklist = new ArrayList<>();
    }

    @Override
    public void run() {
        try {
            // Start TIMEOUT
            long t1 = System.currentTimeMillis();
            log.debug("Cоединение c адреса " + socket.getInetAddress() +
                    ", время = " + System.currentTimeMillis() + ".");
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (getNick() == null) {
                        sendMsg("/TIMEOUT");
                        log.debug("TIMEOUT для соединения, время которого " + (t1) + " мсек.");
                    }
                }
            }, TIMEOUT);
            // end TIMEOUT
            while (true) {
                String str = in.readUTF();
                if (str.startsWith("/auth")) {
                    String[] tokens = str.split(" ");
                    if (tokens.length < 3) {
                        sendMsg("Пустые логин/пароль");
                        continue;
                    }
                    String newNick = AuthService.getNickByLoginAndPass(tokens[1], getHashPass(tokens[2],tokens[1]));
                    if (newNick != null) {
                        if (serverMain.getClientHandler(newNick) == null) {
                            sendMsg("/authok " + newNick);
                            this.nick = newNick;
                            serverMain.subscribe(this);
                            updateBlackList();
                            sendHistory();
                            break;
                        } else {
                            sendMsg("Пользователь " + newNick + " уже подключен!");
                        }
                    } else {
                        sendMsg("Неверный логин/пароль");
                    }
                }
                if (str.startsWith("/end")) {
                    sendMsg("/serverClosed");
                    log.debug("Завершение соединения, время которого " + (t1) + " мсек.");
                    break;
                }
                if (str.startsWith("/newUser")) {
                    String[] tokens = str.split(" ");
                    if (tokens.length < 4) {
                        String strMsg = "Пустые поля при регистрации нового пользователя!";
                        sendMsg(strMsg);
                        log.debug(strMsg);
                        continue;
                    }
                    int hashPass = getHashPass(tokens[2],tokens[3]);
                    if (hashPass == 0) continue;
                    if (AuthService.regNewUser(tokens[1],hashPass,tokens[3])){
                        String strMsg = "Успешная регистрация нового пользователя с ником " + tokens[3];
                        sendMsg(strMsg);
                        log.debug(strMsg);
                        sendMsg("/authok " + tokens[3]);
                        this.nick = tokens[3];
                        serverMain.subscribe(this);
                        updateBlackList();
                        sendHistory();
                        break;
                    } else {
                        sendMsg("Ошибка при регистрации нового пользователя с ником " + tokens[3] + " !!!");
                    }
                }
            }
            while (getNick() != null) {
                String str = in.readUTF();
                // блок служебных сообщений
                if (str.indexOf("/") > -1) {
                    if (str.indexOf("/end") > -1) {
                        out.writeUTF("/serverClosed");
                        break;
                    }
                    if (str.indexOf("/blacklist") > -1) {
                        String[] tokens = str.split(" ");
                        if (serverMain.getClientHandler(tokens[2]) == null) {
                            sendMsg("Пользователь " + tokens[2] + " не подключен");
                        }
                        if (tokens[1].equals("ADD")) {
                            AuthService.addItemForBlackList(getNick(), tokens[2]);
                            sendMsg("Вы добавили пользователя " + tokens[2] + " в черный список!");
                        }
                        if (tokens[1].equals("DEL")) {
                            AuthService.deleteItemForBlackList(getNick(), tokens[2]);
                            sendMsg("Вы удалили пользователя " + tokens[2] + " из черного списка!");
                        }
                        updateBlackList();
                        sendHistory();
                        continue;
                    }
                    //отправка личных сообщений
                    if (str.indexOf("/w ") > -1) {
                        String[] tokens = str.split(" ", 3);
                        if (tokens.length < 3) {
                            sendMsg("Ошибка личного личного сообщения");
                            continue;
                        }
                        ClientHandler clientToPrivatMsg = serverMain.getClientHandler(tokens[1]);
                        if (clientToPrivatMsg != null) {
                            Date currDate = new Date();
                            AuthService.addHistory(currDate,getNick(),clientToPrivatMsg.getNick(),tokens[2]);
                            clientToPrivatMsg.sendMsg(formatMsgStr(currDate, getNick(), tokens[2], true));
                            sendMsg(formatMsgStr(currDate,getNick(),tokens[2],true));
                        } else {
                            sendMsg("Пользователь с ником " + tokens[1] + " не подключен");
                        }
                    }
                } else {
                    AuthService.addHistory(new Date(), getNick(), null, str);
                    serverMain.broadcastMsg(this, formatMsgStr(new Date(),getNick(),str, false));
                    log.debug("Поток: " + Thread.currentThread().getName() + "; строка: " + str);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            serverMain.unsubscribe(this);
        }
    }

    private void sendHistory() {
        sendMsg("/history " + AuthService.getHistory(getNick(),blacklist));
    }

    static String formatMsgStr(java.util.Date dateMsg, String nickname, String strMsg, boolean privatMsg ){
        StringBuilder sb = new StringBuilder();
        sb.append(nickname);
        sb.append(" (");
        sb.append(timeFormat.format(dateMsg));
        if (privatMsg){
            sb.append(")(PRIVAT): ");
        } else {
            sb.append("): ");
        }
        sb.append(strMsg);
        return sb.toString();
    }

    private int getHashPass(String strHash, String nick){
        int hashPass = 0;
        try {
            hashPass = Integer.parseInt(strHash);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        if (hashPass == 0){
            String msg = "Ошибка получения хеша из пароля пользователя с ником " + nick;
            sendMsg(msg);
            log.debug(msg);
        }
        return hashPass;
    }


    private void updateBlackList() {
        blacklist.clear();
        String s = AuthService.getBlackListForUser(nick);
        String[] tokens = s.split(" ");
        Collections.addAll(blacklist, tokens);
        sendMsg("/blacklist " + s);
    }

    boolean checkBlackList(String nick) {
        return blacklist.contains(nick);
    }

    void sendMsg(String strMsg) {
        try {
            if (!socket.isClosed()) out.writeUTF(strMsg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

