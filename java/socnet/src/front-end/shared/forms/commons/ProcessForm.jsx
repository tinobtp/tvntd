/*
 * Vy Nguyen (2017)
 */
'use strict';

import _                  from 'lodash';
import React, {PropTypes} from 'react-mod';

import StateButtonStore   from 'vntd-shared/stores/StateButtonStore.jsx';
import InputStore         from 'vntd-shared/stores/NestableStore.jsx';
import ErrorStore         from 'vntd-shared/stores/ErrorStore.jsx';
import ErrorView          from 'vntd-shared/layout/ErrorView.jsx';
import StateButton        from 'vntd-shared/utils/StateButton.jsx';
import Lang               from 'vntd-root/stores/LanguageStore.jsx';
import Mesg               from 'vntd-root/components/Mesg.jsx';

import { InputEntry }     from 'vntd-shared/forms/commons/GenericForm.jsx';

class FormData
{
    constructor(props, suffix) {
        this.props       = props;
        this.suffix      = suffix || "";
        this.imageId     = null;
        this.buttons     = null;
        this.submitBtn   = null;
        this.initialized = false;

        this.onFocus = this.onFocus.bind(this);
        this.onBlur  = this.onBlur.bind(this);
        this.onClick = this.onClick.bind(this);
        return this;
    }

    getFormData() {
        return this.forms;
    }

    _getId(name) {
        return name + this.suffix;
    }

    clearError() {
        if (this.forms.formId != null) {
            ErrorStore.clearError(this.forms.formId);
        }
    }

    clearData() {
        this.iterFormFields(null, null, function(arg, entry) {
            InputStore.freeItemIndex(entry.inpName);
        });
    }

    setImageId(imgId) {
        this.imageId = imgId;
    }

    getImageId(imgId) {
        return this.imageId;
    }

    getFormId() {
        return this.forms.formId || null;
    }

    getData(entryInfo) {
        let retVal, info, imgRec = InputStore.getItemIndex(this.getImageId());

        if (imgRec == null) {
            imgRec = {
                articleUuid: 0,
                adImgs     : [],
                mainImg    : null
            };
        }
        retVal = {
            authorUuid : 0,
            articleUuid: imgRec.articleUuid,
            adImgs     : imgRec.adImgs,
            mainImg    : imgRec.mainImg,
            imageRec   : imgRec
        };
        this.iterFormFields(entryInfo, null, function(einfo, entry, section) {
            retVal[entry.field] = InputStore.getIndexString(entry.inpName);
            info = {
                section: section,
                field  : entry.field,
                entryId: entry.inpName
            };
            if (entry.dropzone === true) {
                info.emptyOk = true;
            }
            einfo.push(info);
            return einfo;
        });
        return retVal;
    }

    setData(value, dzHandler) {
        if (this.initialized === true) {
            return;
        }
        this.defaultVal  = value;
        this.initialized = true;

        this.iterFormFields(null, null, function(sub, entry, section) {
            entry.inpName = this._getId(entry.inpName);
            if (entry.dropzone === true) {
                this.setImageId(entry.inpName);
                entry.handlers = dzHandler;
            }
            if (entry.onFocus == null) {
                entry.onFocus = this.onFocus;
            }
            this._setDefValue(value, entry);
        }.bind(this));
    }

    _setDefValue(value, entry) {
        let defVal;

        if (value != null && value[entry.field] != null) {
            defVal = value[entry.field];
            entry.inpDefVal = defVal;

        } else if (entry.inpDefVal != null) {
            defVal = entry.inpDefVal;
        } else {
            return;
        }
        InputStore.storeItemIndex(entry.inpName, defVal, false);
    }

    _setAllDefValues() {
        this.iterFormFields(null, null, function(sub, entry, section) {
            this._setDefValue(this.defaultVal, entry);
        }.bind(this));
    }

    uploadImageOk(result) {
        let imgId = this.getImageId(), img;

        if (imgId == null) {
            return null;
        }
        img = InputStore.getItemIndex(imgId);
        if (img == null) {
            img = {
                articleUuid: result.articleUuid,
                authorUuid : result.authorUuid,
                imgObjId   : result.imgObjId
            };
            InputStore.storeItemIndex(imgId, img, false);
        }
        return img;
    }

