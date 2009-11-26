Ext.onReady(function(){

	var reverseAjaxInitialized = false;
	var searchField = new p2p.ux.form.SearchField({
		fieldLabel: 'Search',
		anchor: '100%',
		onTrigger2Click : function(){
			var v = this.getRawValue();
			if(v.length < 1){
				this.onTrigger1Click();
				return;
			}
			var tab = addTab(v);
			// Activate reverse ajax in dwr
			if(!reverseAjaxInitialized) {
				dwr.engine.setActiveReverseAjax(true);
				reverseAjaxInitialized = true;
			}
			tab.get(0).getStore().load();
		}
	});

	var form = new Ext.FormPanel({
		renderTo: 'search',
		border: false,
		width: 500,
		items: searchField
	});

	// Custom rendering Template for the View
	var resultTpl = new Ext.XTemplate(
			'<tpl for=".">',
			'<div class="search-item">',
//			'<h3><span>{modified:date("M j, Y")}</span>',
			'<h3><span>{size}</span>',
			'<a href="file://{path}" target="_blank">{path}</a></h3>',
			'</div></tpl>'
	);

	function addTab(query) {
		var callback = "appendResults";
		var ds = new Ext.data.Store({
			proxy: new Ext.ux.data.DwrProxy({
				apiActionToHandlerMap : {
					read : {
						dwrFunction : SearchFiles.getResults,
						getDwrArgsFunction : function(trans) {
							return [query, callback];
						}
					}
				}
			}),
			reader: new Ext.data.JsonReader({
//				root : 'objectsToConvertToRecords',
				root : '',
				fields : [
				          {name: 'path'},
				          {name: 'size'},
				          {name: 'modified'}
				          ]
			})
			
		});

		var tab = resultsPanel.add({
			title: query,
			closable: true,
			iconCls: 'tabs',
			items: new Ext.DataView({
				tpl: resultTpl,
				store: ds,
				itemSelector: 'div.search-item'
			}),
			bbar: new Ext.PagingToolbar({
				store: ds,
				pageSize: 20,
				displayInfo: true,
				displayMsg: 'Results {0} - {1} of {2}',
				emptyMsg: "No results to display"
			})
		});
		resultsPanel.doLayout();
		tab.show();
		return tab;
	}

	var resultsPanel = new Ext.TabPanel({
		id: 'resultsPanel',
		renderTo: 'search-results',
		resizeTabs:true, // turn on tab resizing
		minTabWidth: 115,
		tabWidth:135,
		enableTabScroll:true,
		width:600,
		height:250,
		autoScroll: true,
		defaults: {autoScroll:true},
		plugins: new Ext.ux.TabCloseMenu()
	});
	
	appendResults = function (results) {
		resultsPanel.get(0).get(0).getStore().loadData(results, true);
	}
});
