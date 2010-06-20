package randoop.plugin.internal.ui.views;

import org.eclipse.ui.PlatformUI;

import randoop.plugin.internal.core.runtime.IMessageListener;
import randoop.runtime.Message;

public class MessageViewListener implements IMessageListener {
  private TestGeneratorViewPart fViewPart;

  private Message fStart;

  public MessageViewListener(TestGeneratorViewPart viewPart) {
    fViewPart = viewPart;
    fStart = null;
  }

  @Override
  public void handleMessage(Message m) {
    if (m.getType() == Message.Type.START) {
      if (fStart == null) {
        fStart = m;
      }
    } else {
      final double percentDone = m.getPercentDone(fStart);
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
