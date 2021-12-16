/*
 * Author: Adin Geist
 * Description: 3 input form to connect to and ip and port server with a given username.
 */

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GUILogin {
    // Fields and window elements that are used in the .form file
    private JFrame frame;
    private JTextField ipField;
    private JTextField portField;
    private JTextField usernameField;
    private JButton connectBtn;
    private JLabel ipLabel;
    private JLabel portLabel;
    private JLabel status;
    private JPanel mainDiv;
    private ChatClient client = null;

    // Constructor used to set up the login window
    public GUILogin () {
        this.frame = new JFrame("ChatApp"); // Title the window ChatApp
        frame.setContentPane(this.mainDiv); // set the content of the window to the mainDiv
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Exit the process when the window is exited
        frame.setSize(300,200); // 300x200px window size
        frame.setVisible(true); // make the window visible

        // Pre-fill the first two form elements with localhost and the default port
        ipField.setText("127.0.0.1");
        portField.setText("8818");

        // When the connect button is pressed it'll call the connect() method
        connectBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                connect();
            }
        });
        // When the enter key is pressed it'll call the connect() method
        usernameField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                connect();
            }
        });
    }

    // Main method that starts the GUI
    public static void main(String[] args) {
        new GUILogin();
    }

    // Connects to the server and tries to login the user
    private void connect() {
        // Get the form values and save into local variables
        String ip = ipField.getText();
        String port = portField.getText();
        String username = usernameField.getText();

        status.setText("Connection status: trying..."); // inform the user on the connection status

        // Connect a client if not done so already
        if (this.client == null) {
            try {
                ChatClient client = new ChatClient(ip, Integer.parseInt(port));
                if (client.connect()) {
                    status.setText("Connection status: success");
                    this.client = client;
                } else
                    status.setText("Connection status: failed");

            } catch (Exception e) {
                status.setText("Connection status: failed");
                e.printStackTrace();
            }
        }

        // Once connected set the username
        if (this.client != null) {
            if (client.login(username)) { // Login the user, return true if successful
                status.setText("Connection status: logged in");
                new GUIApp(this.client, username); // Creates the GUI window
                this.frame.dispose(); // Closes the login window
            } else {
                status.setText("Connection status: bad username");
            }
        }
    }
}
