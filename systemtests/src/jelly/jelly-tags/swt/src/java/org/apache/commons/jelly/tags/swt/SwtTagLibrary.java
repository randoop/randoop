/*
 * Copyright 2002,2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.jelly.tags.swt;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.jelly.JellyException;
import org.apache.commons.jelly.Tag;
import org.apache.commons.jelly.TagLibrary;
import org.apache.commons.jelly.impl.TagFactory;
import org.apache.commons.jelly.tags.swt.converters.ColorConverter;
import org.apache.commons.jelly.tags.swt.converters.PointConverter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.TableTree;
import org.eclipse.swt.custom.TableTreeItem;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Caret;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.CoolItem;
import org.eclipse.swt.widgets.Decorations;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.FontDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Sash;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tracker;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.xml.sax.Attributes;

/**
 * A Jelly custom tag library that creates SWT user interfaces
 *
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version 1.1
 */
public class SwtTagLibrary extends TagLibrary {

    /** The Log to which logging calls will be made. */
    private static final Log log = LogFactory.getLog(SwtTagLibrary.class);

    static {
        // register the various beanutils Converters from Strings to various SWT types
        ConvertUtils.register( new PointConverter(), Point.class );
        ConvertUtils.register( new ColorConverter(), Color.class );
    }

    public SwtTagLibrary() {
        // widgets
        registerWidgetTag( "button", Button.class, SWT.BORDER | SWT.PUSH | SWT.CENTER );
        registerWidgetTag( "canvas", Canvas.class );
        registerWidgetTag( "caret", Caret.class );
        registerWidgetTag( "combo", Combo.class, SWT.DROP_DOWN );
        registerWidgetTag( "composite", Composite.class );
         registerWidgetTag( "scrolledComposite", ScrolledComposite.class, SWT.H_SCROLL | SWT.V_SCROLL);
        registerWidgetTag( "coolBar", CoolBar.class, SWT.VERTICAL );
        registerWidgetTag( "coolItem", CoolItem.class );
        registerWidgetTag( "decorations", Decorations.class );
        registerWidgetTag( "group", Group.class );
        registerWidgetTag( "label", Label.class, SWT.HORIZONTAL | SWT.SHADOW_IN );
        registerWidgetTag( "list", List.class );
        registerMenuTag( "menu", SWT.DEFAULT );
        registerMenuTag( "menuBar", SWT.BAR );
        registerWidgetTag( "menuSeparator", MenuItem.class, SWT.SEPARATOR );
        registerWidgetTag( "menuItem", MenuItem.class );
        registerWidgetTag( "messageBox", MessageBox.class );
        registerWidgetTag( "progressBar", ProgressBar.class, SWT.HORIZONTAL );
        registerWidgetTag( "sash", Sash.class );
        registerWidgetTag( "scale", Scale.class );
        registerWidgetTag( "shell", Shell.class, SWT.BORDER | SWT.CLOSE | SWT.MIN | SWT.MAX | SWT.RESIZE | SWT.TITLE );
        registerWidgetTag( "slider", Slider.class );
        registerWidgetTag( "tabFolder", TabFolder.class );
        registerWidgetTag( "tabItem", TabItem.class );
        registerWidgetTag( "table", Table.class, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION );
        registerWidgetTag( "tableColumn", TableColumn.class );
        registerWidgetTag( "tableItem", TableItem.class );
        registerWidgetTag( "text", Text.class );
        registerWidgetTag( "toolBar", ToolBar.class, SWT.VERTICAL );
        registerWidgetTag( "toolItem", ToolItem.class );
        registerWidgetTag( "tracker", Tracker.class );
        registerWidgetTag( "tree", Tree.class, SWT.MULTI );
        registerWidgetTag( "treeItem", TreeItem.class );

        // custom widgets
        registerWidgetTag( "cTabFolder", CTabFolder.class );
        registerWidgetTag( "cTabItem", CTabItem.class );
        registerWidgetTag( "tableTree", TableTree.class );
        registerWidgetTag( "tableTreeItem", TableTreeItem.class );

        // layouts
        registerLayoutTag("fillLayout", FillLayout.class);
        registerLayoutTag("gridLayout", GridLayout.class);
        registerLayoutTag("rowLayout", RowLayout.class);

        // layout data objects
        registerLayoutDataTag( "gridData", GridData.class );
        registerLayoutDataTag( "rowData", RowData.class );

        // dialogs
        registerDialogTag( "colorDialog", ColorDialog.class );
        registerDialogTag( "directoryDialog", DirectoryDialog.class );
        registerDialogTag( "fileDialog", FileDialog.class );
        registerDialogTag( "fontDialog", FontDialog.class );

        // events
        registerTag("onEvent", OnEventTag.class);

        // other tags
        registerTag("color", ColorTag.class);
        registerTag("colour", FontTag.class);
        registerTag("font", FontTag.class);
        registerTag("gc", GCTag.class);
        registerTag("image", ImageTag.class);

    }

