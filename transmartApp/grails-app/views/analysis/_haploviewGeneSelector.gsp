<table class="searchform">
    <tr>
	<td>Select Genes:</td>
    <tr>
    <tr>
        <td>
            <g:if test="${genes}"><g:select name="haploviewgenes" from="${genes}" multiple="multiple" size="5"/></g:if>
            <g:else>No snp data found for these subsets.</g:else>
        </td>
    </tr>
</table>
