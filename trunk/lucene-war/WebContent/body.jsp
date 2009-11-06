<%@include file="header.jsp"%>
<script type="text/javascript">
Ext.onReady(function(){
	
	new Ext.Viewport({
		layout: 'border',
		items: [{
			region: 'center',
			border: false,
			items: [{
				xtype: 'form',
	        	border: false,
	        	standardSubmit: true,
	        	method: 'GET',
	        	url: 'results.jsp',
		        items: [
			        new p2p.ux.form.SearchField({
			        	name: 'query',
			        	fieldLabel: 'Search'
			        })
			    ]
	        }]
		}]
	});
	
});
</script>
<!--<center> -->
<!--	<form name="search" action="results.jsp" method="get">-->
<!--		<p>-->
<!--			<input name="query" size="44"/>&nbsp;Search Criteria-->
<!--		</p>-->
<!--		<p>-->
<!--			<input name="maxresults" size="4" value="100"/>&nbsp;Results Per Page&nbsp;-->
<!--			<input type="submit" value="Search"/>-->
<!--		</p>-->
<!--        </form>-->
<!--</center>-->
<%@include file="footer.jsp"%>
