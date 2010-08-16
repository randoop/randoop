package randoop.plugin.internal.core.runtime;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

import org.eclipse.core.runtime.IStatus;

import randoop.plugin.RandoopPlugin;
import randoop.plugin.internal.core.RandoopStatus;
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
    int port = -1;
    try {
      Socket sock = fServerSocket.accept();
      port = sock.getLocalPort();
      
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
      
      // We actually end up here if the user presses the stop button, so
      // don't log an error
      // IStatus s = RandoopStatus.COMM_TERMINATED_SESSION.getStatus(port, null);
      // RandoopPlugin.log(s);
    } catch (ClassNotFoundException e) {
      IStatus s = RandoopStatus.COMM_MESSAGE_CLASS_NOT_FOUND.getStatus(port, null);
      RandoopPlugin.log(s);
    } finally {
      try {
        fServerSocket.close();
      } catch (IOException ioe) {
      }
    }
  }


  
}
