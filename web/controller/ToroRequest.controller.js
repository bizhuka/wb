sap.ui.define([
    'com/modekzWaybill/controller/BaseController',
    'sap/m/MessageToast',
    'sap/m/Label',
    'sap/m/MessageBox',
    'sap/ui/core/MessageType',
    'sap/ui/model/Filter',
    'sap/ui/model/FilterOperator',
    'sap/ui/core/UIComponent',
    'com/modekzWaybill/controller/LibReqs',
    'com/modekzWaybill/controller/LibChangeStatus'
], function (BaseController, MessageToast, Label, MessageBox, MessageType, Filter, FilterOperator, UIComponent, LibReqs, LibChangeStatus) {
    "use strict";

    var C_FIX_COLUMN = 2;

    var eoFilterInfo = {
        classFilter: null,
        classFilterPrev: null,
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
            var filtered = _this.getResourceArray(_this.status.STATUS_TEXTS).filter(function (pair) {
                return pair.key === _this.status.NOT_CREATED || pair.key === _this.status.REJECTED;
            });

            this.libReqs = new LibReqs(this, {
                showWbColumn: true,
                selectMode: sap.m.ListMode.MultiSelect,
                statuses: filtered,
                getFilter: function () {
                    return new Filter({
                        filters: [
                            new Filter("Waybill_Id", FilterOperator.EQ, -1), // .EQ NOT_CREATED
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
            this.getModel("ui").setProperty("/showOptColumn", reqLayout === "TwoColumnsBeginExpanded" || reqLayout === "OneColumn");
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
                    var dFrom = _this.dpFrom.getDateValue();
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
            if (eoFilterInfo.classFilterPrev === eoFilterInfo.classFilter)
                return;
            eoFilterInfo.classFilterPrev = eoFilterInfo.classFilter;

            this.filterItemsByUserWerks({
                field: "Swerk",
                and: eoFilterInfo.classFilter,

                ok: function (okFilter) {
                    eoFilterInfo.wholeFilterPrev = okFilter;
                    this.tbSchedule.getBinding("items").filter(okFilter);
                }
            });
        },

        onReqUpdate: function () {
            var _this = this;
            _this.updateDbFrom({
                link: "/r3/REQ_HEADER?_persist=true",

                title: "Заявки",

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

            // For speed
            //_this._onObjectMatched();

            _this.updateDbFrom({
                link: "/r3/SCHEDULE?_persist=true&where=" + encodeURIComponent(
                    "AFKO~GSTRP <= '" + this.toSapDate(this.dpTo.getDateValue()) + "' AND " +
                    "AFKO~GLTRP >= '" + this.toSapDate(this.dpFrom.getDateValue()) + "'"),

                title: "Журнал",

                timeout: 2500, // 2,5 seconds

                afterUpdate: function () {
                    _this._onObjectMatched();
                }
            });
        },

        eoTooltip: function (equnr, nClass, tooName, noDriverDate) {
            var result = [];
            result.push(tooName === '-' ? 'Единица оборудования:' : 'Ид подрядчика:');
            result.push(this.alphaOut(equnr));
            if (tooName !== '-')
                result.push(tooName);
            result.push('Класс:');
            result.push(nClass);

            if (this.isNoDriver(noDriverDate))
                result.push('Водитель отсутсвует на дату: ' + this.toLocaleDate(noDriverDate));

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
            this.detailHeader.setNumber(cnt + "(з.)");

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
                } else {
                    var days = "(" + (this.diffInDays(toDate, fromDate) + 1) + "д.)";

                    this.detailHeader.setNumberUnit(
                        "Дата заявки с " + this.toLocaleDate(fromDate) +
                        " по " + this.toLocaleDate(toDate) + " " + days);
                }

                var oFilters = [];

                for (var key in arrClass)
                    if (arrClass.hasOwnProperty(key)) {
                        // "0" & Ktsch === N_class
                        key = "0" + key;
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
        },

        onEquipSelected: function () {
            var createButton = this.byId('id_wb_create_button');
            var eoItem = this.tbSchedule.getSelectedItem();
            if (!eoItem || !this.getModel("userInfo").getProperty("/WbCreateNew")) {
                createButton.setVisible(false);
                return;
            }
            createButton.setVisible(true);

            // Change appearance
            eoItem = eoItem.getBindingContext("wb").getObject();
            if (eoItem.TooName !== '-') {
                createButton.setIcon(sap.ui.core.IconPool.getIconURI("sap-icon://request"));
                createButton.setText("Создать заявку подрядчика");
            } else {
                createButton.setIcon(sap.ui.core.IconPool.getIconURI("sap-icon://create-form"));
                createButton.setText("Создать путевой лист");
            }
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

            MessageToast.show("Различные классы оборудования " + message);
            return false;
        },

        checkReqs: function (selectedReqs, oWaybill, callBack) {
            var _this = this;
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
                        if (parseInt(item.Waybill_Id) === -1 || item.Status === _this.status.REJECTED)
                            continue;

                        callBack.call(_this, "Заявка " + item.Objnr + " уже обработана");
                        return;
                    }

                    // Second check
                    _this.checkSchedule(oWaybill, callBack);
                },

                error: function () {
                    callBack.call(_this, "Ошибка чтение заявок");
                }
            });
        },

        onCreateWaybill: function () {
            // get selection
            var selectedReqs = this.libReqs.reqTable.getSelectedContexts(true);
            var eoItem = this.tbSchedule.getSelectedItem();

            // Different classes
            if (!this.checkEoFilter())
                return;

            if (!selectedReqs || selectedReqs.length === 0 || !eoItem) {
                MessageBox.warning("Выделите ТС и заявки.");
                return;
            }

            // Prepare objects
            var _this = this;
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
                Status: _this.status.CREATED
            };

            var createWbDialog = new LibChangeStatus(_this);
            createWbDialog.openDialog({
                origin: 'WB',
                title: _this.byId('id_wb_create_button').getText(),
                ok_text: 'Создать',
                text: '',
                fromDate: oWaybill.FromDate,
                toDate: oWaybill.ToDate,
                dateEdit: true,

                check: function (block) {
                    if (toDate.getTime() - fromDate.getTime() > block.toDate.getTime() - block.fromDate.getTime()) {
                        MessageToast.show("Не достаточное количество дней");
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
                            MessageToast.show("Создан запись №" + ret.Id);
                            oWbModel.refresh();

                            // Update TORO header request
                            for (var i = 0; i < selectedReqs.length; i++) {
                                var item = oWbModel.getProperty(selectedReqs[i].sPath);

                                // Modify to new WAYBILL
                                var reqHeader = {
                                    Objnr: item.Objnr,
                                    Waybill_Id: ret.Id
                                };
                                oWbModel.update("/ReqHeaders('" + item.Objnr + "')", reqHeader, {
                                    error: function (err) {
                                        _this.showError(err, "Ошибка при обновлении заявки");
                                    }
                                })
                            }
                        },

                        error: function (err) {
                            _this.showError(err, "Ошибка при создании путевого листа");
                        }
                    }); // oData create new waybill
                } // success text dialog
            }); // Text dialog callback
        }
    });
});