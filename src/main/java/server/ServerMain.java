package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.Vector;

public class ServerMain {

    private Vector<ClientHandler> clientHandlers;

    public ServerMain() throws SQLException {
        clientHandlers = new Vector<> ();
        ServerSocket server = null;
        Socket socket = null;
        try {
            AuthService.connect ();
            server = new ServerSocket ( 8189 );
            System.out.println ( "Сервер запущен, ожидаем клиентов" );
            while (true) {
                socket = server.accept ();
                ClientHandler clientHandler = new ClientHandler ( this, socket );
                Thread threadClient = new Thread ( clientHandler );
                threadClient.setDaemon ( true );
                threadClient.start ();
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
        System.out.println ( "Клиент: " + clientHandler.getNick () + " подключился" );
    }

    public ClientHandler getClientHandler(String nickName) {
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
        System.out.println ( "Клиент: " + clientHandler.getNick () + " отключился" );
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
