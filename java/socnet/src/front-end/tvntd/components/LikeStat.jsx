/**
 * Written by Vy Nguyen (2016)
 * BSD License
 */
'use strict';

import React        from 'react-mod';
import moment       from 'moment';

let LikeStat = React.createClass({

    render: function() {
        let data = this.props.data;
        let dateSplit = this.props.split ? <br/> : null;
        let className = this.props.className ? "list-inline " + this.props.className : "list-inline";
        return (
            <ul className={className}>
                <li><i className="fa fa-comment"/> {data.commentCount}</li>
                <li><i className="fa fa-thumbs-up"/> {data.likesCount}</li>
                <li><i className="fa fa-share"/> {data.sharesCount}</li>
                {dateSplit}
                {data.dateString ? <li> <i className="fa fa-calendar"/> {data.dateString}</li> : null}
                {data.dateMoment ? <li> {moment(data.dateMoment).fromNow()}</li> : null}
            </ul>
        );
    }
});

export default LikeStat;