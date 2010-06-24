package randoop.plugin.internal.ui.views;

import org.eclipse.ui.PlatformUI;

import randoop.plugin.internal.core.runtime.IMessageListener;
import randoop.runtime.IMessage;
import randoop.runtime.PercentDone;
import randoop.runtime.RandoopStarted;

public class MessageViewListener implements IMessageListener {
  private TestGeneratorViewPart fViewPart;

  private IMessage fStart;

  public MessageViewListener(TestGeneratorViewPart viewPart) {
    fViewPart = viewPart;
    fStart = null;
  }

  @Override
  public void handleMessage(IMessage m) {
    if (m instanceof RandoopStarted) {
      if (fStart == null) {
        fStart = m;
      }
      PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
          @Override
          public void run() {
            fViewPart.getProgressBar().start();
          }
        });
    } else if (m instanceof PercentDone) {
      final double percentDone = ((PercentDone)m).getPercentDone();
      PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
        @Override
        public void run() {
          fViewPart.getProgressBar().setPercentDone(percentDone);
        }
      });
    }
  }

  @Override
  public void handleTermination() {
    PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
      @Override
      public void run() {
        fViewPart.getProgressBar().stop();
      }
    });
  }
}
