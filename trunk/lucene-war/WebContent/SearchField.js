Ext.ns('p2p.ux.form.');

p2p.ux.form.SearchField = Ext.extend(Ext.form.TwinTriggerField, {
    initComponent : function(){
		p2p.ux.form.SearchField.superclass.initComponent.call(this);
        this.on('specialkey', function(f, e){
            if(e.getKey() == e.ENTER){
                this.onTrigger2Click();
            }
        }, this);
    },

    validationEvent:false,
    validateOnBlur:false,
    trigger1Class:'x-form-clear-trigger',
    trigger2Class:'x-form-search-trigger',
//    hideTrigger1:true,
    width:180,
//    hasSearch : false,
//    paramName : 'query',

    onTrigger1Click : function(){
    	this.setValue('');
    	this.focus();
    },

    onTrigger2Click : function(){
        var v = this.getRawValue();
        if(v.length < 1){
            this.onTrigger1Click();
            return;
        }

        var fp = this.ownerCt,
        	form = fp.getForm();
        if (form.isValid()) {
        	// check if there are baseParams and if
        	// hiddent items have been added already
//        	if (fp.baseParams && !fp.paramsAdded) {
//        		// add hidden items for all baseParams
//        		for (i in fp.baseParams) {
//        			fp.add({
//        				xtype: 'hidden',
//        				name: i,
//        				value: fp.baseParams[i]
//        			});
//        		}
//        		fp.doLayout();
//        		// set a custom flag to prevent re-adding
//        		fp.paramsAdded = true;
//        	}
        	form.getEl().dom.action = form.url;
        	form.submit();
        }
    }
});
