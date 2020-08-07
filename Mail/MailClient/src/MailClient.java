import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.util.Scanner;

public class MailClient {

    private Socket socket = null; // Client's socket.
    private DataInputStream input = null; // The stream used for client's input.
    private DataOutputStream output = null; // The stream used for client's output.
    private boolean listening; // The flag indicating that the connection to the server is ongoing.

    /**
     * Class constructor.
     *
     * @param IP a String representing the IP of the server that the client's wants to connect to.
     * @param port an int representing the port of the server that the client's wants to connect to.
     */
    public MailClient(String IP, int port){
        boolean connected = true;
        try {
            socket = new Socket(IP, port);
            input = new DataInputStream(socket.getInputStream());
            output = new DataOutputStream(socket.getOutputStream());
            listening = true;
        } catch (IOException e) {
            if (e instanceof ConnectException) {
                System.out.println("> Connection to the Mail Service Failed.");
                connected = false;
            } else {
                e.printStackTrace();
            }
        }
        if (connected) {
            System.out.println("> Successfully Connected to the Mail Service.");
            run();
        }
    }

    /**
     * Request creation method.
     *
     * @param s a String that represents the user's menu selection.
     * @return a properly formatted request String.
     */
    private String createRequest(String s) {
        String request;
        switch (s) {
            case "Register":
                request = "REGISTER_REQUEST";
                break;
            case "LogIn":
                request = "LOGIN_REQUEST";
                break;
            case "NewEmail":
                request = "NEW_EMAIL_REQUEST";
                break;
            case "ShowEmails":
                request = "SHOW_EMAILS_REQUEST";
                break;
            case "ReadEmail":
                request = "READ_EMAIL_REQUEST";
                break;
            case "DeleteEmail":
                request = "DELETE_EMAIL_REQUEST";
                break;
            case "LogOut":
                request = "LOGOUT_REQUEST";
                break;
            case "Exit":
                request = "EXIT_REQUEST";
                break;
            default:
                request = "BAD_REQUEST";
                break;
        }
        return request;
    }

    /**
     * The method responsible for the client side of the system.
     */
    private void run() {
        Scanner in = new Scanner(System.in);
        String clientMessage;
        String serverMessage;
        try {
                while (listening) {
                System.out.print("-------------------\n");
                serverMessage = input.readUTF();
                switch (serverMessage) {
                    case "CONNECTED":
                    case "REQUEST_HANDLED":
                        output.writeUTF("MENU_REQUEST");
                        System.out.print(input.readUTF() + "\n");
                        clientMessage = in.nextLine();
                        output.writeUTF(createRequest(clientMessage));
                        break;
                    case "REGISTER":
                        serverMessage = input.readUTF();
                        System.out.println(serverMessage);
                        if (serverMessage.equals("Enter a username:") || serverMessage.equals("Enter a password:")) {
                            output.writeUTF(in.nextLine());
                            output.flush();
                        }
                        break;
                    case "LOGIN":
                        serverMessage = input.readUTF();
                        System.out.println(serverMessage);
                        if (serverMessage.equals("Enter your username:") || serverMessage.equals("Enter your password:")) {
                            output.writeUTF(in.nextLine());
                            output.flush();
                        }
                        break;
                    case "WRITE_EMAIL":
                        serverMessage = input.readUTF();
                        System.out.println(serverMessage);
                        if (serverMessage.equals("Receiver:") || serverMessage.equals("Subject:") || serverMessage.equals("Main Body:")) {
                            output.writeUTF(in.nextLine());
                            output.flush();
                        }
                        break;
                    case "SEE_EMAILS":
                        System.out.print(input.readUTF() + "\n");
                        break;
                    case "READ_EMAIL":
                    case "DELETE_EMAIL":
                        serverMessage = input.readUTF();
                        System.out.println(serverMessage);
                        if (serverMessage.equals("Enter Email's ID:")) {
                            output.writeUTF(in.nextLine());
                            output.flush();
                        }
                        break;
                    case "DISCONNECT":
                        listening = false;
                        break;
                    case "ERROR":
                        System.out.println(input.readUTF());
                        break;
                }
            }
        } catch (IOException e) {
            if (e instanceof EOFException) {
                System.out.println("> Connection to the server has been lost.");
            } else {
                e.printStackTrace();
            }
        } finally {
            try {
                System.out.println("> Disconnected.");
                if (input != null) {
                    input.close();
                }
                if (output != null) {
                    output.close();
                }
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * The main method of the class.
     *
     * @param args an array of Strings containing the arguments given to the program at execution.
     */
    public static void main(String[] args) {
        int port;
        String IP;
        Scanner in = new Scanner(System.in);
        if (args.length == 0) {
            System.out.println("> Enter the connection info of the server you wish to connect to (IP, port).");
            System.out.println("> IP address: ");
            IP = in.nextLine();
            System.out.println("> Port: ");
            port = Integer.parseInt(in.nextLine());
        } else {
            IP = args[0];
            port = Integer.parseInt(args[1]);
        }
        new MailClient(IP, port);
    }
}