<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang=en lang=en>
<HEAD><meta http-equiv=content-type content="text/html; charset=utf-8" />
	<link rel=stylesheet href="main.css" type="text/css" />
<title>Randoop for Java</title>
</HEAD>
<?php
include_once "preface.php";
?>

<p>

<h1>Index</h1>

<ul>
  <li><a href="#running"><b>Running Randoop</b></a>
  <li><a href="#generating"><b>Generating JUnit Tests</b></a>
  <li><a href="#interpreting"><b>Interpreting the Results</b></a>
  <li><a href="#commands"><b>Randoop commands</b></a>
  <ul>
    <?php include("randoop_commands_list.php"); ?>
  </ul>
</ul>

<p>

<a name="running">
<h2>Running Randoop</h2>
</a>
<p>

The easiest way to run Randoop is by adding to your classpath the file
<tt>randoop.jar</tt>, provided with the Randoop distribution.  Run
Randoop by invoking its main class
<tt>randoop.main.Main</tt>. Randoop's interface is command-based: it
expects a specific <i>command</i> as the first argument, followed by
command arguments. (Currently, Randoop includes only two commands,
<b><tt>help</tt></b> and <b><tt>gentests</tt></b>.) For example, to
run the <tt>help</tt> command, do:


<div class="code"><pre>
java -classpath randoop.jar randoop.main.Main <b><tt>help</tt></b>
</pre></div>

Randoop will print out something like this:

<div class="code"><pre>
Randoop  is a command-line tool that creates unit tests for Java.
It accepts one of the commands listed below. For the user manual,
please visit http://people.csail.mit.edu/cpacheco/randoop/

Type `help' followed by a command name to see documentation.

Commands:

gentests -- Generates unit tests for a set of classes.
help -- Displays a help message for a given command.
</pre></div>

<p>

As the above message states, to get help on a specific command, run
Randoop with `<tt>help</tt> <i>command-name</i>' as arguments. For
example:

<div class="code"><pre>
java -classpath randoop.jar randoop.main.Main <b><tt>help gentests</tt></b>
</pre></div>

Randoop will output the syntax, description and options related to the
<tt>gentests</tt> command.

<a name="generating">
<h2>Generating JUnit tests</h2>
</a>
  
<p>This section shows an example use of Randoop with the goal of
generating JUnit test cases. Imagine we want to generate tests for the
class <tt>java.util.Collections</tt>, a utility class that defines
several methods for manipulating collections.

<p> The first important thing to keep in mind is that Randoop will
<b><i>only generate tests using the classes you specify</i></b>. In
order to effectively test <tt>Collections</tt>, You should probably
also specify some helper classes, including a class that generates
collections. For this example, we will add <tt>java.util.TreeSet</tt>
to the mix.

<p> Invoke Randoop as follows (all in a single line):

<p>
<div class="code"><pre>
java -classpath randoop.jar randoop.main.Main <b><tt>gentests</tt></b>
   --testclass=java.util.TreeSet
   --testclass=java.util.Collections
   --timelimit=10
</pre></div>

<p>Alternatively, you can create a file that lists the names of the classes under test, and use the
<tt>--classlist</tt> option:

<p>
<div class="code"><pre>
java -classpath randoop.jar randoop.main.Main <b><tt>gentests</tt></b>
   --classlist=myclasses.txt
   --timelimit=10
</pre></div>

Where the contents of <tt>myclasses.txt</tt> are as follows.

<p>
<b><tt>myclasses.txt:</tt></b>
<div class="code"><pre>
java.util.Collections
java.util.TreeSet
</pre></div>

<p>After 10 seconds, Randoop stops generating tests (if you omit the
<tt>--timelimit</tt> option, Randoop's default behavior is to generate
tests for 1 minute).  The last thing Randoop prints out is the name of
the JUnit files containing the tests it generated. You should see a
message similar to the following:

<p>
<div class="code"><pre>
Created file: my/home/directory/RandoopTest0.java
Created file: my/home/directory/RandoopTest.java
done.
</pre></div>

<p>The main test driver is in class <tt>RandoopTest</tt>.  You can now
compile and run the tests. Don't forget to include
<tt>randoop.jar</tt> in the classpath when you compile and run the
tests.  In this example, you should also add the current directory
("<tt>.</tt>") when you run the tests.

<p>
<div class="code"><pre>
javac -classpath randoop.jar RandoopTest*.java
java -classpath .:randoop.jar junit.textui.TestRunner RandoopTest
</pre></div>

JUnit will execute the generated tests. Most will pass, and some will
fail. The next section describes the kinds of tests that Randoop
generates, and the reason why they may fail.


