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
package org.apache.commons.jelly.tags.interaction;

import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;

import org.apache.commons.jelly.TagSupport;
import org.apache.commons.jelly.XMLOutput;

/**
 * Jelly Tag that asks the user a question, and puts his answer into
 * a variable, with the attribute "answer".
 * This variable may be reused further as any other Jelly variable.
 * @author <a href="mailto:smor@hasgard.net">Stéphane Mor</a>
 */
public class AskTag extends TagSupport
{
    /** The question to ask to the user */
    private String question;

    /**
     * The variable in which we will stock the user's input.
     * This defaults to "interact.answer".
     */
    private String answer = "interact.answer";

    /** The default value, if the user doesn't answer */
    private String defaultInput;

    /** The user's input */
    private String input = "";

    /** The prompt to display before the user input */
    private String prompt = ">";

    /**
     * Sets the question to ask to the user. If a "default" attribute
     * is present, it will appear inside [].
     * @param question The question to ask to the user
     */
    public void setQuestion(String question)
    {
        this.question = question;
    }

    /**
     * Sets the name of the variable that will hold the answer
     * This defaults to "interact.answer".
     * @param answer the name of the variable that will hold the answer
     */
    public void setAnswer(String answer)
    {
        this.answer = answer;
    }

    /**
     * Sets the default answer to the question.
     * If it is present, it will appear inside [].
     * @param default the default answer to the question
     */
    public void setDefault(String defaultInput)
    {
        this.defaultInput = defaultInput;
    }

    /**
     * Sets the prompt that will be displayed before the user's input.
     * @param promt the prompt that will be displayed before the user's input.
     */
    public void setPrompt(String prompt)
    {
        this.prompt = prompt;
    }


    /**
     * Perform functionality provided by the tag
     * @param output the place to write output
     */
    public void doTag(XMLOutput output)
    {
        if (question != null)
        {
            if (defaultInput != null)
            {
                System.out.println(question + " [" + defaultInput + "]");
            }
            else
            {
                System.out.println(question);
            }
            // The prompt should be just before the user's input,
            // but it doesn't work ...
            //System.out.print(prompt + " ");
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        try {
            input = br.readLine();
            if (defaultInput != null && input.trim().equals(""))
            {
                input = defaultInput;
            }
        } catch (IOException ioe) {
        }
        context.setVariable(answer, input);
    }
}
