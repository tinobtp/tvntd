/**
 * Created by Vy Nguyen (2016)
 * BSD License
 */
'use strict';

import _            from 'lodash';
import Actions      from 'vntd-root/actions/Actions.jsx';
import AuthorStore  from 'vntd-root/stores/AuthorStore.jsx';
import CommentStore from 'vntd-root/stores/CommentStore.jsx';
import UserStore    from 'vntd-shared/stores/UserStore.jsx';
import WebUtils     from 'vntd-shared/utils/WebUtils.jsx';

import {VConst}     from 'vntd-root/config/constants.js';
import {
    Article, Product, AdsItem, ArticleRank, AuthorShelf, AuthorTagMgr, Author
} from 'vntd-root/stores/Article.jsx';

export class ArticleFactory {

    static newInstance(store, data) {
        let article, kind = store.data.storeKind;

        if (kind === VConst.blog) {
            article = new Article(data, store);
        } else if (kind === VConst.estore) {
            article = new Product(data, store);
        } else {
            article = new AdsItem(data, store);
        }
        if (data.rank != null) {
            CommentStore.addArtAttr(data.rank);
            article.rank = AuthorStore.addArticleRankFromJson(data.rank);
        }
        if (article.rank == null) {
            article.authorStore = AuthorStore;
        }
        return article;
    }

    static newDefInstance(store, articleUuid, authorUuid) {
        let json = {
            authorUuid : authorUuid,
            articleUuid: articleUuid,
            createdDate: WebUtils.currentTime
        };
        return ArticleFactory.newDefInstanceFrmRank(
            store, AuthorStore.lookupArticleRankByUuid(articleUuid), json
        );
    }

    static newDefInstanceFrmRank(store, artRank, json) {
        if (json == null) {
            json = {};
        }
        json.noData     = true;
        json.ownerStore = store;

        if (artRank != null) {
            json.authorUuid  = artRank.authorUuid;
            json.articleUuid = artRank.articleUuid;
            json.createdDate = artRank.timeStamp;
            json.topic       = artRank.getArtTitle();
            json.content     = artRank.contentBrief;
            json.createdDate = artRank.timeStamp;
            json.published   = true;
        }
        store.recordMissingUuid(json.articleUuid);
        return ArticleFactory.newInstance(store, json);
    }

    static newArticleRank(data, store, authorTag, article) {
        let artRank = new ArticleRank(data, store, authorTag, article);

        if (article == null) {
            article = store.addDefaultFromRank(artRank);
        }
        if (article != null) {
            article.rank = artRank;
        }
        return artRank;
    }

    static newAuthorShelf(articleUuid, authorUuid) {
        return new AuthorShelf(articleUuid, authorUuid);
    }

    static newAuthorTagMgr(uuid, authorStore) {
        return new AuthorTagMgr(uuid, authorStore);
    }

    static newAuthor(author) {
        return new Author(author);
    }
}

class CommonStore {
    constructor(kind) {
        this.init(kind);
    }

    init(kind) {
        this.data = {
            getItemCount : 0,
            itemsByAuthor: {},
            itemsByUuid  : {},

            requestUuids : null,
            missingUuids : null,
            myItems      : null,
            errorText    : "",
            errorResp    : null,
            itemKinds    : {},
            storeKind    : kind,
            listenChanges: {}
        }
        return this.data;
    }

    listenChanges(listener, key) {
        this.data.listenChanges[key] = listener;
    }

    _notifyListeners(code, changeList) {
        let storeKind = this.data.storeKind;

        _.forOwn(this.data.listenChanges, function(callback, key) {
            callback[key](storeKind, code, changeList);
        });
    }

    getItemsByAuthor(uuid, fetch) {
        let anchor, items = [], owners;

        if (fetch === true) {
            owners = UserStore.getFetchedUuidList(this.storeKind);
            if (owners != null && !_.isEmpty(owners)) {
                Actions.getPublishProds({
                    authorUuid: UserStore.getSelfUuid(),
                    uuidType  : "user",
                    reqKind   : this.storeKind,
                    uuids     : owners
                });
            }
        }
        anchor = this.getItemOwner(uuid);
        if (anchor.hasData() === true) {
            anchor.iterArticles(function(it) {
                items.push(it);
            });
        }
        return items;
    }

