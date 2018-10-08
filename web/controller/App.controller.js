sap.ui.define([
    'com/modekzWaybill/controller/BaseController',
    'sap/ui/model/json/JSONModel'
], function (BaseController, JSONModel) {
    "use strict";

    return BaseController.extend("com.modekzWaybill.controller.App", {
        onInit: function () {
            // call base init
            BaseController.prototype.onInit.apply(this, arguments);

            var oViewModel = new JSONModel({
                busy: true,
                delay: 0,
                appWidthLimited: false,
                layout: "OneColumn",
                previousLayout: "",
                actionButtonsInfo: {
                    midColumn: {
                        fullScreen: false
                    }
                }
            });
            this.setModel(oViewModel, "appView");

            var iOriginalBusyDelay = this.getView().getBusyIndicatorDelay();
            var fnSetAppNotBusy = function () {
                oViewModel.setProperty("/busy", false);
                oViewModel.setProperty("/delay", iOriginalBusyDelay);
            };

            // since then() has no "reject"-path attach to the MetadataFailed-Event to disable the busy indicator in case of an error
            this.getOwnerComponent().getModel("wb").metadataLoaded().then(fnSetAppNotBusy);
            this.getOwnerComponent().getModel("wb").attachMetadataFailed(fnSetAppNotBusy);

            // apply content density mode to root view
            this.getView().addStyleClass(this.getContentDensityClass());
        }
    });
});