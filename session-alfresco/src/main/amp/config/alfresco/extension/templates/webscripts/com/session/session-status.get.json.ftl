{
  <#if isloggedIn?exists>
    "isloggedIn":"${isloggedIn?string('yes', 'no')}"
  <#else>
    "isloggedIn":"no"
  </#if>
  <#if userObject?exists>
  	,"ticket":"<#if userObject.properties['myc:loggedInToken']??>${userObject.properties['myc:loggedInToken']}<#else></#if>"
  </#if>
}