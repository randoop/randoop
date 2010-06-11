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
package org.apache.commons.jelly.tags.swing;

import java.awt.Color;
import javax.swing.BorderFactory;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;

import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.MissingAttributeException;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Creates an etched border.
 * The border will either be exported as a variable defined by the 'var' attribute
 * or will be set on the parent widget's border property
 *
 * @author <a href="mailto:robert@bull-enterprises.com">Robert McIntosh</a>
 * @version $Revision: 1.2 $
 */
public class EtchedBorderTag extends BorderTagSupport {

    /** The Log to which logging calls will be made. */
    private static final Log log = LogFactory.getLog(EtchedBorderTag.class);

    private int     etchType  = -1;
    private Color   highlight = null;
    private Color   shadow    = null;

    // Tag interface
    //-------------------------------------------------------------------------
    public void doTag(final XMLOutput output) throws MissingAttributeException, JellyTagException {
        if( highlight != null && shadow == null ) {
            throw new MissingAttributeException("shadow must be supplied when highlight is supplied");
        }
        if( shadow != null && highlight == null) {
            throw new MissingAttributeException("highlight must be supplied when shadow is supplied");
        }
        if( etchType != EtchedBorder.LOWERED || etchType != EtchedBorder.RAISED ) {
            if( log.isDebugEnabled() ) log.debug( "etchType set to [" + etchType + "], which is invalid. Reseting to -1" );
        }
        super.doTag(output);
    }

    // Properties
    //-------------------------------------------------------------------------
    /**
     * Sets the etch type. Must be either EtchedBorder.LOWERED or EtchedBorder.RAISED
     * @param type
     */
    public void setEtchType( int type ) {
        etchType = type;
    }

    /**
     * Sets the highlight color
     * @param highlight
     */
    public void setHighlight( Color highlight ) {
        this.highlight = highlight;
    }

    /**
     * Sets the shadow color
     * @param shadow
     */
    public void setTop( Color shadow ) {
        this.shadow = shadow;
    }

    /**
     * Factory method to create a new EtchedBorder instance.
     */
    protected Border createBorder() {
        if( etchType == -1 && shadow == null && highlight == null) {
            return BorderFactory.createEtchedBorder();
        }
        else if ( highlight != null && shadow != null && etchType > -1 ) {
            return BorderFactory.createEtchedBorder( etchType, highlight, shadow );
        }
        else {
            return BorderFactory.createEtchedBorder( highlight, shadow );
        }

    }

}
