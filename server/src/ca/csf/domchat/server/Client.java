package ca.csf.domchat.server;

public class Client {

    private int id;
    private String username;

    public Client(int id){
        this.id = id;
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
