sap.ui.define([
    'com/modekzWaybill/controller/BaseController',
    'sap/ui/core/UIComponent',
    'sap/m/MessageBox',
    'sap/m/MessageToast',
    'sap/ui/model/json/JSONModel',
    'sap/ui/model/Filter',
    'sap/ui/model/FilterOperator',
    'com/modekzWaybill/controller/LibDriver'
], function (BaseController, UIComponent, MessageBox, MessageToast, JSONModel, Filter, FilterOperator, LibDriver) {
    "use strict";

    return BaseController.extend("com.modekzWaybill.controller.Start", {
        libDriver: null,

        onInit: function () {
            // call base init
            BaseController.prototype.onInit.apply(this, arguments);

            this.libDriver = new LibDriver(this);

            UIComponent.getRouterFor(this).getRoute("main").attachPatternMatched(this._onObjectMatched, this);
        },

        _onObjectMatched: function () {
            this.getModel("appView").setProperty("/appWidthLimited", true);
        },

        showToroRequest: function () {
            this.doNavTo("toroRequest");
        },


        showFinishedReqs: function () {
            this.doNavTo("finishedReqs");
        },

        showWayill: function () {
            this.doNavTo("waybill");
        },

        importR3Tables: function () {
            this.getRouter().navTo("importR3");
        },

        doNavTo: function (path) {
            this.getModel("appView").setProperty("/appWidthLimited", false);
            this.getRouter().navTo(path);
        },

        setDriverValidDate: function () {
            var _this = this;
            this.libDriver.driverOpenDialog({
                bindPath: "wb>/Drivers",

                text: "",

                confirmMethod: function (oEvent) {
                    var context = oEvent.getParameter("listItem").getBindingContext("wb");
                    var oDriver = context.getObject();

                    _this.getModel("wb").update(context.getPath(), {
                        ValidDate: new Date(1)
                    }, {
                        success: function () {
                            MessageToast.show(_this.getBundle().getText("okDriverUpdate", [oDriver.Fio, oDriver.Bukrs]));
                        },
                        error: function (err) {
                            _this.showError(err, _this.getBundle().getText("errUpdate"));
                        }
                    });
                }
            });
        },

        showPdfTemplates: function () {
            if (!this._pdfDiaolog) {
                this._pdfDiaolog = this.createFragment("com.modekzWaybill.view.frag.SelectPdfDialog");
                this._pdfDiaolog.setModel(new JSONModel('/json/pdfBlanks.json'));
            }
            this._pdfDiaolog.open();
        },

        pdfSearch: function (oEvent) {
            var sValue = oEvent.getParameter("value");
            var oBinding = oEvent.getSource().getBinding("items");
            oBinding.filter(
                new Filter({
                    filters: [
                        new Filter("Title", FilterOperator.Contains, sValue),
                        new Filter("Desc", FilterOperator.Contains, sValue),
                        new Filter("Info", FilterOperator.Contains, sValue)],
                    and: false
                })
            );
        },

        pdfClose: function (oEvent) {
            var aContexts = oEvent.getParameter("selectedContexts");
            if (!aContexts || !aContexts.length)
                return;

            this.navToPost({
                url: "/printDoc/template?",
                objid: aContexts[0].getObject().Id,
                contentType: "application/pdf"
            })
        },

        openAnalytics: function () {
            window.open('https://erp-service.eu1.sapanalytics.cloud/sap/fpa/ui/tenants/009/app.html#;view_id=story;storyId=78A5EADAC36110C14FED3BA56FF91751', '_blank');
        },

        showUserInfo: function () {
            window.open('https://erp-service.accounts.ondemand.com/ui/protected/profilemanagement');
        },

        showDocumentation: function () {
            MessageToast.show(this.getBundle().getText("manualNotPrepared"));
        },

        eoValidation: function () {
            if (!this.eoDialog)
                this.eoDialog = this.createFragment("com.modekzWaybill.view.frag.EoDialog");

            this.eoDialog.open();
        },

        onEoUpdate: function () {
            this.eoUpdate(function () {
                this.findById('id_eo_table').getBinding("items").refresh();
            });
        },

        onEoCloseDialog: function () {
            this.eoDialog.close();
        },

        onEoAfterClose: function () {
            this.eoDialog.destroy();
            this.eoDialog = null;
        },

        eoSearch: function (oEvent) {
            var _this = this;

            var text = oEvent.getParameter("query");
            _this.filterEo(text, function (okFilter) {
                _this.findById('id_eo_table').getBinding("items").filter(okFilter);
            });
        },

        filterEo: function (text, callback) {
            var _this = this;

            var textFilter = null;
            if (text)
                textFilter = _this.getEoTextFilter(text);

            // Read user permissions
            _this.filterItemsByUserWerks({
                field: "Swerk",

                and: textFilter,

                ok: function (okFilter) {
                    callback.call(this, _this.makeAndFilter(okFilter, new Filter("TooName", FilterOperator.EQ, '-')));
                }
            });
        },

        setNoDriverDate: function () {
            var items = this.findById('id_eo_table').getSelectedItems();
            if (items.length === 0) {
                MessageToast.show(this.getBundle().getText("selectRows"));
                return;
            }

            var oWbModel = this.getModel("wb");
            var editObj = {
                NoDriverDate: new Date(1)
            };

            // Update 1 by one
            for (var i = 0; i < items.length; i++) {
                var item = items[i].getBindingContext("wb").getObject();
                oWbModel.update("/Equipments('" + item.Equnr + "')", editObj);
            }
            oWbModel.refresh();
        }
    });
});