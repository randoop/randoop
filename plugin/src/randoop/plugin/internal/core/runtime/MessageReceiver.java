package randoop.plugin.internal.core.runtime;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

import org.eclipse.ui.PlatformUI;

import randoop.plugin.internal.ui.views.TestGeneratorViewPart;
import randoop.runtime.Message;

public class MessageReceiver implements Runnable {
  private TestGeneratorViewPart fViewPart;
  private ServerSocket fServerSocket;

  /**
   * 
   * @param viewPart
   * @throws IOException
   *           if unable to create socket
   */
  public MessageReceiver(TestGeneratorViewPart viewPart) throws IOException {
    fViewPart = viewPart;

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
      Message work = null;
      do {
        work = (Message) objectInputStream.readObject();

        final double percentDone = work.getPercentDone(start);
        PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
          @Override
          public void run() {
            fViewPart.getProgressBar().step(percentDone);
          }
        });
      } while (work != null && work.getType() != Message.Type.DONE);
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
