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
package org.apache.commons.jelly.tags.fmt;

import org.apache.commons.jelly.TagLibrary;

/** Describes the tag library for tags in JSTL.
 *
 * @author <a href="mailto:willievu@yahoo.com">Willie Vu</a>
 * @version 1.2
 *
 * @task implement &lt;fmt:formatNumber&gt;
 * @task implement &lt;fmt:parseNumber&gt;
 * @task implement &lt;fmt:parseDate&gt;
 * @task decide how to support &lt;fmt:requestEncoding&gt;
 */
public class FmtTagLibrary extends TagLibrary {

    /** Creates a new instance of FmtTagLibrary */
    public FmtTagLibrary() {
        registerTag("bundle", BundleTag.class);
        registerTag("formatDate", FormatDateTag.class);
        registerTag("message", MessageTag.class);
        registerTag("param", ParamTag.class);
        registerTag("setBundle", SetBundleTag.class);
        registerTag("setLocale", SetLocaleTag.class);
        registerTag("setTimeZone", SetTimeZoneTag.class);
        registerTag("timeZone", TimeZoneTag.class);
    }

}
