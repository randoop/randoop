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

/**
 * A default Factory implementation that creates new instances from a bean class
 *
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version $Revision: 1.7 $
 */
public class BeanFactory implements Factory {

    private Class beanClass;

    public BeanFactory(Class beanClass) {
        this.beanClass = beanClass;
    }

    /**
     * Create a new component instance
     */
    public Object newInstance() throws InstantiationException {
        try {
          return beanClass.newInstance();
        } catch (IllegalAccessException e) {
            throw new InstantiationException(e.toString());
        }
    }
}
