/*
 * Author: Adin Geist
 * Description: Displays users online and enables messaging communication.
 */

import javax.swing.*;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GUIApp implements MessageListener, UserStatusListener {
    // All fields must be included, even if not used because of the .form file
    private String login;
    private ChatClient client;
    private JPanel mainDiv;
    private JEditorPane messagesText;
    private JButton sendButton;
    private JTextField inputField;
    private JLabel usersOnlineLabel;
    private JLabel messagesLabel;
    private JPanel inputDiv;
    private JPanel leftPanel;
    private JPanel rightPanel;
    private JTextArea usersOnlineText;
    // Hold a list of all online users to be displayed in the left panel
    private ArrayList<String> onlineUsers = new ArrayList<>();

    // Constructor that builds the application window
    public GUIApp(ChatClient client, String login) {
        this.client = client;
        this.login = login;

        JFrame frame = new JFrame("ChatApp"); // Title the window ChatApp
        frame.setContentPane(this.mainDiv); // Set the content of the pain to the mainDiv element
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // Do not close on exit because we must disconnect first
        frame.setSize(600,450); // 600x450px window size
        frame.setVisible(true); // Make the frame visible
        // Whenever the window closes, the connection will close, logoff, and then exit the process
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                String[] tokens = {"logoff", login};
                client.handleOffline(tokens);
                System.out.println(login + " logged off unexpectedly");
                try {
                    client.unexpectedLogoffMsg();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                frame.dispose();
                System.exit(0);
            }
        });

        // Since this class implements the MessageListener and UserStatusListener, we can set the chat clients
        // listeners to this since this class has implementation for those commands
        client.setMessageListener(this);
        client.setStatusListener(this);
        client.startMessageReader(); // Start listening for messages in a new thread

        // Sends a message whenever the send button is pressed
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                send();
            }
        });
        // Sends a message whenever enter is pressed inside the input field
        inputField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                send();
            }
        });

        // Set the CSS styles in the HTML messages text box
        HTMLEditorKit kit = new HTMLEditorKit();
        StyleSheet styleSheet = kit.getStyleSheet();
        styleSheet.addRule(".serverMsg {color: red;}");
        // User Courier New as the default font
        styleSheet.addRule("p {font-family: Courier New, Arial, Helvetica, sans-serif;}");
        Document doc = kit.createDefaultDocument();
        messagesText.setDocument(doc);
    }

    // Sends a message to all other users
    private void send() {
        String msg = inputField.getText(); // Get text from the input field
        if (msg == null || msg.isEmpty()) return; // Don't send an empty message
        try {
            client.msg(msg); // Use the client to send the message to the server
        } catch (IOException e) {
            onMessage("WARN MSG", "Failed to send."); // Paste a failed to send message in red in the chat
        }
    }

    // Pastes received messages into the messages HTML chat box
    @Override
    public void onMessage(String username, String body) {
        // Don't format < and > as HTML in the user's message
        body = body.replaceAll("<","&lt;"); // someone typing in <h1>big</h1> won't be big now
        body = body.replaceAll(">","&gt;");

        // Get the HTML of the text box already
        String currentText = messagesText.getText();
        inputField.setText(""); // Reset the text input field

        // Extract only the body of the HTML
        Pattern patt = Pattern.compile("(?<=body>).*(?=</body>)", Pattern.DOTALL);
        Matcher matcher = patt.matcher(currentText);
        String matchedString;
        if (matcher.find()) {
            matchedString = matcher.group(); // Get all the comments from the HTML
        } else {
            matchedString = "";
        }
        // Append the message to the body section of the html and set it to the messageText
        if (username.equals("WARN MSG"))
            messagesText.setText(matchedString + "<p class=\"serverMsg\" style=\"margin-top: 2px;\">" + body + "</p>");
        else
            messagesText.setText(matchedString + "<p style=\"margin-top: 2px;\"><b>" + username + "</b> " + body + "</p>");
    }

    // Whenever a user comes online add them to the list and update it
    @Override
    public void online(String login) {
        onlineUsers.add(login);
        updateOnlineList();
    }

    // Whenever a user goes offline remove them from the list and update it
    @Override
    public void offline(String login) {
        onlineUsers.removeIf(username -> username.equals(login));
        updateOnlineList();
    }

    // Re-renders the online list with the current list of users online
    private void updateOnlineList() {
        String onlineText = "";
        for (String user : onlineUsers) {
            onlineText = onlineText + user + "\n";
        }
        usersOnlineText.setText(onlineText);
    }
}
