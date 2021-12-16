/*
 * Author: Adin Geist
 * Description: Accepts new clients onto the server and creates a new socket and thread to handle future requests.
 */

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class Server extends Thread {
    private int port;
    private ArrayList<ClientController> controllerList = new ArrayList<>();

    // Constructor takes in a port, which is set to this server's field
    public Server(int port) {
        this.port = port;
    }

    @Override
    public void run () {
        try {
            // Connect a ServerSocket to the provided port
            ServerSocket serverSocket = new ServerSocket(this.port);
            String ip = InetAddress.getLocalHost().getHostAddress();
            // Print out the ip and port
            System.out.println("Server listening at: " + ip + ":"+ port);
            // Continuouslly check for new connections
            while (true) {
                // Whenever a user connects to port, a socket is formed
                Socket socket = serverSocket.accept(); // blocking!
                // Create a new client controller and pass the server object and the socket to it
                ClientController clientController = new ClientController(this, socket);
                controllerList.add(clientController);
                // Handle future requests with this client on a separate thread
                clientController.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Getter method that returns the list of controllers to the caller
    public List<ClientController> getControllerList () {
        return controllerList;
    }

    // Method that removes the gives controller
    public void removeController(ClientController clientController) {
        controllerList.remove(clientController);
    }
}
