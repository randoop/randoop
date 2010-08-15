package randoop.plugin.internal.core.runtime;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.util.NLS;

import randoop.plugin.internal.core.RandoopStatus;
import randoop.runtime.ClosingStream;
import randoop.runtime.IMessage;

public class MessageReceiver extends Job {
  
  private static int RECEIVER_ID = 1;
  
  private IMessageListener fIMessageListener;
  private ServerSocket fServerSocket;

  /**
   * 
   * @param messageListener
   * @throws IOException
   *           if unable to create socket
   */
  public MessageReceiver(IMessageListener messageListener) throws IOException {
    super(NLS.bind("Randoop Message Receiver {0}", RECEIVER_ID++));
    
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

  @Override
  public IStatus run(IProgressMonitor monitor) {
    if (monitor == null) {
      monitor = new NullProgressMonitor();
    }
    
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
      // don't return an error
      // return RandoopStatus.COMM_TERMINATED_SESSION.getStatus(port, null);
    } catch (ClassNotFoundException e) {
      return RandoopStatus.COMM_MESSAGE_CLASS_NOT_FOUND.getStatus(port, null);
    } finally {
      try {
        fServerSocket.close();
      } catch (IOException ioe) {
      }
    }
    return RandoopStatus.OK_STATUS;
  }


  
}
