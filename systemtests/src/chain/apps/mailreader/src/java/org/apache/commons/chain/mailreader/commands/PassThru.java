/*
 * $Header: /home/cvspublic/jakarta-commons/chain/apps/mailreader/src/java/org/apache/commons/chain/mailreader/commands/PassThru.java,v 1.2 2004/06/01 00:48:41 husted Exp $
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

/**
 * Dummy command that just returns false.
 * @version $Revision: 1.2 $ $Date: 2004/06/01 00:48:41 $
 */
public class PassThru implements Command {

    // See interface for Javadoc
    public boolean execute(Context context) throws Exception {

        return false;

    }

}
