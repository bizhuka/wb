sap.ui.define([
    'com/modekzWaybill/controller/BaseController',
    'sap/m/MessageToast'
], function (BaseController, MessageToast) {
    "use strict";

    return BaseController.extend("com.modekzWaybill.controller.ImportR3", {
        onInit: function () {
            // call base init
            BaseController.prototype.onInit.apply(this, arguments);
        },

        importWerks: function () {
            this.updateDbFrom({
                link: "/r3/WERK?_persist=true",

                title: "Заводы - БЕ"
            });
        },

        importGasType: function () {
            this.updateDbFrom({
                link: "/r3/GAS_TYPE?_persist=true",

                title: "Материалы"
            });
        },

        loadWlnVehicle:function(){
            this.updateDbFrom({
                link: "/wialon/loadWlnVehicle",

                title: "Объектов wialon"
            });
        },

        onUploadDriverComplete: function (oEvent) {
            var sResponse = $(oEvent.getParameter("response")).text();
            var _this = this;
            _this.showUpdateInfo(JSON.parse(sResponse), {
                title: "Обновление Id карт",
                afterUpdate: function () {
                    _this.getModel("wb").refresh();
                }
            })
        },

        onUploadDriverPress: function (oEvent) {
            this.byId("id_driver_uploader").upload();
        }
    });
});