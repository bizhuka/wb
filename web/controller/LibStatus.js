sap.ui.define([
        'sap/ui/base/Object',
        'sap/ui/model/json/JSONModel'
    ], function (BaseObject, JSONModel) {
        "use strict";

        // Static data
        var allTexts = null;

        return BaseObject.extend("com.modekzWaybill.controller.LibStatus", {
            owner: null,

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

            WB_STATUS: "WB", // Waybill
            RC_STATUS: "RC", // Request confirm
            RR_STATUS: "RR", // Request reject
            DR_STATUS: "DR", // Delay reason

            constructor: function (owner, statTexts) {
                if (owner) {
                    owner.getView().setModel(new JSONModel(this), "status");
                    this.owner = owner;
                    return;
                }

                // Called as static constructor
                if (!statTexts)
                    return;
                allTexts = statTexts;
            },

            getStatusLangArray: function (name) {
                // Already prepared
                if (this[name])
                    return this[name];

                // Return as an array
                var result = [];

                // Detect language
                var lang = this.owner.getBundle().getText('lang') === 'ru' ? 'Ru' : 'Kz';
                for (var i = 0; i < allTexts.length; i++) {
                    var item = allTexts[i];

                    // Type of status
                    if (item.Stype !== name)
                        continue;

                    result.push({
                        key: parseInt(item.Id),
                        text: item[lang]
                    });
                }

                // Save and return
                this[name] = result;
                return result;
            },

            getStatusLangText: function (name, id) {
                if (id === undefined)
                    return "-Error-";

                var arr = this.getStatusLangArray(name);
                for (var i = 0; i < arr.length; i++) {
                    var item = arr[i];
                    if (item.key === id)
                        return item.text;
                }

                // No mapping
                return "-E-" + id + "-E-";
            }
        });
    }
);