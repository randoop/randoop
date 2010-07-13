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
package org.apache.commons.jelly.ant.task;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildException;

/*

<taskdef
   name="nested"
   classname="jellybug.NestedTask"
>
   <classpath>
      <pathelement path="somewhere"/>
   </classpath>
</taskdef>

<nested>
   <ding/>
   <dang/>
   <dong/>
   <hiphop/>
   <wontstop/>
   <tillyoudrop/>
   <hipHop/>
   <wontStop/>
   <tillYouDrop/>
</nested>

Ant:
   [nested] a
   [nested] b
   [nested] c
   [nested] d
   [nested] e
   [nested] f
   [nested] g
   [nested] h
   [nested] i

Maven/Jelly:
a
b
c
d
e
f
g
h
i

*/

/**
 * A sample Task to test out the Ant introspection logic
 *
 * @author Aslak Hellesøy (aslak.hellesoy@bekk.no)
 * @version $Revision: 1.8 $
 */
public class DummyTask extends Task {
    private int i = 0;
    private String[] messages = { "a", "b", "c", "d", "e", "f", "g", "h", "i" };
    private boolean force;

    public void execute() throws BuildException {
        if (!force) {
            throw new BuildException("Should have set force to be true!");
        }
    }

    public Thingy createDing() {
        System.out.println("createDing: " + messages[i++]);
        return new Thingy();
    }

    public void addDang(Thingy thingy) {
        System.out.println("addDang: " + messages[i++]);
    }

    public void addConfiguredDong(Thingy thingy) {
        System.out.println("addConfiguredDong: " + messages[i++]);
    }

    public Thingy createHipHop() {
        System.out.println("createHipHop: " + messages[i++]);
        return new Thingy();
    }

    public void addWontStop(Thingy thingy) {
        System.out.println("addWontStop: " + messages[i++]);
    }

    public void addConfiguredTillYouDrop(Thingy thingy) {
        System.out.println("addConfiguredTillYouDrop: " + messages[i++]);
    }

    public boolean isForce() {
        return force;
    }

    public void setForce(boolean force) {
        this.force = force;
    }

    public static class Thingy {
    }
}