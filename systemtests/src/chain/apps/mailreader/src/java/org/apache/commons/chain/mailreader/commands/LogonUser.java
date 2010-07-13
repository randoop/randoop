/*
 * $Header: /home/cvspublic/jakarta-commons/chain/apps/mailreader/src/java/org/apache/commons/chain/mailreader/commands/LogonUser.java,v 1.2 2004/06/01 00:48:41 husted Exp $
 * $Revision: 1.2 $
 * $Date: 2004/06/01 00:48:41 $
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
package org.apache.commons.chain.mailreader.commands;

import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.apache.commons.chain.mailreader.MailReader;
import org.apache.struts.webapp.example.User;
import org.apache.struts.webapp.example.UserDatabase;

/**
 * Find username in database, confirm credentials, and set as current user.
 */
public class LogonUser implements Command {

    /**
     * Return true if null or empty.
     * @param string
     * @return true if null or empty
     */
    static boolean isBlank(String string) {
        return ((string == null) || (string.trim().length() == 0));
    }

    // See interface for Javadoc
    public boolean execute(Context context) throws Exception {

        MailReader app = (MailReader) context;
        Context input = app.getInput();
        String username = (String) input.get(MailReader.PN_USERNAME);
        String password = (String) input.get(MailReader.PN_PASSWORD);
        UserDatabase database = app.getDatabase();
        User user = database.findUser(username);
        if (null != user) {
            String dbPassword = user.getPassword();
            if (!password.equals(dbPassword)) user = null;
        }

        app.setUser(user);

        return false;
    }
}
