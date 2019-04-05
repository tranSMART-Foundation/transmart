<script type="text/javascript">
	jQuery(function(){
	  jQuery(".multiselect").multiselect();
	});
</script>

<select id="multiselectbox" class="multiselect" multiple="multiple" name="dataTypes[]" style="width: 96%; height: 94%">
    <g:each in="${dataTypes}" var='dataType'>
	<option value="${dataType.key}">${dataType.value}</option>
    </g:each>
</select>