<a name="interpreting">
<h2>Interpreting the Results</h2>
</a>

<p>Randoop generates two kinds of unit tests, <i>regression tests</i>
and <i>contract-violating tests</i>.

<h3>Regression tests</h3>

Most of the tests that Randoop generates will probably pass. They are
regression tests: they record the <i>current</i> behavior of the
classes under test. Regression tests are useful because they can alert
you in the future if you make a change to your code that changes the
external behavior of the classes. Here is an example of a regression
test for the <tt>TreeSet</tt> class we generated tests for in the
previous section.

<div class="code"><pre>
<font color="#00bb00">// This test passes when executed</font>
public void test10() throws Throwable {

  java.util.TreeSet var0 = new java.util.TreeSet();
  java.lang.Short var1 = new java.lang.Short((short)100);
  boolean var2 = var0.contains(var1);
    
  // Regression assertion (captures the current behavior of the code)
  assertTrue(var2 == false);
}
</pre></div>

This test will pass when you run it right after executing it. But
notice that it captures an important behavior of the method
<tt>TreeSet.contains</tt>: it returns <tt>false</tt> if the set is
empty. If later, as the developers of this class, we introduced an
error that caused <tt>contains</tt> to return <tt>true</tt> on an
empty set, the test would fail and thus alert us to the error.

<h3>Contract-violating tests</h3>

Some of the tests that Randoop generated may <i>fail</i> when you
  first run the tests, indicating that an assertion was
  violated. These are contract-violating tests. The failure suggests a
  potential error in one or more classes under test.  A
  contract-violating test shows a specific use of a class under test
  that leads to a violation of an <i>API contract.</i> A contract is a
  property that should hold (e.g.  an object invariant, or a method
  postcondition), and a contract violation
  <i>suggests</i> an error.  Currently, Randoop checks that the
  classes under test exhibit the following properties:

<p>
<ul>
  <li>Given a non-null object <tt>o</tt>,  <tt>o.equals(o)</tt>
  should always return <tt>true</tt>, and <tt>o.equals(null)</tt>
  should always return <tt>false</tt>.
  <li>The <tt>hashCode()</tt> and <tt>toString()</tt> methods should
  not throw an exception.
</ul>

<p>
Take a look at the generated unit tests. Open the file
<tt>RandoopTest0.java</tt>. Each unit test consists of a snippet of
code that use the classes under test, along with assertions stating
properties that fail to hold. For example, one of the tests might look
as follows (actually, it will probably be longer, but we show a short test
for simplicity).

<div class="code"><pre>
<font color="#bb0000">// This test fails when executed</font>
public static void test1() {
  LinkedList var0 = new LinkedList();
  Object var1 = new Object();
  var0.addFirst(var1);
  TreeSet var2 = new TreeSet(var0);
  Set var3 = Collections.synchronizedSet(var2);
  // Checks the contract:  var3.equals(var3)
  Assert.assertTrue(var3.equals(var3) == true);
}
</pre></div>

<p> This test shows a scenario that makes it possible to create a set
that does not preserve reflexivity of equality, a property specified
in the API for <tt>java.lang.Object</tt>. If you spent some time with
the debugger, you would discover that the erroneous behavior
arises because the constructor call <tt>new TreeSet(var0)</tt> fails to
throw a <tt>ClassCastException</tt>, which it should because the
element in <tt>var0</tt> is not <tt>Comparable</tt> (see the API for
<tt>java.util.TreeSet</tt>). This is an error in the TreeSet
constructor. Later on, somewhere deep inside the call to
<tt>var3.equals(var3)</tt>, a <tt>ClassCastException</tt> is thrown, but
it is caught, and the <tt>equals</tt> method ends up returning
<tt>true</tt>. This tests reveals at least one error, and possibly
two, that could be fixed as follows:

<ul>
  <li>The constructor to <tt>TreeSet</tt> should throw an exception
  when given a single-element list with a non-comparable element.
  <li>The first statement in the <tt>equals</tt> method in
  <tt>SynchronizedSet</tt> should be
<div class="code"><pre>
        if (o == this) return true;
</pre></div>
</ul>

In general, all implementations of equals should include this check.

<p>Note that not all failing tests that Randoop generates may reveal errors.
Some tests may exhibit behavior that represents normal operation of
the classes under test. For example, consider the output of Randoop on
the class <tt>java.util.Formatter</tt>.

<a name="commands">
<h2>Randoop Commands</h2>
</a>

This section describes Randoop's commands.  The information presented
here is the same as that which you can get via Randoop's <tt>help</tt>
command.

<?php include("randoop_commands.php"); ?>

<?php
include_once "conclusion.php";
?>

</HTML>
