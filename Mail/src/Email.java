public class Email {

    private boolean isNew; // A flag indicating whether the email has been read or not.
    private final String sender; // The sender of the email.
    private final String receiver; // The receiver of the email.
    private final String subject; // The subject of the email.
    private final String mainbody; // The main body of the email.

    /**
     * Class constructor.
     *
     * @param sender a String representing the sender of the email.
     * @param receiver a String representing the receiver of the email.
     * @param subject a String representing the subject of the email.
     * @param mainbody a String representing the main body of the email.
     */
    public Email(String sender, String receiver, String subject, String mainbody) {
        this.isNew = true;
        this.sender = sender;
        this.receiver = receiver;
        this.subject = subject;
        this.mainbody = mainbody;
    }

    /**
     * Sender field getter.
     *
     * @return a String representing the sender of the email.
     */
    public String getSender() {
        return this.sender;
    }

    /**
     * Receiver field getter.
     *
     * @return a String representing the receiver of the email.
     */
    public String getReceiver() {
        return this.receiver;
    }

    /**
     * Subject field getter.
     *
     * @return a String representing the subject of the email.
     */
    public String getSubject() {
        return this.subject;
    }

    /**
     * Main body field getter.
     *
     * @return a String representing the main body of the email.
     */
    public String getMainbody() {
        return this.mainbody;
    }

    /**
     * IsNew field getter
     *
     * @return a flag indicating whether the email has been read or not.
     */
    public boolean isUnread() {
        return this.isNew;
    }

    /**
     * Marks an email as read by updating the isNew flag.
     */
    public void markAsRead() {
        this.isNew = false;
    }
}