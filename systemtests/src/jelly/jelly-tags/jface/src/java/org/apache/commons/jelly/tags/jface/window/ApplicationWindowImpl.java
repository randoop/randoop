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
package org.apache.commons.jelly.tags.jface.window;

import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * This is the default implementation for a ApplicationWindowTag
 *
 * @author <a href="mailto:ckl@dacelo.nl">Christiaan ten Klooster</a>
 */
public class ApplicationWindowImpl extends ApplicationWindow {

    /**
     * @param shell
     */
    public ApplicationWindowImpl(Shell parentShell) {

        super(parentShell);

        // default at all
        addMenuBar();
        addStatusLine();
        addToolBar(SWT.NULL);

        setBlockOnOpen(true);

        // create window
        create();
    }

    /*
     * override to make public
     * @see org.eclipse.jface.window.Window#getContents()
     */
    public Control getContents() {
        return super.getContents();
    }

}
