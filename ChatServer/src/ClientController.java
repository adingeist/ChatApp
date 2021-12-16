/*
 * Author: Adin Geist
 * Description: Handles interactions between a single client and the server.
 */

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ClientController extends Thread {
    private Server server;
    private Socket socket;
    private String login = null;
    private BufferedReader inputStream;
    private OutputStream outputStream;

    // Constructor that accepts a server and socket, which has its input and output streams extracted to fields
    public ClientController(Server server, Socket socket) {
        try {
            this.server = server;
            this.socket = socket;
            InputStream inputStream = socket.getInputStream();
            this.inputStream = new BufferedReader(new InputStreamReader(inputStream));
            this.outputStream = socket.getOutputStream();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // The run method, which will be run in a separate thread when called.
    @Override
    public void run() {
        try {
            String line;
            // Read the next line from the user forever
            while ((line = inputStream.readLine()) != null) { // Blocking!
                String[] tokens = line.split(" "); // Split the user's input into tokens on each space
                if (tokens.length > 0) { // Make sure the message has a token
                    switch (tokens[0].toLowerCase()) { // tokens[0] is the command
                        case "msg":
                            String body = line.substring(tokens[0].length()+1);
                            // Send the message body to all other controllers connected to the server
                            handleMsg(body);
                            break;
                        case "login":
                            // Send to all controllers on the server that THIS user connected
                            handleLogin(tokens);
                            break;
                        case "logoff":
                            // Send to all controllers on the server that THIS user disconnected
                            handleLogoff();
                            break;
                        default: // Tell the user the command wasn't understood
                            outputStream.write(("Unknown command: \""+tokens[0]+"\"\n").getBytes(StandardCharsets.UTF_8));
                    }
                }
            }
        } catch (IOException e) {
            System.out.println(login + " disconnected.");; // Couldn't read line from the user
        }
    }

    // Command format:   msg body
    // Method that sends the given message to all connected controllers
    private void handleMsg(String body) {
        List<ClientController> controllerList = server.getControllerList();
        // Send a message to each controller connected
        for (ClientController controller : controllerList) {
            String msg = "msg " + login + " " + body + "\n";
            controller.send(msg);
        }
        // Print to the console the message send to all
        System.out.println(login + " " + body);
    }

    // Send a message to THIS client
    private void send(String msg) {
        if (login != null) {
            try {
                // Try to write out to the client the message
                outputStream.write(msg.getBytes(StandardCharsets.UTF_8));
            } catch (Exception e) {
                // If the message can't send, the connection is broken. Disconnect the user from the server.
                System.out.println("Broken " + this.login);
                server.removeController(this);
            }
        }
    }

    // Command format:  login username
    // Checks to see if the username is valid and isn't already taken. If so, logins the user.
    private void handleLogin(String[] tokens) {
        String login = "";
        // Ensure the input is valid
        if (tokens.length == 2) {
            login = tokens[1];
        }

        List<ClientController> controllerList = server.getControllerList();

        // Check if the login is okay using regex and checking against all others connected clients' login names
        boolean loginOk = true;
        try {
            for (ClientController clientController : controllerList) {
                if (
                        (this.socket != clientController.socket && login.equalsIgnoreCase(clientController.login)) ||
                                login.isEmpty() ||
                                login.isBlank() ||
                                !login.matches("^[A-Za-z0-9_]*$") // Must contain alphanumerics and _    Nothing else
                ) {
                    this.outputStream.write("Username is invalid.\n".getBytes(StandardCharsets.UTF_8));
                    loginOk = false;
                    break;
                }
            }

            if (loginOk) {
                this.login = login;
                System.out.println("User logged in: " + login + "  |  " + "IP: " + this.socket.getInetAddress());
                this.outputStream.write("login ok\n".getBytes(StandardCharsets.UTF_8));

                // send current user all other online logins
                for(ClientController clientController : controllerList) {
                    if (clientController.getLogin() != null && !login.equals(clientController.getLogin())) {
                        String msg2 = "online " + clientController.getLogin() + "\n";
                        send(msg2);
                    }
                }

                // Send to other user's this user logged on
                String onlineMsg = "online " + login + "\n";
                for(ClientController clientController : controllerList) {
                     clientController.send(onlineMsg);
                }
            }
        } catch (IOException e) {
            System.out.println("Login failed for: " + login);
            e.printStackTrace();
        }
    }

    // Command format: logoff user
    // Disconnects a client's socket from the server and removes the controller from the server's controller list
    private void handleLogoff() {
        // Remove this controller from the server's controller list
        server.removeController(this);
        List<ClientController> controllerList = server.getControllerList();

        // send other online users current user's status
        String onlineMsg = "offline " + login + "\n";
        for(ClientController clientController : controllerList) {
            if (!login.equals(clientController.getLogin())) {
                clientController.send(onlineMsg);
            }
        }
        try {
            // Try to close the input and socket
            inputStream.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Return the login for this controller
    public String getLogin() {
        return login;
    }
}
