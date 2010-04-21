<%
  String uploadURL = (String) request.getAttribute("uploadURL");
%><!DOCTYPE html>
<html>
<link rel='stylesheet' type='text/css' href='style.css'>
<title>Mivvi Data Upload</title>
<body>
<form action='<%= uploadURL %>' method='POST' enctype='multipart/form-data'>
<label for='file'>File:</label>
<input type='file' name='file'>
<input type='submit' value="Submit">
</form>
</body>
