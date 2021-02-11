import java.io.*;
import java.net.*;
// import java.util.concurrent.TimeUnit;

// TCP Server thread that interacts with the client
class Client extends Thread{
    // Attributes used for socket connection
    public PrintWriter out;
    public BufferedReader in;
    public int port;
    public Socket serverSocket;

    // Attributes used for thread information
    public String name;
    public String id;
    public Boolean active;
    public int errors;

    // Attributes used for file transfers
    public DataOutputStream fileOut;
    public static int BYTE_SIZE = 1024;
    File folder = new File("./data");
    File[] listOfFiles;

    /**
    * Handles all sever side communication with the client
    *
    * @param  socket    the socket number to establish the connection on
    * @param  id        the id for the current client
    */ 
    Client(Socket socket, int id){
        // Initializing the socket variable
        this.serverSocket = socket;
        
        // Tries to establish an IO stream with the client, exits if it connection fails
        try {
            this.out = new PrintWriter (this.serverSocket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader (this.serverSocket.getInputStream()));
            this.fileOut = new DataOutputStream(this.serverSocket.getOutputStream());
        } catch (Exception e) {
            System.out.printf("%s> ERROR: Could not establish read-write connection with client\n", this.id);
            this.exit();
        }

        // Initializes variables, this is done only if connection is established
        this.id = "Client#0" + String.valueOf(id+1);
        this.listOfFiles = folder.listFiles();
    }


    /**
    * Message input handler that reads the incoming message from the client
    *
    * @return  returns the message received from the client
    */ 
    public String get_message(){
        // Tries to read the message
        // If unsuccessful, logs the error and returns null
        try {
            return this.in.readLine();
        } catch (Exception e) {
            System.out.printf("%s> ERROR: Could not read input\n", this.id);
            this.error_log();
            return null;
        }
    }

    /**
    * Process the input from the client and responds appropriately
    *
    * @param  input  the message read from the client
    */ 
    public void processInput(String input){
        // States the message from the client on server side
        System.out.printf("%s> %s\n", this.id, input);

        // Runs a response depending on the message
        switch (input) {
            // 'exit' indicates the client is closing the connecting
            // Server also closes their connection
            case "exit":
                this.exit();
                break;

            // 'list' indicates the client wants a list of files in the directory
            case "list":
                this.write_message("Server> Ack: " + input);
                this.send_dir();
                break;

            // 'file' indicates the client wants a file from the server
            case "file":
                this.write_message("Server> Ack: " + input);
                this.send_file();
                break;

            // default response is to send back acknowledgement of the message
            default:
                this.write_message("Server> Ack: " + input);
        }
    }


    /**
    * Message output handler that sends an outgoing message to the client
    *
    * @param  message  the message to send to the client
    */ 
    public void write_message(String message){
        // Tries to send the message to the client
        // If unsuccessful, logs the error
        try {
            this.out.println(message);
            this.out.flush();

        } catch (Exception e) {
            System.out.printf("%s> ERROR: Could not write input\n", this.id);
            this.error_log();
        }
    }


    /**
    * Updates the contents of the server directory list and sends it to the client
    */ 
    public void send_dir(){
        // Updates the client server directory list
        this.listOfFiles = folder.listFiles();

        // Indicates to the client how many file names it's gonna be sending
        this.write_message(String.valueOf(this.listOfFiles.length));

        // Sends each file as it's own message with the file number
        for (int i = 0; i < this.listOfFiles.length; i++){
            if (listOfFiles[i].isFile()) 
               write_message("File #" + String.valueOf(i+1) + ": " + listOfFiles[i].getName());   
        }
    }

    /**
    * Sends a file to the client
    */ 
    public void send_file() {
        // Gets the file number the client wants
        int fileNum = Integer.parseInt(this.get_message());

        // If the file number is zero, the client doesn't want the file anymore
        if (fileNum == 0)
            return;

        // Tries to send the file to the client
        try {
            // Server attempts to get client's file from the directory
            File file = this.listOfFiles[fileNum-1];
            // Sends the file name to the client
            this.write_message(file.getName());
            System.out.printf("%s> Sending file %s\n", this.id, file.getName());

            int bytes = 0;
            FileInputStream fileInputStream = new FileInputStream(file);
            System.out.println("File Size " + file.length());
            // send file size
            // this.fileOut.writeLong(file.length());  
            this.write_message(String.valueOf(file.length()));
            // break file into chunks
            byte[] buffer = new byte[BYTE_SIZE];
            int x = 0;
            while ((bytes = fileInputStream.read(buffer)) != -1){
                x ++;
                // System.out.println(buffer);
                this.fileOut.write(buffer, 0, bytes);
                this.fileOut.flush();
            }
            System.out.println(x);
            // TimeUnit.SECONDS.sleep(2);
            this.get_message();
            fileInputStream.close();
            System.out.println("Server> Done sending file");
            
            this.write_message("Server> Successfully sent " + file.getName());

            
            
        } catch (Exception e) {
            // If the user wants an invalid file number, or the file can't send
            // The server responds with an error to the client
            System.out.printf("%s> File unsuccessfully sent\n", this.id);
            this.write_message("error");
            this.write_message("Server> File not found");
        }
        
    }

    /**
    * Logs an error when trying to send or receive a message. The server disconnects
    * from the client if more than 5 errors occur.
    */ 
    public void error_log(){
        this.errors ++;

        if (this.errors > 5){
            this.write_message("Server> Too many errors occurred, disconnecting from server");
            this.exit();
        }
    }


    /**
    * Resets the thread attributes
    */ 
    public void reset(){
        this.out = null;
        this.in = null;
        this.name = null;
        this.port = 0;
        this.active = false;
    }


    /**
    * Server closes all connections and resets variables
    */ 
    public void exit(){
        try{
            this.out.close();
            this.in.close();
            this.serverSocket.close();
        } catch (Exception e) {
            System.out.printf("%s> ERROR: Could not successfully close the connection\n", this.id);
        }

        this.reset();
    }


    /**
    * Runs the thread for the client
    */ 
    public void run(){
        // Initializes variables for when the client thread starts
        // These variables indicate a successful connection
        String input;
        this.active = true;
        this.errors = 0;
    
        // Gets the preferred name from the client
        this.name = this.get_message();
        
        if (this.name.equals("")){
            this.write_message("Server> Ack: No preferred name was given");
            this.name = this.id;
        } else {
            this.write_message("Server> Ack: Your preferred name of " + this.name + " was saved");
        }
        
        System.out.printf("Server> %s --> %s\n", this.id, this.name);

        // Continues to run the thread as long as the client is active
        while(this.active){
            input = this.get_message();
            this.processInput(input);
        }
    }
}