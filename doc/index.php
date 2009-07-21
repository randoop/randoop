<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<!-- <!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN"> -->
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang=en lang=en>

<HEAD><meta http-equiv=content-type content="text/html; charset=utf-8" />
	<link rel=stylesheet href="main.css" type="text/css" />
<title>Randoop for Java</title>
</HEAD>
<?php
include_once "preface.php";
?>

<h2>What is Randoop?</h2>

<p>
<ul>

<li> Randoop is an <b>automatic test generator</b> for Java.  It
automatically creates unit tests for your classes, in <a
href="http://www.junit.org">JUnit</a> format.

<p>

<li> Randoop generates unit tests using <b>feedback-directed random
test generation</b>. In a nutshell, this technique randomly, but
smartly, generates sequences of methods and constructor invocations
for the classes under test, and uses the sequences to create
tests. Randoop executes the sequences it creates, using the results of
the execution to create assertions that capture the behavior or your
program and that catch bugs.  <p>

<li> Randoop has created tests that find <b>previously unkwon errors
even in widely-used libraries</b> including Sun and IBM's JDKs. A .NET
version of Randoop, used internally at Microsoft, has been used
successfully by a team of test engineers to find errors in a core .NET
component that has been heavily tested for years. Randoop's
combination of randomized test generation and test 
execution results in a highly effective test generation technique.
<p>

</ul>
To learn more about Randoop, follow the above links, which include the
download site, manual, and a list of publications delving more deeply
into the technical aspects of the tool, and its uses in both research
and industry.

<h2>System Requirements</h2>

<p>

We have tested Randoop using <b>Java 5 or greater under Linux and Mac
OS X (1.14).</b> <br>

<p>

Because it is written in Java, Randoop should work on Java 5 or
greater under Windows, but we haven't tested it. We welcome feedback
on user experiences under Windows.


<h2>What's new</h2>

<p>

<ul>

  <li> 07/22/2008. <b>Talk:</b> Finding Errors in .NET with
Feedback-directed Random Testing at
  <em>ISSTA 2008: International Symposium on Software Testing and
Analysis.</em>
<p>
  <li> 07/21/2008. <b>New release:</b> Randoop version 1.2 has been
released.
  
</ul>

<p>


<?php
include_once "conclusion.php";
?>

</html>


