/**
 * Copyright by Vy Nguyen (2016)
 * BSD License
 */
'use strict';

import _               from 'lodash';
import React           from 'react-mod'
import TabPanel        from 'vntd-shared/layout/TabPanel.jsx';
import AdminStore      from 'vntd-root/stores/AdminStore.jsx';
import ArticleTagStore from 'vntd-root/stores/ArticleTagStore.jsx';
import ArticleTagBrief from 'vntd-root/components/ArticleTagBrief.jsx';

class MainBlog extends React.Component
{
    constructor(props) {
        super(props);

        this.state = {
            pubTags: ArticleTagStore.getAllPublicTags(true)
        };
        this._updateState = this._updateState.bind(this);
        this._getBlogTab = this._getBlogTab.bind(this);
    }

    componentDidMount() {
        this.unsub = ArticleTagStore.listen(this._updateState);
    }

    componentWillUnmount() {
        if (this.unsub != null) {
            this.unsub();
            this.unsub = null;
        }
    }

    _updateState(data) {
        this.setState({
            pubTags: ArticleTagStore.getAllPublicTags(true)
        });
    }

    _getBlogTab() {
        let idx = 0;
        let out = {
            tabItems: [],
            tabContents: []
        };
        let pubTags = this.state.pubTags;
        _.forOwn(pubTags, function(tag) {
            out.tabItems.push({
                domId  : _.uniqueId('tag-'),
                tabText: tag.tagName,
                tabIdx : idx++
            });
            out.tabContents.push(
                <div key={_.uniqueId('tab-content-')} className="no-padding">
                    <ArticleTagBrief key={_.uniqueId('tag-brief-')} tag={tag}/>
                </div>
            );
        });
        return out;
    }

    render() {
        let tabData = this._getBlogTab();
        console.log(this.props.params);
        return (
            <div id="content">
                <div className="row">
                    <div className="col-sm-12 col-md-12 col-lg-12">
                        <TabPanel className="padding-top-10" context={tabData}>
                            {tabData.tabContents}
                        </TabPanel>
                    </div>
                </div>
            </div>
        )
    }
}

export default MainBlog;
