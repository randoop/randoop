package randoop.plugin.internal.ui.views;

/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Stephan Michels, stephan@apache.org - 104944 [JUnit] Unnecessary code in JUnitProgressBar
 *******************************************************************************/

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import randoop.plugin.internal.core.runtime.TestGeneratorSession;

/**
 * A progress bar with a red/green indication for success or failure.
 * 
 * (see org.eclipse.jdt.internal.junit.ui.JUnitProgressBar)
 */
public class RandoopProgressBar extends Canvas {
  private static final int DEFAULT_WIDTH = 160;
  private static final int DEFAULT_HEIGHT = 18;

  private double fPercentDone;
  private int fColorBarWidth;
  private Color fOKColor;
  private Color fFailureColor;
  private Color fStoppedColor;
  private boolean fHasError;
  private boolean fIsTerminated;

  public RandoopProgressBar(Composite parent) {
    super(parent, SWT.NONE);
    reset();

    addControlListener(new ControlAdapter() {
      @Override
      public void controlResized(ControlEvent e) {
        fColorBarWidth = scale();
        redraw();
      }
    });
    addPaintListener(new PaintListener() {
      public void paintControl(PaintEvent e) {
        paint(e);
      }
    });
    addDisposeListener(new DisposeListener() {
      public void widgetDisposed(DisposeEvent e) {
        fFailureColor.dispose();
        fOKColor.dispose();
        fStoppedColor.dispose();
      }
    });
    Display display = parent.getDisplay();
    fFailureColor = new Color(display, 159, 80, 80);
    fOKColor = new Color(display, 95, 191, 95);
    fStoppedColor = new Color(display, 120, 120, 120);
  }

  private void paintStep(int startX, int endX) {
    GC gc = new GC(this);
    setStatusColor(gc);
    Rectangle rect = getClientArea();
    startX = Math.max(1, startX);
    gc.fillRectangle(startX, 1, endX - startX, rect.height - 2);
    gc.dispose();
  }

  private void setStatusColor(GC gc) {
    if (fIsTerminated)
      gc.setBackground(fStoppedColor);
    else if (fHasError)
      gc.setBackground(fFailureColor);
    else
      gc.setBackground(fOKColor);
  }

  private int scale() {
    Rectangle r = getClientArea();
    return (int) (fPercentDone * r.width - 2);
  }

  private void drawBevelRect(GC gc, int x, int y, int w, int h, Color topleft,
      Color bottomright) {
    gc.setForeground(topleft);
    gc.drawLine(x, y, x + w - 1, y);
    gc.drawLine(x, y, x, y + h - 1);

    gc.setForeground(bottomright);
    gc.drawLine(x + w, y, x + w, y + h);
    gc.drawLine(x, y + h, x + w, y + h);
  }

  private void paint(PaintEvent event) {
    GC gc = event.gc;
    Display disp = getDisplay();

    Rectangle rect = getClientArea();
    gc.fillRectangle(rect);
    drawBevelRect(gc, rect.x, rect.y, rect.width - 1, rect.height - 1, disp
        .getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW), disp
        .getSystemColor(SWT.COLOR_WIDGET_HIGHLIGHT_SHADOW));

    setStatusColor(gc);
    fColorBarWidth = Math.min(rect.width - 2, fColorBarWidth);
    if (fColorBarWidth > 0) {
      gc.fillRectangle(1, 1, fColorBarWidth, rect.height - 2);
    }
  }

  @Override
  public Point computeSize(int wHint, int hHint, boolean changed) {
    checkWidget();
    Point size = new Point(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    if (wHint != SWT.DEFAULT)
      size.x = wHint;
    if (hHint != SWT.DEFAULT)
      size.y = hHint;
    return size;
  }

  public void reset() {
    fPercentDone = 0;
    fColorBarWidth = 0;
    fHasError = false;
    fIsTerminated = false;
    redraw();
  }
  
  public void initializeFrom(TestGeneratorSession session) {
    reset();
    fHasError = session.getErrorCount() > 0;
    fIsTerminated = session.isTerminated();
    setPercentDone(session.getPercentDone());
  }
  
  public void setPercentDone(double percentDone) {
    fPercentDone = percentDone;
    int x = fColorBarWidth;

    fColorBarWidth = scale();

    if (fColorBarWidth >= (getClientArea().width - 2)) {
      fColorBarWidth = getClientArea().width - 1;
    }
    paintStep(x, fColorBarWidth);
  }
  
  public void terminate() {
    fIsTerminated = true;
    redraw();
  }
  
  public void error() {
    fHasError = true;
    redraw();
  }
  
}
