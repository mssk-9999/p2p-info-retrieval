Ext.onReady(function(){

	dwr.engine.setActiveReverseAjax(true);
//	dwr.engine.setNotifyServerOnPageUnload(true);

	// Custom rendering Template for the View
	var resultTpl = new Ext.XTemplate(
			'<tpl for=".">',
			'<div class="search-item">',
//			'<h3><span>{modified:date("M j, Y")}</span>',
			'<h3><span>{respondingIP}<br />{size}</span>',
			'<a href="http://{respondingIP}:8080/getFile?path={path}" target="_blank">{path}</a></h3>',
			'</div></tpl>'
	);

	function addTab(query) {
		var callback = "appendResults";
		var storeId = Ext.id();
		var ds = new Ext.data.Store({
			storeId: storeId,
			proxy: new Ext.ux.data.DwrProxy({
				apiActionToHandlerMap : {
					read : {
						dwrFunction : SearchFilesInterface.getResults,
						getDwrArgsFunction : function(request) {
							request.callback = Ext.emptyFn;
							return [query, storeId, callback];
						},
						getDwrArgsScope : this
					}
				}
			}),
			reader: new Ext.data.JsonReader({
//				root : 'objectsToConvertToRecords',
				root : '',
				fields : [
				          {name: 'path'},
				          {name: 'size'},
				          {name: 'modified'},
				          {name: 'respondingIP'}
				          ]
			})
			
		});
		
		var resultsPanel = viewport.findById('resultsPanel');

		var tab = resultsPanel.add({
			title: query,
			closable: true,
			iconCls: 'icon-searchtabs',
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
		viewport.doLayout();
		tab.show();
		return storeId;
	}
	
	appendResults = function (storeId, results) {
		Ext.StoreMgr.lookup(storeId).loadData(results, true);
	};
	
	var viewport = new Ext.Viewport({
		layout: 'border',
		
		items: [{
			layout: 'hbox',
			layoutConfig: {
				align: 'middle',
				pack: 'center'
			},
			region: 'north',
			height: 100,
			items: {
				layout: 'form',
				border: false,
				items: new p2p.ux.form.SearchField({
					fieldLabel: 'Search',
					width: 500,
					onTrigger2Click : function(){
						var v = this.getRawValue();
						if(v.length < 1){
							this.onTrigger1Click();
							return;
						}
						var storeId = addTab(v);
						Ext.StoreMgr.lookup(storeId).proxy.doRequest("read");
					}
				})
			}
		},
		new Ext.TabPanel({
			id: 'resultsPanel',
			resizeTabs:true, // turn on tab resizing
			activeTab: 0,
			region: 'center',
			minTabWidth: 115,
			tabWidth:135,
			enableTabScroll:true,
			autoScroll: true,
			defaults: {autoScroll:true},
			plugins: new Ext.ux.TabCloseMenu(),
			items: {iconCls:'icon-hometab', contentEl:'tab1', title:'Search Home'}
		}),
        new Ext.BoxComponent({
            region: 'south',
            height: 32, // give north and south regions a height
            autoEl: {
                tag: 'div',
                cls: 'south-content',
                html:'<center><p><a href="http://code.google.com/p/p2p-info-retrieval/" target="_blank">Project Home Page</a></p></center>'
            }
        })
		]
	});
});
