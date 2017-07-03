function main(){
	var userName = json.get("userName");
	if(userName == null || userName == undefined || userName == ''){
		model.isSuccess = false;
		model.errMsg = "User name parameter not found";
		return;
	}
	var ticket = json.has("ticket") ? json.get("ticket") : "";
	if(ticket == null || ticket == undefined){
		ticket = '';
	}
	var luceneQuery = "@cm\\:userName:" + userName;
	var users = search.luceneSearch(luceneQuery);
	var logList = new Array();
	logList.push("Input ticket:"+ticket);
	if(users != null && users.length == 1){
		if(users[0].hasAspect("myc:loggedIn")){
			logList.push("hasAspect");
			if(ticket == ""){
				logList.push("Tiket1:"+users[0].properties["myc:loggedInToken"]);
				users[0].properties["myc:isloggedIn"] = false;
				users[0].properties["myc:loggedInToken"] = '';
				users[0].save();
				users[0].removeAspect("myc:loggedIn");
				logList.push("Removed Aspect 1");
			}else{
				logList.push("Tiket2:"+users[0].properties["myc:loggedInToken"]);
				users[0].properties["myc:isloggedIn"] = true;
				users[0].properties["myc:loggedInToken"] = ticket;
			}
			users[0].save();
			model.isSuccess =  true;
			logList.push("Aspect Added 1");
			model.logList = logList;
			return;
		}else{
			logList.push("does not have Aspect");
			if(ticket != ""){
				var props = new Array();
				props["myc:isloggedIn"] = true;
				props["myc:loggedInToken"] = ticket;
				users[0].addAspect("myc:loggedIn", props);
				users[0].save();
				logList.push("Aspect Added 2");
			}else{
				logList.push("Tiket3:"+users[0].properties["myc:loggedInToken"]);
				users[0].properties["myc:isloggedIn"] = false;
				users[0].properties["myc:loggedInToken"] = '';
				users[0].save();
				users[0].removeAspect("myc:loggedIn");
				logList.push("Removed Aspect 2");
			}

			model.logList = logList;
			model.isSuccess =  true;
			return;
		}
	}else{

			logList.push("user not found:"+userName);
	}
	model.isSuccess =  false;
	model.logList = logList;
	model.errMsg = "User not found with provided username";
	return
}

main();
