var enableLog = true;

refresh();

function refresh(){
	var request = $.ajax({
	  url: "/sparql/status",
	  type: "GET"
	});
	 
	request.done(function( msg ) {	
		log( msg );
		updateStatus($.parseJSON(msg));
	});
	 
	request.fail(function( jqXHR, msg ) {
		$( "#log" ).html( msg );
	});
    setTimeout(refresh, 10000);
}
	

$('#btnrefresh').click(function() {
	refresh();
});


$('#btnAddSource').click(function() {
	addSource($('#endpoint').val());
});

$('#btnRemoveSource').click(function() {
	removeSource($('#endpoint').val());
});

$('#btnRemoveSource').click(function() {
	removeSource($('#endpoint').val());
});

$('#btnDQPOn').click(function() {
	setDQPMode(true);
});

$('#btnDQPOff').click(function() {
	setDQPMode(false);
});

$('#btnLoad').click(function() {
	load($('#rdfSourcePath').val(), $('#graph').val());
});

$('#btnReset').click(function() {
	reset();
});

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
	if(enableLog){
		$( "#log" ).show();
		$( "#log" ).append("<br/>" + msg);
	}
}

function load(rdfSourcePath, graph){

	$('#alertinfotext').html("<strong>loading</strong> "+ rdfSourcePath + " into "+graph+" ...");
	$('#alertinfo').show();
	$("#btnLoad").attr('disabled','disabled');
	var request = $.ajax({
	  url: "/sparql/load",
	  type: "POST",
	  data: { rdfSourcePath : rdfSourcePath, graph : graph }
	});
	 
	request.done(function( msg ) {
		$("#btnLoad").removeAttr('disabled');
		$('#alertinfo').hide();
		if(eval(msg)){
			$('#alertsuccesstext').html("successfully loaded "+ rdfSourcePath + " into "+graph);
			$('#alertsuccess').show();
		}else{
			$('#alertwarningtext').html("an error occurred while loading "+ rdfSourcePath + " into "+graph+": "+msg);
			$('#alertwarning').show();
		}
		log( "load "+ rdfSourcePath + " into "+graph+": "+msg );
	});
	 
	request.fail(function( jqXHR, msg ) {
		$("btnLoad").removeAttr('disabled');
		$('#alertinfo').hide();
		$('#alertwarningtext').html("an error occurred while loading "+ rdfSourcePath + " into "+graph+": "+msg);
		$('#alertwarning').show();
		log( msg );
	});
}



function reset(){
	var request = $.ajax({
	  url: "/sparql/reset",
	  type: "POST"
	});
	 
	request.done(function( msg ) {
		$('#alertsuccesstext').html("successfully reset local graph");
		$('#alertsuccess').show();
		log(msg);
	});
	 
	request.fail(function( jqXHR, msg ) {
		$('#alertdangertext').html("an error occurred while reseting local graph");
		$('#alertdanger').show();
		log( msg );
	});
}

function setDQPMode(isOn){
	var request = $.ajax({
		  url: "/sparql/setDQPMode/"+isOn,
		  type: "GET"
		});
		 
		request.done(function( msg ) {	
			log( msg );
			updateStatus($.parseJSON(msg));
		});
		 
		request.fail(function( jqXHR, msg ) {
			log( msg );
		});
}

function updateStatus(status){
	if(status.dqpMode){
		$('#btnDQPOn').hide();
		$('#btnDQPOff').show();
		$('#sources').show();
		$('#load').hide();
	}
	else{
		$('#btnDQPOn').show();
		$('#btnDQPOff').hide();
		$('#sources').hide();
		$('#load').show();
	}
	var sources = status.datasources;
	$("#sourcestable").html("");
	if(sources.length > 0){
		$("#sourcestable").append(
				"<tr>" +
					"<th>endpoint</th>" +
					"<th>action</th>" +
				"</tr>");
		for(i = 0; i < sources.length; i++){
			$("#sourcestable").append(
					"<tr>" +
						"<td>"+sources[i]+"</td>" +
						"<td>" +
							"<button type='button' class='btn btn-warning' onclick='removeSource(\""+sources[i]+"\")'>Remove</button>" +
						"</td>" +
					"</tr>");
		}
	}
	var queries = status.running;
	$("#querytable").html("");
	if(queries.length == 0){
		$("#noquery").show();
	}
	else{
		$("#noquery").hide();
		$("#querytable").append(
				"<tr>" +
					"<th>start time</th>" +
					"<th>query</th>" +
					"<th>status</th>" +
					"<th>action</th>" +
				"</tr>");
		for(i = 0; i < queries.length; i++){
			$("#querytable").append(
					"<tr>" +
						"<td>"+queries[i].startTime+"</td>" +
						"<td>"+queries[i].query+"</td>" +
						"<td>running</td>" +
						"<td>" +
							"<button type='button' class='btn btn-warning' onclick='stopQuery(\""+queries[i].id+"\")'>Stop</button>" +
						"</td>" +
					"</tr>");
		}
		queries = status.waiting;
		for(i = 0; i < queries.length; i++){
			$("#querytable").append(
					"<tr>" +
						"<td>"+queries[i].startTime+"</td>" +
						"<td>"+queries[i].query+"</td>" +
						"<td>waiting</td>" +
						"<td>" +
							"<button type='button' class='btn btn-warning' onclick='stopQuery(\""+queries[i].id+"\")'>Stop</button>" +
						"</td>" +
					"</tr>");
		}
	}
}

function stopQuery(queryId){
	var request = $.ajax({
	  url: "/sparql/stopQuery/"+queryId,
	  type: "POST"
	});
	 
	request.done(function( msg ) {
		//var status = eval(msg);
		//alert(status);
		updateStatus( $.parseJSON(msg));
		log( msg );
	});
	 
	request.fail(function( jqXHR, textStatus ) {
		log( msg );
	});
}

function addSource(endpoint){
	var request = $.ajax({
	  url: "/sparql/addDataSource",
	  type: "POST",
	  data: { endpoint : endpoint }
	});
	 
	request.done(function( msg ) {
		//var status = eval(msg);
		//alert(status);
		updateStatus( $.parseJSON(msg));
		log( msg );
	});
	 
	request.fail(function( jqXHR, textStatus ) {
		log( msg );
	});
}

function removeSource(endpoint){
	var request = $.ajax({
	  url: "/sparql/removeDataSource",
	  type: "POST",
	  data: { endpoint : endpoint }
	});
	 
	request.done(function( msg ) {
		var status = $.parseJSON(msg); 
		updateStatus(status);
		log( msg );
	});
	 
	request.fail(function( jqXHR, textStatus ) {
		log( msg );
	});
}
