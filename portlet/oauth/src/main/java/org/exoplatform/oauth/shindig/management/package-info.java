@Application
@Assets(
   scripts = {
      @Script(src = "js/jquery-1.7.1.min.js"),
      @Script(src = "js/jquery-ui-1.7.2.custom.min.js"),
      @Script(src = "js/oauthstore.js")
   },
   stylesheets = {
      @Stylesheet(src = "skin/stylesheet.css")
   }
)
package org.exoplatform.oauth.shindig.management;

import org.juzu.Application;
import org.juzu.plugin.asset.Assets;
import org.juzu.plugin.asset.Script;
import org.juzu.plugin.asset.Stylesheet;