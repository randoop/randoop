/*
* Copyright 2002-2004 The Apache Software Foundation
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.apache.commons.jelly.tags.swing;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.Script;
import org.apache.commons.jelly.TagSupport;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FocusListenerTag extends TagSupport
{
  protected static final Log log = LogFactory.getLog(FocusListenerTag.class);

  protected String var;
  protected Script gained;
  protected Script lost;

  /**
   * 
   */
  public FocusListenerTag()
  {
    super();
  }

  /**
  * @param var
  */
  public void setVar(String var)
  {
    this.var = var;
  }

  /**
   * @param gained
   */
  public void setGained(Script gained)
  {
    this.gained = gained;
  }

  /**
   * @param lost
   */
  public void setLost(Script lost)
  {
    this.lost = lost;
  }

  public void doTag(final XMLOutput output) throws JellyTagException
  {
    // now lets add this action to its parent if we have one
    ComponentTag tag = (ComponentTag)findAncestorWithClass(ComponentTag.class);
    if (tag != null)
    {
      FocusListener listener = new FocusListener()
      {
        public void focusGained(FocusEvent e)
        {
          invokeScript(output, e, gained);
        }

        public void focusLost(FocusEvent e)
        {
          invokeScript(output, e, lost);
        }
      };
      tag.addFocusListener(listener);
    }
  }

  protected void invokeScript(XMLOutput output, FocusEvent event, Script script)
  {
    if (var != null)
    {
      // define a variable of the event
      context.setVariable(var, event);
    }

    try
    {
      if (script != null)
      {
        script.run(context, output);
      }
      else
      {
        // invoke the body
        invokeBody(output);
      }
    }
    catch (Exception e)
    {
      log.error("Caught exception processing window event: " + event, e);
    }
  }

}
