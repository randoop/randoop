/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.cli;

/**
 * @author Emmanuel Bourg
 * @version $Revision: 695410 $, $Date: 2008-09-15 03:25:38 -0700 (Mon, 15 Sep 2008) $
 */
public class BasicParserTest extends ParserTestCase
{
    public void setUp()
    {
        super.setUp();
        parser = new BasicParser();
    }

    public void testPropertiesOption() throws Exception
    {
        // not supported by the BasicParser
    }

    public void testShortWithEqual() throws Exception
    {
        // not supported by the BasicParser
    }

    public void testShortWithoutEqual() throws Exception
    {
        // not supported by the BasicParser
    }

    public void testLongWithEqual() throws Exception
    {
        // not supported by the BasicParser
    }

    public void testLongWithEqualSingleDash() throws Exception
    {
        // not supported by the BasicParser
    }
}
