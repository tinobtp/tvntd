/**
 * Created by griga on 12/23/15.
 * Modified by Vy Nguyen
 */
import _ from 'lodash';

export default class MenuItem {

    constructor(data, parent) {
        this._id = _.uniqueId('nav-store-');

        this.titleKey = data.title;
        this.title    = data.title;
        this.route    = data.route;
        this.icon     = data.icon;
        this.badge    = data.badge;
        this.counter  = data.counter;
        this.parent   = parent;

        if (data.items) {
            this.items = _.map(data.items, function(child) {
                return new MenuItem(child, this);
            }.bind(this));
        }
        this.isOpen = this._isOpen();
        this.isActive = this._isActive();
        this.isHome = this.route === '/';

        return this;
    }

    _isOpen() {
        if (this._hasOpenChildren()) {
            return true;
        }
        let route = MenuItem.getRoute();
        if (route === '/' && this.route === '/') {
            return true;
        }
        return (this.route && route == this.route);
    }

    _hasOpenChildren() {
        return _.some(this.items, function(item){
            return item._isOpen()
        });
    }

    _isActive() {
        return MenuItem.getRoute() == this.route || (this._isOpen() && !this.parent);
    }

    updateState() {
        this.isOpen = this._isOpen();
        this.isActive = this._isActive();
    }

    updateActive() {
        this.isActive = this._isActive();
    }

    isSibling(item) {
        return this._id != item._id && this.parent &&
            _.some(this.parent.items, function(child) {
                return child._id == item._id && child._id != this._id
            });
    }

    isParentOf(item) {
        return this.items &&
            _.some(this.items, function(_item) {
                return _item._id == item._id || _item.isParentOf(item)
            });
    }

    static getRoute() {
        return location.hash.replace(/#/, '');
    }
}
