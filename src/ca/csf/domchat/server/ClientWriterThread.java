package ca.csf.ClientServer.Server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ClientWriterThread implements Runnable{
    private Socket socket;
    private boolean isAlive;

    private List<String> messageList;

    public ClientWriterThread(Socket socket) {
        this.socket = socket;
        this.messageList = new ArrayList<String>();
    }

    @Override
    public void run(){
        isAlive = true;
        try{
            PrintWriter printWriter = new PrintWriter(socket.getOutputStream());
            while(isAlive){
                synchronized (messageList){
                    while(!messageList.isEmpty()){
                        printWriter.println(messageList.remove(0));
                        printWriter.flush();
                    }

                    messageList.wait();
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            isAlive = false;
            try{
                socket.close();
            } catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    public void sendMessage(String message){
        synchronized (messageList){
            messageList.add(message);
            messageList.notify();
        }
    }

    public void stop(){
        isAlive = false;

        messageList.notify();
    }
}
