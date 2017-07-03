function main(){
  var userName = args["userName"];
  if(userName != null && userName != ""){
    var luceneQuery = "@cm\\:userName:" + userName;
    var users = search.luceneSearch(luceneQuery);
    if(users != null && users.length == 1){
    	if(users[0].hasAspect("myc:loggedIn")){
			users[0].properties["myc:isloggedIn"] = false;
			users[0].properties["myc:loggedInToken"] = '';
			users[0].save();
			users[0].removeAspect("myc:loggedIn");
			model.isloggedIn =  true;
			return;
		}
    }
  }
  model.isloggedIn =  false;
  return
}

main();