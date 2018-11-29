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
                if (!container)
                    container = owner.findById(uiData.container);
                container[uiData.containerMethod].call(container, fragment);

                // Define UI params
                this.uiModel = new JSONModel(uiData);
                container.setModel(this.uiModel, "ui");
            },

            // onBrowserEvent: function (oEvent) {
            //     this._bKeyboard = oEvent.type === "keyup";
            // },

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
                //this._bKeyboard
                this[id].open(false, menuButton, eDock.BeginTop, eDock.BeginBottom, menuButton);
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
                        new Filter("KtschTxt", FilterOperator.Contains, textFilter),
                        new Filter("Fing", FilterOperator.Contains, textFilter)
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
                this.owner.filterBy({
                    filters: [
                        {
                            field: "Iwerk",
                            scope: "werks"
                        },

                        {
                            field: "Beber",
                            scope: "beber"
                        },

                        {
                            field: "Ingpr",
                            scope: "ingrp"
                        },

                        andFilter
                    ],

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

            showReqTime: function (duration, begTime, endTime) {
                var ok = begTime && endTime ?
                    begTime.getTime() !== -21600000 ||
                    endTime.getTime() !== -21600000 : false;
                var result = ok ? this.toShortTime(begTime) + " - " +
                    (endTime.getTime() === -21600000 ? "24:00" : this.toShortTime(endTime)) : "";

                // Duration in hours
                if (parseInt(duration)) {
                    var hours = Math.floor(duration);
                    var minutes = Math.round((duration - hours) * 60);

                    if (hours < 10) hours = "0" + hours;
                    if (minutes < 10) minutes = "0" + minutes;

                    result += ' (' + hours + ':' + minutes + ')';
                }

                return result;
            },

            toShortTime: function (time) {
                return time ? time.toLocaleTimeString("ru-RU", {
                    hour: '2-digit',
                    minute: '2-digit'
                }) : "";
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

            onWaybillPress: function (oEvent) {
                this.owner.onWaybillPress.call(this.owner, oEvent);
            },

            getHourDiff: function (fromDate, toDate) {
                if (!fromDate || !toDate)
                    return "";

                var sec_num = (toDate.getTime() - fromDate.getTime()) / 1000;

                var hours = Math.floor(sec_num / 3600);
                var minutes = Math.round((sec_num - (hours * 3600)) / 60);
                //var seconds = sec_num - (hours * 3600) - (minutes * 60);

                if (hours < 10) hours = "0" + hours;
                if (minutes < 10) minutes = "0" + minutes;
                //if (seconds < 10) seconds = "0" + seconds;
                return hours + ':' + minutes; //+ ':' + seconds;
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
                var bundle = _this.owner.getBundle();
                changeStat.openDialog({
                    origin: 'REQ',
                    title: bundle.getText("closeReqs"),
                    ok_text: bundle.getText("confirm"),
                    text: obj.Reason ? obj.Reason : bundle.getText("done"),
                    reason: obj.StatusReason,
                    fromDate: obj.FromDate ? obj.FromDate : obj.Gstrp,
                    toDate: obj.ToDate ? obj.ToDate : obj.Gltrp,
                    dateEdit: true,

                    check: function (block) {
                        editFields.Reason = block.text;
                        editFields.StatusReason = parseInt(block.reason);
                        editFields.FromDate = block.fromDate;
                        editFields.ToDate = block.toDate;

                        block.afterChecked(editFields.StatusReason !== _this.owner.status.REQ_SET);
                    },

                    success: function () {
                        oWbModel.update("/ReqHeaders('" + obj.Objnr + "')", editFields, {
                            success: function () {
                                oWbModel.refresh();
                            },
                            error: function (err) {
                                _this.owner.showError(err, _this.owner.getBundle().getText("errUpdateReqs"));
                            }
                        })
                    }
                });
            },

            setWaybillId: function (selectedReqs, params) {
                // Update TORO header request
                var owner = this.owner;
                var oWbModel = owner.getModel("wb");

                // If reqs updated
                var cnt = 0;
                var checkIsLast = function () {
                    if (--cnt === 0)
                        oWbModel.refresh();
                };
                if (selectedReqs.length === 0)
                    oWbModel.refresh();

                for (var i = 0; i < selectedReqs.length; i++) {
                    var item = oWbModel.getProperty(selectedReqs[i].sPath);

                    // Only if is equal to original
                    if (params.unset && parseInt(item.Waybill_Id) !== parseInt(params.waybillId))
                        continue;
                    cnt++;

                    // Modify to new WAYBILL
                    var reqHeader = {
                        Objnr: item.Objnr,
                        Waybill_Id: params.unset ? "-1" : params.waybillId,
                        StatusReason: params.unset ? owner.status.REQ_NEW : owner.status.REQ_SET
                    };
                    oWbModel.update("/ReqHeaders('" + item.Objnr + "')", reqHeader, {
                        success: function () {
                            checkIsLast();
                        },

                        error: function (err) {
                            owner.showError(err, owner.getBundle().getText("errUpdateReqs"));
                            checkIsLast();
                        }
                    })
                }

                // Select items again
                this.reqTable.removeSelections(true);
            }
        });
    }
);