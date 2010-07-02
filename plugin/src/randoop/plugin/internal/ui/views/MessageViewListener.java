package randoop.plugin.internal.ui.views;

import org.eclipse.ui.PlatformUI;

import randoop.ErrorRevealed;
import randoop.plugin.internal.core.runtime.IMessageListener;
import randoop.runtime.IMessage;
import randoop.runtime.PercentDone;
import randoop.runtime.RandoopStarted;

public class MessageViewListener implements IMessageListener {
  private TestGeneratorViewPart fViewPart;

  public MessageViewListener(TestGeneratorViewPart viewPart) {
    fViewPart = viewPart;
  }

  @Override
  public void handleMessage(IMessage m) {
    if (m instanceof RandoopStarted) {
      PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
          @Override
          public void run() {
            fViewPart.getProgressBar().start();
            fViewPart.getCounterPanel().reset();
            fViewPart.randoopErrors.reset();
            
          }
        });
    } else if (m instanceof PercentDone) {
      final PercentDone p = (PercentDone)m;
      PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
        @Override
        public void run() {
          fViewPart.getProgressBar().setPercentDone(p.getPercentDone());
          fViewPart.getCounterPanel().numSequences(p.getSequencesGenerated());
        }
      });
    } else if (m instanceof ErrorRevealed) {
      final ErrorRevealed err = (ErrorRevealed)m;
      PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
        @Override
        public void run() {
          fViewPart.getProgressBar().error();
          fViewPart.getCounterPanel().errors();
          fViewPart.randoopErrors.add(err);
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
