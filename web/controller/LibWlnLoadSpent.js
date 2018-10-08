sap.ui.define([
        'sap/ui/base/Object'
    ], function (BaseObject) {
        "use strict";

        return BaseObject.extend("com.modekzWaybill.controller.LibWlnLoadSpent", {
            owner: null,
            wlnModel: null,

            constructor: function (owner, id, wialonId, fromDate, toDate, callback) {
                var _this = this;
                _this.owner = owner;

                $.ajax({
                    dataType: "json",
                    url: "/././wialon/getSpentByWialon?wialonId=" + wialonId + "&from=" + fromDate + "&to=" + toDate,
                    success: function (result) {
                        callback.call(_this, result);
                    },
                    error: function () {
                        callback.call(_this, false);
                    }
                });
            }
        });
    }
);