define(["dojo/_base/declare","alfresco/buttons/AlfDynamicPayloadButton","dojo/_base/lang","cmm/CMMConstants"],function(b,d,e,c){return b([d],{mapData:function a(i,h){for(var f in i){if(i.hasOwnProperty(f)){var g=e.getObject(f,false,h);e.setObject(i[f],g!=null?g:null,this.publishPayload)}}}})});