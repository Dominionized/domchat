package ca.csf.domchat.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client {

    private String address;
    private int port;

    private String username;

    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;

    public static void main(String[] args) {
        new Client();

    }

    public Client(){

        Scanner scanner = new Scanner(System.in);

        try {
            System.out.println("Address : ");
            address = scanner.nextLine();

            System.out.println("Port : ");
            port = Integer.parseInt(scanner.nextLine());


            socket = new Socket(address, port);
            output = new PrintWriter(socket.getOutputStream());
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            do {

                System.out.println("Choose a username (cannot contain ; or :)");
                username = scanner.nextLine();

                if (username.contains(";") || username.contains(":")){
                    continue;
                }

                output.println("/username;" + username + ";");
                output.flush();

                if (input.readLine().startsWith("/username;error")){
                    System.out.println("Username already in use");
                    continue;
                }
                else if (input.readLine().startsWith("/username;ok")){
                    break;
                }

            } while (true);

            System.out.println("Connected to " + address + ":" + Integer.toString(port) + " as " + username);

            Thread imThread = new IncomingMessagesThread(socket, this);
            imThread.start();

            while (true){
                output.println(scanner.nextLine());
                output.flush();
            }

        } catch (NumberFormatException numberFormatException) {
            System.out.println("Bad port number");
        } catch (UnknownHostException e) {
            System.out.println("Unknown host");
        } catch (IOException e) {
            System.out.println("Cannot connect");
        }
        finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void onReadLine(String line){

        // EXEMPLE
        // dseptembre:Ceci est mon message

       if (line.startsWith("/")){
           String command = line.substring(line.charAt(1), line.indexOf(";"));
            switch(command){
            }
        }

        String username = line.substring(0, line.indexOf(":"));
        String message = line.substring(line.indexOf(":") + 1);

        System.out.println("[" + username + "] " + message);
    }
}