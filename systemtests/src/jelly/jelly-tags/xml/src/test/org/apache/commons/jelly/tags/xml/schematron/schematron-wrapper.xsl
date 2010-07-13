<?xml version="1.0" encoding="UTF-8"?>
<!--
  Copyright 2002-2004 The Apache Software Foundation

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

      <!-- import the basic framework -->
      <xsl:import href="skeleton1-5.xsl"/>

      <!-- override the process-root template and wrap output as HTML -->
      <!--xsl:template name="process-root">
         <xsl:param name="title"/>
         <xsl:param name="contents" />
         <html>
            <head><title>Validation Report For: <xsl:value-of select="$title"/></title></head>
            <body>
            <h1>Validation Report For : <xsl:value-of select="$title"/></h1>

            <pre>
            <xsl:copy-of select="$contents" />
            </pre>

            </body>
         </html>
      </xsl:template-->

      <xsl:template name="process-root">
         <xsl:param name="title"/>
         <xsl:param name="contents" />
         <schematron-validation>Validation Report For: <xsl:value-of select="$title"/>
         <xsl:copy-of select="$contents" />
         </schematron-validation>
      </xsl:template>

      <!-- override additional templates as needed -->
    <xsl:template name="process-assert">
        <xsl:param name="role"/>
        <xsl:param name="id"/>
        <xsl:param name="test"/>
        <xsl:param name="subject"/>
        <xsl:param name="diagnostics"/>
        <!-- unused parameters: icon -->
        <assert>
            <xsl:if test="$role">
                <role><xsl:value-of select="$role" /></role>
            </xsl:if>
            <xsl:if test="$id">
                <id><xsl:value-of select="$role" /></id>
            </xsl:if>
            <test><xsl:value-of select="$test" /></test>
            <result><xsl:apply-templates mode="text" /></result>
            <xsl:if test="$subject">
                <subject><xsl:value-of select="$subject" /></subject>
            </xsl:if>
            <xsl:if test="$diagnostics">
                <diagnostics>
                    <xsl:call-template name="diagnosticsSplit">
                        <xsl:with-param name="str" select="$diagnostics" />
                    </xsl:call-template>
                </diagnostics>
            </xsl:if>
        </assert>
    </xsl:template>

      <!-- override additional templates as needed -->
    <xsl:template name="process-report">
        <xsl:param name="role"/>
        <xsl:param name="test"/>
        <xsl:param name="id"/>
        <xsl:param name="subject"/>
        <xsl:param name="diagnostics"/>
        <!-- unused parameters: icon -->
        <report>
            <xsl:if test="$role">
                <role><xsl:value-of select="$role" /></role>
            </xsl:if>
            <xsl:if test="$id">
                <id><xsl:value-of select="$role" /></id>
            </xsl:if>
            <test><xsl:value-of select="$test" /></test>
            <result><xsl:apply-templates mode="text" /></result>
            <xsl:if test="$subject">
                <subject><xsl:value-of select="$subject" /></subject>
            </xsl:if>
            <xsl:if test="$diagnostics">
                <diagnostics>
                    <xsl:call-template name="diagnosticsSplit">
                        <xsl:with-param name="str" select="$diagnostics" />
                    </xsl:call-template>
                </diagnostics>
            </xsl:if>
        </report>
    </xsl:template>

</xsl:stylesheet>

