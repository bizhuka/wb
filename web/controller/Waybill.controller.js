sap.ui.define([
    "com/modekzWaybill/controller/BaseController",
    "sap/ui/core/UIComponent",
    "sap/ui/model/odata/v2/ODataModel",
    "sap/ui/model/Sorter",
    "sap/ui/model/Filter",
    'sap/ui/model/FilterOperator',
    "sap/ui/model/json/JSONModel",
    "sap/ui/core/MessageType"
], function (BaseController, UIComponent, ODataModel, Sorter, Filter, FilterOperator, JSONModel, MessageType) {
    "use strict";

    var wayBillTable, wbSearchField, wbStatusCombo;
    var prevFilt;

    return BaseController.extend("com.modekzWaybill.controller.Waybill", {
        onInit: function () {
            // call base init
            var _this = this;
            BaseController.prototype.onInit.apply(_this, arguments);

            wayBillTable = this.byId("id_waybill_table");
            wbSearchField = this.byId("id_wb_search_field");

            wbStatusCombo = this.byId("id_wb_status_combo");

            // What status to show
            var filtered = _this.getResourceArray(_this.status.STATUS_TEXTS).filter(function (pair) {
                return pair.key !== _this.status.NOT_CREATED;
            });
            wbStatusCombo.setModel(new JSONModel(filtered));
        },

        onLineItemPressed: function (oEvent) {
            var oItem = oEvent.getSource().getBindingContext("wb").getObject();
            this.onWaybillPress(oItem.Id);
        },

        onUpdateStartedTable: function () {
            var oFilter = [];

            var textFilter = wbSearchField.getValue();
            var comboFilter = wbStatusCombo.getSelectedKey();
            var byToo = this.byId('id_by_too_checkbox').getSelected();

            // Called twice
            if (prevFilt && prevFilt.text === textFilter && prevFilt.combo === comboFilter && prevFilt.byToo === byToo)
                return;
            prevFilt = {
                text: textFilter,
                combo: comboFilter,
                byToo: byToo
            };

            if (textFilter && textFilter.length > 0) {
                var arr = [
                    new Filter("Description", FilterOperator.Contains, textFilter),
                    new Filter("Fio", FilterOperator.Contains, textFilter),
                    new Filter("Driver", FilterOperator.Contains, textFilter),
                    new Filter("Equnr", FilterOperator.Contains, textFilter),
                    new Filter("Eqktx", FilterOperator.Contains, textFilter),
                    new Filter("TooName", FilterOperator.Contains, textFilter),
                    new Filter("License_num", FilterOperator.Contains, textFilter)
                ];
                if (!isNaN(textFilter))
                    arr.push(new Filter("Id", FilterOperator.EQ, textFilter));

                oFilter.push(
                    new Filter({
                        filters: arr,
                        and: false
                    }));
            }

            if (comboFilter.length !== 0)
                oFilter.push(new Filter("Status", FilterOperator.EQ, comboFilter));

            if (byToo)
                oFilter.push(new Filter("TooName", FilterOperator.NE, '-'));

            var andFilter = oFilter.length > 0 ? new Filter({filters: oFilter, and: true}) : null;

            this.filterItemsByUserWerks({
                field: "Werks",

                and: andFilter,

                ok: function (okFilter) {
                    wayBillTable.getBinding("items").filter(okFilter);
                }
            });
        },

        handleSelection: function () {
            this.onUpdateStartedTable()
        },

        onSortPressed: function () {
            if (!this._oSortDialog)
                this._oSortDialog = this.createFragment("com.modekzWaybill.view.frag.WaybillSortDialog");
            this._oSortDialog.open();
        },

        onSortDialogConfirmed: function (oEvent) {
            var mParams = oEvent.getParameters();
            var oBinding = wayBillTable.getBinding("items");

            var sPath = mParams.sortItem.getKey();
            var bDescending = mParams.sortDescending;
            var sorters = new Sorter(sPath, bDescending);
            oBinding.sort(sorters);
        },

        onResetPressed: function () {
            wayBillTable.getBinding("items").sort(null);
            wbSearchField.setValue("");
            wbStatusCombo.setValue("");

            this.onUpdateStartedTable();
        },

        getWaybillInfo: function (status, reqCnt, schCnt, histCnt, gasCnt, delayReason, tooName) {
            var result = {
                status: parseInt(status),
                reqCnt: parseInt(reqCnt),
                schCnt: parseInt(schCnt),
                histCnt: parseInt(histCnt),
                gasCnt: parseInt(gasCnt),
                delayReason: parseInt(delayReason),
                tooName: tooName,
                errors: [],
                info: []
            };

            var bundle = this.getBundle();
            // No GSM
            if (result.status >= this.status.IN_PROCESS && result.gasCnt === 0)
                result.errors.push(bundle.getText("noGsm", ["GasSpent"]));

            switch (result.status) {
                case this.status.REJECTED:
                    if (result.schCnt > 0)
                        result.errors.push(bundle.getText("hasSchedule", ["Schedule"]));

                    if (result.histCnt === 0)
                        result.errors.push(bundle.getText("noReqHistory", ["ReqHistory"]));
                    break;

                case this.status.CLOSED:
                    if (result.histCnt > 0)
                        result.errors.push(bundle.getText("hasReqHistory", ["ReqHistory"]));
                    break;
            }

            if (result.status !== this.status.REJECTED) {
                if (result.reqCnt === 0)
                    result.errors.push(bundle.getText("noReqs") + " (ReqHeader)");

                if (result.schCnt === 0)
                    result.errors.push(bundle.getText("noSchedule", ["Schedule"]));
            }

            if (result.delayReason > 0)
                result.info.push(bundle.getText("delayReason") + " " + this.getDelayReasonText(result.delayReason));
            if (result.tooName !== '-')
                result.info.push(bundle.getText("too") + " " + result.tooName);

            return result;
        },

        errorDesc: function (status, reqCnt, schCnt, histCnt, gasCnt, delayReason, tooName) {
            var wbInfo = this.getWaybillInfo(status, reqCnt, schCnt, histCnt, gasCnt, delayReason, tooName);

            // Join together
            var message = wbInfo.errors.concat(wbInfo.info);

            return message.length === 0 ? "" : message.join("\n");
        },

        rowHighlight: function (status, reqCnt, schCnt, histCnt, gasCnt, delayReason, tooName) {
            var wbInfo = this.getWaybillInfo(status, reqCnt, schCnt, histCnt, gasCnt, delayReason, tooName);

            if (wbInfo.errors.length > 0)
                return MessageType.Error;

            // Cancelled
            if (wbInfo.status === this.status.REJECTED)
                return MessageType.Warning;

            // Ok closed
            if (wbInfo.status === this.status.CLOSED)
                return MessageType.Success;

            if (wbInfo.info.length > 0)
                return MessageType.Information;

            return MessageType.None;
        }

    });
});