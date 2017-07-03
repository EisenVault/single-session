{
 	<#if isSuccess?exists>
    	"isSuccess":"${isSuccess?string('yes', 'no')}"
  		<#if !isSuccess>
    		,"errMsg":"${errMsg}"
    	</#if>
  	<#else>
  		"isSuccess":"no",
  		"errMsg":"Error In processing request."
  	</#if>
	<#if logList?exists>
		,"logList":[
			<#list logList as logitem >
			"${logitem}"<#if logitem_has_next>,</#if>
			</#list>
		]
	</#if>
}