    setValues(errFlags) {
        let val;

        this.iterFormFields(errFlags, null, function(flags, entry, section) {
            if (flags[entry.field] != null) {
                entry.errorFlag = true;
            } else {
                entry.errorFlag = null;
            }
            entry.errorId = entry.inpName;
            val = InputStore.getItemIndex(entry.inpName);
            if (val != null) {
                entry.inpDefVal = val;
            }
            return flags;
        });
    }

    iterFormFields(cbObj, iterSection, iterField) {
        _.forEach(this.forms.formEntries, function(fItem) {
            if (iterSection != null) {
                cbObj = iterSection(cbObj, fItem);
            }
            _.forEach(fItem.entries, function(entry) {
                if (iterField != null) {
                    cbObj = iterField(cbObj, entry, fItem);
                }
                return cbObj;
            })
        });
        return cbObj;
    }

    createButtons() {
        let obj, buttons = this.forms.buttons;

        if (this.buttons != null) {
            return;
        }
        this.buttons = {};
        if (buttons != null) {
            _.forEach(buttons, function(btn) {
                obj = StateButtonStore.createButton(btn.btnName, btn.btnCreate);
                this.buttons[btn.btnName] = obj;
                if (btn.btnSubmit === true) {
                    this.submitBtn = btn;
                }
            }.bind(this));
        }
    }

    renderButtons(onSubmit) {
        let buttons, name, form = this.forms;

        if (form.buttons == null) {
            return null;
        }
        buttons = form.buttons.map(function(btn) {
            name = btn.btnName;
            if (btn.btnSubmit !== true) {
                return (
                    <StateButton btnId={name} className={btn.btnFmt}
                        onClick={this.onClick.bind(this, btn, this.buttons[name])}/>
                );
            }
            return (
                <StateButton btnId={name} className={btn.btnFmt} onClick={onSubmit}/>
            );
        }.bind(this));
        return (
            <div className="btn-group pull-right">
                {buttons}
            </div>
        );
    }

    renderTwoCols(section, onBlur) {
        let blur, col1, col2, entry,
            out = [], entries = section.entries, length = entries.length;

        for (let i = 0; i < length; i = i + 2) {
            col2  = null;
            entry = entries[i];

            col1 = InputEntry.render(entry, onBlur);
            if ((i + 1) < length) {
                entry = entries[i + 1];
                col2  = InputEntry.render(entry, onBlur);
            }
            out.push(
                <div className="row" key={_.uniqueId('form-col-')}>
                    <div className="col-xs-12 col-sm-12 col-md-6 col-lg-6">
                        {col1}             
                    </div>
                    <div className="col-xs-12 col-sm-12 col-md-6 col-lg-6">
                        {col2}             
                    </div>
                </div>
            );
        }
        return out;
    }

    renderForm(onBlur) {
        let entries, legend, form = this.forms;

        return form.formEntries.map(function(section) {
            if (section.twoCols != null) {
                entries = this.renderTwoCols(section, onBlur);
            } else {
                entries = section.entries.map(function(entry) {
                    return InputEntry.render(entry, onBlur);
                });
            }
            legend = section.legend != null ?
                <legend><Mesg text={section.legend}/></legend> : null;

            return (
                <div className={section.sectFmt} key={_.uniqueId('form-fields')}>
                    {legend}
                    <fieldset>
                        {entries}
                    </fieldset>
                </div>
            );
        }.bind(this));
    }

    renderHeader() {
        return null;
    }

    renderFooter() {
        return null;
    }

    changeSubmitState(state, click) {
        let btn = this.submitBtn;
        if (btn != null) {
            StateButtonStore.setButtonStateObj(this.buttons[btn.btnName], state);
            if (click === true) {
                this.onClick(btn, this.buttons[btn.btnName]);
            }
        }
    }

    onBlur(entry) {
        this.changeSubmitState("needSave", false);
    }

    onFocus(entry) {
        ErrorStore.clearError(this.getFormId());
    }

    onClick(btn, btnState) {
    }

