/*
 * Author: Adin Geist
 * Description: Main function for the chat server. This is the single process all clients communicate to.
 * Tutorial followed and made significant adaptions to: https://youtu.be/cRfsUrU3RjE
 */

import java.util.Scanner;

public class ServerMain {
    public static void main(String[] args) {
        // Create a scanner that will obtain input from the user
        Scanner scanner = new Scanner(System.in);
        // Ask the server admin which port the server should run on
        int port = 8818;
        System.out.println("Enter port for server to run on (press enter for default, 8818):\n");
        String inp = scanner.nextLine();
        // If the port is empty run the server on the default port
        if (!inp.isEmpty()) {
            try {
                port = Integer.parseInt(inp.split(" ")[0]);
            } catch (Exception e) { // Unable to parse an integer indicates bad input
                port = 8818;
                System.out.println("Port provided wasn't an integer. Starting on port 8818.");
            }
        }
        // Start a new server on the provided or default port
        Server server = new Server(port);
        server.start();
    }
}
