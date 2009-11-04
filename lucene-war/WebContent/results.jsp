<%@ page import = "  javax.servlet.*, javax.servlet.http.*, java.io.*, org.apache.lucene.analysis.*, org.apache.lucene.analysis.standard.StandardAnalyzer, org.apache.lucene.document.*, org.apache.lucene.index.*, org.apache.lucene.store.*, org.apache.lucene.search.*, org.apache.lucene.queryParser.*, org.apache.lucene.demo.*, org.apache.lucene.demo.html.*, java.net.URLEncoder, org.apache.lucene.util.Version" %>

<%@include file="header.jsp"%>

<script type="text/javascript">
Ext.onReady(function(){
	new Ext.Viewport({
		layout: 'border',
		items: [{
			xtype: 'grid',
			region: 'center',
			border: false,
			store: new Ext.data.ArrayStore({
				fields: ['name', 'link']
		    }),
		    columns: [{
		    	id:'name',
		    	header: "Name",
		    	sortable: true
		    }],
			stripeRows: true,
			height:350,
			width:600
		}]
	});
});

</script>

<%@include file="footer.jsp"%>        
