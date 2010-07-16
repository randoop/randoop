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

/**
 *
 * Created: Thu Nov 22 20:47:16 2001
 *
 * @author Eric D. Friedman
 * @version $Id: Result.java,v 1.3 2008/05/07 19:26:31 robeden Exp $
 */
class Result {
    long theirs;
    long ours;
    int iterations;
    String description;
        
    /**
     * Gets the value of theirs
     *
     * @return the value of theirs
     */
    public long getTheirs() {
        return this.theirs;
    }

    /**
     * Sets the value of theirs
     *
     * @param argTheirs Value to assign to this.theirs
     */
    public void setTheirs(long argTheirs){
        this.theirs = argTheirs;
    }

    /**
     * Gets the value of ours
     *
     * @return the value of ours
     */
    public long getOurs() {
        return this.ours;
    }

    /**
     * Sets the value of ours
     *
     * @param argOurs Value to assign to this.ours
     */
    public void setOurs(long argOurs){
        this.ours = argOurs;
    }

    /**
     * Gets the value of theirAvg
     *
     * @return the value of theirAvg
     */
    public long getTheirAvg() {
        return theirs / iterations;
    }

    /**
     * Gets the value of ourAvg
     *
     * @return the value of ourAvg
     */
    public long getOurAvg() {
        return ours / iterations;
    }

    /**
     * Gets the value of iterations
     *
     * @return the value of iterations
     */
    public int getIterations() {
        return this.iterations;
    }

    /**
     * Sets the value of iterations
     *
     * @param argIterations Value to assign to this.iterations
     */
    public void setIterations(int argIterations){
        this.iterations = argIterations;
    }

    /**
     * Gets the value of description
     *
     * @return the value of description
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Sets the value of description
     *
     * @param argDescription Value to assign to this.description
     */
    public void setDescription(String argDescription){
        this.description = argDescription;
    }

    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append(getDescription() + "\n");
        b.append("Iterations: " + getIterations() + "\n");
        b.append("Their total (msec): " + getTheirs() + "\n");
        b.append("Our total (msec): " + getOurs() + "\n");
        b.append("Their average (msec): " + getTheirAvg() + "\n");
        b.append("Our average (msec): " + getOurAvg() + "\n");
        return b.toString();
    }
}
