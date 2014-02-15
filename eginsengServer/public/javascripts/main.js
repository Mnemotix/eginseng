$('#alertsuccessclose').click(function() {
	$('#alertsuccess').hide();
});

$('#alertinfoclose').click(function() {
	$('#alertinfo').hide();
});

$('#alertwarningclose').click(function() {
	$('#alertwarning').hide();
});

$('#alertdangerclose').click(function() {
	$('#alertdanger').hide();
});

$('#alertsuccess').hide();
$('#alertinfo').hide();
$('#alertwarning').hide();
$('#alertdanger').hide();

function log(msg){
	if($('#activateLogs').is(':checked')){
		$( "#log" ).show();
		$( "#log" ).append("<br/>" + msg);
	}
}

function htmlEncode(value){
	//http://stackoverflow.com/questions/1219860/html-encoding-in-javascript-jquery
	return $('<div/>').text(value).html();
}

