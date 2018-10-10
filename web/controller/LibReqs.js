sap.ui.define([
        "sap/ui/base/Object",
        "sap/ui/model/json/JSONModel",
        "sap/ui/model/Filter",
        "sap/ui/model/FilterOperator",
        "com/modekzWaybill/controller/LibChangeStatus"
    ], function (BaseObject, JSONModel, Filter, FilterOperator, LibChangeStatus) {
        "use strict";

        return BaseObject.extend("com.modekzWaybill.controller.LibReqs", {
            owner: null,
            uiData: null,

            reqTable: null,
            searchField: null,
            statusCombo: null,

            prevFilt: null,

            constructor: function (owner, uiData) {
                var _this = this;
                _this.owner = owner;
                _this.status = owner.status;

                // Default values
                if (uiData.showOptColumn === undefined)
                    uiData.showOptColumn = true;

                if (uiData.showWbColumn === undefined)
                    uiData.showWbColumn = false;

                if (uiData.showReason === undefined)
                    uiData.showReason = false;

                if (uiData.showActual === undefined)
                    uiData.showActual = false;

                // Filter by status and sort group
                uiData.showSortGroup = uiData.showStatus = !!uiData.statuses;

                if (uiData.selectMode === undefined)
                    uiData.selectMode = sap.m.ListMode.None;

                // Where to put the fragment
                if (uiData.container === undefined)
                    uiData.container = "id_reqs_container";

                if (uiData.containerMethod === undefined)
                    uiData.containerMethod = "addContent";
                _this.uiData = uiData;


                // Add fragment by code
                var fragment = owner.createFragment("com.modekzWaybill.view.frag.ReqsTable", _this);
                var container = owner.byId(uiData.container);
                container[uiData.containerMethod].call(container, fragment);

                // Define UI params
                _this.owner.getView().setModel(new JSONModel(uiData), "ui");
            },

            onBrowserEvent: function (oEvent) {
                this._bKeyboard = oEvent.type === "keyup";
            },

            onSortPress: function (oEvent) {
                var _this = this;
                this.onSortGroupPress(oEvent.getSource(), "id_sort_menu", "com.modekzWaybill.view.frag.ReqsMenuSort", function (oEvent) {
                    var sorter = [];
                    var field = oEvent.getParameter("item").getId().replace("id_sort_", "");
                    if (field)
                        sorter.push(new sap.ui.model.Sorter(field));

                    _this.reqTable.getBinding("items").sort(sorter);
                });
            },

            onGroupPress: function (oEvent) {
                var _this = this;
                this.onSortGroupPress(oEvent.getSource(), "id_group_menu", "com.modekzWaybill.view.frag.ReqsMenuGroup", function (oEvent) {
                    var sorter = [];
                    var field = oEvent.getParameter("item").getId().replace("id_group_", "");
                    if (field)
                        sorter.push(new sap.ui.model.Sorter(field, null, function (oContext) {
                            var v = oContext.getProperty(field);
                            return {key: v, text: v};
                        }));

                    _this.reqTable.getBinding("items").sort(sorter);
                });
            },

            onSortGroupPress(menuButton, id, pathFragment, callBack) {
                if (!this[id])
                    this[id] = this.owner.createFragment(pathFragment, {
                        onSortGroupClick: callBack
                    });

                var eDock = sap.ui.core.Popup.Dock;
                this[id].open(this._bKeyboard, menuButton, eDock.BeginTop, eDock.BeginBottom, menuButton);
            },

            onUpdateStartedReqs(oEvent) {
                this.reqTable = oEvent.getSource();
                this.doFilter();
            },

            onComboSelectionChange: function (oEvent) {
                this.statusCombo = oEvent.getSource();
                this.doFilter();
            },

            onTextSearch: function (oEvent) {
                this.searchField = oEvent.getSource();
                this.doFilter();
            },

            doFilter: function (force) {
                // The table not ready yet
                if (!this.reqTable)
                    return;

                // No filter yet
                var filter = this.uiData.getFilter();
                this.reqTable.setBusy(!filter);
                if (!filter)
                    return;
                var arrFilter = [filter];

                // Text search and combo
                var textFilter = this.searchField ? this.searchField.getValue() : "";
                var comboFilter = this.statusCombo ? this.statusCombo.getSelectedKey() : "";

                // Called twice
                if (this.prevFilt && this.prevFilt.text === textFilter && this.prevFilt.combo === comboFilter && force !== true)
                    return;
                this.prevFilt = {
                    text: textFilter,
                    combo: comboFilter
                };

                // Filter by status
                if (comboFilter.length !== 0) {
                    if (parseInt(comboFilter) === this.owner.status.NOT_CREATED)
                        arrFilter.push(new Filter("Waybill_Id", FilterOperator.EQ, -1));
                    else
                        arrFilter.push(new Filter("Status", FilterOperator.EQ, comboFilter));
                }

                if (textFilter && textFilter.length > 0) {
                    var arr = [
                        new Filter("Objnr", FilterOperator.Contains, textFilter),
                        new Filter("Aufnr", FilterOperator.Contains, textFilter),
                        new Filter("Innam", FilterOperator.Contains, textFilter),
                        new Filter("Ilatx", FilterOperator.Contains, textFilter),
                        new Filter("Pltxt", FilterOperator.Contains, textFilter),
                        new Filter("Ltxa1", FilterOperator.Contains, textFilter),
                        new Filter("Stand", FilterOperator.Contains, textFilter),
                        new Filter("Priokx", FilterOperator.Contains, textFilter),
                        new Filter("Ktsch", FilterOperator.Contains, textFilter),
                        new Filter("KtschTxt", FilterOperator.Contains, textFilter)
                    ];

                    if (!isNaN(textFilter))
                        arr.push(new Filter("Waybill_Id", FilterOperator.EQ, textFilter));

                    arrFilter.push(
                        new Filter({
                            filters: arr,
                            and: false
                        }));
                }

                var andFilter = new Filter({filters: arrFilter, and: true});

                // And filter
                var reqsItems = this.reqTable.getBinding("items");
                this.owner.filterItemsByUserWerks({
                    field: "Iwerk",

                    and: andFilter,

                    ok: function (okFilter) {
                        reqsItems.filter(okFilter);
                    }
                });
            },

            onReqListSelectionChange(oEvent) {
                if (this.owner.onReqListSelectionChange)
                    this.owner.onReqListSelectionChange.call(this.owner, oEvent);
            },

            getPriority: function (priority) {
                switch (priority) {
                    case "1":
                        return "Error";
                    case "2":
                        return "Warning";
                    case "3":
                        return "Success";
                }
                return "None";
            },

            getPriorityIcon: function (priority) {
                switch (priority) {
                    case "1":
                        return "sap-icon://status-negative";
                    case "2":
                        return "sap-icon://status-in-process";
                    case "3":
                        return "sap-icon://status-positive";
                }
                return "sap-icon://status-inactive";
            },

            getOutObjnr: function (objnr) {
                return this.owner.alphaOut(objnr.substr(2, 10)) + "~" + this.owner.alphaOut(objnr.substr(12, 8))
            },

            waybillOut: function (waybillId) {
                // Do not show with empty waybill
                if (parseInt(waybillId) === -1)
                    return "";
                return waybillId;
            },

            getStatusText: function (statusInd, waybillId) {
                return this.owner.getStatusText.call(this.owner, statusInd, waybillId);
            },

            getStatusReasonText: function (id) {
                var _owner = this.owner;
                return _owner.getResText(_owner.status.REQ_STATUS_TEXTS, id);
            },

            onWaybillPress(oEvent) {
                this.owner.onWaybillPress.call(this.owner, oEvent);
            },

            onStatusReasonPress: function (oEvent) {
                var obj = oEvent.getSource().getBindingContext("wb").getObject();
                var changeStat = new LibChangeStatus(this.owner);
                var _this = this;
                var oWbModel = _this.owner.getModel("wb");

                // What would be edited
                var editFields = {
                    Objnr: obj.Objnr
                };
                changeStat.openDialog({
                    origin: 'REQ',
                    title: 'Закрытие заявки',
                    ok_text: "Подтвердить",
                    text: obj.Reason,
                    reason: obj.StatusReason,
                    fromDate: obj.FromDate ? obj.FromDate : obj.Gstrp,
                    toDate: obj.ToDate ? obj.ToDate : obj.Gltrp,
                    motoHour: obj.MotoHour,
                    dateEdit: true,

                    check: function (block) {
                        editFields.Reason = block.text;
                        editFields.StatusReason = parseInt(block.reason);
                        editFields.MotoHour = parseFloat(block.motoHour);
                        editFields.FromDate = block.fromDate;
                        editFields.ToDate = block.toDate;

                        block.afterChecked(true);
                    },

                    success: function () {
                        oWbModel.update("/ReqHeaders('" + obj.Objnr + "')", editFields, {
                            success: function () {
                                oWbModel.refresh();
                            },
                            error: function (err) {
                                _this.owner.showError(err, "Ошибка при обновлении заявки");
                            }
                        })
                    }
                });
            }
        });
    }
);