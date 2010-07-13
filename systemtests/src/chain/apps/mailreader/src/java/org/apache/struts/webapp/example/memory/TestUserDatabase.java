/*
 * Copyright 1999-2002,2004 The Apache Software Foundation.
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
package org.apache.struts.webapp.example.memory;

import org.apache.struts.webapp.example.ExpiredPasswordException;
import org.apache.struts.webapp.example.User;

/**
 * <p>Help test exception handling by throwing exceptions when "magic" user names are requested.</p>
 */
public final class TestUserDatabase extends MemoryUserDatabase {


    /**
     * If the username is "expired" throw an ExpiredPasswordException
     * to simulate a business exception.
     * If the username is "arithmetic" throw an Aritmetic exception to
     * simulate a system exception.
     * Otherwise, delegate to MemoryDatabase.
     * @param username
     * @return
     */
    public User findUser(String username) throws ExpiredPasswordException {

        if ("expired".equals(username)) throw new ExpiredPasswordException("Testing ExpiredPasswordException ...");
        if ("arithmetic".equals(username)) throw new ArithmeticException();
        return super.findUser(username);
    }

}
