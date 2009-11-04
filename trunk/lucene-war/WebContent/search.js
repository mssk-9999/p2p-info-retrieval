Ext.onReady(function(){
	new Ext.Viewport({
		layout: 'border',
		items: [{
			xtype: 'gridpanel',
			region: 'center',
			border: false,
			store: new Ext.data.ArrayStore({
				fields: ['name', 'link']
		    }),
		    columns: [{
		    	id:'name',
		    	header: "name",
		    	sortable: true,
		    	dataIndex: 'company'
		    }],
			stripeRows: true,
			height:350,
			width:600
		}]
	});
});
