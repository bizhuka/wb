sap.ui.define([
        'sap/ui/base/Object',
        'sap/ui/model/json/JSONModel',
        'sap/m/MessageToast'
    ], function (BaseObject, JSONModel, MessageToast) {
        "use strict";

        return BaseObject.extend("com.modekzWaybill.controller.LibChangeStatus", {
            owner: null,
            dialog: null,
            gui: null,

            constructor: function (owner) {
                var _this = this;
                _this.owner = owner;
            },

            openDialog: function (gui) {
                var _owner = this.owner;
                switch (gui.origin) {
                    case "WB":
                        gui.reasons = _owner.getResourceArray(_owner.status.DELAY_TEXTS).filter(function (pair) {
                            return pair.key !== 0;
                        });
                        gui.reasonLabel = 'Причина смещения сроков';
                        break;

                    case "REQ":
                        gui.reasons = _owner.getResourceArray(_owner.status.REQ_STATUS_TEXTS).filter(function (pair) {
                            return pair.key !== 0;
                        });
                        gui.reasonLabel = 'Статус выполнения заявки';
                        break;
                }
                // No text
                gui.text = gui.text ? gui.text : "";

                this.gui = gui;
                this.dialog = this.owner.createFragment("com.modekzWaybill.view.frag.ChangeStatusDialog", this);
                this.dialog.setModel(new JSONModel(this.gui), "gui");
                this.dialog.open();

                // Initial dates
                if (gui.origin === 'WB') {
                    this.owner.findById('id_reason_combo').setEnabled(false);
                    this.fromTime = gui.fromDate.getTime();
                    this.toTime = gui.toDate.getTime();
                }

                // First time
                this.checkOkEnabled();
            },

            onTextChange: function (oEvent) {
                // After text changed
                this.gui.text = oEvent.getParameter('value');
                this.checkOkEnabled();
            },

            onDateChanged: function () {
                this.checkOkEnabled();

                if (this.gui.origin === 'WB')
                    this.owner.findById('id_reason_combo').setEnabled(
                        this.gui.fromDate.getTime() !== this.fromTime ||
                        this.gui.toDate.getTime() !== this.toTime
                    )
            },

            checkOkEnabled: function () {
                var _this = this;
                var okButton = _this.dialog.getBeginButton();

                // By default
                okButton.setEnabled(false);

                // Data and callback
                var combo = this.owner.findById('id_reason_combo');
                var block = {
                    text: _this.gui.text,
                    fromDate: _this.gui.fromDate,
                    toDate: _this.gui.toDate,
                    reason: _this.gui.reason, // combo.getSelectedKey(),
                    motoHour: _this.gui.motoHour,

                    afterChecked: function (ok) {
                        okButton.setEnabled(ok);
                    }
                };

                // No text
                if (block.text.length === 0)
                    return;

                // Oops
                if (block.fromDate.getTime() > block.toDate.getTime()) {
                    MessageToast.show("Не верный диапазон дат");
                    return;
                }

                if (combo.getEnabled() && !block.reason) {
                    MessageToast.show("Заполните поле " + this.gui.reasonLabel);
                    return;
                }

                // Do the check
                _this.gui.check.call(this.owner, block);
            },

            onChangeStatusConfirm: function () {
                this.gui.success.call(this);
                this.dialog.close();
            },

            onChangeStatusClose: function () {
                this.dialog.close();
            },

            onChangeStatusAfterClose: function () {
                this.dialog.destroy();
                this.dialog = null;
            }

        });
    }
);