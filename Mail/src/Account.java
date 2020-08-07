import java.util.ArrayList;
import java.util.List;

public class Account {

    private final String username; // The account's username.
    private final String password; // The account's password.
    private final List<Email> mailbox; // The account's mailbox.

    /**
     * Class constructor.
     *
     * @param username a String representing the account's username.
     * @param password a String representing the account's password.
     */
    public Account(String username, String password) {
        this.username = username;
        this.password = password;
        this.mailbox = new ArrayList<>();
    }

    /**
     * Username field getter.
     *
     * @return a String representing the account's username.
     */
    public String getUsername() {
        return this.username;
    }

    /**
     * Checks if the given password matches the stored one.
     *
     * @param password a String representing the account's password.
     * @return a boolean value that indicates the matching of the given password with the stored one.
     */
    public boolean isValidPassword(String password) {
        return this.password.equals(password);
    }

    /**
     * Mailbox field getter.
     *
     * @return a List representing the account's mailbox.
     */
    public List<Email> getMailbox() {
        return this.mailbox;
    }

    /**
     * Adds an email in the account's mailbox
     *
     * @param e an Email object.
     */
    public void addMail(Email e) {
        mailbox.add(e);
    }

    /**
     * Deletes an email from the account's mailbox
     *
     * @param ID a String representing the email's ID number.
     * @return a String containing information about the result of the process.
     */
    public String deleteMail(String ID) {
        int idx = Integer.parseInt(ID) - 1;
        if(idx < 0 || idx > mailbox.size() - 1) {
            return "FAILED_TO_DELETE_EMAIL";
        } else {
            mailbox.remove(idx);
            return "EMAIL_DELETED";
        }
    }
}
