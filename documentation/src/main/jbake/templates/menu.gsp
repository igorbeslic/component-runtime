<%/*
  Copyright (C) 2006-2018 Talend Inc. - www.talend.com
   Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/%>
<div class="navbar navbar-inverse navbar-fixed-top">
  <div class="container">
    <div class="navbar-header">
      <a href="${config.jbake_site_rootpath}/index.html" class="navbar-brand">Component Kit</a>
      <button class="navbar-toggle" type="button" data-toggle="collapse" data-target="#navbar-main">
        <span class="icon-bar"></span>
        <span class="icon-bar"></span>
        <span class="icon-bar"></span>
      </button>
    </div>
    <div class="navbar-collapse collapse" id="navbar-main">
      <ul class="nav navbar-nav">
        <li class="dropdown">
          <a class="dropdown-toggle" data-toggle="dropdown" href="#" id="entries">Documentation <span class="caret"></span></a>
          <ul class="dropdown-menu" aria-labelledby="entries">
            <li><a href="${config.jbake_site_rootpath}/index.html">Home</a></li>
            <li><a href="documentation-overview.html">Overview</a></li>
            <li><a href="getting-started.html">Getting Started</a></li>
            <li><a href="documentation.html">Reference</a></li>
            <li><a href="best-practices.html">Best Practices</a></li>
            <li><a href="documentation-testing.html">Testing</a></li>
            <li><a href="documentation-rest.html">REST API</a></li>
            <li><a href="wrapping-a-beam-io.html">How to wrap a Beam I/O</a></li>
            <li><a href="studio.html">Studio Integration</a></li>
            <li><a href="changelog.html">Changelog</a></li>
            <li><a href="contributors.html">Wall of Fame</a></li>
            <li><a href="apidocs.html">API Documentation</a></li>
            <li><a href="appendix.html">Appendix</a></li>
          </ul>
        </li>
        <li class="dropdown">
          <a class="dropdown-toggle" data-toggle="dropdown" href="#" id="versions">Versions <span class="caret"></span></a>
          <ul class="dropdown-menu" aria-labelledby="versions">
            <li><a href="${config.jbake_site_rootpath}/index.html">Current</a></li>
            <li><a href="${config.jbake_site_rootpath}/latest/index.html">Latest</a></li>
            <!-- VERSIONS -->
          </ul>
        </li>
      </ul>
      <div class="navbar-collapse collapse">
        <form class="navbar-form navbar-right" role="search" action="search.html">
          <div class="form-group">
            <input type="text" class="form-control" placeholder="Search" name="q">
            </div>
          </div>
        </form>
      </div>
    </div>
  </div>
</div>