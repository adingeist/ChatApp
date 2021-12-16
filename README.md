# ChatApp
Multithreaded chat server and client user interface that allows people to communicate over a local network. 
![ChatApp Preview](https://i.imgur.com/h7hE2zG.png)

# How does it work?
Uses websockets to perform live communication on a local network. The server accepts new connections and creates a new thread to handle future requests to a client that connects. The server listens in each thread's input stream and relays messages to all clients connected to the server. Whenever a client logs on, disconnects, or messages, everyone connected are informed.

# How do I start the server?
Navigate to the downloaded file, WChatServer.jar, in a terminal window.
Run the command:
java -jar WChatServer.jar

# How do I run a client instance?
Double click the downloaded WChatClient.jar file.

# How do I connect my client to the server?
The server will tell you where it is listening for new connections.
![Server connection message](https://i.imgur.com/3fIIDYb.png)

Enter the IP and port in the chat client along with your choice of username.
![Client connection screen](https://i.imgur.com/h7hE2zG.png)

