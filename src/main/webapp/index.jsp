<%
// String base = "<i>http://mivvi-server/</i>rest/";
String base = "http://mivviweb.appspot.com/";
%><!DOCTYPE html>
<html>
<title>Mivvi REST</title>
<link rel='stylesheet' href='style.css'>
<body>
<h1>Mivvi REST</h1>
<p>A web API to Mivvi.</p>
<h2>recognise</h2>
<h3><code><%= base %>recognise/<i>filename</i></code></h3>
<p>Tries to identify a program from a broadcast listing
title or filename.</p>
<!-- TODO: Submission form -->
<p>e.g. to ask about 'Example Show - Named Episode':</p>
<pre><%= base %>recognise/Example%20Show%20-%20Named%20Episode</pre>

<h2>about</h2>
<h3><code><%= base %>about?subject=<i>uri</i></code></h3>
<p>Provides information about an episode or other Mivvi resource.
Give the URI.</p>
<p>
<form action='about'>
<label for='subject'>Subject URI:</label>
<input name='subject'>
<input type='submit' value='Submit'>
</form>
</p>
<p>e.g. to ask about <code>&lt;http://www.example.com/1/1#&gt;</code>:</p>
<pre><%= base %>about?subject=http%3A%2F%2Fwww.example.com%2F1%2F1%23</pre>

<h2>series</h2>
<h3><code><%= base %>series</code></h3>
<p>Provides a list of all known series along with their titles
and alternative descriptions.<p>
<p><a href='series'>All known series.</a></p>

</body>
</html>
