sap.ui.define([
        'sap/ui/base/Object',
        'sap/ui/model/json/JSONModel'
    ], function (BaseObject, JSONModel) {
        "use strict";

        return BaseObject.extend("com.modekzWaybill.controller.LibStatus", {
            // Waybill statuses
            NOT_CREATED: 0,
            CREATED: 10,
            //AGREED: 20,
            REJECTED: 30,
            IN_PROCESS: 40,
            ARRIVED: 50,
            CLOSED: 60,

            // Request statuses
            REQ_NEW: 100,
            REQ_SET: 200,

            // Delay status
            NO_DELAY: 1000,

            STATUS_TEXTS: "statusTexts",
            REQ_STATUS_TEXTS: "reqStatusTexts",
            DELAY_TEXTS: "delayTexts",

            constructor: function (owner) {
                owner.getView().setModel(new JSONModel(this), "status");
            }
        });
    }
);