sap.ui.define([
    'com/modekzWaybill/controller/BaseController',
    'sap/m/MessageToast',
    'sap/ui/core/MessageType',
    'sap/ui/unified/FileUploader',
    'sap/ui/model/json/JSONModel'
], function (BaseController, MessageToast, MessageType, FileUploader, JSONModel) {
    "use strict";

    return BaseController.extend("com.modekzWaybill.controller.ImportR3", {
        onInit: function () {
            // call base init
            BaseController.prototype.onInit.apply(this, arguments);
        },

        importWerks: function () {
            this.updateDbFrom({
                link: "/r3/WERK?_persist=true",

                title: "Заводы - БЕ"
            });
        },

        importGasType: function () {
            this.updateDbFrom({
                link: "/r3/GAS_TYPE?_persist=true",

                title: "Материалы"
            });
        },

        loadWlnVehicle: function () {
            this.updateDbFrom({
                link: "/wialon/loadWlnVehicle",

                title: "Объектов wialon"
            });
        },

        uploadDriverMedCards: function () {
            this.loadFromFile({
                title: "Мед карты водителей",
                url: "./csv/uploadDriverMedCards",
                columns: [
                    "ИИН работника", "Табельный номер", "Работник", "Мед карта"
                ]
            });
        },

        uploadEquipment: function () {
            this.loadFromFile({
                title: "ТС подрядчика",
                url: "./csv/uploadEquipment",
                columns: [
                    "БЕ", "Завод", "Наименование ТС", "Подрядчик", "Гос номер", "Класс"
                ]
            });
        },

        loadFromFile: function (params) {
            var _this = this;

            // Dynamic columns
            var columns = [];
            var cells = [];
            for (var i = 0; i < params.columns.length; i++) {
                columns.push(new sap.m.Column({header: new sap.m.Label({text: params.columns[i]})}));
                cells.push(new sap.m.Label({text: "{COL_" + i + "}"}));
            }
            // Show results in table
            var table = new sap.m.Table({
                columns: columns
            });
            table.bindAggregation("items", "/items", new sap.m.ColumnListItem({
                cells: cells,
                highlight: {
                    parts: [{path: 'flag'}],

                    formatter: function (flag) {
                        switch (flag) {
                            case 'I':
                                return MessageType.Success;
                            case 'U':
                                return MessageType.Information;
                        }
                        return MessageType.None;
                    }
                }
            }));

            var fileUploader = new FileUploader('id_csv_uploader', {
                uploadUrl: params.url,

                uploadComplete: function (oEvent) {
                    // Whole data
                    var response = JSON.parse($(oEvent.getParameter("response")).text());

                    // Data model
                    var data = {
                        items: []
                    };

                    // First line headers
                    for (var i = 0; i< response.items.length; i++) {
                        var resItem = response.items[i];
                        var parts = resItem.data;

                        // update info
                        var item = {
                            flag: resItem.result
                        };
                        // Add fields to item
                        for (var p = 0; p < parts.length; p++)
                            item["COL_" + p] = parts[p];
                        data.items.push(item);
                    }

                    // Set new data
                    dialog.setModel(new JSONModel(data));

                    _this.showUpdateInfo(response, {
                        title: params.title,
                        afterUpdate: function () {
                            _this.getModel("wb").refresh();
                        }
                    })
                }
            });

            var dialog = new sap.m.Dialog({
                title: params.title,
                contentWidth: "85%",
                model: true,

                subHeader: new sap.m.Bar({
                    contentLeft: [
                        new sap.m.Label({text: "Разделитель: ';'"}),
                        new sap.m.Label({text: "Кодировка: 'UTF-8'"})
                    ],

                    contentMiddle: [fileUploader],

                    contentRight: [new sap.m.Button({
                        icon: "sap-icon://upload",
                        text: "Импорт",
                        press: function () {
                            fileUploader.upload();
                        }
                    })]
                }),

                content: [table],

                buttons: [
                    new sap.m.Button({
                        icon: "sap-icon://accept",
                        press: function () {
                            dialog.close();
                        }
                    })],

                afterClose: function () {
                    dialog.destroy();
                }
            });

            dialog.addStyleClass(this.getContentDensityClass());
            dialog.open();
        }

    });
});