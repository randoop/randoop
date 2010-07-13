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
package org.apache.commons.jelly.tags.swt.converters;

import java.util.StringTokenizer;

import org.apache.commons.beanutils.Converter;

import org.eclipse.swt.graphics.Point;

/**
 * A Converter that turns Strings in the form "x, y" into Point objects
 *
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version $Revision: 1.6 $
 */
public class PointConverter implements Converter {

    private static final PointConverter instance = new PointConverter();

    public static PointConverter getInstance() {
        return instance;
    }

    /**
     * Parsers a String in the form "x, y" into an SWT Point class
     * @param text
     * @return Point
     */
    public Point parse(String text) {
        StringTokenizer items = new StringTokenizer( text, "," );
        int x = 0;
        int y = 0;
        if ( items.hasMoreTokens() ) {
            x = parseNumber( items.nextToken() );
        }
        if ( items.hasMoreTokens() ) {
            y = parseNumber( items.nextToken() );
        }
        return new Point( x, y );
    }

    // Converter interface
    //-------------------------------------------------------------------------
    public Object convert(Class type, Object value) {
        Object answer = null;
        if ( value != null ) {
            String text = value.toString();
            answer = parse(text);
        }

        System.out.println("Converting value: " + value + " into: " + answer);

        return answer;
    }

    protected int parseNumber(String text) {
        text = text.trim();
        return Integer.parseInt(text.trim());
    }

}