/*global history */
sap.ui.define([
    'sap/ui/core/mvc/Controller',
    'com/modekzWaybill/controller/LibStatus',
    'sap/ui/core/routing/History',
    'sap/m/MessageBox',
    'sap/m/MessageToast',
    "sap/ui/model/Filter",
    'sap/ui/model/FilterOperator',
    'sap/m/Dialog',
    'sap/m/TextArea',
    'sap/m/Label',
    'sap/m/Button',
    'sap/ui/Device'
], function (Controller, LibStatus, History, MessageBox, MessageToast, Filter, FilterOperator, Dialog, TextArea, Label, Button, Device) {
    "use strict";

    var allowedBukrs = null;
    return Controller.extend("com.modekzWaybill.controller.BaseController", {
        status: null,

        onInit: function () {
            this.status = new LibStatus(this);
        },

        showError: function (err, message) {
            MessageBox.error(message);
            if (err)
                console.log(err);
        },

        // Create new with And
        makeAndFilter: function (mainFilter, addFilter) {
            if (addFilter)
                return new Filter({
                    filters: [mainFilter, addFilter],
                    and: true
                });
            return mainFilter;
        },

        filterItemsByUserWerks: function (params) {
            var allowedWerks = this.getModel("userInfo").getProperty("/werks");

            var filters = [];
            for (var j = 0; j < allowedWerks.length; j++)
                filters.push(new Filter(params.field, FilterOperator.EQ, allowedWerks[j]));
            var werksFilter = new Filter({filters: filters, and: false});

            // Return filter
            params.ok.call(this, this.makeAndFilter(werksFilter, params.and));
        },

        filterItemsByUserBukrs: function (params) {
            var _this = this;

            // Cached bukrs
            if (allowedBukrs) {
                _this.doFiltByBukrs(params);
                return;
            }

            // Slow read
            this.filterItemsByUserWerks({
                field: "Werks",

                ok: function (okFilter) {
                    _this.getModel("wb").read("/Werks", {
                        filters: [okFilter],

                        success: function (odata) {
                            allowedBukrs = odata.results;
                            _this.doFiltByBukrs(params);
                        }
                    });
                }
            });
        },

        doFiltByBukrs: function (params) {
            var filters = [];
            for (var j = 0; j < allowedBukrs.length; j++)
                filters.push(new Filter(params.field, FilterOperator.EQ, allowedBukrs[j].Bukrs));
            var bukrsFilter = new Filter({filters: filters, and: false});

            // Return filter
            params.ok.call(this, this.makeAndFilter(bukrsFilter, params.and));
        },

        getRouter: function () {
            return this.getOwnerComponent().getRouter();
        },

        getModel: function (sName) {
            return this.getView().getModel(sName);
        },

        setModel: function (oModel, sName) {
            return this.getView().setModel(oModel, sName);
        },

        getBundle: function () {
            return this.getOwnerComponent().getModel("i18n").getResourceBundle();
        },

        getResourceArray: function (name) {
            // Already prepared
            if (this[name])
                return this[name];

            // New array
            this[name] = [];
            var arr = this.getBundle().getText(name).split(";");

            for (var i = 0; i < arr.length; i++) {
                var pair = arr[i].split("-");
                this[name].push({
                    key: parseInt(pair[0]),
                    text: pair[1]
                })
            }

            return this[name];
        },

        getResText: function (name, id) {
            if (id === undefined)
                return "-Error-";

            var arr = this.getResourceArray(name);
            for (var i = 0; i < arr.length; i++) {
                var item = arr[i];
                if (item.key === id)
                    return item.text;
            }

            // No mapping
            return "-E-" + id + "-E-";
        },

        // Just use numeric index
        getStatusText: function (statusInd, waybillId) {
            if (waybillId === undefined)
                return "-Error-";

            // Do not show with empty waybill
            if (parseInt(waybillId) === -1)
                return ""; // "Not created";

            return this.getResText(this.status.STATUS_TEXTS, statusInd);
        },

        getDelayReasonText: function (id) {
            return this.getResText(this.status.DELAY_TEXTS, id);
        },

        onNavBack: function () {
            var sPreviousHash = History.getInstance().getPreviousHash();

            if (typeof sPreviousHash !== "undefined") {
                // The history contains a previous entry
                history.go(-1);
            } else {
                // Otherwise we go backwards with a forward history
                var bReplace = true;
                this.getRouter().navTo("main", {}, bReplace);
            }
        },

        findById: function (id) {
            return sap.ui.getCore().byId(id);
        },

        toSapDate: function (date) {
            return date ? date.getFullYear() +
                ('0' + (date.getMonth() + 1)).slice(-2) +
                ('0' + date.getDate()).slice(-2) : "";
        },

        toSapDateTime: function (date) {
            return date ? this.toSapDate(date) + ('0' + date.getHours()).slice(-2) +
                ('0' + (date.getMinutes())).slice(-2) +
                ('0' + date.getSeconds()).slice(-2) : "";
        },

        toLocaleDate: function (date) {
            return date ? date.toLocaleDateString("ru-RU", {
                year: 'numeric',
                month: '2-digit',
                day: '2-digit'
            }) : "";
        },

        toLocaleDateTime: function (date) {
            return date ? date.toLocaleDateString("ru-RU", {
                year: 'numeric',
                month: '2-digit',
                day: '2-digit',
                hour: '2-digit',
                minute: '2-digit',
                second: '2-digit'
            }) : "";
        },

        alphaOut: function (inStr) {
            return inStr ? inStr.replace(/^0+/, '') : inStr; //"";
        },

        alphaIn: function (num, length) {
            var pad = new Array(1 + length).join('0');
            return (pad + num).slice(-pad.length);
        },

        formatTime: function (time) {
            if (!time || (typeof time !== "string") || time.length !== 6)
                return;
            return time.substring(0, 2) + ":" + time.substring(2, 4) + ":" + time.substring(4, 6);
        },

        createFragment: function (sDialog, controller) {
            var fragment = sap.ui.xmlfragment(sDialog, controller ? controller : this);
            fragment.addStyleClass(this.getContentDensityClass());
            this.getView().addDependent(fragment);
            return fragment;
        },

        // This method can be called to determine whether the sapUiSizeCompact or sapUiSizeCozy design mode class should be set, which influences the size appearance of some controls.
        getContentDensityClass: function () {
            if (this._sContentDensityClass === undefined) {
                // check whether FLP has already set the content density class; do nothing in this case
                if (jQuery(document.body).hasClass("sapUiSizeCozy") || jQuery(document.body).hasClass("sapUiSizeCompact")) {
                    this._sContentDensityClass = "";
                } else if (!Device.support.touch) { // apply "compact" mode if touch is not supported
                    this._sContentDensityClass = "sapUiSizeCompact";
                } else {
                    // "cozy" in case of touch support; default for most sap.m controls, but needed for desktop-first controls like sap.ui.table.Table
                    this._sContentDensityClass = "sapUiSizeCozy";
                }
            }
            return this._sContentDensityClass;
        },

        onWaybillPress: function (oEvt) {
            if (!this.getModel("userInfo").getProperty("/WbShowOne")) {
                MessageToast.show(this.getBundle().getText("noOneWbRights"));
                return;
            }
            var id = isNaN(oEvt) ? oEvt.getSource().getBindingContext("wb").getObject().Waybill_Id : oEvt;
            this.getRouter().navTo("waybillDetail", {waybillId: id});
        },

        updateDbFrom: function (params) {
            var _this = this;
            $.ajax({
                dataType: "json",
                url: params.link,
                timeout: params.timeout ? params.timeout : 7000, // 7 seconds by default
                success: function (result) {
                    _this.showUpdateInfo(result, params);
                },
                error: function () {
                    MessageToast.show(_this.getBundle().getText("errDict", [params.title]));

                    if (params.afterUpdate)
                        params.afterUpdate.call(_this, false);
                }
            });
        },

        showUpdateInfo(json, params) {
            var bundle = this.getBundle();
            MessageToast.show(
                bundle.getText("okDict", [params.title, json.updated, json.inserted]) +
                (json.dbcnt ? bundle.getText("okDictR3", [json.dbcnt]) : "") +
                (json.deleted ? bundle.getText("okDictDel", [json.dbcnt]) : ""));

            if (params.afterUpdate)
                params.afterUpdate.call(this, true);
        },

        addDays: function (date, cnt) {
            return new Date(date.getTime() + cnt * 3600 * 24 * 1000)
        },

        diffInDays: function (dTo, dFrom) {
            return parseInt((dTo.getTime() - dFrom.getTime()) / (3600 * 24 * 1000));
        },

        checkSchedule: function (oWaybill, callBack) {
            var _this = this;
            var oWbModel = _this.getModel("wb");

            oWbModel.read("/Schedules", {
                // Bug in odata filter + 1 day
                filters: [
                    new Filter("Werks", FilterOperator.EQ, oWaybill.Werks),
                    new Filter("Datum", FilterOperator.BT,
                        _this.addDays(oWaybill.FromDate, -1),
                        _this.addDays(oWaybill.ToDate, 1)),
                    new Filter("Equnr", FilterOperator.EQ, oWaybill.Equnr)
                ],

                success: function (oData) {
                    var items = oData.results;
                    for (var i = 0; i < items.length; i++) {
                        var item = items[i];

                        // added one day previously
                        if (item.Datum.getTime() > oWaybill.ToDate.getTime() ||
                            item.Datum.getTime() < oWaybill.FromDate.getTime())
                            continue;

                        item.Waybill_Id = parseInt(item.Waybill_Id);

                        // Planned work
                        if (item.Waybill_Id === -1 && item.Ilart) {
                            callBack.call(_this, _this.getBundle().getText("occupiedByRepair",
                                [_this.toLocaleDate(item.Datum), _this.alphaOut(item.Equnr), item.Ilart]));
                            return;
                        }

                        if (item.Waybill_Id !== parseInt(oWaybill.Id)) {
                            callBack.call(_this, _this.getBundle().getText("occupiedByWb",
                                [_this.toLocaleDate(item.Datum), _this.alphaOut(item.Equnr), item.Waybill_Id]));
                            return;
                        }
                    }

                    callBack.call(_this, null);
                },

                error: function () {
                    callBack.call(_this, _this.getBundle().getText("errReadDict"));
                }
            });
        },

        eoUpdate: function (callBack) {
            var _this = this;
            _this.updateDbFrom({
                link: "/r3/EQUIPMENT?_persist=true",

                title: _this.getBundle().getText("eoList"),

                afterUpdate: function () {
                    callBack.call(_this);
                }
            });
        },

        // Use get :)
        navToPost: function (navParams) {
            var url = navParams.url;
            delete navParams.url;

            for (var key in navParams)
                if (navParams.hasOwnProperty(key))
                    url += ("&" + key + "=" + encodeURIComponent(navParams[key]));
            window.location = url;
        },

        getEoTextFilter: function (text) {
            return new Filter({
                filters: [
                    new Filter("Equnr", FilterOperator.Contains, text),
                    new Filter("Eqktx", FilterOperator.Contains, text),
                    new Filter("TooName", FilterOperator.Contains, text),
                    new Filter("License_num", FilterOperator.Contains, text),
                    new Filter("N_class", FilterOperator.Contains, text),
                    new Filter("Eqart", FilterOperator.Contains, text),
                    new Filter("Typbz", FilterOperator.Contains, text),
                    new Filter("Imei", FilterOperator.Contains, text),
                    new Filter("Pltxt", FilterOperator.Contains, text)
                ],
                and: false
            })
        }
    });

});