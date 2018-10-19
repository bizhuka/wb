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
                reqLayout: "TwoColumnsMidExpanded",
                previousLayout: "",
                actionButtonsInfo: {
                    midColumn: {
                        fullScreen: false
                    }
                }
            });
            this.setModel(oViewModel, "appView");

            var iOriginalBusyDelay = this.getView().getBusyIndicatorDelay();
            var counter = 0;
            var fnSetAppNotBusy = function (result) {
                if (++counter >= 2) {
                    oViewModel.setProperty("/busy", false);
                    oViewModel.setProperty("/delay", iOriginalBusyDelay);
                }
            };

            // since then() has no "reject"-path attach to the MetadataFailed-Event to disable the busy indicator in case of an error
            this.getOwnerComponent().getModel("wb").metadataLoaded().then(fnSetAppNotBusy);
            this.getOwnerComponent().getModel("wb").attachMetadataFailed(fnSetAppNotBusy);

            // wait for response
            var userInfoModel = new JSONModel("/userInfo");
            userInfoModel.attachRequestFailed(fnSetAppNotBusy);
            userInfoModel.attachRequestCompleted(fnSetAppNotBusy);
            this.setModel(userInfoModel, "userInfo");

            // apply content density mode to root view
            this.getView().addStyleClass(this.getContentDensityClass());
        }
    });
});