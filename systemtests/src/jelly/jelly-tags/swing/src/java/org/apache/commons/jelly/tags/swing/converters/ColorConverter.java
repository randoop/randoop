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
package org.apache.commons.jelly.tags.swing.converters;

import java.awt.Color;
import java.awt.SystemColor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.apache.commons.beanutils.Converter;

/**
 * A Converter that turns Strings in the form "#uuuuuu" (as RGB triple)
 * or the name of one of the {@link Color}-constants of the class
 * {@link Color} or {@link SystemColor}.
 *    <p>
 *    TODO: provide support of ARGB colors as well.<br>
 * Future: provide support for color-spaces, indexed colors...
 *        (in particular theme-based colors)
 *
 * @author <a href="mailto:paul@activemath.org">Paul Libbrecht</a>
 * @version $Revision: $
 */
public class ColorConverter implements Converter {

    private static String usageText =
        "A color is encoded as a java.awt.Color name or a #xxxxxx triple of hex-bytes.";

    public Object convert(Class type, Object value) {
        if (value != null) {
            String s = value.toString();
            if (s.length() <= 1) {
                throw new IllegalArgumentException(usageText);
            }
            if (s.charAt(0) == '#') {
                if (s.length() != 7) {
                    throw new IllegalArgumentException(usageText);
                }
                int colorValue = 0;
                try {
                    colorValue = Integer.parseInt(s.substring(1), 16);
                    return new Color(colorValue);
                }
                catch (NumberFormatException ex) {
                    throw new IllegalArgumentException(
                        "Can't parse \""
                            + s
                            + "\" as an hexadecimal number: "
                            + ex);
                }
            }
            else {
                // a color name
                try {
                    // could it be this is already somewhere: get the value of  a static final by string
                    Field f = SystemColor.class.getField(s);
                    if (f == null
                        || !Modifier.isStatic(f.getModifiers())
                        || !Modifier.isFinal(f.getModifiers())
                        || !Modifier.isPublic(f.getModifiers())
                        || !Color.class.isAssignableFrom(f.getType())) {

                        throw new IllegalArgumentException(usageText);
                    }
                    return (Color) f.get(SystemColor.class);
                }
                catch (Exception ex) {
                    throw new IllegalArgumentException(
                        "Can't parse \"" + s + "\" as a color-name: " + ex);
                }
            }
        }
        return null;
    }

}
