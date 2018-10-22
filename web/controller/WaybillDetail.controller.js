sap.ui.define([
    'com/modekzWaybill/controller/BaseController',
    'sap/ui/model/json/JSONModel',
    'sap/ui/core/UIComponent',
    'sap/ui/model/Filter',
    'sap/ui/model/FilterOperator',
    'sap/m/MessageBox',
    'sap/m/MessageToast',
    'com/modekzWaybill/controller/LibDriver',
    'com/modekzWaybill/controller/LibReqs',
    'com/modekzWaybill/controller/LibMessage',
    'com/modekzWaybill/controller/LibChangeStatus',
    'com/modekzWaybill/controller/LibLgort'
], function (BaseController, JSONModel, UIComponent, Filter, FilterOperator, MessageBox, MessageToast, LibDriver, LibReqs, LibMessage, LibChangeStatus, LibLgort) {
    "use strict";

    var waybillId, bindingObject;

    var allTabs, driverInput;

    return BaseController.extend("com.modekzWaybill.controller.WaybillDetail", {
        libDriver: null,
        libReqs: null,
        libMessage: null,
        reqsFilter: null,

        onInit: function () {
            // call base init
            var _this = this;
            BaseController.prototype.onInit.apply(_this, arguments);

            this.libDriver = new LibDriver(this);
            this.libMessage = new LibMessage(this);

            this.libReqs = new LibReqs(this, {
                showActual: true,

                getFilter: function () {
                    return _this.reqsFilter;
                }
            });

            allTabs = this.byId("id_all_tabs");
            driverInput = this.byId("id_driver_input");

            var oRouter = UIComponent.getRouterFor(this);
            oRouter.getRoute("waybillDetail").attachPatternMatched(this._onObjectMatched, this);
        },

        getBindingPath(forUpdate) {
            if (forUpdate)
                return "/Waybills(" + waybillId + "l)";
            return "/VWaybills(" + waybillId + "l)";
        },

        getGasSpentPath(pos) {
            return "/GasSpents(Waybill_Id=" + waybillId + "L,Pos=" + pos + ")";
        },

        _onObjectMatched: function (oEvent) {
            var _this = this;
            var oWbModel = _this.getModel("wb");
            waybillId = oEvent.getParameter("arguments").waybillId;

            // Default tab
            allTabs.setSelectedKey("id_eo_tab");

            // Current waybill
            var path = "wb>" + this.getBindingPath();
            this.getView().bindElement(path);
            _this.byId("id_eo_tab").bindElement(path);
            _this.byId("id_dr_tab").bindElement(path);

            // Set reqs table busy
            _this.reqsFilter = null;
            _this.libReqs.doFilter(true);

            // Fill default tab
            _this.readBindingObject(function () {
                _this.byId("id_eo_tab").bindElement({
                    path: "/Equipments('" + bindingObject.Equnr + "')",
                    model: "wb"
                });

                // update driver tab
                _this.updateDriverTab();

                // Use 1 Bukrs
                _this.libDriver.filterDrivers("", bindingObject.Bukrs, function (okFilter) {
                    driverInput.getBinding("suggestionItems").filter(okFilter)
                });

                // Hide or show tabs
                var visible = bindingObject.TooName === '-';
                this.byId('id_dr_tab').setVisible(visible);
                this.byId('id_close_tab').setVisible(visible);
                this.byId('id_date_tab').setVisible(visible);
            });

            // set fuel
            var fuel = {
                data: []
            };
            for (var i = 0; i < 10; i++)
                fuel.data.push({
                    GasMatnr: "",
                    GasBefore: 0,
                    GasGive: 0,
                    GasGiven: 0,
                    GasSpent: 0,
                    GasAfter: 0,
                    GasLgort: ""
                });
            var fuelModel = new JSONModel(fuel);
            _this.setModel(fuelModel, "fuel");

            oWbModel.read("/GasSpents", {
                filters: [
                    new Filter("Waybill_Id", FilterOperator.EQ, parseInt(waybillId)),
                    new Filter("Pos", FilterOperator.BT, 0, fuel.data.length - 1)
                ],

                success: function (spents) {
                    spents = spents.results;
                    for (i = 0; i < spents.length; i++) {
                        var spent = spents[i];
                        fuel.data[spent.Pos] = spent;
                    }
                    fuelModel.setProperty("/data", fuel.data);

                    // Recalc fields
                    _this.onFuelChanged({
                        skipSave: true,
                        skipMessage: true
                    });
                }
            });
        },

        readBindingObject: function (callback) {
            var _this = this;

            _this.getModel("wb").read(_this.getBindingPath(), {
                success: function (waybill) {
                    bindingObject = waybill;

                    // Prepare reqs tab
                    var reqsTab = _this.byId("id_reqs_container");
                    var reqsOk = bindingObject.Status !== _this.status.REJECTED;
                    var reqsText = _this.getBundle().getText(reqsOk ? "reqs" : "cancelledReqs");

                    // Change reqs UI
                    _this.getModel("ui").setProperty("/showWbColumn", !reqsOk);
                    reqsTab.setIcon(sap.ui.core.IconPool.getIconURI(reqsOk ? "multiselect-all" : "multiselect-none"));
                    reqsTab.setTooltip(reqsText);
                    _this.byId("id_reqs_title").setText(reqsText);

                    var filter = new Filter("Waybill_Id", FilterOperator.EQ, parseInt(waybillId));
                    if (reqsOk) {
                        _this.reqsFilter = filter;
                        _this.libReqs.doFilter(true);
                    } else // From history table
                        _this.getModel("wb").read("/ReqHistorys", {
                            filters: [filter],

                            success: function (data) {
                                var arr = data.results;
                                var objnrFilter = [];
                                // Add new history filter
                                for (var i = 0; i < arr.length; i++)
                                    objnrFilter.push(new Filter("Objnr", FilterOperator.EQ, arr[i].Objnr));

                                // Pass old objnr-s
                                _this.reqsFilter = new Filter({
                                    filters: objnrFilter,
                                    and: false
                                });

                                _this.libReqs.doFilter(true);
                            }
                        });

                    // Can change driver
                    driverInput.setEnabled(bindingObject.Status === _this.status.CREATED); // AGREED

                    // And if ok
                    if (callback)
                        callback.call(_this, waybill);
                }
            });
        },

        onWlnMessagePress: function (oEvent) {
            if (!bindingObject.GarageDepDate || !bindingObject.GarageArrDate) {
                MessageToast.show(this.getBundle().getText("errNotInGarage"));
                return;
            }

            // Is Not Integer
            if (isNaN(parseFloat(bindingObject.WialonId)) || !isFinite(bindingObject.WialonId)) {
                MessageToast.show(_this.getBundle().getText("errMesPointValue"));
                return;
            }

            // Disable for second press
            var button = oEvent.getSource();
            button.setEnabled(false);

            // Which button was pressed
            var id = button.getId().split("-");
            id = id[id.length - 1];
            var _this = this;

            var objExt = {
                id: id,
                wialonId: bindingObject.WialonId,

                // Date in linux format
                fromDate: parseInt(bindingObject.GarageDepDate.getTime() / 1000),
                toDate: parseInt(bindingObject.GarageArrDate.getTime() / 1000),

                wlnOk: function () {
                    MessageToast.show(_this.getBundle().getText("okWialon"));
                    button.setEnabled(true);
                },

                wlnError: function () {
                    MessageToast.show(_this.getBundle().getText("errWialon"));
                    button.setEnabled(true);
                }
            };

            switch (objExt.id) {
                case "wln_load_spent":
                    objExt.className = "LibWlnLoadSpent";

                    objExt.wlnCallback = function (json) {
                        var oWbModel = _this.getModel("wb");
                        var path = _this.getBindingPath();
                        bindingObject = oWbModel.getProperty(path);

                        bindingObject.OdoDiff = (json.OdoDiff / 1000).toFixed(2);
                        bindingObject.MotoHour = (json.MotoHour / 3600).toFixed(2);
                        bindingObject.GasSpent = json.GasSpent.toFixed(2);

                        // Not working!!!
                        oWbModel.setProperty(path, bindingObject);

                        // TODO fix with model.setProperty
                        _this.byId("id_wb_odo_diff").setValue(bindingObject.OdoDiff);
                        _this.byId("id_wb_moto_hour").setValue(bindingObject.MotoHour);
                        _this.byId("id_wb_gas_spent").setValue(bindingObject.GasSpent);

                        _this.onFuelChanged({
                            skipSave: true
                        });
                    };
                    break;

                case "wln_show_fuel":
                case "wln_show_map":
                    objExt.className = "LibWlnMessage";
                    break;
            }

            // Use different libraries
            sap.ui.require(["com/modekzWaybill/controller/" + objExt.className], function (WialonLib) {
                new WialonLib(_this, objExt);
            });
        },

        updateDriverTab: function () {
            if (bindingObject.Driver)
                this.byId("id_dr_tab").bindElement({
                    path: "/Drivers(Bukrs='" + bindingObject.Bukrs + "',Pernr='" + bindingObject.Driver + "')",
                    model: "wb"
                });
        }
        ,

        on_tab_select: function (oEvent) {
            var key = oEvent.getParameter("selectedKey").split("-");
            var fm = key[key.length - 1] + "_click";

            // Call if exist
            if (this[fm])
                this[fm](oEvent);
        }
        ,

        handle_dr_f4Selected: function (oEvent) {
            var text = false;

            var oItem = oEvent.getParameter("selectedItem");
            if (oItem) {
                text = oItem.getText();
                text = text.split("-")[0].trim();
            } else {
                oItem = oEvent.getParameter("listItem");
                if (oItem)
                    text = oItem.getBindingContext("wb").getObject().Pernr;
            }
            if (!text)
                return;
            oEvent.cancelBubble();

            var oWbModel = this.getModel("wb");
            var _this = this;
            oWbModel.read("/VDrivers(Bukrs='" + bindingObject.Bukrs + "',Pernr='" + text + "')", {
                success: function (curDriver) {
                    // Set new DRIVER
                    var obj = {
                        Id: waybillId,
                        Driver: curDriver.Pernr
                    };

                    // Change in UI Or in 1 level deeper ?
                    bindingObject.Driver = curDriver.Pernr;
                    _this.updateDriverTab();

                    oWbModel.update(_this.getBindingPath(true), obj, {
                        success: function () {
                            oWbModel.refresh();
                        },

                        error: function () {
                            _this.showError(null, _this.getBundle().getText("updateDriverError"));
                        }
                    });
                }
            });
        }
        ,

        handle_dr_f4: function () {
            var _this = this;
            this.libDriver.driverOpenDialog({
                bindPath: "wb>/VDrivers",

                text: driverInput.getValue(),

                bindBukrs: bindingObject.Bukrs,

                confirmMethod: function (oEvent) {
                    _this.handle_dr_f4Selected(oEvent)
                }
            });
        }
        ,

        handle_lgort_f4: function (oEvent) {
            var _this = this;
            var libLgort = new LibLgort(_this);
            var input = oEvent.getSource();
            var context = input.getBindingContext("fuel");

            libLgort.lgortOpenDialog({
                lgort: input.getValue(),
                werks: bindingObject.Werks,

                confirmLgort: function (evt) {
                    // input.setValue();
                    var obj = context.getObject();
                    obj.GasLgort = evt.getParameter("listItem").getBindingContext("wb").getObject().Lgort;
                    _this.getModel("fuel").setProperty(context.getPath(), obj);

                    // Save to DB
                    _this.onFuelChanged({
                        skipMessage: true
                    });
                }
            });
        }
        ,

        on_set_status: function (oEvt) {
            var button = oEvt.getSource();
            var id = button.getId().split("-");
            id = id[id.length - 1];

            var obj = {
                Id: bindingObject.Id
            };
            var _this = this;
            var oWbModel = _this.getModel("wb");

            switch (id) {
                case "id_bt_dep_date": // id_bt_confirm
                    // case "id_bt_dep_date":
                    if (!bindingObject.Driver) {
                        this.showError(null, this.getBundle().getText("noDriver"));
                        allTabs.setSelectedKey("id_dr_tab");
                        return;
                    }
                    if (parseInt(bindingObject.Gas_Cnt) === 0) {
                        this.showError(null, this.getBundle().getText("noGas"));
                        allTabs.setSelectedKey("id_close_tab");
                        return;
                    }

                    if (parseInt(bindingObject.Req_Cnt) === 0) {
                        this.showError(null, this.getBundle().getText("noReqs"));
                        return;
                    }

                    var changeStat = new LibChangeStatus(_this);
                    changeStat.openDialog({
                        origin: 'WB',
                        title: _this.getBundle().getText("outGarage"), // Confirm WB
                        ok_text: _this.getBundle().getText("out"), // Confirm
                        text: bindingObject.Description,
                        reason: bindingObject.DelayReason,
                        fromDate: bindingObject.FromDate,
                        toDate: bindingObject.ToDate,
                        dateEdit: false,

                        check: function (block) {
                            // Set from dialog
                            obj.Description = block.text;

                            // Check in DB
                            _this.checkSchedule(bindingObject, function (err_message) {
                                if (err_message) {
                                    _this.showError(null, err_message);
                                    block.afterChecked(false);
                                    oWbModel.refresh();
                                    return;
                                }
                                block.afterChecked(true);
                            });
                        },

                        success: function () {
                            // obj.ConfirmDate = new Date(1);
                            // obj.Status = _this.status.AGREED;
                            obj.GarageDepDate = new Date(1);
                            obj.Status = _this.status.IN_PROCESS;
                            //break;

                            _this.setNewStatus(obj);
                        }
                    });
                    return;

                case "id_bt_cancel":
                    obj.ConfirmDate = new Date(1);
                    obj.Status = _this.status.REJECTED;

                    changeStat = new LibChangeStatus(_this);
                    changeStat.openDialog({
                        origin: 'WB',
                        title: _this.getBundle().getText("cancelWb"),
                        ok_text: _this.getBundle().getText("canceling"),
                        text: bindingObject.Description,
                        reason: bindingObject.DelayReason,
                        fromDate: bindingObject.FromDate,
                        toDate: bindingObject.ToDate,
                        dateEdit: false,

                        check: function (block) {
                            obj.Description = block.text;
                            block.afterChecked(true);
                        },

                        success: function () {
                            _this.setNewStatus(obj);
                        }
                    });
                    return;

                case "id_bt_arr_date":
                    obj.GarageArrDate = new Date(1);
                    obj.Status = _this.status.ARRIVED;
                    break;

                case "id_bt_close":
                    var activeTab = allTabs.getSelectedKey();
                    allTabs.setSelectedKey("id_close_tab");
                    if (activeTab !== "id_close_tab") {
                        MessageToast.show(this.getBundle().getText("errCheckSensors"));
                        return;
                    }

                    var bindObj = oWbModel.getProperty(_this.getBindingPath());
                    var odoEmpty = (!parseFloat(bindObj.OdoDiff) && !parseFloat(bindObj.MotoHour));
                    var fuelEmpty = !parseFloat(bindObj.GasSpent);
                    if (odoEmpty || fuelEmpty) {
                        this.showError(null, this.getBundle().getText("errEnterSensors"));
                        return;
                    }

                    // Reqs not closed yet
                    if (!this.checkReqsStatus())
                        return;

                    var fuelRows = _this.onFuelChanged({
                        skipSave: true
                    });
                    // Fuel not ok
                    if (fuelRows === false)
                        return;

                    // Same check as in SAP
                    if (bindObj.Mptyp !== "O" && bindObj.Mptyp !== "S") {
                        MessageToast.show(this.getBundle().getText("errMptyp", [bindObj.Mptyp, bindObj.Point]));
                        return;
                    }

                    var spents = [];
                    for (var i = 0; i < fuelRows.length; i++)
                        if (fuelRows[i].GasMatnr)
                            spents.push({
                                matnr: fuelRows[i].GasMatnr,
                                menge: fuelRows[i].GasSpent,
                                lgort: fuelRows[i].GasLgort
                            });

                    button.setEnabled(false);
                    $.ajax({
                        url: '/././measureDoc?doc=' + encodeURIComponent(JSON.stringify({
                            disMode: "N", // As background task
                            point: bindObj.Point,
                            equnr: bindObj.Equnr,
                            werks: bindObj.Werks,
                            gstrp: _this.toSapDateTime(bindObj.GarageDepDate),
                            gltrp: _this.toSapDateTime(bindObj.GarageArrDate),
                            text: _this.getBundle().getText("wbNum", [bindObj.Id]),
                            odoDiff: bindObj.OdoDiff,
                            motoHour: bindObj.MotoHour,
                            spents: spents
                        })),
                        //type: 'POST', data: ,
                        contentType: 'application/json; charset=utf-8',
                        dataType: 'json',
                        success: function (doc) {
                            button.setEnabled(true);

                            // Check for errors
                            if (!_this.libMessage.messageOpenDialog(doc.messages))
                                return;

                            // Set new date
                            obj.CloseDate = new Date(1);
                            obj.Status = _this.status.CLOSED;
                            // From sensors
                            obj.OdoDiff = bindObj.OdoDiff;
                            obj.MotoHour = bindObj.MotoHour;
                            obj.GasSpent = bindObj.GasSpent;
                            _this.setNewStatus(obj);
                        },

                        error: function (err) {
                            button.setEnabled(true);
                            _this.showError(err, _this.getBundle().getText("errCloseWb"));
                        }
                    });
                    return;
            }

            _this.setNewStatus(obj);
        }
        ,

        setNewStatus: function (obj) {
            var _this = this;
            var oWbModel = _this.getModel("wb");
            oWbModel.update(_this.getBindingPath(true), obj, {
                success: function () {
                    _this.readBindingObject();
                    oWbModel.refresh();
                },
                error: function () {
                    _this.showError(null, _this.getBundle().getText("errWbUpdate"));
                }
            });
        }
        ,

        on_wb_print: function () {
            this.navToPost({
                url: "/././printDoc/templateWithData?",
                waybillId: bindingObject.Id
            });
        }
        ,

        checkReqsStatus: function () {
            var content = this.byId('id_reqs_container').getContent();
            var table = content[content.length - 1];
            var items = table.getItems();

            for (var i = 0; i < items.length; i++) {
                var item = items[i].getBindingContext("wb").getObject();
                if (item.StatusReason === 0) {
                    MessageToast.show(this.getBundle().getText("reqsNotConfirmed", [i + 1]));
                    return false;
                }
            }

            return true;
        }
        ,

        onFuelChanged: function (oEvent) {
            var _this = this;
            var oWbModel = _this.getModel("wb");
            var bindObj = oWbModel.getProperty(this.getBindingPath());

            // From wialon
            var gasTotalSpent = parseFloat(bindObj.GasSpent);

            // No need
            var data = this.getModel("fuel").getProperty("/data");
            if (data.length === 0)
                return false;

            for (var i = 0; i < data.length; i++) {
                var row = data[i];
                var totalBefore = parseFloat(row.GasBefore) + parseFloat(row.GasGiven);

                gasTotalSpent -= totalBefore;
                if (gasTotalSpent > 0) {
                    data[i].GasSpent = String(totalBefore);
                    data[i].GasAfter = String(0);
                } else {
                    data[i].GasSpent = String(totalBefore + gasTotalSpent);
                    data[i].GasAfter = String(-gasTotalSpent);
                    gasTotalSpent = 0;
                }

                // Modify items in DB
                if (!row.GasMatnr || oEvent.skipSave)
                    continue;

                // Check Lgort
                if (!data[i].GasLgort && !oEvent.skipMessage) {
                    MessageToast.show(_this.getBundle.getText("noLgort", [i + 1]));
                    return false;
                }

                // Only this fields
                var updFields = {
                    GasMatnr: row.GasMatnr,
                    GasBefore: row.GasBefore,
                    GasGive: row.GasGive,
                    GasGiven: row.GasGiven,
                    GasLgort: row.GasLgort
                };

                oWbModel.update(_this.getGasSpentPath(i), updFields, {
                    error: function (err) {
                        _this.showError(err, _this.getBundle().getText("errGasUpdate"));
                    }
                });
            }
            if (gasTotalSpent > 0 && !oEvent.skipMessage) {
                MessageToast.show(this.getBundle().getText("moreSpent", [gasTotalSpent]));
                return false;
            }
            this.getModel("fuel").setProperty("/data", data);
            return data;
        }
        ,

        on_save_dates: function () {
            var _this = this;
            var oWbModel = _this.getModel("wb");
            // From view
            bindingObject = oWbModel.getProperty(_this.getBindingPath());

            oWbModel.update(_this.getBindingPath(true), {
                CreateDate: bindingObject.CreateDate,
                ConfirmDate: bindingObject.ConfirmDate,
                GarageDepDate: bindingObject.GarageDepDate,
                GarageArrDate: bindingObject.GarageArrDate,
                CloseDate: bindingObject.CloseDate
            }, {
                success: function () {
                    MessageToast.show(_this.getBundle().getText("dateTimeUpdated"));
                }
            });
        }
        ,

        onFuelTypeChange: function (oEvent) {
            var comboGasType = oEvent.getSource();

            // Which pos
            var id = comboGasType.getId().split("-");
            id = id[id.length - 1];

            try {
                var gasMatnr = comboGasType.getSelectedKey();
                if (gasMatnr === "")
                    this.getModel("wb").remove(this.getGasSpentPath(id));
                else
                    this.getModel("wb").create("/GasSpents", {
                        Waybill_Id: waybillId + "", // As string!
                        Pos: parseInt(id),
                        GasMatnr: gasMatnr
                    });
            } catch (e) {
                console.log(e)
            }

            this.onFuelChanged({
                skipSave: true
            });

            // Read from DB
            this.readBindingObject();
        }
    });
})
;