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

<p> The zip file containing Randoop includes the sources. We do not
currently provide much technical support if you want to modify
Randoop, but the following instructions are meant to get you started.

<h1>Index</h1>

<ul>
  <li><a href="#compiling"><b>Compiling Randoop</b></a>
</ul>

<p>

<a name="compiling">
<h2>Compiling Randoop</h2>
</a>

<p> <b>In Eclipse.</b> The top-level <tt>randoop</tt> directory is
an Eclipse project. In Eclipse, create a new Java project,
and in the "New Java Project" window, select "Create project from
existing source" and select the <tt>randoop</tt> directory. After
that, you'll have Randoop as a project and everything should compile
automatically.

<p> <b>Outside Eclipse.</b> You can compile Randoop using a java compiler
for Java 5 or greater. The <tt>src</tt> and
<tt>tests</tt> directories contain the sources. The sources depend on
the jar files contained in the <tt>lib</tt> directory, which you
should add to your classpath when compiling.

<p> As an example, here is one way to compile Randoop. First, create a
file <tt>classes.txt</tt> that lists all the Randoop source files:

<div class="code"><pre>
find src/ tests/ -name "*.java" > classes.txt
</pre></div>

Next, call <tt>javac</tt> giving the argument <tt>@classes.txt</tt>,
and the jar files under <tt>lib</tt> in the classpath:

<div class="code"><pre>
javac -cp lib/jakarta-oro-2.0.8.jar:lib/junit-4.3.1.jar @classes.txt
</pre></div>

<?php
include_once "conclusion.php";
?>

</HTML>
