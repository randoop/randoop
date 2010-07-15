/*

 * $Header: /home/cvspublic/jakarta-commons/chain/apps/mailreader/src/test/org/apache/commons/mailreader/ProfileCheckTest.java,v 1.1 2004/06/01 00:52:47 husted Exp $
 * $Revision: 1.1 $
 * $Date: 2004/06/01 00:52:47 $
 *
 * Copyright 2000-2004 Apache Software Foundation
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
package org.apache.commons.mailreader;

import junit.framework.TestCase;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.apache.commons.chain.mailreader.commands.ProfileCheck;
import org.apache.commons.chain.mailreader.commands.Profile;
import org.apache.commons.chain.impl.ContextBase;

public class ProfileCheckTest extends TestCase {

    public void testProfileCheckNeed() {

        Context context = new ContextBase();
        Command command = new ProfileCheck();
        try {
            command.execute(context);
        } catch (Exception e) {
            fail(e.getMessage());
        }

        Profile profile = (Profile) context.get(Profile.PROFILE_KEY);
        assertNotNull("Missing Profile", profile);

    }

    public void testProfileCheckHave() {

        Profile profile = new Profile();
        Context context = new ContextBase();
        context.put(Profile.PROFILE_KEY, profile);

        Command command = new ProfileCheck();
        try {
            command.execute(context);
        } catch (Exception e) {
            fail(e.getMessage());
        }

        Profile profile2 = (Profile) context.get(Profile.PROFILE_KEY);
        assertNotNull("Missing Profile", profile2);
        assertEquals("Profile instance changed", profile, profile2);

    }

}
