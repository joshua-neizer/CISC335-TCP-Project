import java.net.*;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;


// The Sever program for the TCP Network
class TCP_Server{
    // Variable instantiation
    private int maxClients;
    public ServerSocket serverSocket;
    public Socket socket;
    private Client[] Clients;
    public ArrayList<String[]> connectionLog;

    /**
    * Runs an infinite loop waiting for connections to start and end.
    *
    * @param  mc        the integer maximum number of concurrent clients on the server
    * @param  port      the integer port the server will host the connection on
    */ 
    TCP_Server(int mc, int port){
        // Setting up the attributes for the server
        System.out.println("Starting Up...\n");
        this.maxClients = mc;
        this.Clients = new Client [mc];
        this.connectionLog = new ArrayList<String[]>();

        // Creates the socket for the server 
        try {
            this.serverSocket = new ServerSocket(port);
        } catch (Exception e) {
            System.out.printf("ERROR: Could not establish Server Socket\n");
            System.exit(1);
        }

        // Multi-threads every client-server TCP connection
        for (int i=0; i < mc; i++){
            System.out.printf("Server> Waiting for Client#0%d\n", i+1);
            this.start_client(i);
        }

        // Connects to a new client
        this.run_server();
    }
    

    /**
    * Establishes a new TCP client connection and starts it as a thread
    *
    * @param  i   type integer specifies which client is being started
    */ 
    public void start_client(int i){
        // Tries to establish a TCP connection
        try {
            socket = serverSocket.accept();
        } catch (Exception e) {
            // If the thread doesn't start successfully, the server will shutdown
            this.distribute_messages("Server is shutting down...");
            e.printStackTrace();
            try {TimeUnit.SECONDS.sleep(3);} catch (Exception E) {}
            System.exit(1);
        }

        this.Clients [i] = new Client(socket, i);
        System.out.printf("Server> Client#0%d connection successfully established\n", i+1);

        // If successfully established, the client thread starts
        this.Clients [i].start();
    }


    /**
    * Runs an indefinite checking for closed connections and starting new ones
    */ 
    public void run_server(){
        while (true){
            // Stalls 3 seconds to make sure new connections can establish
            try {TimeUnit.SECONDS.sleep(3);} catch (Exception e) {}

            // Checks every thread for a closed connection
            for (int i=0; i < this.maxClients; i++){
                // If the connection has closed or inactive, it is replaced
                if (!this.Clients [i].active){
                    // Previous thread is stopped
                    this.Clients [i].stop();

                    // Adds the connection log to ongoing log list
                    this.connectionLog.add(this.Clients[i].log);
                    System.out.printf("Server> Client#0%d successfully closed connection\n", i+1);
                    System.out.printf("Server> Waiting for new Client#0%d\n", i+1);

                    // Connects to a new client
                    this.start_client(i);
                    
                }
            }
        }
    }


    /**
    * Sends a message to every active client on the network
    *
    * @param  message   the String message to distribute
    */ 
    public void distribute_messages(String message){
        // Iterates over all possible clients and sends a message if it is active
        for (int i = 0; i < this.maxClients; i++){
            if (Clients [i].active)
                Clients [i].write_message("Server> " + message);
        }
    }


    public static void main(String[] args){
        new TCP_Server(3, 5069);
    }

}