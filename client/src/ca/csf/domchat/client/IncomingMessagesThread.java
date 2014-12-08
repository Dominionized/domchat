package ca.csf.domchat.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class IncomingMessagesThread extends Thread {

    private Socket socket;
    private Client client;

    private BufferedReader input;

    public IncomingMessagesThread(Socket socket, Client client) {

        this.socket = socket;
        this.client = client;
    }

    @Override
    public void run() {

        try {

            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            while (true) {
                try {
                    client.onReadLine(input.readLine());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
