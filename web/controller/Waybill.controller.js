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
            this.getRouter().navTo("waybillDetail", {waybillId: oItem.Id});
        },

        onUpdateStartedTable: function () {
            var oFilter = [];

            var textFilter = wbSearchField.getValue();
            var comboFilter = wbStatusCombo.getSelectedKey();

            // Called twice
            if (prevFilt && prevFilt.text === textFilter && prevFilt.combo === comboFilter)
                return;
            prevFilt = {
                text: textFilter,
                combo: comboFilter
            };

            if (textFilter && textFilter.length > 0) {
                var arr = [
                    new Filter("Description", FilterOperator.Contains, textFilter),
                    new Filter("Fio", FilterOperator.Contains, textFilter),
                    new Filter("Driver", FilterOperator.Contains, textFilter),
                    new Filter("Equnr", FilterOperator.Contains, textFilter),
                    new Filter("Eqktx", FilterOperator.Contains, textFilter),
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

        getWaybillInfo: function (status, reqCnt, schCnt, histCnt, gasCnt, delayReason) {
            var result = {
                status: parseInt(status),
                reqCnt: parseInt(reqCnt),
                schCnt: parseInt(schCnt),
                histCnt: parseInt(histCnt),
                gasCnt: parseInt(gasCnt),
                delayReason: parseInt(delayReason),
                errors: [],
                info: []
            };

            // No GSM
            if (result.status >= this.status.IN_PROCESS && result.gasCnt === 0)
                result.errors.push("Нет позиций ГСМ (GasSpent) после выезда из гаража");

            switch (result.status) {
                case this.status.REJECTED:
                    if (result.schCnt > 0)
                        result.errors.push("В журнале работ (Schedule) имееются позиции");

                    if (result.histCnt === 0)
                        result.errors.push("В журнале заявок (ReqHistory) нет позиций");
                    break;

                case this.status.CLOSED:
                    if (result.histCnt > 0)
                        result.errors.push("Остались позиции в журнале заявок (ReqHistory)");
                    break;
            }

            if (result.status !== this.status.REJECTED) {
                if (result.reqCnt === 0)
                    result.errors.push("Нет заявок (ReqHeader)");

                if (result.schCnt === 0)
                    result.errors.push("Нет журнала работ (Schedule)");
            }

            if (result.delayReason > 0)
                result.info.push("Причина смещения сроков " + this.getDelayReasonText(result.delayReason));

            return result;
        },

        errorDesc: function (status, reqCnt, schCnt, histCnt, gasCnt, delayReason) {
            var wbInfo = this.getWaybillInfo(status, reqCnt, schCnt, histCnt, gasCnt, delayReason);

            // Join together
            var message = wbInfo.errors.concat(wbInfo.info);

            return message.length === 0 ? "" : message.join("\n");
        },

        rowHighlight: function (status, reqCnt, schCnt, histCnt, gasCnt, delayReason) {
            var wbInfo = this.getWaybillInfo(status, reqCnt, schCnt, histCnt, gasCnt, delayReason);

            if (wbInfo.errors.length > 0)
                return MessageType.Error;

            // Отмена
            if (wbInfo.status === this.status.REJECTED)
                return MessageType.Warning;

            // Закрытые
            if (wbInfo.status === this.status.CLOSED)
                return MessageType.Success;

            if (wbInfo.info.length > 0)
                return MessageType.Information;

            return MessageType.None;
        }

    });
});