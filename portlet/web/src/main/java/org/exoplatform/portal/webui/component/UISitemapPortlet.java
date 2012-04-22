/**
 * Copyright (C) 2009 eXo Platform SAS.
 * 
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.exoplatform.portal.webui.component;

import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;

import org.exoplatform.portal.mop.navigation.GenericScope;
import org.exoplatform.portal.mop.navigation.Scope;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minhdv81@yahoo.com
 * Jul 3, 2006  
 */

@ComponentConfig(lifecycle = UIApplicationLifecycle.class, template = "system:/groovy/webui/core/UISitemap.gtmpl", events = {
   @EventConfig(listeners = UISitemapPortlet.CollapseAllNodeActionListener.class)})
public class UISitemapPortlet extends UIPortletApplication
{

   public static final int DEFAULT_LEVEL = 2;
   
   public UISitemapPortlet() throws Exception
   {

      PortletRequestContext context = (PortletRequestContext)WebuiRequestContext.getCurrentInstance();
      PortletRequest prequest = context.getRequest();
      PortletPreferences prefers = prequest.getPreferences();

      UISiteMapTree sitemapTree = addChild(UISiteMapTree.class, null, null);
      sitemapTree.setUseAjax(isUseAjax());
      
      int level = DEFAULT_LEVEL; 
      try 
      {
         level = Integer.valueOf(prefers.getValue("level", String.valueOf(DEFAULT_LEVEL)));       
      }
      catch (Exception ex) 
      {
         log.warn("Preference for navigation level can only be integer");
      }
      
      if (level <= 0)
      {
         sitemapTree.setScope(Scope.ALL);                     
      }
      else
      {
         sitemapTree.setScope(GenericScope.treeShape(level));
      }
   } 
   
   public boolean isUseAjax()
   {
      PortletRequestContext context = (PortletRequestContext)WebuiRequestContext.getCurrentInstance();
      PortletRequest prequest = context.getRequest();
      PortletPreferences prefers = prequest.getPreferences();
      return Boolean.valueOf(prefers.getValue("useAJAX", "true"));
   }
   
   static public class CollapseAllNodeActionListener extends EventListener<UISitemapPortlet>
   {
      public void execute(Event<UISitemapPortlet> event) throws Exception
      {
         UISiteMapTree sitemapTree = event.getSource().getChild(UISiteMapTree.class);
         event.getRequestContext().addUIComponentToUpdateByAjax(sitemapTree);
      }
   }
}
