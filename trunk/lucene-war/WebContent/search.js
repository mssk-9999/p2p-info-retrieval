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
