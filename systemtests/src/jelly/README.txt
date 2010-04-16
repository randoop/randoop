  Copyright 2002,2004 The Apache Software Foundation.
  
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
  
       http://www.apache.org/licenses/LICENSE-2.0
  
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.

Jelly
=====

The primary build tool for this project is Maven. 
So all you should need to do is install Maven and just type

	maven

Some common maven goals for building and testing this project are

	clean    : cleans up the build so new builds will start from fresh
	test     : just run the unit tests
	jar      : compiles, runs unit tests and if they work build the jar
	javadoc  : creates the javadoc
	site     : build the complete documentation with reports, javadoc etc
	dist     : creates a distribution


Jelly contains a number of individual tag libraries which can be built 
by themselves by changing to the directory jelly-tags/foo and performing the
above commands to build the library you're interested in.

			
For more help using Maven please go to

  http://maven.apache.org

Maven also supports the auto-generation of Ant build files so
you may also be able to use Ant to build the code.

Enjoy!