    validateInput(data, errFlags) {
        return data;
    }

    submitNotif(store, result, status) {
        this.changeSubmitState("saved", false);
    }

    submitFailure(result, status) {
        this.changeSubmitState("failure", false);
        this._setAllDefValues();
    }

    submitAct(data) {
        let submitFn = this.forms.submitFn;

        this.changeSubmitState("saving", true);
        if (submitFn != null) {
            submitFn(data);
        }
    }

    render(onBlur, onSubmit) {
        return (
            <div className={this.forms.formFmt}>
                {this.renderHeader()}
                {this.renderForm(onBlur)}
                <ErrorView errorId={this.getFormId()}/>
                {this.renderButtons(onSubmit)}
                {this.renderFooter()}
            </div>
        );
    }
}

class ProcessForm extends React.Component
{
    constructor(props) {
        let form;
        super(props);

        this._imgDz        = null;
        this._dzSend       = this._dzSend.bind(this);
        this._dzSuccess    = this._dzSuccess.bind(this);
        this._dzError      = this._dzError.bind(this);
        this._onBlurInput  = this._onBlurInput.bind(this);
        this._submitClick  = this._submitClick.bind(this);
        this._updateState  = this._updateState.bind(this);
        this._imgUploadOk  = this._imgUploadOk.bind(this);
        this._getInitState = this._getInitState.bind(this);

        props.form.createButtons();
        this.state = this._getInitState();
    }

    _getInitState() {
        return {
            errFlags: {}
        };
    }

    componentWillMount() {
        let value = this.props.value, context = this.props.form;

        if (value == null) {
            value = this.props.defValue;
        }
        context.setData(value, {
            sending: this._dzSend,
            success: this._dzSuccess,
            error  : this._dzError,
            init   : function(dz) {
                this._imgDz = dz;
            }
        });
    }

    componentDidMount() {
        this.unsub = this.props.store.listen(this._updateState);
    }

    componentWillUnmount() {
        if (this.unsub != null) {
            this.unsub();
            this.unsub = null;
        }
    }

    _dzSend(files, xhr, form) {
        form.append('name', files.name);
        form.append('authorUuid', '');
        form.append('articleUuid', '');
    }

    _dzSuccess(dz, hdr, progress) {
        this._imgUploadOk(null, hdr);
    }

    _dzError() {
    }

    _onBlurInput(entry) {
        let context = this.props.form;

        context.clearError();
        context.onBlur(entry);
        if (!_.isEmpty(this.state.errFlags)) {
            this.setState({
                errFlags: {}
            });
        }
    }

    _imgUploadOk(entry, result) {
        this.props.form.uploadImageOk(result);
    }

    _updateState(data, status) {
        let context = this.props.form;

        context.clearData();
        context.submitNotif(this.props.store, data, status);

        if (this._imgDz != null) {
            this._imgDz.removeAllFiles();
        }
        this.setState(this._getInitState());
    }

    _submitClick() {
        let field, value, data,
            context = this.props.form, errFlags = {}, entryInfo = [];

        data = context.getData(entryInfo);
        _.forEach(entryInfo, function(info) {
            field = info.field;
            value = data[field];
            if (info.emptyOk == null && ((value == null) ||
                (typeof value === 'string' && value === ''))) {
                errFlags[field] = true;
                errFlags.helpText = Lang.translate("Enter values in highlighted fields");
                errFlags.errText  = Lang.translate("Please correct highlighted values");
            }
        });
        data = context.validateInput(data, errFlags);
        if (errFlags.errText == null) {
            context.submitAct(data);
            return;
        }
        this.setState({
            errFlags: errFlags
        });
        ErrorStore.reportErrMesg(context.getFormId(),
            errFlags.errText, errFlags.helpText);
    }

    render() {
        let context = this.props.form;

        context.setValues(this.state.errFlags);
        return context.render(this._onBlurInput, this._submitClick);
    }
}

ProcessForm.propTypes = {
    form    : PropTypes.object.isRequired,
    store   : PropTypes.object.isRequired,
    value   : PropTypes.object,
    defValue: PropTypes.object
};

export default ProcessForm;
export { FormData, ProcessForm }