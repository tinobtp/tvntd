/*
 * Written by Vy Nguyen (2017)
 */
'use strict';

import _               from 'lodash';
import React           from 'react-mod';
import Spinner         from 'react-spinjs';
import {VntdGlob}      from 'vntd-root/config/constants.js';

class WebUtils
{
    constructor() {
        this.curretTime = (new Date()).getTime();
    }

    static spinner() {
        return <Spinner config={VntdGlob.spinner}/>;
    }
}

class SeqContainer
{
    constructor() {
        this.curr = 0;
        this.dict = {};
        this.data = {};
        this.dictSelOpt = [];
        this.dataSelOpt = [ {
            label: "No selection",
            value: "_empty_"
        } ];
    }

    push(data) {
        let curr = this.curr.toString();

        this.dict[curr] = data;
        this.curr = this.curr + 1;
        return curr;
    }

    pushData(key, value) {
        let shortKey = key.substr(0, 16);

        this.data[shortKey] = value;
        this.dataSelOpt.push({
            label: key,
            value: shortKey,
            item : value
        });
    }

    getDataKey(key) {
        return this.data[key.substr(0, 16)];
    }

    getItem(pos) {
        return this.dict[pos];
    }

    getItems() {
        return this.dict;
    }

    getItemCount() {
        return this.curr;
    }

    getSelectOpt(data) {
        return data ? this.dataSelOpt : this.dictSelOpt;
    }
}

export default WebUtils;
export { WebUtils, SeqContainer }