    iterAuthorItemStores(uuid, func, arg) {
        let anchor = this.getItemOwner(uuid);
        if (anchor != null) {
            anchor.iterArticles(func, arg);
        }
        return anchor;
    }

    getItemOwner(uuid) {
        let anchor = this.data.itemsByAuthor[uuid];
        if (anchor == null) {
            anchor = this._createOwnerAnchor(uuid, null);
            this.data.itemsByAuthor[uuid] = anchor;
        }
        return anchor;
    }

    /*
     * Return author's items sorted to display.
     */
    getSortedItemsByAuthor(uuid) {
        let anchor = this.getItemOwner(uuid);
        return anchor.getSortedArticles();
    }

    getMyItems() {
        if (this.data.myItems != null) {
            return this.data.myItems.sortedArticles;
        }
        return null;
    }

    getMySavedItems() {
        let myShelf = this.data.myItems;
        return (myShelf != null) ? myShelf.getSortedSavedArts() : null;
    }

    getItemByUuid(uuid, authorUuid) {
        let item = this.data.itemsByUuid[uuid];

        if (item == null) {
            if (authorUuid == null) {
                return null;
            }
            item = this._addDefaultItem(this, uuid, authorUuid);
        }
        return item;
    }

    getAuthorUuid(articleUuid) {
        let item = this.data.itemsByUuid[articleUuid];
        if (item != null) {
            return item.getAuthorUuid();
        }
        return null;
    }

    onPublishItemCompleted(item, store) {
        let it = this._addItemStore(item, true), pubTag = it.publicTag;

        this._notifyListeners("add", [it]);
        store.trigger(this.data, [it], "postOk", true, it.authorUuid);
    }

    onPublishItemFailure(item, store) {
        store.trigger(this.data, null, "failure", false, item.authorUuid);
    }

    onGetPublishItemCompleted(data, key, store) {
        let items = [];

        CommentStore.onGetCommentsCompleted(data);
        _.forEach(data[key], function(item) {
            items.push(this._addItemStore(item, false));
        }.bind(this));

        this.data.requestUuids = null;
        if (!_.isEmpty(items)) {
            this._notifyListeners("add", items);
        }
        store.trigger(this.data, items, "getOk", !_.isEmpty(items), null);
    }

    onGetPublishItemFailure(data, store) {
        store.trigger(this.data, null, "failure", false, null);
    }

    requestItems(actionFn) {
        let uuids;

        if (this.data.requestUuids != null ||
            this.data.missingUuids == null || _.isEmpty(this.data.missingUuids)) {
            return;
        }
        uuids = [];
        this.data.requestUuids = this.data.missingUuids;
        this.data.missingUuids = null;

        _.forOwn(this.data.requestUuids, function(v, k) {
            uuids.push(k);
        });
        actionFn({
            authorUuid: null,
            uuidType  : this.data.storeKind,
            reqKind   : this.data.storeKind,
            uuids     : uuids
        });
    }

    onDeleteItemCompleted(data, store) {
        let out = this._removeItemStore(data.uuids, data.authorUuid);

        this._notifyListeners("remove", out);
        store.trigger(this.data, [data], "delOk", true, data.authorUuid);
    }

    recordMissingUuid(uuid) {
        let missing = this.data.missingUuids;

        if (missing == null) {
            this.data.missingUuids = {};
            missing = this.data.missingUuids;
        }
        missing[uuid] = true;
        return missing;
    }

    updateMissingUuid(uuids) {
        let store = this.data.itemsByUuid,
            missing = this.data.missingUuids;

        _.forEach(uuids, function(uid, key) {
            if (store[key] == null) {
                if (this.data.missingUuids == null) {
                    this.data.missingUuids = {};
                    missing = this.data.missingUuids;
                }
                missing[key] = true;
            }
        }.bind(this));
    }

