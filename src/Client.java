//  LAN ConnectFour
//  Created by Amir Afzali
//  Copyright © 2018 Amir. All rights reserved.

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class Client {
    boolean connected;
    boolean start;

    private BufferedReader clientReader;
    private BufferedReader serverReader;
    private PrintWriter clientOutputServer;
    private Socket clientSocket;

    String name;
    private String message="";

    public ConnectFour connectFour;
    private Client clientInstance = this;
    private Server server;
    private int timeout =0;

    public static void main(String[] args) {
        Client client = new Client();
    }
    public Client() {
        //Initialize connection
        initializeConnection();
        while(true) {
            send(message);
            if(connected) {
                if (name == null) {
                    name = "Red";
                }
                initializeGame();
                break;
            }
        }
    }

    public void send(String message) {
        try {
            if (message.equals("")) {
                clientOutputServer.println("UPDATE");
            } else {
                clientOutputServer.println(message);
                System.out.println("Client says " + message);
            }
            clientOutputServer.flush();
            receive();
        } catch (Exception e) {}
    }
    public void receive() {
        String response;
        try{
            response = serverReader.readLine();
            System.out.println("Server Response : " + response);

            timeout++;
            if(timeout==70) {
                connectFour.kill();
            }

            if(response.startsWith("START")) {
                timeout =0;
                start=true;
            } else if(response.startsWith("POS")) {
                timeout =0;
                connectFour.checkDrop(response);
            }else if(response.startsWith("WAIT")) {
                timeout =0;
            }
        }
        catch(IOException e){
            e.printStackTrace();
            System.out.println("Socket read Error");
        }

    }
    public void initializeConnection() {
        //make client reading buffer
        clientReader = new BufferedReader(new InputStreamReader(System.in));
        try {
            //Try to establish connection to existing server
            System.out.println("Client: Trying to connect to server...");
            clientSocket = new Socket(InetAddress.getLocalHost(), 3000);
            serverReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            clientOutputServer = new PrintWriter(clientSocket.getOutputStream());
            System.out.println("Client: Connection Established");
            clientInstance.connected=true;
        } catch (Exception e) {
            //If no server exists, make one. Also make this player blue
            System.out.println("Client: No server found, making one...");
            server = new Server(this);
            name="Blue";
            initializeConnection();
        }
    }
    private void initializeGame() {
        Thread gameThread = new Thread() {
            public void run() {
                connectFour = new ConnectFour(clientInstance);
            }
        }; gameThread.run();
    }
}