    /**
     * Register a layout tag for the given name
     */
    protected void registerLayoutTag(String name, final Class layoutClass) {
        registerTagFactory(
            name,
            new TagFactory() {
                /**
                 * @see org.apache.commons.jelly.impl.TagFactory#createTag(java.lang.String, org.xml.sax.Attributes)
                 */
                public Tag createTag(String name, Attributes attributes)
                    throws JellyException {
                    return new LayoutTag(layoutClass);
                }
            }
        );
    }

    /**
     * Register a layout data tag for the given name
     */
    protected void registerLayoutDataTag(String name, final Class layoutDataClass) {
        registerTagFactory(
            name,
            new TagFactory() {
                /**
                 * @see org.apache.commons.jelly.impl.TagFactory#createTag(java.lang.String, org.xml.sax.Attributes)
                 */
                public Tag createTag(String name, Attributes attributes)
                    throws JellyException {
                    return new LayoutDataTag(layoutDataClass);
                }
            }
        );
    }

    /**
     * Register a widget tag for the given name
     */
    protected void registerWidgetTag(String name, Class widgetClass) {
        registerWidgetTag(name, widgetClass, SWT.NULL);
    }

    /**
     * Register a widget tag for the given name
     */
    protected void registerWidgetTag(String name, final Class widgetClass, final int style) {
        registerTagFactory(
            name,
            new TagFactory() {
                /**
                 * @see org.apache.commons.jelly.impl.TagFactory#createTag(java.lang.String, org.xml.sax.Attributes)
                 */
                public Tag createTag(String name, Attributes attributes)
                    throws JellyException {
                    return new WidgetTag(widgetClass, style);
                }
            }
        );
    }

    /**
     * Register a registerDialogTag tag for the given name
     */
    protected void registerDialogTag(String name, Class widgetClass) {
        registerDialogTag(name, widgetClass, SWT.NULL);
    }

    /**
       * Register a dialog tag for the given name
       */
    protected void registerDialogTag(String name, final Class widgetClass, final int style) {
          registerTagFactory(
              name,
              new TagFactory() {
                  /**
                   * @see org.apache.commons.jelly.impl.TagFactory#createTag(java.lang.String, org.xml.sax.Attributes)
                   */
                  public Tag createTag(String name, Attributes attributes)
                      throws JellyException {
                      return new DialogTag(widgetClass, style);
                  }
              }
          );
      }

    /**
     * Register a menu tag for the given name and style
     */
    protected void registerMenuTag(String name, final int style) {
        registerTagFactory(
            name,
            new TagFactory() {
                /**
                 * @see org.apache.commons.jelly.impl.TagFactory#createTag(java.lang.String, org.xml.sax.Attributes)
                 */
                public Tag createTag(String name, Attributes attributes)
                    throws JellyException {
                    return new MenuTag(style);
                }
            }
        );
    }



}
