import React from 'react-mod'
import moment from 'moment'

let Moment = React.createClass({
    render: function () {
        return (
            <span>{
                moment(this.props.date).format(this.props.format || 'llll')
            }</span>
        )
    }
});

export default Moment
