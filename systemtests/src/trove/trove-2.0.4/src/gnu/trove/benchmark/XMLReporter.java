///////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2001, Eric D. Friedman All Rights Reserved.
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
///////////////////////////////////////////////////////////////////////////////

package gnu.trove.benchmark;

import java.io.*;

/**
 *
 * Created: Thu Nov 22 20:49:24 2001
 *
 * @author Eric D. Friedman
 * @version $Id: XMLReporter.java,v 1.2 2006/11/10 23:27:59 robeden Exp $
 */

class XMLReporter implements Reporter {
    PrintWriter out;

    XMLReporter() {
        this.out = new PrintWriter(new OutputStreamWriter(System.out),
                                   true);
    }
        
    XMLReporter(PrintWriter out) {
        this.out = out;
    }

    public void report(Result result) {
        out.println("<result>");
        out.print("<desc>");
        out.print(result.getDescription());
        out.println("</desc>");
            
        out.print("<iterations>");
        out.print(result.getIterations());
        out.println("</iterations>");
            
        out.print("<theirTotal>");
        out.print(result.getTheirs());
        out.println("</theirTotal>");
            
        out.print("<theirAvg>");
        out.print(result.getTheirAvg());
        out.println("</theirAvg>");
            
        out.print("<ourTotal>");
        out.print(result.getOurs());
        out.println("</ourTotal>");
            
        out.print("<ourAvg>");
        out.print(result.getOurAvg());
        out.println("</ourAvg>");

        out.println("</result>");
    }

    public void start() {
        out.println("<?xml version=\"1.0\" encoding=\"ASCII\" ?>");
        out.println("<benchmark>");
        out.print("<env>");
        for (int i = 0; i < ENV_PROPS.length; i++) {
            String key = ENV_PROPS[i];
            out.print(System.getProperty(key));
            out.print(" ");
        }
        out.println("</env>");
    }

    public void finish() {
        out.println("</benchmark>");
    }
}
