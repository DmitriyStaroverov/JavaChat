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

    public void subscribe(ClientHandler clientHandler){
        clientHandlers.add ( clientHandler );
        System.out.println ( "Клиент подключился" );
    }

    public void unsubscribe(ClientHandler clientHandler){
        clientHandlers.remove ( clientHandler );
        System.out.println ( "Клиент отключился, поток: " + Thread.currentThread ().getName () );
    }

    public void broadcastMsg(String strMsg) {
        for (ClientHandler client :
                clientHandlers) {
            client.sendMsg ( strMsg );
        }
    }
}
