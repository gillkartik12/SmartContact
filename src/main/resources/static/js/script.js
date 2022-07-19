console.log("this is a script");

const toggleSidebar = () => {
	
	if($(".sidebar").is(":visible")){
	//to hide sidebar
	$(".sidebar").css("display", "none");
	$(".content").css("margin-left","0%");	
	}
	else{
	//show side bar
	$(".sidebar").css("display", "block");
	$(".content").css("margin-left","20%");		
	}
};


