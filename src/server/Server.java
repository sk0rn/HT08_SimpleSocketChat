package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Server {
    // Список всех клиентских соеденений
    private List<ClientConnection> clients = Collections.synchronizedList(new ArrayList<>());
    private ServerSocket server;
    public final static int PORT = 4768;

    public Server() {
        start();
    }

    private void start() {
        try {
            server = new ServerSocket(PORT);
            while (true) {
                Socket socket = server.accept();
                ClientConnection client = new ClientConnection(socket);
                clients.add(client);
                client.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // потоки которые опрашивают своих клиентов(читают входящие потоки их сокетов),
    // и если они получат сообщение, то проходятся по списку всех всех соеденений,
    // и в исходящий поток транслируют каждому потоку полученное им сообщение
    private class ClientConnection extends Thread {
        private BufferedReader input;
        private PrintWriter output;
        private Socket socket;
        private String name = "default";

        public ClientConnection(Socket socket) {
            this.socket = socket;

            try {
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                output = new PrintWriter(socket.getOutputStream(), true);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                name = input.readLine();
                String msg;
                while (true) {
                    msg = input.readLine();
                    if (msg.equals("exit")) {
                        break;
                    }
                    // отправляем входящее сообщение от клиента для всех клиентов
                    for (ClientConnection c : clients) {
                        c.output.println(name + ": " + msg);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        void close() {
            try {
                input.close();
                output.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

}