/*
 * Vy Nguyen (2017)
 */
'use strict';

import _                  from 'lodash';
import React, {PropTypes} from 'react-mod';

import {
    SelectWrap, TAWrap
} from 'vntd-shared/forms/commons/GenericForm.jsx';

class BrowseSelection extends React.Component
{
    constructor(props) {
        let i, entry, labels = props.labels, len = labels.length;

        super(props);
        this._onSearch   = this._onSearch.bind(this);
        this._onSelected = this._onSelected.bind(this);

        for (i = 0; i < len; i++) {
            entry = labels[i].entry;
            this._setDefEntry(entry);

            if (entry.inpDefVal != null) {
                break;
            }
        }
        this.state = {
            select: entry
        };
    }

    getCurrEntry() {
        return this.state.entry;
    }

    _onSearch() {
    }

    _setDefEntry(entry) {
        if (!_.isEmpty(entry.selectOpt) && entry.inpDefVal == null) {
            entry.inpDefVal = entry.selectOpt[0].label;
        }
    }

    _clickLabel(label) {
        let entry = label.entry;

        this._setDefEntry(entry);
        entry.onSelect(entry, entry.inpDefVal);

        this.setState({
            select: entry
        });
    }

    _onSelected(entry, val) {
        entry.inpDefVal = val;
        entry.onSelect(entry, val);
    }

    render() {
        let search = {
            taOptions : [ "abc", "def", "hef" ],
            maxVisible: 100,
            inpHolder : "abc",
            onSelect  : this._onSelected
        },
        inpLabels, select = this.state.select, labels = [];

        inpLabels = this.props.labels;
        _.forEach(inpLabels, function(item) {
            let fmt = (select.inpDefVal === item.label) ?
                "btn btn-danger" : "btn btn-primary";

            labels.push(
                <button type="button" className={fmt} key={_.uniqueId('lbl-')}
                    onClick={this._clickLabel.bind(this, item)}>
                    <span>{item.label}</span>
                </button>
            );
        }.bind(this));

        //<ul className="list-inline">{labels}</ul>
        return (
            <div className="row">
                <div className="btn-group" role="group">
                    {labels}
                </div>
                <div className="row">
                    <div className="col-xs-6 col-sm-6 col-md-6 col-lg-6">
                        <SelectWrap entry={select} value={select.inpDefVal}
                            onSelected={this._onSelected}/>
                    </div>
                    <div className="col-xs-6 col-sm-6 col-md-6 col-lg-6">
                        <div className="input-group">
                            <TAWrap entry={search}
                                taFormat={{input: 'form-control input-md'}}/>
                            <span className="input-group-btn">
                                <button className="btn btn-secondary"
                                    onClick={this._onSearch}>
                                    <i className="fa fa-search"/>
                                </button>
                            </span>
                        </div>
                    </div>
                </div>
            </div>
        );
    }
};

BrowseSelection.propTypes = {
};

export default BrowseSelection;
