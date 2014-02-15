$('#btnRun').click(function() {
	runQuery($('#query').val(), "json", $('#timeout').val());
});

function runQuery(query, format, timeout){

	$('#resultTable').html("");
	$('#alertsuccess').hide();
	$('#alertwarning').hide();
	$('#alertdanger').hide();
	$('#alertinfotext').html("running query ...");
	$('#alertinfo').show();
	$("#btnRun").attr('disabled','disabled');

	log("waiting "+format+ " results for: "+ query);
	var request = $.ajax({
	  url: "/sparql",
	  type: "GET",
	  data: { query : query, format : format, timeout : timeout }
	});
	
	request.done(function( msg ) {
		
		var alert = "running query : completed";
		log(alert);
		$("#btnRun").removeAttr('disabled');
		$('#alertinfo').hide();
		try{
			printResults(msg);
			$('#alertsuccesstext').html(alert);
			$('#alertsuccess').show();
		}catch(err){
			$('#alertwarningtext').html(htmlEncode(msg).replace(/\n/g,'<br/>'));
//			$('#alertwarningtext').html(msg);
			$('#alertwarning').show();
		}
		log(msg);
	});
	 
	request.fail(function( jqXHR, msg ) {
		$("#btnRun").removeAttr('disabled');
		$('#alertinfo').hide();
		$('#alertwarningtext').html("an error occurred: "+msg);
		$('#alertwarning').show();
		log( msg );
	});
}

function printResults(result){
	var vars = result.head.vars;
	var bindings = result.results.bindings;
	var headerline = "<tr>";
	for(i = 0; i < vars.length; i ++){
		headerline += "<th>"+vars[i]+"</th>";
	}
	$('#resultTable').append(headerline+"</tr>");
	for(i = 0; i < bindings.length; i ++){
		var resultLine = "<tr>";
		for(j = 0; j < vars.length; j ++){
			resultLine += "<td><text>"+eval("bindings[i]."+vars[j]).value+"</text></td>";
		}
		$('#resultTable').append(resultLine + "</tr>");
	}

}
