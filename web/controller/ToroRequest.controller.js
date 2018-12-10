sap.ui.define([
    'com/modekzWaybill/controller/BaseController',
    'sap/m/MessageToast',
    'sap/m/Label',
    'sap/m/ButtonType',
    'sap/ui/core/MessageType',
    'sap/ui/model/Filter',
    'sap/ui/model/FilterOperator',
    'sap/ui/model/json/JSONModel',
    'sap/ui/core/UIComponent',
    'com/modekzWaybill/controller/LibReqs',
    'com/modekzWaybill/controller/LibChangeStatus'
], function (BaseController, MessageToast, Label, ButtonType, MessageType, Filter, FilterOperator, JSONModel, UIComponent, LibReqs, LibChangeStatus) {
    "use strict";

    var C_FIX_COLUMN = 2;

    var eoFilterInfo = {
        classFilter: null,
        classFilterPrev: null,
        textFilter: null,
        wholeFilterPrev: null
    };

    var fromDate;
    var toDate;

    return BaseController.extend("com.modekzWaybill.controller.ToroRequest", {

        onInit: function () {
            // call base init
            var _this = this;
            BaseController.prototype.onInit.apply(_this, arguments);

            this.detailHeader = this.byId("id_detail_header");
            this.tbSchedule = this.byId("id_eo_schedule");
            this.dpFrom = this.byId("dpFrom");
            this.dpTo = this.byId("dpTo");

            // Date from to
            var datFrom = new Date();
            var datTo = this.addDays(datFrom, 3);

            this.dpFrom.setDateValue(datFrom);
            this.dpTo.setDateValue(datTo);

            this.onDatePickChange(null);

            // What status to show
            var filtered = _this.status.getStatusLangArray(_this.status.WB_STATUS).filter(function (pair) {
                return pair.key === _this.status.NOT_CREATED || pair.key === _this.status.REJECTED;
            });

            this.libReqs = new LibReqs(this, {
                showWbColumn: true,
                selectMode: sap.m.ListMode.MultiSelect,
                statuses: filtered,
                canReject: true,
                getFilter: function () {
                    return new Filter({
                        filters: [
                            new Filter("Waybill_Id", FilterOperator.EQ, _this.status.WB_ID_NULL), // .EQ NOT_CREATED
                            new Filter("Status", FilterOperator.EQ, _this.status.REJECTED)
                        ],
                        and: false
                    })
                }
            });

            var oRouter = UIComponent.getRouterFor(this);
            oRouter.getRoute("toroRequest").attachPatternMatched(this._onObjectMatched, this);
        },

        _onObjectMatched: function () {
            var listItems = this.tbSchedule.getBinding("items");
            if (!listItems)
                return;

            var _this = this;
            listItems.attachDataReceived(function () {
                _this.readSchedule();
            });
            _this.readSchedule();
        },

        onReqStateChange: function (oEvent) {
            // Show additional columns if have enough space
            var reqLayout = oEvent ? oEvent.getParameter("layout") : this.getModel("appView").getProperty("/reqLayout");
            this.libReqs.uiModel.setProperty("/showOptColumn", reqLayout === "TwoColumnsBeginExpanded" || reqLayout === "OneColumn");
        },

        setReqLayout: function (newLayout) {
            var appViewModel = this.getModel("appView");
            var curLayout = appViewModel.getProperty("/reqLayout");

            if (curLayout.indexOf('Expanded') > 0)
                this.prevReqLayout = curLayout;

            if (newLayout === undefined)
                newLayout = this.prevReqLayout;

            if (!newLayout)
                newLayout = "TwoColumnsMidExpanded";

            appViewModel.setProperty("/reqLayout", newLayout);
            this.onReqStateChange();
        },

        navOneColumn: function () {
            this.setReqLayout("OneColumn");
        },

        navMidColumnFullScreen: function () {
            this.setReqLayout("MidColumnFullScreen");
        },

        navBackLayout: function () {
            this.setReqLayout();
        },

        readSchedule: function () {
            var _this = this;
            var eoItems = _this.tbSchedule.getBinding("items").getContexts();
            if (!eoItems.length)
                return;

            var oWbModel = _this.getModel("wb");

            var items = {};
            var werksArr = [], werksKeys = [];
            var equnrArr = [];

            for (var i = 0; i < eoItems.length; i++) {
                var context = eoItems[i];
                var item = oWbModel.getProperty(context.sPath);

                // Save for changing
                items[item.Equnr] = {
                    path: context.sPath,
                    row: item,
                    changed: false
                };

                if (werksKeys.indexOf(item.Swerk) < 0) {
                    werksKeys.push(item.Swerk);
                    werksArr.push(new Filter("Werks", FilterOperator.EQ, item.Swerk));
                }
                equnrArr.push(new Filter("Equnr", FilterOperator.EQ, item.Equnr));
            }

            oWbModel.read("/Schedules", {
                filters: [
                    new Filter({
                        filters: [
                            new Filter("Datum", FilterOperator.BT,
                                _this.addDays(this.dpFrom.getDateValue(), -1),
                                _this.addDays(this.dpTo.getDateValue(), 1)),
                            new Filter({
                                filters: werksArr,
                                and: false
                            }),
                            new Filter({
                                filters: equnrArr,
                                and: false
                            })
                        ],
                        and: true
                    })
                ],

                success: function (odata) {
                    var schedules = odata.results;
                    if (!_this.tbSchedule)
                        return;

                    var items = _this.tbSchedule.getItems();

                    // Without time!
                    var dFrom = _this.dpFrom.getDateValue();
                    dFrom.setHours(0, 0, 0, 0);

                    var showOne = _this.getModel("userInfo").getProperty("/WbShowOne") === true;

                    for (var i = 0; i < items.length; i++) {
                        var cells = items[i].getCells();
                        // Empty cells
                        for (var c = C_FIX_COLUMN; c < schedules.length; c++) {
                            var link = cells[c];
                            if (link) {
                                link.setText("-");
                                link.setEnabled(false);
                            }
                        }

                        var equnr = items[i].getBindingContext("wb").getObject().Equnr;

                        for (var s = 0; s < schedules.length; s++) {
                            var schedule = schedules[s];
                            if (schedule.Equnr === equnr) {
                                var daysOff = _this.diffInDays(schedule.Datum, dFrom) + C_FIX_COLUMN;

                                link = cells[daysOff];
                                if (link && daysOff > (C_FIX_COLUMN - 1)) {
                                    link.setText(schedule.Ilart ? schedule.Ilart : schedule.Waybill_Id);
                                    link.setEnabled(!schedule.Ilart && showOne);
                                }
                            }
                        }
                    }
                }
            });
        },

        onUpdateStartedSchedule: function () {
            var _this = this;
            var textFilter = _this.byId('id_eo_search').getValue();

            if (eoFilterInfo.classFilterPrev === eoFilterInfo.classFilter && eoFilterInfo.textFilter === textFilter)
                return;
            eoFilterInfo.classFilterPrev = eoFilterInfo.classFilter;
            eoFilterInfo.textFilter = textFilter;

            this.filterBy({
                filters: [
                    {
                        field: "Swerk",
                        scope: "werks"
                    },

                    eoFilterInfo.classFilter
                ],

                ok: function (okFilter) {
                    eoFilterInfo.wholeFilterPrev = textFilter ?
                        _this.makeAndFilter(okFilter, _this.getEoTextFilter(textFilter)) :
                        okFilter;
                    this.tbSchedule.getBinding("items").filter(eoFilterInfo.wholeFilterPrev);
                }
            });
        },

        onReqUpdate: function () {
            var _this = this;
            // Watch @29!
            _this.updateDbFrom({
                link: "/r3/REQ_HEADER?_persist=true&_where="
                + encodeURIComponent("AFKO~GSTRP >= '" + _this.toSapDate(_this.addDays(new Date(), -29)) + "'"),

                title: _this.getBundle().getText("reqs"),

                afterUpdate: function () {
                    _this.libReqs.reqTable.getBinding("items").refresh();
                }
            });
        },

        onEoUpdate: function () {
            this.eoUpdate(function () {
                this.tbSchedule.getBinding("items").refresh();
            });
        },

        onNavToFinished: function () {
            this.getRouter().navTo("finishedReqs");
        },

        onDatePickChange: function (oEvent) {
            var dFrom = this.dpFrom.getDateValue();
            var dTo = this.dpTo.getDateValue();

            if (oEvent) {
                var diff = this.diffInDays(dTo, dFrom);
                if (diff > 10 || diff < 0)
                    switch (oEvent.getSource()) {
                        case this.dpTo:
                            this.dpFrom.setDateValue(this.addDays(dTo, -7));
                            break;

                        case this.dpFrom:
                            this.dpTo.setDateValue(this.addDays(dFrom, 7));
                            break;
                    }
            }

            // Use copy
            dFrom = new Date(this.dpFrom.getDateValue());
            dTo = new Date(this.dpTo.getDateValue());

            // Binding cells
            var info = this.tbSchedule.getBindingInfo("items");
            var arrCells = info.template.getCells();
            arrCells.splice(C_FIX_COLUMN);

            // Columns
            for (var i = this.tbSchedule.getColumns().length; i >= C_FIX_COLUMN; i--)
                this.tbSchedule.removeColumn(i);

            // Create new cells & columns this.tbSchedule.destroyColumns();
            while (dFrom < dTo) {
                // Column
                var text = this.toLocaleDate(dFrom);
                this.tbSchedule.addColumn(new sap.m.Column({header: new Label({text: text})}));

                // Cell
                arrCells.push(new sap.m.Link({
                    text: "-",
                    enabled: false,
                    press: function () {
                        _this.onWaybillPress(parseInt(this.getText()))
                    }
                }));

                dFrom.setDate(dFrom.getDate() + 1);
            }

            // template: this.byId ?
            var oTemplate = new sap.m.ColumnListItem({
                cells: arrCells,
                highlight: {
                    parts: [{path: 'wb>TooName'}, {path: 'wb>NoDriverDate'}],

                    formatter: function (tooName, noDriverDate) {
                        if (_this.isNoDriver(noDriverDate))
                            return MessageType.Warning;

                        return tooName !== '-' ? MessageType.Information : MessageType.None;
                    }
                }
            });

            this.tbSchedule.bindItems({
                path: "wb>" + info.path,
                template: oTemplate,
                filters: eoFilterInfo.wholeFilterPrev
            });

            // Read from R3
            var _this = this;

            // Check rights
            var userModel = new JSONModel("/./userInfo");
            userModel.attachRequestCompleted(function () {
                var loadSchedule = userModel.getProperty("/WbLoaderSchedule") === true;
                if (!loadSchedule)
                    _this._onObjectMatched();
                else
                    _this.updateDbFrom({
                        link: "/r3/SCHEDULE?_persist=true&_where=" + encodeURIComponent(
                            "AFKO~GSTRP <= '" + _this.toSapDate(_this.dpTo.getDateValue()) + "' AND " +
                            "AFKO~GLTRP >= '" + _this.toSapDate(_this.dpFrom.getDateValue()) + "'"),

                        title: _this.getBundle().getText("journal"),

                        timeout: 2500, // 2,5 seconds

                        afterUpdate: function () {
                            _this._onObjectMatched();
                        }
                    });
            });
        },

        eoTooltip: function (equnr, nClass, tooName, noDriverDate) {
            var result = [];
            result.push(this.getBundle().getText(tooName === '-' ? 'eo' : 'idToo') + ":");
            result.push(this.alphaOut(equnr));
            if (tooName !== '-')
                result.push(tooName);
            result.push(this.getBundle().getText("class") + ":");
            result.push(nClass);

            if (this.isNoDriver(noDriverDate))
                result.push(this.getBundle().getText("noDriverToDate", [this.toLocaleDate(noDriverDate)]));

            return result.join("\n");
        },

        isNoDriver: function (noDriverDate) {
            return noDriverDate && this.toSapDate(noDriverDate) === this.toSapDate(new Date());
        },

        onReqListSelectionChange: function (oEvt) {
            // get selection
            var oWbModel = this.getModel("wb");
            var arrContexts = this.libReqs.reqTable.getSelectedContexts(true);

            this.detailHeader.destroyAttributes();
            var cnt = arrContexts ? arrContexts.length : 0;
            this.detailHeader.setNumber(cnt + " " + this.getBundle().getText("request"));

            eoFilterInfo.classFilter = null;
            if (cnt !== 0) {
                var arrClass = {};
                fromDate = new Date(8640000000000000);
                toDate = new Date(0);

                for (var i = 0; i < arrContexts.length; i++) {
                    var item = oWbModel.getProperty(arrContexts[i].sPath);
                    arrClass[item.Ktsch] = item.Ktsch;

                    this.detailHeader.addAttribute(new sap.m.ObjectAttribute({
                        title: item.Aufnr,
                        text: item.Innam + " - " + item.Ltxa1 + " - " + item.Pltxt
                    }));

                    if (item.Gstrp.getTime() < fromDate.getTime())
                        fromDate = item.Gstrp;
                    if (item.Gltrp.getTime() > toDate.getTime())
                        toDate = item.Gltrp;
                }

                // Set range of waybill
                if (toDate === 0) {
                    this.detailHeader.setNumberUnit("");
                    fromDate = toDate = null;
                } else
                    this.detailHeader.setNumberUnit(
                        this.getBundle().getText("reqsPeriod",
                            [this.toLocaleDate(fromDate),
                                this.toLocaleDate(toDate),
                                this.diffInDays(toDate, fromDate) + 1]));

                var oFilters = [];
                for (var key in arrClass)
                    if (arrClass.hasOwnProperty(key)) {
                        // "0" & Ktsch === N_class
                        // key = "0" + key;
                        oFilters.push(new Filter("N_class", FilterOperator.EQ, key));
                    }


                // Called twice
                eoFilterInfo.classFilter = new Filter({
                    filters: oFilters,
                    and: false
                });
                this.checkEoFilter();
            }

            this.onUpdateStartedSchedule();

            // If have no reqs
            this.onEquipSelected();
        },

        onEquipSelected: function () {
            var createButton = this.byId('id_wb_create_button');
            var eoItem = this.tbSchedule.getSelectedItem();
            var selectedReqs = this.libReqs.reqTable.getSelectedContexts(true);
            var userInfo = this.getModel("userInfo");

            if (!eoItem || !userInfo.getProperty("/WbCreateNew")) {
                createButton.setVisible(false);
                return;
            }
            createButton.setVisible(true);

            // Change appearance
            eoItem = eoItem.getBindingContext("wb").getObject();
            var withNoReqs = (selectedReqs.length === 0) && userInfo.getProperty("/WbCreateNoReq");
            var addText = withNoReqs ? " - " + this.getBundle().getText("noReqs2") : "";
            if (eoItem.TooName !== '-') {
                createButton.setIcon(sap.ui.core.IconPool.getIconURI("sap-icon://request"));
                createButton.setText(this.getBundle().getText("createWithToo") + addText);
            } else {
                createButton.setIcon(sap.ui.core.IconPool.getIconURI("sap-icon://create-form"));
                createButton.setText(this.getBundle().getText("createWb") + addText);
            }
            createButton.setType(withNoReqs ? ButtonType.Reject : ButtonType.Default);
        },

        checkEoFilter: function () {
            if (!eoFilterInfo.classFilter || eoFilterInfo.classFilter.aFilters.length <= 1)
                return true;

            var message = "";
            for (var i = 0; i < eoFilterInfo.classFilter.aFilters.length; i++) {
                var key = eoFilterInfo.classFilter.aFilters[i].oValue1;

                if (message)
                    message += ("\n" + key);
                else
                    message = key;
            }

            MessageToast.show(this.getBundle().getText("errDiffClass", [message]));
            return false;
        },

        checkReqs: function (selectedReqs, oWaybill, callBack) {
            var _this = this;

            if (selectedReqs.length === 0 && oWaybill.WithNoReqs) {
                // Second check
                _this.checkSchedule(oWaybill, callBack);
                return;
            }

            var oWbModel = _this.getModel("wb");
            var reqFilter = [];

            for (var i = 0; i < selectedReqs.length; i++) {
                var item = oWbModel.getProperty(selectedReqs[i].sPath);
                reqFilter.push(new Filter("Objnr", FilterOperator.EQ, item.Objnr));
            }

            oWbModel.read("/VReqHeaders", {
                filters: [new Filter({
                    filters: reqFilter,
                    and: false
                })],

                success: function (oData) {
                    var items = oData.results;
                    for (var i = 0; i < items.length; i++) {
                        var item = items[i];
                        if (parseInt(item.Waybill_Id) === _this.status.WB_ID_NULL || item.Status === _this.status.REJECTED)
                            continue;

                        callBack.call(_this, _this.getBundle().getText("errReqsProcessed", [item.Objnr]));
                        return;
                    }

                    // Second check
                    _this.checkSchedule(oWaybill, callBack);
                },

                error: function () {
                    callBack.call(_this, _this.getBundle().getText("errReadReqs"));
                }
            });
        },

        onCreateWaybill: function () {
            // get selection
            var _this = this;
            var selectedReqs = this.libReqs.reqTable.getSelectedContexts(true);
            var eoItem = this.tbSchedule.getSelectedItem();

            // Different classes
            if (!this.checkEoFilter())
                return;

            var haveRights = this.getModel("userInfo").getProperty("/WbCreateNoReq");
            if (selectedReqs.length === 0 || !eoItem) {
                if (haveRights)
                    MessageToast.show(this.getBundle().getText("selectItems"));
                else
                    _this.showError(null, this.getBundle().getText("selectItems"));

                // If no car or do not have permission
                if (!eoItem || !haveRights)
                    return;
            }

            // Prepare objects
            var oWbModel = this.getModel("wb");
            eoItem = oWbModel.getProperty(eoItem.getBindingContextPath());

            // New waybill
            var oWaybill = {
                Equnr: eoItem.Equnr,
                Werks: eoItem.Swerk,
                Bukrs: eoItem.Bukrs,
                Description: "",
                CreateDate: new Date(1),
                FromDate: fromDate,
                ToDate: toDate,
                Status: _this.status.CREATED,
                WithNoReqs: selectedReqs.length === 0
            };

            var createWbDialog = new LibChangeStatus(_this);
            createWbDialog.openDialog({
                origin: _this.status.DR_STATUS,
                title: _this.byId('id_wb_create_button').getText(),
                ok_text: _this.getBundle().getText("create"),
                text: '',
                fromDate: oWaybill.FromDate,
                toDate: oWaybill.ToDate,
                dateEdit: true,

                check: function (block) {
                    if (!oWaybill.WithNoReqs && toDate.getTime() - fromDate.getTime() > block.toDate.getTime() - block.fromDate.getTime()) {
                        MessageToast.show(_this.getBundle().getText("errNoEnoughDays"));
                        block.afterChecked(false);
                        return;
                    }

                    // Set from dialog
                    oWaybill.Description = block.text;
                    oWaybill.FromDate = block.fromDate;
                    oWaybill.ToDate = block.toDate;
                    oWaybill.DelayReason = parseInt(block.reason);

                    // Check in DB
                    _this.checkReqs(selectedReqs, oWaybill, function (err_message) {
                        if (err_message) {
                            _this.showError(null, err_message);
                            block.afterChecked(false);
                            return;
                        }
                        block.afterChecked(true);
                    });
                },

                success: function () {
                    oWbModel.create('/Waybills', oWaybill, {
                        success: function (ret) {
                            MessageToast.show(_this.getBundle().getText("okCreateItem", [ret.Id]));

                            _this.libReqs.setWaybillId(selectedReqs, {
                                waybillId: ret.Id
                            });

                            _this.tbSchedule.removeSelections(true);
                        },

                        error: function (err) {
                            _this.showError(err, _this.getBundle().getText("errCreateWb"));
                        }
                    }); // oData create new waybill
                } // success text dialog
            }); // Text dialog callback
        }
    });
});