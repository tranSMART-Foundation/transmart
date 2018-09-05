
	<g:select style="width: 400px" id="measurement" name="measurement" noSelection="${['null':'Select...']}" from="${measurements}" value="${measurement}"
			onchange="${remoteFunction(action:'ajaxTechnologies', update: 'technologywrapper', onSuccess: 'updatePlatforms()', params:'\'measurementName=\' + this.value + \'&technologyName=\' + jQuery(\'#technology\').val() + \'&vendorName=\' + jQuery(\'#vendor\').val()' )};
					${remoteFunction(action:'ajaxVendors', update: 'vendorwrapper', onSuccess: 'updatePlatforms()', params:'\'measurementName=\' + this.value + \'&technologyName=\' + jQuery(\'#technology\').val() + \'&vendorName=\' + jQuery(\'#vendor\').val()' )}"/>
