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
package org.apache.commons.jelly.tags.define;

import org.apache.commons.jelly.TagLibrary;


/**
 * This is a tag library that allows you to define new tag libraries at run time.
 * Thus tag libraries and tags can be easily implemented in Jelly rather than
 * in Java code.
 *
 * Please see the individual tag classes for more information, particularly
 * {@link TaglibTag Taglib tag} and {@link TagTag Tag tag}.
 *
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version $Revision: 1.5 $
 */
public class DefineTagLibrary extends TagLibrary {

    public DefineTagLibrary() {
        registerTag( "taglib", TaglibTag.class );
        registerTag( "tag", TagTag.class );
        registerTag( "bean", BeanTag.class );
        registerTag( "dynaBean", DynaBeanTag.class );
        registerTag( "jellybean", JellyBeanTag.class );
        registerTag( "attribute", AttributeTag.class );
        registerTag( "invokeBody", InvokeBodyTag.class );
        registerTag( "script", ScriptTag.class );
        registerTag( "invoke", InvokeTag.class );
        registerTag( "classLoader", ClassLoaderTag.class );
        registerTag( "extend", ExtendTag.class );
        registerTag( "super", SuperTag.class );
    }
}
