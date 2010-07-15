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
import org.eclipse.swt.graphics.RGB;

/**
 * A Converter that converts Strings in the form "#uuuuuu" or "x,y,z" into a RGB object
 *
 * @author <a href="mailto:ckl@dacelo.nl">Christiaan ten Klooster</a>
 * @version $Revision: 1.5 $
 */
public class ColorConverter implements Converter {

    private static final ColorConverter instance = new ColorConverter();

    private static String usageText =
        "Color value should be in the form of '#xxxxxx' or 'x,y,z'";

    public static ColorConverter getInstance() {
        return instance;
    }

    /**
     * Parsers a String in the form "x, y, z" into an SWT RGB class
     * @param value
     * @return RGB
     */
    protected RGB parseRGB(String value) {
        StringTokenizer items = new StringTokenizer(value, ",");
        int red = 0;
        int green = 0;
        int blue = 0;
        if (items.hasMoreTokens()) {
            red = parseNumber(items.nextToken());
        }
        if (items.hasMoreTokens()) {
            green = parseNumber(items.nextToken());
        }
        if (items.hasMoreTokens()) {
            blue = parseNumber(items.nextToken());
        }
        return new RGB(red, green, blue);
    }

    /**
     * Parsers a String in the form "#xxxxxx" into an SWT RGB class
     * @param value
     * @return RGB
     */
    protected RGB parseHtml(String value) {
        if (value.length() != 7) {
            throw new IllegalArgumentException(usageText);
        }
        int colorValue = 0;
        try {
            colorValue = Integer.parseInt(value.substring(1), 16);
            java.awt.Color swingColor = new java.awt.Color(colorValue);
            return new RGB(
                swingColor.getRed(),
                swingColor.getGreen(),
                swingColor.getBlue());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(
                value + "is not a valid Html color\n " + ex);
        }
    }

    /**
     * Parse a String
     */
    public RGB parse(String value) {
        if (value.length() <= 1) {
            throw new IllegalArgumentException(usageText);
        }

        if (value.charAt(0) == '#') {
            return parseHtml(value);
        } else if (value.indexOf(',') != -1) {
            return parseRGB(value);
        } else {
            throw new IllegalArgumentException(usageText);
        }
    }

    // Converter interface
    //-------------------------------------------------------------------------
    public Object convert(Class type, Object value) {
        Object answer = null;
        if (value != null) {
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