    updatePublicTags(tags, actionFn) {
        _.forEach(tags, function(t) {
            if (t.articleRank != null) {
                this.updateMissingUuid(t.articleRank);
            }
        }.bind(this));
        this.requestItems(actionFn);
    }

    /**
     * Internal methods, used by derrived stores.
     */
    errorHandler(error, store) {
        this.data.errorText = error.getErrorCodeText();
        this.data.errorResp = error.getUserText();
        store.trigger(this.data, null, "failure", false, null);
    }

    _createOwnerAnchor(authorUuid, article) {
        let anchor = ArticleFactory.newAuthorShelf(article, authorUuid);

        this.data.itemsByAuthor[authorUuid] = anchor;
        if (UserStore.isUserMe(authorUuid)) {
            this.data.myItems = anchor;
        }
        return anchor;
    }

    /**
     * Add default article when we only have article uuid and author uuid.
     */
    _addDefaultItem(store, articleUuid, authorUuid) {
        return this._addItemStore(
            ArticleFactory.newDefInstance(store, articleUuid, authorUuid), false
        );
    }

    /**
     * Add default article generated from article rank.
     */
    addDefaultFromRank(artRank) {
        return this._addItemStore(
            ArticleFactory.newDefInstanceFrmRank(this, artRank, null), false
        );
    }

    /**
     * Add article item to the store where item is in json format or Article type.
     */
    _addItemStore(item, preend) {
        let articleUuid, authorUuid, anchor, authorTagMgr, article;

        articleUuid = item.articleUuid;
        authorUuid  = item.authorUuid;
        anchor      = this.getItemOwner(authorUuid);
        article     = this.data.itemsByUuid[articleUuid];

        if (article == null) {
            if (item instanceof Article) {
                article = item;
            } else {
                article = ArticleFactory.newInstance(this, item);
            }
            this.data.itemsByUuid[articleUuid] = article;
            anchor.addArticle(article, preend);
        } else {
            article.updateFromJson(item);
        }
        if (item.rank != null && !(item instanceof Article)) {
            authorTagMgr = AuthorStore.getAuthorTagMgr(authorUuid);
            article.rank = authorTagMgr.addArticleRank(item.rank);
        }
        article.getArticleRank();
        return article;
    }

    _removeItemStore(itemUuids, authorUuid, silent) {
        let item, result = [], anchor = this.getItemOwner(authorUuid);

        _.forEach(itemUuids, function(articleUuid) {
            anchor.removeArticle(articleUuid);
            item = this.data.itemsByUuid[articleUuid];
            result.push(item);

            if (silent == true) {
                AuthorStore.removeArticleRank(item, silent);
            }
            delete this.data.itemsByUuid[articleUuid];
        }.bind(this));
        return result;
    }

    _triggerStore(store, item, code) {
        store.trigger(this.data, item, code, true, item.authorUuid);
    }

    addFromJson(items, key, index) {
        let oldArt, itemsByKey = this.data[key];

        _.forOwn(items, function(it, k) {
            oldArt = itemsByKey[it.articleUuid];

            if (oldArt == null) {
                itemsByKey[it.articleUuid] = ArticleFactory.newInstance(this, it);

            } else if (oldArt.noData == true) {
                oldArt.updateFromJson(it);
            }
        }.bind(this));

        if (index == true) {
            this.indexAuthors(items);
        }
        return this.data;
    }

    indexAuthors(items) {
        let itemsByUuid = this.data.itemsByUuid,
            itemsByAuthor = this.data.itemsByAuthor;

        _.forOwn(items, function(jsonItem, key) {
            let anchor, item = itemsByUuid[jsonItem.articleUuid];
            if (item == null) {
                return;
            }
            if (item.author == null) {
                item.author = UserStore.getUserByUuid(item.authorUuid);
            }
            anchor = this.getItemOwner(item.authorUuid);
            anchor.addSortedArticle(item);

            if (UserStore.isUserMe(item.authorUuid)) {
                this.data.myItems = anchor;
            }
        }.bind(this));
    }
    
    dumpData(hdr) {
        console.log(hdr);
        console.log(this.data);
    }
}

export default CommonStore;
