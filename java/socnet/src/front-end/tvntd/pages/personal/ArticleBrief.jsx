/**
 * Written by Vy Nguyen (2017)
 */
'use strict';

import _               from 'lodash';
import React           from 'react-mod';

import {ArticleStore}  from 'vntd-root/stores/ArticleStore.jsx';
import ArticleTagBrief from 'vntd-root/components/ArticleTagBrief.jsx';
import ArticleBox      from 'vntd-root/components/ArticleBox.jsx';

class ArticleBrief extends ArticleTagBrief
{
    constructor(props) {
        super(props);
    }

    _renderArtBrief(art) {
        return this._renderArtBriefUuid(art.articleUuid);
    }

    _renderArtFull(art) {
        return this._renderArtFullUuid(art, art.articleUuid, true);
    }

    render() {
        let art, tag = this.props.tag, ranks = tag.sortedArts, articles = [];

        if (ranks == null) {
            return null;
        }
        _.forEach(ranks, function(artRank) {
            art = ArticleStore.getArticleByUuid(artRank.articleUuid);
            if (art != null) {
                articles.push(art);
            }
        });
        if (_.isEmpty(articles)) {
            return null;
        }
        if (!_.isEmpty(articles)) {
            let video = articles[0];
            video.youtube = "FaomPmTIKuw";
        }
        return (
            <section id='widget-grid'>
                {ArticleTagBrief.renderArtBox(articles,
                    this._renderArtBrief, this._renderArtFull, true)}
            </section>
        );
    }
}

export default ArticleBrief;
