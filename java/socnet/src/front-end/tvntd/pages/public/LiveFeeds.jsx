/*
 * Modified by Vy Nguyen (2016)
 */
'use strict';

import React from 'react-mod';

// import JarvisWidget from  'vntd-shared/widgets/JarvisWidget.jsx'

import LiveStats     from './LiveStats.jsx';
import SocialNetwork from './SocialNetwork.jsx';
import Revenue       from './Revenue.jsx';

class LiveFeeds extends React.Component
{
/*
            <JarvisWidget
                togglebutton={false}
                editbutton={false}
                fullscreenbutton={false}
                colorbutton={false}
                deletebutton={false}>
              */
    render() {
        return (
            <div>
                <header>
                    <span className="widget-icon">
                        <i className="glyphicon glyphicon-stats txt-color-darken"/>
                    </span>
                    <h2>Live Feeds </h2>
                    <ul className="nav nav-tabs pull-right in" id="myTab">
                        <li className="active">
                            <a data-toggle="tab" href="#s1"><i className="fa fa-clock-o" />
                                <span className="hidden-mobile hidden-tablet">Live Stats</span>
                            </a>
                        </li>
                        <li>
                            <a data-toggle="tab" href="#s2"><i className="fa fa-facebook" />
                                <span className="hidden-mobile hidden-tablet">Social Network</span>
                            </a>
                        </li>
                        <li>
                            <a data-toggle="tab" href="#s3"><i className="fa fa-dollar" />
                                <span className="hidden-mobile hidden-tablet">Revenue</span>
                            </a>
                        </li>
                    </ul>
                </header>
                <div className="no-padding">
                    <div className="widget-body">
                        <div id="myTabContent" className="tab-content">
                            <LiveStats className="tab-pane fade active in padding-10 no-padding-bottom" id="s1"/>
                            <SocialNetwork className="tab-pane fade" id="s2" />
                            <Revenue className="tab-pane fade" id="s3" />
                        </div>
                    </div>
                </div>
            </div>
        );
    }
}

export default LiveFeeds
