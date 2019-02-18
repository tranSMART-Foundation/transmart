<g:each in="${aList}" var='a'>
    <g:render template='/GWAS/bmanalysis' model="[analysis: a]"/>
</g:each>
