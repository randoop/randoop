package randoop.plugin.internal.core.runtime;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

import randoop.runtime.ClosingStream;
import randoop.runtime.IMessage;

public class MessageReceiver implements Runnable {
  private IMessageListener fIMessageListener;
  private ServerSocket fServerSocket;

  /**
   * 
   * @param messageListener
   * @throws IOException
   *           if unable to create socket
   */
  public MessageReceiver(IMessageListener messageListener) throws IOException {
    if (messageListener == null) {
      fIMessageListener = new NullMessageListener();
    } else {
      fIMessageListener = messageListener;
    }
    
    fServerSocket = new ServerSocket(0);
    assert fServerSocket.isBound();
  }

  public int getPort() {
    return fServerSocket.getLocalPort();
  }

  public void run() {
    try {
      Socket sock = fServerSocket.accept();
      InputStream iStream = sock.getInputStream();
      ObjectInputStream objectInputStream = new ObjectInputStream(iStream);

      IMessage work = (IMessage) objectInputStream.readObject();
      while (work != null && !(work instanceof ClosingStream)) {
        fIMessageListener.handleMessage(work);
        
        // Receive the next message
        work = (IMessage) objectInputStream.readObject();
      }
    } catch (IOException ioe) {
      // Stream terminated unexpectedly
      fIMessageListener.handleTermination();
    } catch (ClassNotFoundException e) {
      System.err.println("Incorrect class " + e);
    } finally {
      try {
        fServerSocket.close();
      } catch (IOException ioe) {
      }
    }
  }
  
}
