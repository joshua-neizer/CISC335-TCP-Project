# CISC335-TCP-Project
**Author:** Joshua Neizer
## Project Overview
A TCP Client-Server Java program created for a computer networking class


## Project Description
A  simple  client-server  communication application (a chat-like service). Detailed requirements are found below. 

## Project Components
1. Design a client-server communication/chatting application.
2. Clients and server are to communicate using sockets ONLY(TCP). You will launch only one server and as many clients as the server can serve.
3. Once created,clients are assigned a name in the form of ["Client" + incremental number starting with 1]. That is, first client name will be "Client#01", second client is "Client02", and so on. 
4. Once the connection is accepted, the client will communicate his/her name to the server.
5. The server will maintain a cache of accepted clients during current session along with date and time accepted, and date and time finished. Cache will be only in memory, no need to use files. 
6. A server  can handle a limited number of clients (you will hard code or configure this number, let’s assume 3 clients). 
7. Points 3,6 are easier to be implemented on the server side. But you can choose either sides.
8. Once connected and its name sent to server, a client can send any string using CLI. The server on the other side should echo the same string with the word"ACK" attached.
9. Once a client finishes sending messages, it can send an "exit" message to terminate the connection. On the server side, and upon receiving a connection closure request, the server closes the connection to free resources for other clients.
10. The server will have a repository of files. After accepting the client’s connection, the client sends the "list" message to the server. The server should then reply with the list of files in its repo. The client can send the name of any file to the server, and the server should stream/serialize the file to the client. You should handle all cases, like sending a file name that doesn’t exist.