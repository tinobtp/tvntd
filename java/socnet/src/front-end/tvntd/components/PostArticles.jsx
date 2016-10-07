/**
 * Copyright by Vy Nguyen (2016)
 * BSD License
 */
'use strict';

import React from 'react-mod';
import _     from 'lodash';

import ArticleStore from 'vntd-root/stores/ArticleStore.jsx';
import PostPane     from 'vntd-root/components/PostPane.jsx';
import WidgetGrid   from 'vntd-shared/widgets/WidgetGrid.jsx';

let PostArticles = React.createClass({

    render: function() {
        let panes = null;

        if (this.props.data && !_.isEmpty(this.props.data)) {
            panes = [];
            _.forOwn(this.props.data, function(article, idx) {
                panes.push(<PostPane data={article} key={_.uniqueId('post-pane-')}/>);
            });
        } else {
            panes = <div><h2>No articles</h2></div>
        }
        return (
            <WidgetGrid className={this.props.className} style={{ height: 'auto' }}>
                {panes}
            </WidgetGrid>
        )
    }
});

export default PostArticles;
