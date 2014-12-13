package ca.csf.domchat.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.logging.Level;

public class ClientReaderThread implements Runnable {
    private Socket socket;
    private Server server;
    private Client client;

    private Boolean isAlive;

    public ClientReaderThread(Socket socket, Server server, Client client) {
        this.socket = socket;
        this.server = server;
        this.client = client;
    }

    @Override
    public void run() {
        isAlive = true;
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            while (isAlive) {
                String readLine = bufferedReader.readLine();
                parseCommand(readLine);
            }
        } catch (Exception e) {
        } finally {
            isAlive = false;
            try {
                server.removeClient(client);
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void parseCommand(String commandsToParse) {
        if (commandsToParse.charAt(0) == '/') {
            String[] commands = commandsToParse.split(";");
            if (commands[0].equalsIgnoreCase("/username")) {
                server.changeUsername(commands[1], client);
            } else if (commands[0].equalsIgnoreCase("/quit")) {
                stop();
            } else if (commands[0].equalsIgnoreCase("/blacklist")) {
                for (Client client : server.getClients()) {
                    if (client.getUsername().equalsIgnoreCase(commands[1])) {
                        server.blacklist(client);
                        server.onMessage("SERVER:Blacklisted IP" + client.getSocket().getInetAddress().toString());
                        server.logger.log(Level.WARNING, "Blacklisted IP " + client.getSocket().getInetAddress().toString());
                        try {
                            client.getSocket().close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } else {
            server.onMessage(client.getUsername() + ":" + commandsToParse);
        }
    }

    public void stop() {
        isAlive = false;
    }
}
