import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Scanner;

public class MailServer {

    private final ArrayList<Account> accounts; // The List that stores every account saved in the server.
    private ServerSocket serverSocket; // The socket that the server is running in.
    private boolean running; // The flag indicating that the server is up and running.

    /**
     * Class constructor.
     *
     * @param port an int representing the port that the server is running in.
     */
    public MailServer(int port) {
        serverSocket = null;
        running = true;
        accounts = new ArrayList<>();
        try {
            serverSocket = new ServerSocket(port);
            Account a = new Account("jim", "1");
            a.addMail(new Email("jim", "dekas", "Morning", "Good Morning dekas"));
            a.addMail(new Email("jim", "dekas", "Afternoon", "Good Afternoon dekas"));
            a.addMail(new Email("jim", "dekas", "Night", "Good Night dekas"));
            accounts.add(a);
            Account b = new Account("dekas", "2");
            b.addMail(new Email("dekas", "jim", "Morning", "Good Morning jim"));
            b.addMail(new Email("dekas", "jim", "Afternoon", "Good Afternoon jim"));
            b.addMail(new Email("dekas", "jim", "Night", "Good Night jim"));
            accounts.add(b);
            new Thread(new exitThread()).start();
            handshakingThread();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (serverSocket != null) {
                    serverSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Class responsible for terminating the server.
     */
    private class exitThread implements Runnable {
        @Override
        public void run() {
            System.out.println("> Press Enter to exit.");
            Scanner in = new Scanner(System.in);
            in.nextLine();
            running = false;
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Method responsible for creating new client threads.
     */
    private void handshakingThread() {
        System.out.println("> Server is Running.");
        try {
            while (running) {
                Socket s = serverSocket.accept();
                new Thread(new RequestThread(s)).start();
                System.out.println("> New Client Connected.");
            }
        } catch (IOException e) {
            if (e instanceof SocketException && !running) {
                System.out.println("> Server Shutting Down.");
            } else {
                e.printStackTrace();
            }
        }
    }

    /**
     * Class responsible for handling the incoming client requests.
     */
    private class RequestThread implements Runnable{

        private final Socket requestSocket; // Server's socket.
        private DataInputStream input = null; // The stream used for server's input.
        private DataOutputStream output = null; // The stream used for server's output.
        private Account user; // The account used by the user on the client side.
        boolean listening; // The flag indicating that the connection to the client is ongoing.

        /**
         * Class constructor.
         *
         * @param socket the Socket used to host the server.
         */
        public RequestThread(Socket socket) {
            this.requestSocket = socket;
            user = null;
            try {
                input = new DataInputStream(this.requestSocket.getInputStream());
                output = new DataOutputStream(this.requestSocket.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
            listening = true;
        }

        /**
         * The method responsible for the server side of the system.
         */
        public void run() {
            try {
                output.writeUTF("CONNECTED");
                String request;
                while (listening) {
                    request = input.readUTF();
                    switch (request) {
                        case "MENU_REQUEST":
                            if (user != null) {
                                output.writeUTF("Welcome " + user.getUsername() + ".\n" + "===============\n> NewEmail\n> ShowEmails\n> ReadEmail\n> DeleteEmail\n> LogOut\n> Exit\n===============");
                            } else {
                                output.writeUTF("==========\n> Register\n> LogIn\n> Exit\n==========");
                            }
                            break;
                        case "REGISTER_REQUEST":
                            output.writeUTF("REGISTER");
                            output.writeUTF("Enter a username:");
                            String username = input.readUTF();
                            output.writeUTF("REGISTER");
                            output.writeUTF("Enter a password:");
                            String password = input.readUTF();
                            output.writeUTF("REGISTER");
                            if (register(username, password).equals("ACCOUNT_CREATED")) {
                                output.writeUTF("Account created.");
                            } else if (register(username, password).equals("USERNAME_EXISTS")) {
                                output.writeUTF("Username exists.");
                            }
                            output.writeUTF("REQUEST_HANDLED");
                            break;
                        case "LOGIN_REQUEST":
                            output.writeUTF("LOGIN");
                            output.writeUTF("Enter your username:");
                            String logUsername = input.readUTF();
                            output.writeUTF("LOGIN");
                            output.writeUTF("Enter your password:");
                            String logPassword = input.readUTF();
                            String result = logIn(logUsername, logPassword);
                            output.writeUTF("LOGIN");
                            switch (result) {
                                case "LOGGED_IN":
                                    user = getUser(logUsername);
                                    output.writeUTF("LogIn Success.");
                                    break;
                                case "USER_NOT_FOUND":
                                    output.writeUTF("Username not found.");
                                    break;
                                case "INVALID_PASSWORD":
                                    output.writeUTF("Wrong Password.");
                                    break;
                            }
                            output.writeUTF("REQUEST_HANDLED");
                            break;
                        case "NEW_EMAIL_REQUEST":
                            output.writeUTF("WRITE_EMAIL");
                            output.writeUTF("Receiver:");
                            String receiver = input.readUTF();
                            output.writeUTF("WRITE_EMAIL");
                            output.writeUTF("Subject:");
                            String subject = input.readUTF();
                            output.writeUTF("WRITE_EMAIL");
                            output.writeUTF("Main Body:");
                            String mainBody = input.readUTF();
                            String writeResult = newEmail(user.getUsername(), receiver, subject, mainBody);
                            output.writeUTF("WRITE_EMAIL");
                            if (writeResult.equals("EMAIL_SENT")) {
                                output.writeUTF("Email sent.");
                            } else if (writeResult.equals("INVALID_RECEIVER")) {
                                output.writeUTF("Invalid receiver.");
                            }
                            output.writeUTF("REQUEST_HANDLED");
                            break;
                        case "SHOW_EMAILS_REQUEST":
                            ArrayList<String> emails = showEmails();
                            StringBuilder sb = new StringBuilder();
                            for (String e : emails) {
                                sb.append(e);
                                sb.append("\n");
                            }
                            output.writeUTF("SEE_EMAILS");
                            output.writeUTF(sb.toString());
                            output.writeUTF("REQUEST_HANDLED");
                            break;
                        case "READ_EMAIL_REQUEST":
                            output.writeUTF("READ_EMAIL");
                            output.writeUTF("Enter Email's ID:");
                            String readID = input.readUTF();
                            String readResult = readEmail(readID);
                            output.writeUTF("READ_EMAIL");
                            if (readResult.equals("INVALID_ID")) {
                                output.writeUTF("Email's ID not found.");
                            } else {
                                output.writeUTF(readResult);
                            }
                            output.writeUTF("REQUEST_HANDLED");
                            break;
                        case "DELETE_EMAIL_REQUEST":
                            output.writeUTF("DELETE_EMAIL");
                            output.writeUTF("Enter Email's ID:");
                            String deleteId = input.readUTF();
                            String deleteResult = deleteEmail(deleteId);
                            output.writeUTF("DELETE_EMAIL");
                            if (deleteResult.equals("EMAIL_DELETED")) {
                                output.writeUTF("Email Deleted.");
                            } else if (deleteResult.equals("FAILED_TO_DELETE_EMAIL")) {
                                output.writeUTF("Invalid Email ID.");
                            }
                            output.writeUTF("REQUEST_HANDLED");
                            break;
                        case "LOGOUT_REQUEST":
                            logOut();
                            output.writeUTF("REQUEST_HANDLED");
                            break;
                        case "EXIT_REQUEST":
                            output.writeUTF("DISCONNECT");
                            exit();
                            break;
                        case "BAD_REQUEST":
                            output.writeUTF("ERROR");
                            output.writeUTF("Wrong request.");
                            output.writeUTF("REQUEST_HANDLED");
                            break;
                    }
                }
            } catch (IOException e) {
                if (e instanceof EOFException) {
                    System.out.println("> Client lost connection.");
                } else {
                    e.printStackTrace();
                }
            } finally {
                try {
                    if (output != null) {
                        output.close();
                    }
                    if (input != null) {
                        input.close();
                    }
                    if (requestSocket != null) {
                        requestSocket.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * Getter for account based on the given username.
         *
         * @param username a String representing the given username.
         * @return the Account matching the given username information.
         */
        private Account getUser(String username) {
            Account user = null;
            for (Account a : accounts) {
                if (a.getUsername().equals(username)) {
                    user = a;
                }
            }
            return user;
        }

        /**
         * Registers a new account into the system.
         *
         * @param username a String representing the account's username.
         * @param password a String representing the account's password.
         * @return a confirmation String.
         */
        private String register(String username, String password) {
            for (Account a : accounts) {
                if (a.getUsername().equals(username)) {
                    return "USERNAME_EXISTS";
                }
            }
            accounts.add(new Account(username, password));
            return "ACCOUNT_CREATED";
        }

        /**
         * Logs in the account into the system.
         *
         * @param username a String representing the account's username.
         * @param password a String representing the account's password.
         * @return a confirmation String.
         */
        private String logIn(String username, String password) {
            Account user = null;
            for (Account a : accounts) {
                if (a.getUsername().equals(username)) {
                    user = a;
                }
            }
            if (user != null) {
                boolean validPass = user.isValidPassword(password);
                if (validPass) {
                    this.user = user;
                    return "LOGGED_IN";
                } else {
                    return "INVALID_PASSWORD";
                }
            } else {
                return "USER_NOT_FOUND";
            }
        }

        /**
         * Creates a new email.
         *
         * @param senderUsername a String representing the sender's username.
         * @param receiverUsername a String representing the receiver's username.
         * @param subject a String representing the email's subject.
         * @param mainBody a String representing the email's main body.
         * @return a confirmation String.
         */
        private String newEmail(String senderUsername, String receiverUsername, String subject, String mainBody) {
            Account receiver = null;
            for (Account a : accounts) {
                if (a.getUsername().equals(receiverUsername)) {
                    receiver = a;
                    break;
                }
            }
            if (receiver != null) {
                receiver.addMail(new Email(senderUsername, receiverUsername, subject, mainBody));
                return "EMAIL_SENT";
            } else {
                return "INVALID_RECEIVER";
            }
        }

        /**
         * Returns a list of the account's emails.
         *
         * @return a List of Strings filled with the emails of the current logged in account.
         */
        private ArrayList<String> showEmails() {
            ArrayList<String> emails = new ArrayList<>();
            int ID = 1;
            String status;
            String sender;
            String subject;
            for (Email e : user.getMailbox()) {
                if (e.isUnread()) {
                    status = "[New]";
                } else {
                    status = "     ";
                }
                sender = e.getSender();
                subject = e.getSubject();
                String result = ID + "." + " " + status + " " + sender + "      " + subject;
                emails.add(result);
                ID += 1;
            }
            return emails;
        }

        /**
         * Returns the main body of the email based on the given ID.
         *
         * @param ID a String representing the ID of the email that the user requests to read.
         * @return the email's main body.
         */
        private String readEmail(String ID) {
            int idx = Integer.parseInt(ID) - 1;
            if (idx < 0 || idx > user.getMailbox().size() - 1) {
                return "INVALID_ID";
            } else {
                Email targetEmail = user.getMailbox().get(idx);
                String result = targetEmail.getMainbody();
                targetEmail.markAsRead();
                return result;
            }
        }

        /**
         * Deletes the email based on the given ID.
         *
         * @param ID a String representing the ID of the email that the user requests to read.
         * @return a confirmation String.
         */
        private String deleteEmail(String ID) {
            return user.deleteMail(ID);
        }

        /**
         * Logs out the current logged in account.
         */
        private void logOut() {
            user = null;
        }

        /**
         * Terminates client connection.
         */
        private void exit() {
            System.out.println("> Client Disconnected.");
            listening = false;
        }
    }

    /**
     * The main method of the class.
     *
     * @param args an array of Strings containing the arguments given to the program at execution.
     */
    public static void main(String[] args) {
        int port;
        Scanner in = new Scanner(System.in);
        if (args.length == 0) {
            System.out.println("> Enter the server's port: ");
            port = in.nextInt();
        } else {
            port = Integer.parseInt(args[0]);
        }
        new MailServer(port);
        System.exit(0);
    }
}