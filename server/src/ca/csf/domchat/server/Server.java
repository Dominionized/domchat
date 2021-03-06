package ca.csf.domchat.server;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {

    private static int port = 4444;

    private List<ClientReaderThread> clientReaderThreads;
    private List<ClientWriterThread> clientWriterThreads;

    public List<Client> getClients() {
        return clients;
    }

    private List<Client> clients;
    int nbrClient;

    private File blacklist;

    private boolean isAlive;

    //TODO domdraw ?
    protected static Logger logger = Logger.getLogger("ca.csf.domdraw.server");

    public static void main(String[] args) throws IOException {
        Handler fh = new FileHandler("server.log", false);
        logger.addHandler(fh);

        System.out.print("Choisissez le port de démarrage serveur (Defaut 4444) : ");
        Scanner scanner = new Scanner(System.in);
        String changePort = scanner.nextLine();
        if (!changePort.isEmpty()) {
            port = Integer.parseInt(changePort);
        }
        new Server();
    }

    public Server() throws IOException {
        clientReaderThreads = new ArrayList<ClientReaderThread>();
        clientWriterThreads = new ArrayList<ClientWriterThread>();
        clients = new ArrayList<Client>();

        blacklist = new File("blacklist.txt");

        printWelcome(port);
        ServerSocket serverSocket = new ServerSocket(port);
        nbrClient = 0;
        isAlive = true;
        while (isAlive) {
            Socket socket = serverSocket.accept();

            //Check if blacklisted

            if (isIPBlacklisted(socket.getInetAddress().toString(), this.blacklist)) {
                PrintWriter pw = new PrintWriter(socket.getOutputStream());
                pw.println("/blacklisted;");
                logger.log(Level.WARNING, "L'IP " + socket.getInetAddress().toString() + " a essayé de se connecter mais était blacklistée.");
                pw.flush();
                continue;
            }


            int clientId = nbrClient++;
            Client client = new Client(clientId, socket);
            clients.add(client);

            ClientReaderThread clientReaderThread = new ClientReaderThread(socket, this, client);

            Thread readerThread = new Thread(clientReaderThread);
            readerThread.start();

            ClientWriterThread clientWriterThread = new ClientWriterThread(socket, client);

            Thread writerThread = new Thread(clientWriterThread);
            writerThread.start();

            synchronized (clientReaderThread) {
                clientReaderThreads.add(clientReaderThread);
            }

            synchronized (clientWriterThread) {
                clientWriterThreads.add(clientWriterThread);
            }

            logger.info("Un client s'est connecté : " + clientId);

        }

        serverSocket.close();
        logger.info("Server has stopped");
    }

    static private void printWelcome(Integer port) {
        String info = "\r\nDomDraw Server : Par Dominique Bégin et Dominique Septembre\r\n";
        try {
            info += "Démarre sur : " + InetAddress.getLocalHost() + ":" + port.toString() + "\r\n";
        } catch (UnknownHostException e) {
            logger.log(Level.WARNING, e.toString());
        }
        logger.log(Level.INFO, info + "----------");
    }

    public void changeUsername(String username, Client client) {
        int clientId = client.getId();
        boolean exists = false;
        for (Client checkClient : clients) {
            if (checkClient.getUsername().equalsIgnoreCase(username)) {
                exists = true;
            }
        }

        ClientWriterThread clientWriter = clientWriterThreads.get(clientId);

        synchronized (clientWriter) {
            if (exists) {
                logger.info("SERVER:Username " + username + " already exists ! User : " + client.getId());
                clientWriter.sendMessage("/username;error");
                clientWriter.sendMessage("SERVER:Username " + username + " already exists !");
            } else {
                clientWriter.sendMessage("/username;ok");
                logger.info("SERVER:Username change for : " + username + " : user " + client.getId());
                clientWriter.sendMessage("SERVER:Username change for : " + username + " : user " + client.getId());
                client.setUsername(username);
            }
        }
    }

    public void onMessage(String readLine) {
        logger.info(readLine);
        synchronized (clientWriterThreads) {
            for (ClientWriterThread clientWriterThread : clientWriterThreads) {
                clientWriterThread.sendMessage(readLine);
            }
        }
    }

    public void removeClient(Client client) {
        logger.info(client.getUsername() + " s'est déconnecté !");
        onMessage("SERVER:" + client.getUsername() + " s'est déconnecté !");
        clients.remove(client);
        nbrClient--;
    }

    public void stop() {
        isAlive = false;
    }

    private boolean isIPBlacklisted(String address, File blacklistFile) {
        try {
            String line;
            BufferedReader br = new BufferedReader(new FileReader(blacklistFile));

            while ((line = br.readLine()) != null) {
                if (line.equals(address)){
                    return true;
                }
            }
        } catch (IOException e) {
            System.out.println("Error while accessing/parsing blacklist file");
        }
        return false;
    }

    public void blacklist(Client client){
        try {
            FileWriter fw = new FileWriter(blacklist, true);
            fw.write(client.getSocket().getInetAddress().toString());
            fw.flush();
            fw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
