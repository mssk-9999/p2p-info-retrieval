Ext.onReady(function(){
	var searchField = new p2p.ux.form.SearchField({
		fieldLabel: 'Search',
		anchor: '100%',
		onTrigger2Click : function(){
			var v = this.getRawValue();
	        if(v.length < 1){
	            this.onTrigger1Click();
	            return;
	        }
	        
	        ds.doRequest();
		}
	});
	
	var form = new Ext.FormPanel({
		renderTo: 'search',
		border: false,
		width: 500,
		items: searchField
	});

	var ds = new Ext.data.Store({
		proxy: new Ext.ux.data.DwrProxy({
			apiActionToHandlerMap : {
				read : {
					dwrFunction : SearchFilesInterface.getResults,
					getDwrArgsFunction : function(trans) {
						return [
							searchField.getValue()
						];
					}
				}
			}
		}),
		reader: new Ext.data.JsonReader({
			root : 'objectsToConvertToRecords',
			fields : [
				{name: 'text'},
				{name: 'link'}
			]
		})
	});

	// Custom rendering Template for the View
	var resultTpl = new Ext.XTemplate(
			'<tpl for=".">',
			'<div class="search-item">',
			'<h3><a href="{link}" target="_blank">{text}</a></h3>',
			'</div></tpl>'
	);

	var resultsPanel = new Ext.Panel({
		renderTo: 'results',
		resizeTabs:true, // turn on tab resizing
		minTabWidth: 115,
		tabWidth:135,
		enableTabScroll:true,
		width:600,
		height:250,
		defaults: {autoScroll:true},
		plugins: new Ext.ux.TabCloseMenu(),

		items: new Ext.DataView({
			tpl: resultTpl,
			store: ds,
			itemSelector: 'div.search-item'
		})
	});
});
