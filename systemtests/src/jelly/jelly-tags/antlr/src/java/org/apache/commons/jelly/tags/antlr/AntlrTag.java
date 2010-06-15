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
import org.apache.commons.jelly.MissingAttributeException;
import org.apache.commons.jelly.JellyTagException;

import antlr.Tool;

import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.security.Permission;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

public class AntlrTag extends TagSupport
{
    private List grammars;
    private File outputDir;

    public AntlrTag()
    {
        this.grammars = new ArrayList( 1 );
    }


    // Tag interface
    //-------------------------------------------------------------------------

    /**
     * Evaluate the body to register all the various goals and pre/post conditions
     * then run all the current targets
     */
    public void doTag(final XMLOutput output) throws MissingAttributeException, JellyTagException
    {
        if ( this.outputDir == null )
        {
            throw new MissingAttributeException( "outputDir" );
        }

        invokeBody( output );

        Iterator grammarIter = this.grammars.iterator();
        String eachGrammar = null;

        String sourceDir = (String) getContext().getVariable( "maven.antlr.src.dir" );
        File grammar = null;

        while ( grammarIter.hasNext() )
        {
            eachGrammar = ((String) grammarIter.next()).trim();

            grammar = new File( sourceDir,
                                eachGrammar );

            File generated = getGeneratedFile( grammar.getPath() );

            if ( generated.exists() )
            {
                if ( generated.lastModified() > grammar.lastModified() )
                {
                    // it's more recent, skip.
                    return;
                }
            }

            if ( ! generated.getParentFile().exists() )
            {
                generated.getParentFile().mkdirs();
            }

            String[] args = new String[]
                {
                    "-o",
                    generated.getParentFile().getPath(),
                    grammar.getPath(),
                };

            SecurityManager oldSm = System.getSecurityManager();

            System.setSecurityManager( NoExitSecurityManager.INSTANCE );

            try
            {
                Tool.main( args );
            }
            catch (SecurityException e)
            {
                if ( ! e.getMessage().equals( "exitVM-0" ) )
                {
                    throw new JellyTagException( e );
                }
            }
            finally
            {
                System.setSecurityManager( oldSm );
            }
        }
    }

    protected File getGeneratedFile(String grammar) throws JellyTagException
    {
        File grammarFile = new File( grammar );

        String generatedFileName = null;

        String className = null;
        String packageName = "";

        try {

            BufferedReader in = new BufferedReader(new FileReader(grammar));

            String line;
            while ((line = in.readLine()) != null) {
                line = line.trim();
                int extendsIndex = line.indexOf(" extends ");
                if (line.startsWith("class ") &&  extendsIndex > -1) {
                    generatedFileName = line.substring(6, extendsIndex).trim();
                    break;
                }
                else if ( line.startsWith( "package" ) ) {
                    packageName = line.substring( 8 ).trim();
                }
            }
            in.close();
        } catch (Exception e) {
            throw new JellyTagException("Unable to determine generated class",
                                     e);
        }
        if (generatedFileName == null) {
            return null;
        }

        File genFile = null;

        if ( "".equals( packageName ) )
        {
            genFile = new File( getOutputDir(),
                                generatedFileName + ".java" );
        }
        else
        {
            String packagePath = packageName.replace( '.',
                                                      File.separatorChar );

            packagePath = packagePath.replace( ';',
                                               File.separatorChar );

            genFile = new File( new File( getOutputDir(), packagePath),
                                generatedFileName + ".java" );
        }

        return genFile;
    }

    void addGrammar(String grammar)
    {
        this.grammars.add( grammar );
    }

    public void setOutputDir(File outputDir)
    {
        this.outputDir = outputDir;
    }

    public File getOutputDir()
    {
        return this.outputDir;
    }
}

class NoExitSecurityManager extends SecurityManager
{
    static final NoExitSecurityManager INSTANCE = new NoExitSecurityManager();

    private NoExitSecurityManager()
    {
    }

    public void checkPermission(Permission permission)
    {
    }

    public void checkExit(int status)
    {
        throw new SecurityException( "exitVM-" + status );
    }
}
