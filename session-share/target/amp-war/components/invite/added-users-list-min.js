(function(){var d=YAHOO.util.Dom;var b=Alfresco.util.encodeHTML;Alfresco.AddedUsersList=function(f){Alfresco.AddedUsersList.superclass.constructor.call(this,"Alfresco.AddedUsersList",f,["button","container","datasource","datatable","json"]);return this};YAHOO.extend(Alfresco.AddedUsersList,Alfresco.component.Base,{onReady:function c(){var g=document.getElementById(this.id+"-add-users-button");var f=d.getElementsByClassName("sinvite","div","bd")[0];var h=f.getElementsByTagName("button")[0];h.innerHTML=this.msg("added-users-list.add-button-text");g.appendChild(f.firstElementChild);this.widgets.dataSource=new YAHOO.util.DataSource([],{responseType:YAHOO.util.DataSource.TYPE_JSARRAY});this._setupDataTable();YAHOO.Bubbling.on("usersAdded",this.onUsersAdded,this)},onUsersAdded:function e(h,g){var f=g[1].users;this.widgets.dataTable.addRows(f,0);var i=d.getElementsByClassName("added-users-list-tally","div","bd")[0];i.innerHTML=this.msg("added-users-list.tally",this.widgets.dataTable.getRecordSet().getLength());d.removeClass(i,"hidden")},_setupDataTable:function a(){var h=this;var f=function j(q,p,r,s){var n=p.getData(),o=YAHOO.lang.trim(n.firstName+" "+n.lastName),m=h.msg("role."+n.role);q.innerHTML='<h3 class="itemname">'+b(o)+'</h3><div class="detail">'+b(m)+"</div>"};var i=function g(n,m,o,p){n.innerHTML='<div class="alf-green-check"></div>'};var l=[{key:"user",label:"User",sortable:false,formatter:f},{key:"checkbox",label:"Confirmation",sortable:false,formatter:i}];var k=this.msg("added-users-list.empty");this.widgets.dataTable=new YAHOO.widget.DataTable(this.id+"-added-users-list-content",l,this.widgets.dataSource,{MSG_EMPTY:k})}})})();