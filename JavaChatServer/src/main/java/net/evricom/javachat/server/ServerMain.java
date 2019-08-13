package net.evricom.javachat.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerMain {

    public static final long TIMEOUT = 600;

    private static final Logger log = LoggerFactory.getLogger(ServerMain.class);

    private Vector<ClientHandler> clientHandlers;

    public ServerMain() throws SQLException {
        clientHandlers = new Vector<> ();
        ServerSocket server = null;
        Socket socket = null;
        try {
            AuthService.connect ();
            server = new ServerSocket ( 8189 );
            log.debug ( "Сервер запущен, ожидаем клиентов" );
            while (true) {
                socket = server.accept ();
                ClientHandler clientHandler = new ClientHandler ( this, socket );
                Thread threadClient = new Thread ( clientHandler );
                threadClient.setDaemon ( true );
                threadClient.start ();

                log.debug("Сокет принял соединение c адреса " + socket.getInetAddress() + ", ");
//                System.out.println("Current time" + System.currentTimeMillis());
//                Timer timer = new Timer();
//                timer.schedule(new TimerTask() {
//                    @Override
//                    public void run() {
//                        System.out.println("123456");
//                        if(clientHandler.getNick().equals("")){
//                            System.out.println("123");
//                        }
//                    }
//                },ServerMain.TIMEOUT);
//                System.out.println("Current time" + System.currentTimeMillis());

            }
        } catch (IOException e) {
            e.printStackTrace ();
        } finally {
            try {
                socket.close ();
            } catch (IOException e) {
                e.printStackTrace ();
            }
            try {
                server.close ();
            } catch (IOException e) {
                e.printStackTrace ();
            }
            AuthService.disconnect ();
        }
    }

    public void subscribe(ClientHandler clientHandler) {
        clientHandlers.add ( clientHandler );
        broadcastClientList ();
        log.debug ( "Клиент: " + clientHandler.getNick () + " подключился" );
    }

    public ClientHandler getClientHandler(String nickName) {
        if (nickName == null) {
            return null;
        }
        for (ClientHandler client :
                clientHandlers) {
            if (client.getNick ().equals ( nickName )) {
                return client;
            }
        }
        return null;
    }

    public void unsubscribe(ClientHandler clientHandler) {
        clientHandlers.remove ( clientHandler );
        broadcastClientList ();
        log.debug ( "Клиент: " + clientHandler.getNick () + " отключился" );
    }

    public void broadcastMsg(ClientHandler from, String strMsg) {
        for (ClientHandler client :
                clientHandlers) {
            if (!client.checkBlackList ( from.getNick () )) {
                client.sendMsg ( strMsg );
            }
        }
    }

    public void broadcastClientList() {
        StringBuilder sb = new StringBuilder ();
        sb.append ( "/clientList " );
        for (ClientHandler client : clientHandlers) {
            sb.append ( client.getNick () + " " );
        }
        String out = sb.toString ();
        for (ClientHandler client : clientHandlers) {
            client.sendMsg ( out );
        }

    }
}
