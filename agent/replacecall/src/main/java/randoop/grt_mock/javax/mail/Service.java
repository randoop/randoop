package randoop.grt_mock.javax.mail;

import javax.mail.MessagingException;

public abstract class Service extends javax.mail.Service {

    public Service(javax.mail.Session session, javax.mail.URLName urlname) {
        super(session, urlname);
    }

    @Override
    public synchronized void connect() throws MessagingException {
        throw new MessagingException("Network operations are disabled during testing.");
    }

    @Override
    public synchronized void connect(String host, String user, String password) throws MessagingException {
        throw new MessagingException("Network operations are disabled during testing.");
    }
    
    @Override
    public synchronized void connect(String host, int port, String user, String password) throws MessagingException {
        throw new MessagingException("Network operations are disabled during testing.");
    }

    // Implement other connect methods as needed
}
