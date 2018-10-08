sap.ui.define([
	"sap/ui/core/UIComponent",
	"sap/ui/Device",
	"com/modekzWaybill/model/models"
], function(UIComponent, Device, models) {
	"use strict";

	return UIComponent.extend("com.modekzWaybill.Component", {

		metadata: {
			manifest: "json"
		},

		/**
		 * The component is initialized by UI5 automatically during the startup of the app and calls the init method once.
		 * @public
		 * @override
		 */
		init: function() {

			// call the base component's init function
			UIComponent.prototype.init.apply(this, arguments);

			// set the device model
			this.setModel(models.createDeviceModel(), "device");

            // var sServiceUrl = this.getMetadata().getManifestEntry("sap.app").dataSources.wb.uri;
            // var oWbModel = new sap.ui.model.odata.ODataModel(sServiceUrl, {json: true,loadMetadataAsync: true});
            // this.setModel(oWbModel,"wb");
            //this.getView().setModel(new ODataModel("/odata.svc"), "wb");

			// create the views based on the url/hash
			this.getRouter().initialize();
		}
	});
});