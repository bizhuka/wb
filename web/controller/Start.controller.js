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


        showFinishedReqs:function(){
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
                            MessageToast.show(oDriver.Fio + " (" + oDriver.Bukrs + ")\nмедосмотр пройден")
                        },
                        error: function (err) {
                            _this.showError(err, "Ошибка при обновлении 'Годен на дату'");
                        }
                    });
                }
            });
        },

        showPdfTemplates: function () {
            if (!this._pdfDiaolog) {
                this._pdfDiaolog = this.createFragment("com.modekzWaybill.view.frag.SelectPdfDialog");
                this._pdfDiaolog.setModel(new JSONModel({
                    pdf: [
                        {
                            Id: "ZWB_CAR_KZ.PDF",
                            Title: "Жеңіл көліктің жол парағы",
                            Info: "kz"
                        },
                        {
                            Id: "ZWB_CAR_RU.PDF",
                            Title: "Путевой лист легкового автомобиля",
                            Info: "ru"
                        },
                        {
                            Id: "ZWB_BUS_KZ.PDF",
                            Title: "Автобустың жол парағы",
                            Info: "kz"
                        },
                        {
                            Id: "ZWB_BUS_RU.PDF",
                            Title: "Путевой лист автобуса",
                            Info: "ru"
                        },
                        {
                            Id: "ZWB_TRUCK_RU.PDF",
                            Title: "Путевой лист грузового автомобиля",
                            Info: "ru"
                        },
                        {
                            Id: "ZWB_GENERAL_FULL.PDF",
                            Title: "АВТОКӨЛІК ЖОЛДАМА ҚАҒАЗЫ",
                            Desc: "ТАПСЫРЫС БЕРУШІНІҢ ТАЛОНЫ",
                            Info: "kz"
                        },
                        {
                            Id: "ZWB_GENERAL_PART.PDF",
                            Title: "АВТОКӨЛІК ЖОЛДАМА ҚАҒАЗЫ",
                            Desc: "-",
                            Info: "kz"
                        }
                    ]
                }));
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
        }
    });
});