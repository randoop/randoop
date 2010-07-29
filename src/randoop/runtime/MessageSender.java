package randoop.runtime;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

public class MessageSender {
  public final static String SERVER_HOSTNAME = "127.0.0.1"; //$NON-NLS-1$

  private Socket fSocket;
  ObjectOutputStream fObjectOutputStream;

  public MessageSender(int port) throws IOException {
    fSocket = new Socket(SERVER_HOSTNAME, port);
    OutputStream oStream = fSocket.getOutputStream();
    fObjectOutputStream = new ObjectOutputStream(oStream);
  }

  public boolean send(IMessage payload) {
    try {
      fObjectOutputStream.writeObject(payload);

      if (payload instanceof ClosingStream) {
        close();
      }
      return true;
    } catch (IOException ioe) {
      System.err.println(ioe);
      close();
      return false;
    }
  }

  public void close() {
    try {
      fSocket.close();
    } catch (IOException e) {
    }
  }

  public boolean isClosed() {
    return fSocket.isClosed();
  }
}
