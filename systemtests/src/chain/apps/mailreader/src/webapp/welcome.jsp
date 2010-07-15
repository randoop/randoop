<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="/tags/struts-bean" prefix="bean" %>
<%@ taglib uri="/tags/struts-html" prefix="html" %>

<html>
<head>
<title><bean:message key="welcome.title"/></title>
<link rel="stylesheet" type="text/css" href="base.css" />
</head>

<h3><bean:message key="welcome.heading"/></h3>
<ul>
<li><html:link action="/RegisterForm"><bean:message key="welcome.registration"/></html:link></li>
<li><html:link action="/LogonForm"><bean:message key="welcome.logon"/></html:link></li>
</ul>

<h3>Language Options</h3>
<ul>
<li><html:link action="/LocaleChange?language=en">English</html:link></li>
<li><html:link action="/LocaleChange?language=ja" useLocalEncoding="true">Japanese</html:link></li>
<li><html:link action="/LocaleChange?language=ru" useLocalEncoding="true">Russian</html:link></li>
</ul>

<hr />

<p><html:img bundle="alternate" pageKey="struts.logo.path" altKey="struts.logo.alt"/></p>

<p><html:link action="/Tour"><bean:message key="welcome.tour"/></html:link></p>

</body>
</html>
