package client;

import server.Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private BufferedReader input;
    private PrintWriter output;
    private Socket socket;

    public Client() {
        connect();
    }

    private void connect() {
        Scanner scan = new Scanner(System.in);
        try {
            // создаем точку соеденения с сервером
            socket = new Socket(InetAddress.getLoopbackAddress().getHostAddress(), Server.PORT);

            // обертки для работы с потоками
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new PrintWriter(socket.getOutputStream(), true);

            // представляемся серверу
            System.out.println("Введите ваше имя:");
            output.println(scan.nextLine());

            // нить, которая будет постоянно опрашивать входящий поток,
            // и выводить сообщения в консоль
            Listener listener = new Listener();
            listener.start();

            // Пока пользователь не введёт "exit" отправляем на сервер всё, что
            // введено из консоли
            String msg = "";
            while (!msg.equals("exit")) {
                msg = scan.nextLine();
                output.println(msg);
            }
            // после ввода exit останавливаем нить слушатель
            // и закрывам потоки
            listener.disable();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeConnection();
        }
    }

    private void closeConnection() {
        try {
            // закрывам все потоки
            input.close();
            output.close();
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class Listener extends Thread {

        private boolean isActive = true;

        public void disable() {
            isActive = false;
        }

        @Override
        public void run() {
            try {
                String msg;
                while (isActive) {
                    msg = input.readLine();
                    if (msg != null) {
                        System.out.println(msg);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
