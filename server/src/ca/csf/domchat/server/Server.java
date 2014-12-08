package ca.csf.domchat.server;

import java.io.IOException;
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
    private List<Client> clients;
    int nbrClient;

    private boolean isAlive;

    //TODO domdraw ?
    protected static Logger logger = Logger.getLogger("ca.csf.domdraw.server");

    public static void main(String[] args) throws IOException {
        Handler fh = new FileHandler("server.log", false);
        logger.addHandler(fh);

        System.out.print("Choisissez le port de démarrage serveur (Defaut 4444) : ");
        Scanner scanner = new Scanner(System.in);
        String changePort = scanner.nextLine();
        if(!changePort.isEmpty()){
           port = Integer.parseInt(changePort);
        }
        new Server();
    }

    public Server() throws IOException{
        clientReaderThreads = new ArrayList<ClientReaderThread>();
        clientWriterThreads = new ArrayList<ClientWriterThread>();
        clients = new ArrayList<Client>();

        printWelcome(port);
        ServerSocket serverSocket = new ServerSocket(port);
        nbrClient = 0;
        isAlive = true;
        while(isAlive){
            Socket socket = serverSocket.accept();

            int clientId = nbrClient++;
            Client client = new Client(clientId);
            clients.add(client);

            ClientReaderThread clientReaderThread = new ClientReaderThread(socket, this, client);

            Thread readerThread = new Thread(clientReaderThread);
            readerThread.start();

            ClientWriterThread clientWriterThread = new ClientWriterThread(socket);

            Thread writerThread = new Thread(clientWriterThread);
            writerThread.start();

            synchronized(clientReaderThread){
                clientReaderThreads.add(clientReaderThread);
            }

            synchronized (clientWriterThread){
                clientWriterThreads.add(clientWriterThread);
            }

            logger.info("Un client s'est connecté : " + clientId);

        }

        serverSocket.close();
        logger.info("Server has stopped");
    }

    static private void printWelcome(Integer port)
    {
        String info = "\r\nDomDraw Server : Par Dominique Bégin et Dominique Septembre\r\n";
        try {
            info += "Démarre sur : " + InetAddress.getLocalHost() + ":" + port.toString() + "\r\n";
        } catch(UnknownHostException e){
            logger.log(Level.WARNING, e.toString());
        }
        logger.log(Level.INFO, info + "----------");
    }

    public void changeUsername(String username, Client client){
        int clientId = client.getId();
        boolean exists = false;
        for (Client checkClient : clients){
            if(checkClient.getUsername().equalsIgnoreCase(username)){
                exists = true;
            } else {
                client.setUsername(username);
            }
        }

        ClientWriterThread clientWriter = clientWriterThreads.get(clientId);

        synchronized (clientWriter){
            if(exists) {
                logger.info("SERVER:Username " + username + " already exists ! User : " + client.getId());
                clientWriter.sendMessage("SERVER:Username " + username + " already exists !");
                clientWriter.sendMessage("/username;error");
            } else {
                logger.info("SERVER:Username change for : " + username + " : user "+ client.getId());
                clientWriter.sendMessage("SERVER:Username change for : " + username + " : user "+ client.getId());
                clientWriter.sendMessage("/username;ok");
            }
        }
    }

    public void onMessage(String readLine){
        logger.info(readLine);
        synchronized (clientWriterThreads){
            for(ClientWriterThread clientWriterThread : clientWriterThreads){
                clientWriterThread.sendMessage(readLine);
            }
        }
    }

    public void removeClient(Client client){
        logger.info(client.getUsername() + " s'est déconnecté !");
        onMessage("SERVER:" + client.getUsername() + " s'est déconnecté !");
        clients.remove(client);
        nbrClient--;
    }

    public void stop(){
        isAlive = false;
    }
}
