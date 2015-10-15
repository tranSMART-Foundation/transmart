<!DOCTYPE html>

<style>
</style>

<g:javascript src="resource/d3.js"/> <!-- Or any other js lib you want to use -->

<div id="visualization"> <!-- This div will contain your visualization -->
</div>

<script>
	// This variable will hold everything you added to the R output variable
	var results = ${raw(results)};
	// remember output$data <- data ?
	var data = results.data;
	// remember output$foobarResults <- abc ?
	var someStats = results.foobarResults;





	// And now you do whatever you want to.
	// In a time with HTML5 and CSS3 and more JS libs than you can imagine
	// you are free to take whatever framework you like and do something
	// with the above data. Some so powerful that you won't need more than
	// a single line of code to make something fancy I can imagine
	// and some (like D3) giving you the power to do pretty much everything






	// Oh and if you need on-the-fly updates but don't want to compute it in the browser:
	var data = prepareFormData();
    data = addSettingsToData(data, { foobar: [1,2,3] });
    data = addSettingsToData(data, { blub: 'bla' });
	jQuery.ajax({
        url: pageInfo.basePath + '/SmartR/updateOutputDIV',
        type: "POST",
        timeout: '600000',
        data: data
    }).done(function(serverAnswer) {
        serverAnswer = JSON.parse(serverAnswer);
        if (serverAnswer.error) {
            alert(serverAnswer.error);
            return;
        }
        // do stuff with serverAnswer
        // serverAnswer is just like the initial results variable and
        // maps to the updated output list in your R script
    }).fail(function() {
        // react to a failure
    });



</script>