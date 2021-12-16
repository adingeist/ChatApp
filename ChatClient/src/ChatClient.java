/*
 * Author: Adin Geist
 * Description: The mediator between the GUI and the server. Provides implementation to send and receive messages
 *              from the server.
 */

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class ChatClient {
    private final String serverIp;
    private final int port;
    private Socket socket;
    private BufferedReader inputStream;
    private OutputStream outputStream;
    private UserStatusListener statusListener;
    private MessageListener messageListener;
    private String login;

    // Constructor that stores the ip and port to a field
    public ChatClient(String ip, int port) {
        this.serverIp = ip;
        this.port = port;
    }

    // Start reading messages in a loop forever in a new thread, so it doesn't block the program
    public void startMessageReader() {
        Thread t = new Thread() {
            @Override
            public void run() {
                readMessageLoop();
            }
        };
        t.start();
    }

    // Read messages forever and handle them according to the command
    private void readMessageLoop() {
        try {
            String line;
            while ((line = inputStream.readLine()) != null) { // Blocking!
                String[] tokens = line.split(" "); // Split the command and arguments
                if (tokens.length > 0) {
                    String cmd = tokens[0];
                    if ("online".equalsIgnoreCase(cmd)) {
                        // Received: online user
                        handleOnline(tokens);
                    } else if ("offline".equalsIgnoreCase(cmd)) {
                        // Received: offline user
                        handleOffline(tokens);
                    } else if ("msg".equalsIgnoreCase(cmd)) {
                        String userSentMsg = tokens[1];
                        // Received: msg user msgBody
                        String msg = line.substring(tokens[0].length()+1 + tokens[1].length()+1);
                        handleMessage(userSentMsg, msg);
                    }
                }
            }
        } catch (Exception ex) {
            try {
                // Try to close the socket if it fails reading a message
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Call the message listener that was set to this client
    private void handleMessage(String login, String msg) {
        messageListener.onMessage(login, msg);
    }

    // Call the offline status listener that was set to this client
    public void handleOffline(String[] tokens) {
        String login = tokens[1];
        statusListener.offline(login);
    }

    // Call the online status listener that was set to this client
    private void handleOnline(String[] tokens) {
        String login = tokens[1];
        statusListener.online(login);
    }

    // Send a login command to the server with the givne login
    public boolean login(String login) {
        String cmd =  "login " + login + "\n";
        String response = "";
        try {
            // The server will respond if the login is ok or not
            outputStream.write(cmd.getBytes(StandardCharsets.UTF_8));
            response = inputStream.readLine(); // blocking
        } catch (IOException e) {
            return false; // Login was bad
        }

        if (response.equalsIgnoreCase("login ok")) {
            this.login = login; // set this client's login to the given login
            return true; // successful login
        } else {
            return false; // failed login
        }
    }

    // Connect to the socket and will return true if successful
    public boolean connect() {
        try {
            this.socket = new Socket(serverIp, port); // Connect a socket to a given ip and port
            // Set the clients input and output streams
            this.outputStream = socket.getOutputStream();
            InputStream serverIn = socket.getInputStream();
            this.inputStream = new BufferedReader(new InputStreamReader(serverIn));
            return true; // Connected ok
        } catch (IOException e) {
            return false; // Failed to connect
        }
    }

    // Setter method to set the status listener
    public void setStatusListener(UserStatusListener userStatusListener) {
        this.statusListener = userStatusListener;
    }

    // Setter method to set the message listener
    public void setMessageListener(MessageListener messageListener) {
        this.messageListener = messageListener;
    }

    // Send to the server a message command that will be sent to all other users the provided message
    public void msg(String body) throws IOException {
        String cmd = "msg " + body + "\n";
        outputStream.write(cmd.getBytes());
    }

    // Logoff the server is the user exits out of the window unexpectedly
    public void unexpectedLogoffMsg() throws IOException {
        if (this.login != null) {
            String cmd = "logoff " + login + "\n";
            outputStream.write(cmd.getBytes());
        }
    }
}
