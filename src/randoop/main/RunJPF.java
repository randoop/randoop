package randoop.main;

import randoop.experiments.JPFRun;

public class RunJPF extends CommandHandler {

  public RunJPF() {
    super("run-jpf", "", "", "", "", null, "", "", "", null);
  }

  @Override
  public boolean handle(String[] args) throws RandoopTextuiException {
    JPFRun.run();
    return true;
  }

}
