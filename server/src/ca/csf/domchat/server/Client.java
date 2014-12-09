package ca.csf.domchat.server;

import java.net.Socket;

public class Client {

    private int id;
    private String username;

    public Socket getSocket() {
        return socket;
    }

    private Socket socket;

    public Client(int id, Socket socket){
        this.id = id;
        this.socket = socket;
        this.username = Integer.toString(id);
    }

    public void setUsername(String username){
        this.username = username;
    }

    public String getUsername(){
        return this.username;
    }

    public int getId() {
        return id;
    }
}
