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

package org.apache.commons.jelly.tags.antlr;

import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.jelly.TagSupport;
import org.apache.commons.jelly.JellyTagException;

public class GrammarTag extends TagSupport
{
    public GrammarTag()
    {
    }


    // Tag interface
    //-------------------------------------------------------------------------

    public void doTag(final XMLOutput output) throws JellyTagException
    {
        String grammar = getBodyText();

        AntlrTag antlr = (AntlrTag) findAncestorWithClass( AntlrTag.class );

        if ( antlr == null )
        {
            throw new JellyTagException( "<grammar> should only be used within an <antlr> block." );
        }

        antlr.addGrammar( grammar );
    }
}
