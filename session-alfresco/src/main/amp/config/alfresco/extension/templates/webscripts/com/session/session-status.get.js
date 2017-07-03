function main(){
  var userName = args["userName"];
  if(userName != null && userName != ""){
    var luceneQuery = "@cm\\:userName:" + userName;
    var users = search.luceneSearch(luceneQuery);
    if(users != null && users.length == 1){
      if(users[0].hasAspect("myc:loggedIn")){
        model.isloggedIn = true;
        model.userObject = users[0];
        return;
      }else{
          model.isloggedIn = false;
          model.userObject = users[0];
          return;
      }
    }
  }
  model.isloggedIn =  false;
  return
}

main();