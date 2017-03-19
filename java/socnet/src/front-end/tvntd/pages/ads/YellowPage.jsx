/*
 * Vy Nguyen (2017)
 */
'use strict';

import _                  from 'lodash';
import React, {PropTypes} from 'react-mod';

import BrowseSelection    from 'vntd-shared/layout/BrowseSelection.jsx';
import ArticleTagStore    from 'vntd-root/stores/ArticleTagStore.jsx';
import ArticleTagBrief    from 'vntd-root/components/ArticleTagBrief.jsx';

const sample = [
    { value: "abc",    label: "abc" },
    { value: "def",    label: "def" },
    { value: "img",    label: "img" },
    { value: "dws",    label: "dws" },
];

function filterLeterTags(tags, letter)
{
    let first, out = [], lower = letter.toLowerCase();

    _.forEach(tags, function(tag) {
        if (tag.tagName != null) {
            first = tag.tagName.charAt(0);
            if (first === letter || first === lower) {
                out.push(tag);
            }
        }
    });
    return out;
}

function filterTagBuckets(tags)
{
    let index, letter, out = [], first = 65, last = 90;

    _.forEach(tags, function(tag) {
        if (tag.tagName != null) {
            letter = tag.tagName.charCodeAt(0);
            if (letter > last) {
                letter = letter - 32;
            }
            if (first <= letter <= last) {
                index = letter - first;
                if (out[index] == null) {
                    out[index] = [];
                }
                out[index].push(tag);
            }
        }
    });
    return out;
}

class YellowPage extends React.Component
{
    constructor(props) {
        let index;

        super(props);
        this._onSelected    = this._onSelected.bind(this);
        this._updateArtTags = this._updateArtTags.bind(this);

        this.browse = [
            { label: "A", entry: {} },
            { label: "B", entry: {} },
            { label: "C", entry: {} },
            { label: "D", entry: {} },
            { label: "E", entry: {} },
            { label: "F", entry: {} },
            { label: "G", entry: {} },
            { label: "H", entry: {} },
            { label: "I", entry: {} },
            { label: "J", entry: {} },
            { label: "K", entry: {} },
            { label: "L", entry: {} },
            { label: "M", entry: {} },
            { label: "N", entry: {} },
            { label: "O", entry: {} },
            { label: "P", entry: {} },
            { label: "Q", entry: {} },
            { label: "R", entry: {} },
            { label: "S", entry: {} },
            { label: "T", entry: {} },
            { label: "U", entry: {} },
            { label: "V", entry: {} },
            { label: "W", entry: {} },
            { label: "X", entry: {} },
            { label: "Y", entry: {} },
            { label: "Z", entry: {} }
        ];
        index = 0;
        _.forEach(this.browse, function(it) {
            it.entry = {
                index    : index++,
                select   : true,
                inpName  : 'yp-' + index,
                inpDefVal: null,
                selectOpt: null,
                onSelect : this._onSelected
            };
        }.bind(this));

        this.state = {
            currentTag: this._updateArtTags()
        };
    }

    componentDidMount() {
        this.unsub = ArticleTagStore.listen(this._updateArtTags);
    }

    componentWillUnmount() {
        if (this.unsub != null) {
            this.unsub();
            this.unsub = null;
        }
    }

    _updateArtTags() {
        let label, select, defTag = null,
            buckets = ArticleTagStore.getFilterTag("blog", filterTagBuckets);

        _.forEach(buckets, function(item, key) {
            label  = this.browse[parseInt(key)];
            select = [];

            _.forEach(item, function(tag) {
                if (defTag == null) {
                    defTag = tag.tagName;
                }
                select.push({
                    label: tag.tagName,
                    value: tag.tagName
                });
            });
            label.entry.selectOpt = select;
        }.bind(this));
        return defTag;
    }

    _onSelected(entry, val) {
        console.log("select entry " + entry.index + " val " + val);
        console.log(val);
        this.setState({
            currentTag: val
        });
    }

    _renderTagAds(tagName) {
        let tag = ArticleTagStore.getTagByName(tagName);

        if (tag == null) {
            return null;
        }
        return (
            <div className="panel panel-default">
                {/*
                <div className="panel-heading">
                    <h4 className="panel-title">{tag.tagName}</h4>
                </div>
                    */}
                <div className="panel-body">
                    <ArticleTagBrief tag={tag}/>
                </div>
            </div>
        );
    }

    render() {
        return (
            <div className="padding-top-10">
                <BrowseSelection labels={this.browse}/>
                <div className="row">
                    {this._renderTagAds(this.state.currentTag)}
                </div>
            </div>
        );
    }
}

YellowPage.propTypes = {
};

export default YellowPage;