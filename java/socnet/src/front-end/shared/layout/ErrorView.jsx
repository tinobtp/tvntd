/*
 * Written by Vy Nguyen (2016)
 */
'use strict';

import React         from 'react-mod';
import ErrorStore    from 'vntd-shared/stores/ErrorStore.jsx';

class ErrorView extends React.Component
{
    constructor(props) {
        super(props);
        this.state = {
            error: ErrorStore.hasError(props.errorId, props.mesg)
        };
        this._changeState = this._changeState.bind(this);
        this._onCloseError = this._onCloseError.bind(this);
        this._errorMesg = this._errorMesg.bind(this);
    }

    componentDidMount() {
        this.unsub = ErrorStore.listen(this._changeState);
    }

    componentWillUnmount() {
        if (this.unsub != null) {
            this.unsub();
            this.unsub = null;
        }
    }

    _onCloseError(event) {
        event.stopPropagation();
        ErrorStore.clearError(this.props.errorId);
    }

    _changeState(data, notif) {
        if (notif != null && this.props.errorId === notif.getErrorId()) {
            if (notif.hasError() == false) {
                notif = null;
            }
            this.setState({
                error: notif
            });
        }
    }

    _errorMesg(errorText) {
        return (
            <span className={this.props.className}>
                <a className="close pull-left" onClick={this._onCloseError}><i className="fa fa-times"/></a>
                {errorText}
            </span>
        );
    }

    render() {
        let error = this.state.error;
        if (error == null) {
            return null;
        }
        let codeText = error.getErrorCodeText();
        if (codeText != null) {
            codeText = (
                <div>
                    <span>Status {error.getErrorCode()}: {codeText}</span>
                    <hr/>
                </div>
            );
        }
        let userText = error.getUserText();
        if (userText != null) {
            if (this.props.mesg === true) {
                return this._errorMesg(userText);
            }
            userText = <p>Reason: {userText}</p>;
        }
        let userHelp = error.getUserHelp();
        if (userHelp != null) {
            userHelp = <p>Action: {userHelp}</p>;
        }
        return (
            <div className={this.props.className}>
                <div className="row">
                    <div className="col-sm-1 col-md-1 col-lg-1">
                        <a className="close pull-left" onClick={this._onCloseError}><i className="fa fa-times"/></a>
                    </div>
                    <div className="col-sm-11 col-md-11 col-lg-11">
                        <h2>{codeText}</h2>
                        <h2>{userText}</h2>
                        <h3>{userHelp}</h3>
                    </div>
                </div>
                {this.props.children}
            </div>
        );
    }

    static stackTrace() {
        let err = new Error();
        console.log(err.stack);
        return err.stack;
    }
}

export default ErrorView;
