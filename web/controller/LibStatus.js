sap.ui.define([
        'sap/ui/base/Object',
        'sap/ui/model/json/JSONModel'
    ], function (BaseObject, JSONModel) {
        "use strict";

        return BaseObject.extend("com.modekzWaybill.controller.LibStatus", {
            NOT_CREATED: 0,
            CREATED: 10,
            AGREED: 20,
            REJECTED: 30,
            IN_PROCESS: 40,
            ARRIVED: 50,
            CLOSED: 60,

            STATUS_TEXTS: "statusTexts",
            DELAY_TEXTS: "delayTexts",
            REQ_STATUS_TEXTS: "reqStatusTexts",

            constructor: function (owner) {
                owner.getView().setModel(new JSONModel(this), "status");
            }
        });
    }
);