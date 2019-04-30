package server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class ServerMain {
    public static void main(String[] args) {
        ServerSocket server = null;
        Socket socket = null;
        try {
            server = new ServerSocket ( 8189 );
            System.out.println ("Сервер запущен");
            socket = server.accept ();
            System.out.println ("Клиент подключился");

            Scanner in = new Scanner ( socket.getInputStream () );
            PrintWriter out = new PrintWriter ( socket.getOutputStream (), true );


            while (true){
                String str = in.nextLine ();
                if (str.equals ( "/end" )) break;
                out.println ( "echo :"  + str );
                System.out.println ("Client: " + str);
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
        }


    }
}
