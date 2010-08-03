/*
 * Copyright 1999-2004 The Apache Software Foundation
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
package org.apache.commons.chain.impl;


import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;


/**
 * <p>Implementation of {@link Command} that logs its identifier and
 * and delegates to the rest of the chain.</p>
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.5 $ $Date: 2004/02/25 00:01:05 $
 */

public class DelegatingCommand extends NonDelegatingCommand {


    // ------------------------------------------------------------ Constructor


    public DelegatingCommand() {
    this("");
    }


    // Construct an instance that will log the specified identifier
    public DelegatingCommand(String id) {
        super(id);
    }


    // -------------------------------------------------------- Command Methods


    // Execution method for this Command
    public boolean execute(Context context) throws Exception {

        super.execute(context);
        return (false);

    }


}
