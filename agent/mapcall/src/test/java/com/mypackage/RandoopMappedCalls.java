package com.mypackage;

import java.awt.Component;
import java.io.*;
import java.sql.*;

public class RandoopMappedCalls {

  // These methods replace a dialog box by a printed indication that the
  // dialog box would have popped up.

  public static void showMessageDialog(
      Component parentComponent, Object message, String title, int messageType) {
    System.out.printf("Dialog box with message: %s%n", message);
  }

  public static void showConfirmDialog(
      Component parentComponent, Object message, String title, int messageType) {
    System.out.printf("Confirm Dialog box with message: %s%n", message);
  }

  public static void showConfirmDialog(
      Component parentComponent, Object message, String title, int messageType, int ii) {
    System.out.printf("Confirm Dialog box with message: %s%n", message);
  }

  // This method replaces a call to System.exit by a printed indication
  // that the call would have been made.

  public static void exit(int status) {
    System.out.printf("System exit with status %d ignored%n", status);
  }
}
