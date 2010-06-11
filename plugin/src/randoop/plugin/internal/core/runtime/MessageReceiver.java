package randoop.plugin.internal.core.runtime;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

import randoop.runtime.Message;

public class MessageReceiver implements Runnable {
  private ServerSocket fServerSocket;

  /**
   * 
   * @throws IOException
   *           if unable to create socket
   */
  public MessageReceiver() throws IOException {
    fServerSocket = new ServerSocket(0);
    assert fServerSocket.isBound();
  }

  public int getPort() {
    return fServerSocket.getLocalPort();
  }

  @Override
  public void run() {
    try {
      Socket sock = fServerSocket.accept();
      InputStream iStream = sock.getInputStream();
      ObjectInputStream objectInputStream = new ObjectInputStream(iStream);

      Message start = (Message) objectInputStream.readObject();
      Message work = (Message) objectInputStream.readObject();
      while (work.getType() != Message.Type.DONE) {
        work = (Message) objectInputStream.readObject();

        // XXX use some kind of monitor or listener here
        System.out.print("Percent done = ");
        System.out.println(work.getPercentDone(start));
      }
    } catch (IOException ioe) {
      System.err.println("Stream terminated unexpectedly");
    } catch (ClassNotFoundException e) {
      System.out.println("Incorrect class " + e);
    } finally {
      try {
        fServerSocket.close();
      } catch (IOException ioe) {
      }
    }
  }
}
