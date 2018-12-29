//  LAN ConnectFour
//  Created by Amir Afzali
//  Copyright © 2018 Amir. All rights reserved.

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    Server serverInstance = this;
    Client clientInstance;


    Socket connectSocket;
    ServerSocket serverSocket;

    int totalClients=0;
    ConnectionThreads[] clients = new ConnectionThreads[2];

    private boolean warned = false;
    public String pos="";

    public static void main(String[] args) {
    }
    Server(Client client) {
        this.clientInstance = client;
        //Establish server
        establishServer();
        //Form new connections
        newConnection();
    }

    private void establishServer() {
        //Attempt to bind to port
        System.out.println("Server: Waiting for client...");
        try {
            serverSocket = new ServerSocket(3000);
        } catch (Exception e) {
            System.out.println("Server: Port busy");
            clientInstance.connectFour.kill();
        }
    }
    public void newConnection() {
        //Create new thread for every connection
        Thread thread = new Thread(){
            public void run(){
                while (true) {
                    try {
                        connectSocket = serverSocket.accept();
                        System.out.println("Server: Connection made with client!");
                        ConnectionThreads client = new ConnectionThreads(connectSocket, totalClients, serverInstance);
                        clients[totalClients] = client;
                        totalClients += 1;
                        client.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                        System.out.println("Problem with connection!");
                        clientInstance.connectFour.kill();
                    }
                    //If 2 clients connected, activate game
                    if(totalClients==2 && !warned) {
                        for (ConnectionThreads client : clients) {
                            System.out.println('x');
                            client.started=true;
                        }
                    }
                }
            }
        };
        thread.start();
    }
}
class ConnectionThreads extends Thread {
    private Socket connected;
    private int id;
    private Server server;

    public boolean started = false;

    private String clientComment;
    private BufferedReader inputRead;
    private PrintWriter output;

    public ConnectionThreads(Socket connected, int id, Server server) {
        this.connected = connected;
        this.id = id;
        this.server = server;
    }
    public void run() {
        while(true) {
            communicate();
        }
    }
    public void communicate() {
        //Take input, respond with according output
        System.out.println("My thread: "+ id);
        try {
            inputRead = new BufferedReader(new InputStreamReader(connected.getInputStream()));
            output = new PrintWriter(connected.getOutputStream());
        } catch (IOException e) {
            System.out.println("Problem making streams");
        }

        try {
            clientComment = inputRead.readLine();
            System.out.println(clientComment);
            //POS indicates game state update
            if(clientComment.startsWith("POS")) {
                server.pos=clientComment;
            }  else if(clientComment.startsWith("RESET")) {
                //Reset indicated new game
                server.pos = "";
            }
            if(server.pos.equals("")) {
                //If position data is blank, tell client to wait
                output.println("WAIT");
            } else {
                //Otherwise distribute position data
                output.println(server.pos);
            }
            if(started) {
                //If game can start, tell clients
                output.println("START");
                started=false;
            }
            output.flush();

        } catch (Exception e) {
            //Disconnect --> kill
            System.out.println("Client terminated");
            server.clientInstance.connectFour.kill();
        }
    }
}

