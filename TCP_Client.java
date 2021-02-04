import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

// The Client program for the TCP connection
public class TCP_Client {
    // Attributes used for socket connection
    public PrintWriter out;
    public BufferedReader in;
    public String ip;
    public int port;
    public Socket socket;

    // Attributes used for client information
    public String name;
    public boolean active;

    // Attributes used for file transfers
    public DataInputStream fileIn;
    Scanner dataInput;
    public static int BYTE_SIZE = 4*1024;

    /**
    * Handles all client side communication with the server
    *
    * @param  ip    the ip address for the server
    * @param  port  the port number for the socket connection
    */ 
    TCP_Client(String ip, int port){
        // Initializing the socket connection attributes
        this.ip = ip;
        this.port = port;
        this.active = true;
        this.dataInput = new Scanner(System.in);

        // Tries to establish a connection with the server
        // If the connection fails, it is put in a 'waiting room'
        try {
            this.socket = new Socket(this.ip, this.port); 
        } catch (Exception e) {
            System.out.printf("ERROR: Could not establish connection with server\n");
            this.waiting_room();
        }
        
        // Established IO stream with the server, exits if it fails
        try {
            this.out = new PrintWriter (this.socket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader (this.socket.getInputStream()));
            this.fileIn = new DataInputStream(socket.getInputStream());
        } catch (Exception e) {
            System.out.printf("ERROR: Could not establish read-write connection with server\n");
            System.exit(1);
        }
        
        // Sets up the connection for the server
        this.setup();

        // Runs the client connection
        this.run();
    }

    /**
    * Message input handler that reads the incoming message from the server
    *
    * @return  returns the message received from the server
    */
    public String get_message(){
        // Tries to read the message, if unsuccessful returns null
        try {
            return this.in.readLine();
        } catch (Exception e) {
            return null;
        }
        
    }

    /**
    * Message output handler that sends an outgoing message to the server
    *
    * @param  message  the message to send to the server
    */ 
    public boolean write_message(String message){
        try {
            // TimeUnit.SECONDS.sleep(1);
            this.out.println(message);
            this.out.flush();
            return true;

        } catch (Exception e) {
            System.out.printf("ERROR: Could not write input\n");
            return false;
        }
    }

    
    /**
    * Returns the input from the user
    *
    * @param  name  the name associated with the client
    * @return       the input from the user
    */ 
    public String get_input(String name){
        System.out.print("\n" + name + "> ");

        return this.dataInput.nextLine();
    }


    /**
    * Processes the message from the server and prints it
    */ 
    public void process_input(){
        String messageIn = this.get_message();

            if (messageIn != null)
                System.out.println(messageIn);
    }

    
    /**
    * Gets the directory information from the sever
    */ 
    public void get_dir(){
        // Acknowledgement from the server that they got the request
        this.process_input();

        // Gets the amount of file names that will be sent
        int len = Integer.parseInt(this.get_message());

        // Prints every file name as its own message
        for (int i = 0; i < len; i++)
            this.process_input();
    }


    /**
    * Writes the incoming file to clients current directory
    *
    * @param  fileName  the name of the file the client wants
    */ 
    public void process_file(String fileName){
        // TO-DO
        try {
            // int bytes = 0;
            FileOutputStream fileOutputStream = new FileOutputStream(fileName);

            long size = Long.parseLong(this.get_message());     // read file size
            System.out.println("File Size " + size);
            byte[] buffer = new byte[BYTE_SIZE];
            int x=0;
            while (size > 0) {
                x++;
                System.out.println(x);
                System.out.println("A: " + size);
                System.out.println("B: " + Math.min(BYTE_SIZE, size) + "\n");
                // System.out.println("A");
                this.fileIn.readFully(buffer, 0, (int) Math.min(BYTE_SIZE, size));
                // System.out.println("B");
                fileOutputStream.write(buffer);
                // System.out.println("C");
                size -= BYTE_SIZE;      // read upto file size
            }

            System.out.println("Got file");
            this.write_message("done");
            fileOutputStream.close();
        } catch (Exception e) {
            System.out.println("Error: Couldn't write file");
        }
    }


    /**
    * Gets the file the user wants to receive from the server
    */ 
    public void get_file(){
        // Acknowledgement of that the server received the request
        this.process_input();

        boolean intCheck = true;
        String fileNum = "0";
        
        // Requests the file number from the user, loop breaks only when a valid
        // integer is inputted
        while (intCheck){
            System.out.println("Server> Input the file number below?");
            System.out.println("(Type '0' to exit and 'list' to get list of files)");
    
            System.out.print("\nFile #> ");
    
            fileNum = this.dataInput.nextLine();
    
            try {
                Integer.parseInt(fileNum);
                intCheck = false;
            } catch (Exception e) {
                System.out.println("Invalid Input: please enter a valid integer");
            }
        }

        // Sends the file index request to the server
        this.write_message(fileNum);
        
        
        // If the request is 0, the request is cancelled
        if (fileNum.equals("0"))
            return;

        // Creates a directory for the file if it doesn't already exist
        File directory = new File("./" + this.name + "_data");
        if (! directory.exists()){
            directory.mkdir();
        }

        // Gets the file name from the server
        String fileName = this.get_message();

        // If the file isn't found, the server responds with error
        if (!fileName.equals("error"))
            this.process_file("./" + this.name + "_data/" + fileName);

        // Acknowledgement that the file was either sent or not sent
        this.process_input();
    }


    /**
    * Runs the communication with the server
    */ 
    public void run(){
        String messageOut;
        System.out.println("\n(Remember to exit just type in 'exit')\n");

        // Continues to run the loop until the client exits
        while(this.active){
            // Gets user input and sends it to server    
            messageOut = this.get_input(this.name);
            this.write_message(messageOut);

            // Runs a response depending on the user input
            switch (messageOut) {
                // 'exit' closes the connection client-side
                case "exit":
                    this.exit();
                    break;

                // 'list' asks the server for the list of files in the directory
                case "list":
                    this.get_dir();
                    break;

                // 'file' asks for a file from the server
                case "file":
                    this.get_file();
                    break;
                
                // default is to get the acknowledgment of the message from the server
                default:
                    this.process_input();
                    break;
            }
        }
    }

    /**
    * Sets up the communication and informs the user how to communicate with the server
    */ 
    public void setup(){
        System.out.println("Server> Welcome!");
        System.out.println("Server> What name do you want to have?");
        
        // Gets preferred name from the user
        this.name = this.get_input("You");
        this.write_message(this.name);

        // If the server is at capacity the client will stall here until 
        // there is an available connection
        System.out.println("Waiting for sever please be patient...");
        this.process_input();
        System.out.print("\n\n");
    }


    /**
    * Stalls the client until the server can be connected to
    */ 
    public void waiting_room(){
        // Attempts to connect to the server every 10 seconds until successful
        while(true){
            System.out.printf("Server> Sever is busy, please wait patiently...\n");
            try {
                TimeUnit.SECONDS.sleep(10);
                this.socket = new Socket(this.ip, this.port); 
                return;
            } catch (Exception e) {}
        }
    }

    /**
    * Closes to the connection with the server
    */ 
    public void exit(){
        // Indicates to the user that connection is closing
        System.out.println("Exiting...");
        this.active = false;
        
        // Closes the connections and streams with the server
        try{
            this.dataInput.close();
            this.out.close();
            this.in.close();
            this.socket.close();
        } catch (Exception e) {
            System.out.printf("ERROR: Could not successfully close the connection\n");
        }
    }

    public static void main(String[] args){
        new TCP_Client("127.0.0.1", 5000);
    }
    
}
