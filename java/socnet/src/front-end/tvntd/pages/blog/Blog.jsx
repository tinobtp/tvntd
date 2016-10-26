/**
 * Copyright by Vy Nguyen (2016)
 * BSD License
 */
import React from 'react-mod'

let rawItems = require('json!../../mock-json/blog.json');

class Blog extends React.Component
{
    render() {
        return (
<div id="content" className="fadeInUp">
    <div className="row">
        <div className="col-sm-9">
            <div className="well padding-10">
                <div className="row">
                    <div className="col-md-4">
                        <img src="rs/img/superbox/superbox-full-15.jpg" className="img-responsive" alt="rs/img"/>
                        <ul className="list-inline padding-10">
                            <li>
                                <i className="fa fa-calendar"></i>
                                <a href-void="" href="#"> March 12, 2015 </a>
                            </li>
                            <li>
                                <i className="fa fa-comments"></i>
                                <a href-void="" href="#"> 38 Comments </a>
                            </li>
                        </ul>
                    </div>
                    <div className="col-md-8 padding-left-0">
                        <h3 className="margin-top-0">
                            <a href-void="" href="#"> Why Should You Make A Separate Mobile Website for your Business? </a><br/><small className="font-xs"><i>Published by <a href-void="" href="#">John Doe</a></i></small></h3>
                        <p>
                            At vero eos et accusamus et iusto odio dignissimos ducimus qui blanditiis praesentium voluptatum deleniti atque corrupti quos dolores et quas molestias excepturi sint occaecati cupiditate non provident, similique sunt in culpa qui officia deserunt mollitia animi, id est laborum et dolorum fuga.
                            <br/><br/>Et harum quidem rerum facilis est et expedita distinctio lorem ipsum dolor sit amet, consectetur adipiscing elit. Ut non libero consectetur adipiscing elit magna. Sed et quam lacus. Fusce condimentum eleifend enim a feugiat. Pellentesque viverra vehicula sem ut volutpat. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Ut non libero magna. Sed et quam lacus. Fusce condimentum eleifend enim a feugiat.
                            <br/><br/>
                        </p>
                        <a className="btn btn-primary" href-void="" href="#"> Read more </a>
                        <a className="btn btn-warning" href-void="" href="#"> Edit </a>
                        <a className="btn btn-success" href-void="" href="#"> Publish </a>
                    </div>
                </div>
                <hr/>
                <div className="row">
                    <div className="col-md-4">
                        <img src="rs/img/superbox/superbox-full-19.jpg" className="img-responsive" alt="rs/img"/>
                        <ul className="list-inline padding-10">
                            <li>
                                <i className="fa fa-calendar"></i>
                                <a href-void="" href="#"> March 12, 2015 </a>
                            </li>
                            <li>
                                <i className="fa fa-comments"></i>
                                <a href-void="" href="#"> 38 Comments </a>
                            </li>
                        </ul>
                    </div>
                    <div className="col-md-8 padding-left-0">
                        <h3 className="margin-top-0"><a href-void="" href="#"> Mums favorite shopping malls in USA </a><br/><small className="font-xs"><i>Published by <a href-void="" href="#">John Doe</a></i></small></h3>
                        <p>
                            At vero eos et accusamus et iusto odio dignissimos ducimus qui blanditiis praesentium voluptatum deleniti atque corrupti quos dolores et quas molestias excepturi sint occaecati cupiditate non provident, similique sunt in culpa qui officia deserunt mollitia animi, id est laborum et dolorum fuga.
                            <br/><br/>Et harum quidem rerum facilis est et expedita distinctio lorem ipsum dolor sit amet, consectetur adipiscing elit. Ut non libero consectetur adipiscing elit magna. Sed et quam lacus. Fusce condimentum eleifend enim a feugiat. Pellentesque viverra vehicula sem ut volutpat. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Ut non libero magna. Sed et quam lacus. Fusce condimentum eleifend enim a feugiat.
                            <br/><br/>
                        </p>
                        <a className="btn btn-primary" href-void="" href="#"> Read more </a>
                    </div>
                </div>
                <hr/>
                <div className="row">
                    <div className="col-md-4">
                        <img src="rs/img/superbox/superbox-full-24.jpg" className="img-responsive" alt="rs/img"/>
                        <ul className="list-inline padding-10">
                            <li>
                                <i className="fa fa-calendar"></i>
                                <a href-void="" href="#"> March 12, 2015 </a>
                            </li>
                            <li>
                                <i className="fa fa-comments"></i>
                                <a href-void="" href="#"> 38 Comments </a>
                            </li>
                        </ul>
                    </div>
                    <div className="col-md-8 padding-left-0">
                        <h3 className="margin-top-0"><a href-void="" href="#"> Best (and Basic) Practices of Mobile Web Design </a><br/><small className="font-xs"><i>Published by <a href-void="" href="#">John Doe</a></i></small></h3>
                        <p>
                            With the plethora of smartphones, mobile phones, and tablets available on the market today, research suggests that mobile devices will soon overtake PCs and laptops in a year. More and more,different platforms are made available for all types of consumers to access the web, even including TVs and gaming consoles.
                            <br/><br/>
                            And all this in rapid-fire turnover—new models and technologies quickly coming and going like fashion trends. So much so that any website that is not mobile friendly cannot claim to be user-friendly anymore. Increasingly, web developers and designers utilize fluid layouts allowing users to browse across different platforms.
                            <br/><br/>
                        </p>
                        <a className="btn btn-primary" href-void="" href="#"> Read more </a>
                    </div>
                </div>
                <hr/>
                <div className="row">
                    <div className="col-md-4">
                        <img src="rs/img/superbox/superbox-full-7.jpg" className="img-responsive" alt="rs/img"/>
                        <ul className="list-inline padding-10">
                            <li>
                                <i className="fa fa-calendar"></i>
                                <a href-void="" href="#"> March 12, 2015 </a>
                            </li>
                            <li>
                                <i className="fa fa-comments"></i>
                                <a href-void="" href="#"> 38 Comments </a>
                            </li>
                        </ul>
                    </div>
                    <div className="col-md-8 padding-left-0">
                        <h3 className="margin-top-0"><a href-void="" href="#"> Responsive Design: Best Practices for Designing a Website </a><br/><small className="font-xs"><i>Published by <a href-void="" href="#">John Doe</a></i></small></h3>
                        <p>
                            The term Responsive design means developing a website in a way that adapts all the computer screen resolutions. Particularly this concept allows a 4 column layout that is 1292px wide, on 1025px wide screen that is divided into 2 columns automatically. It is adaptable for android phones and tablet screens. This designing method is known as “responsive web design”
                            <br/><br/>
                            Responsive designing is a different concept from traditional web designing, so the question arises how you should build a good responsive website. Here is a general practices that can help you to build a responsive website design.
                            <br/><br/>
                        </p>
                        <a className="btn btn-primary" href-void="" href="#"> Read more </a>
                    </div>
                </div>
            </div>
        </div>
        <div className="col-sm-3">
            <div className="well padding-10">
                <h5 className="margin-top-0"><i className="fa fa-search"></i> Blog Search...</h5>
                <div className="input-group">
                    <input type="text" className="form-control"/>
								<span className="input-group-btn">
									<button className="btn btn-default" type="button">
                                        <i className="fa fa-search"></i>
                                    </button> </span>
                </div>
            </div>
            <div className="well padding-10">
                <h5 className="margin-top-0"><i className="fa fa-tags"></i> Popular Tags:</h5>
                <div className="row">
                    <div className="col-lg-6">
                        <ul className="list-unstyled">
                            <li>
                                <a href=""><span className="badge badge-info">Windows 8</span></a>
                            </li>
                            <li>
                                <a href=""><span className="badge badge-info">C#</span></a>
                            </li>
                            <li>
                                <a href=""><span className="badge badge-info">Windows Forms</span></a>
                            </li>
                            <li>
                                <a href=""><span className="badge badge-info">WPF</span></a>
                            </li>
                        </ul>
                    </div>
                    <div className="col-lg-6">
                        <ul className="list-unstyled">
                            <li>
                                <a href=""><span className="badge badge-info">Bootstrap</span></a>
                            </li>
                            <li>
                                <a href=""><span className="badge badge-info">Joomla!</span></a>
                            </li>
                            <li>
                                <a href=""><span className="badge badge-info">CMS</span></a>
                            </li>
                            <li>
                                <a href=""><span className="badge badge-info">Java</span></a>
                            </li>
                        </ul>
                    </div>
                </div>
            </div>
            <div className="well padding-10">
                <h5 className="margin-top-0"><i className="fa fa-thumbs-o-up"></i> Follow Us!</h5>
                <ul className="no-padding no-margin">
                    <p className="no-margin">
                        <a title="Facebook" href=""><span className="fa-stack fa-lg"><i className="fa fa-square-o fa-stack-2x"></i><i className="fa fa-facebook fa-stack-1x"></i></span></a>
                        <a title="Twitter" href=""><span className="fa-stack fa-lg"><i className="fa fa-square-o fa-stack-2x"></i><i className="fa fa-twitter fa-stack-1x"></i></span></a>
                        <a title="Google+" href=""><span className="fa-stack fa-lg"><i className="fa fa-square-o fa-stack-2x"></i><i className="fa fa-google-plus fa-stack-1x"></i></span></a>
                        <a title="Linkedin" href=""><span className="fa-stack fa-lg"><i className="fa fa-square-o fa-stack-2x"></i><i className="fa fa-linkedin fa-stack-1x"></i></span></a>
                        <a title="GitHub" href=""><span className="fa-stack fa-lg"><i className="fa fa-square-o fa-stack-2x"></i><i className="fa fa-github fa-stack-1x"></i></span></a>
                        <a title="Bitbucket" href=""><span className="fa-stack fa-lg"><i className="fa fa-square-o fa-stack-2x"></i><i className="fa fa-bitbucket fa-stack-1x"></i></span></a>
                    </p>
                </ul>
            </div>
            <div className="well padding-10">
                <h5 className="margin-top-0"><i className="fa fa-fire"></i> Popular Posts:</h5>
                <ul className="no-padding list-unstyled">
                    <li>
                        <a href-void="" className="margin-top-0" href="#">WPF vs. Windows Forms-Which is better?</a>
                    </li>
                    <li>
                        <a href-void="" className="padding-top-5 display-block" href="#">How to create responsive website with Bootstrap?</a>
                    </li>
                    <li>
                        <a href-void="" className="margin-top-5" href="#">The best Joomla! templates 2014</a>
                    </li>
                    <li>
                        <a href-void="" className="margin-top-5" href="#">ASP .NET cms list</a>
                    </li>
                    <li>
                        <a href-void="" className="margin-top-5" href="#">C# Hello, World! program</a>
                    </li>
                    <li>
                        <a href-void="" className="margin-top-5" href="#">Java random generator</a>
                    </li>
                </ul>
            </div>
            <div className="well padding-10">
                <h5 className="margin-top-0"><i className="fa fa-video-camera"></i> Featured Videos:</h5>
                <div className="row">
                    <div className="col-lg-12">
                        <ul className="list-group no-margin">
                            <li className="list-group-item">
                                <a href=""> <span className="badge pull-right">15</span> Photograph </a>
                            </li>
                            <li className="list-group-item">
                                <a href=""> <span className="badge pull-right">30</span> Life style </a>
                            </li>
                            <li className="list-group-item">
                                <a href=""> <span className="badge pull-right">9</span> Food </a>
                            </li>
                            <li className="list-group-item">
                                <a href=""> <span className="badge pull-right">4</span> Travel world </a>
                            </li>
                        </ul>
                    </div>
                    <div className="col-lg-12">
                        <div className="margin-top-10">
                            <iframe allowfullscreen="" frameborder="0" height="210" mozallowfullscreen="" src="http://player.vimeo.com/video/87025094" webkitallowfullscreen="" width="100%"></iframe>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
        )
    }
}

export default Blog;
