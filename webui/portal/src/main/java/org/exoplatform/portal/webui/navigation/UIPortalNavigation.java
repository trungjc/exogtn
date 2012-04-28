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

package org.exoplatform.portal.webui.navigation;

import javax.portlet.MimeResponse;
import javax.portlet.ResourceRequest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.portal.mop.Visibility;
import org.exoplatform.portal.mop.navigation.NodeChange;
import org.exoplatform.portal.mop.navigation.NodeChangeQueue;
import org.exoplatform.portal.mop.navigation.Scope;
import org.exoplatform.portal.mop.user.UserNavigation;
import org.exoplatform.portal.mop.user.UserNode;
import org.exoplatform.portal.mop.user.UserNodeFilterConfig;
import org.exoplatform.portal.mop.user.UserPortal;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.web.url.navigation.NavigationResource;
import org.exoplatform.web.url.navigation.NodeURL;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.core.ResourceServingComponent;
import org.exoplatform.webui.core.UIComponent;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by The eXo Platform SARL Author : Dang Van Minh minhdv81@yahoo.com
 * Jul 12, 2006
 */
public class UIPortalNavigation extends UIComponent implements ResourceServingComponent
{
   private boolean useAJAX = true;

   private boolean showUserNavigation = true;

   private String cssClassName = "";

   private String template;

   private final UserNodeFilterConfig NAVIGATION_FILTER_CONFIG;
   
   private Scope navigationScope;
   
   private Log log = ExoLogger.getExoLogger(UIPortalNavigation.class);

   public UIPortalNavigation()
   {
      UserNodeFilterConfig.Builder filterConfigBuilder = UserNodeFilterConfig.builder();
      filterConfigBuilder.withReadWriteCheck().withVisibility(Visibility.DISPLAYED, Visibility.TEMPORAL);
      filterConfigBuilder.withTemporalCheck();
      NAVIGATION_FILTER_CONFIG = filterConfigBuilder.build();
   }

   @Override
   public String getTemplate()
   {
      return template != null ? template : super.getTemplate();
   }

   public void setTemplate(String template)
   {
      this.template = template;
   }

   public UIComponent getViewModeUIComponent()
   {
      return null;
   }

   public void setUseAjax(boolean bl)
   {
      useAJAX = bl;
   }

   public boolean isUseAjax()
   {
      return useAJAX;
   }

   public boolean isShowUserNavigation()
   {
      return showUserNavigation;
   }

   public void setShowUserNavigation(boolean showUserNavigation)
   {
      this.showUserNavigation = showUserNavigation;
   }

   public void setCssClassName(String cssClassName)
   {
      this.cssClassName = cssClassName;
   }

   public String getCssClassName()
   {
      return cssClassName;
   }

   public List<UserNode> getNavigations() throws Exception
   {
      WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
      List<UserNode> nodes = new ArrayList<UserNode>();
      if (context.getRemoteUser() != null)
      {
         UserNode currRootNode = getCurrentNavigation();
         if (currRootNode != null)
         {
            nodes.add(currRootNode);  
         }
      }
      else
      {
         UserPortal userPortal = Util.getPortalRequestContext().getUserPortalConfig().getUserPortal();
         List<UserNavigation> navigations = userPortal.getNavigations();
         for (UserNavigation userNav : navigations)
         {
            if (!showUserNavigation && userNav.getKey().getType().equals(SiteType.USER))
            {
               continue;
            }

            UserNode rootNode = userPortal.getNode(userNav, navigationScope, NAVIGATION_FILTER_CONFIG, null);
            if (rootNode != null)
            {
               nodes.add(rootNode);
            }
         }
      }
      return nodes;
   }

   public UserNode resolvePath(String path) throws Exception
   {
      WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
      UserPortal userPortal = Util.getPortalRequestContext().getUserPortalConfig().getUserPortal();
      
      UserNode node;
      if (context.getRemoteUser() != null)
      {
         node = userPortal.resolvePath(Util.getUIPortal().getUserNavigation(), NAVIGATION_FILTER_CONFIG, path);
      }
      else
      {
         node = userPortal.resolvePath(NAVIGATION_FILTER_CONFIG, path);
      }
      
      if (node != null && !node.getURI().equals(path))
      {
         //Node has been deleted
         return null;
      }
      return updateNode(node);
   }
   
   public UserNode updateNode(UserNode node) throws Exception
   {
      if (node == null)
      {
         return null;
      }
      UserPortal userPortal = Util.getPortalRequestContext().getUserPortalConfig().getUserPortal();
      NodeChangeQueue<UserNode> queue = new NodeChangeQueue<UserNode>();
      userPortal.updateNode(node, navigationScope, queue);
      for (NodeChange<UserNode> change : queue)
      {
         if (change instanceof NodeChange.Removed)
         {
            UserNode deletedNode = ((NodeChange.Removed<UserNode>)change).getTarget();
            if (hasRelationship(deletedNode, node))
            {
               //Node has been deleted
               return null;
            }
         }
      }
      return node;      
   }
      
   private boolean hasRelationship(UserNode parent, UserNode userNode)
   {
      if (parent.getId().equals(userNode.getId()))
      {
         return true;
      }
      for (UserNode child : parent.getChildren())
      {
         if (hasRelationship(child, userNode))
         {
            return true;
         }
      }
      return false;
   }
   
   public UserNode getSelectedNode() throws Exception
   {
      UIPortal uiPortal = Util.getUIPortal();
      if (uiPortal != null)
      {
         return uiPortal.getSelectedUserNode();
      }
      return null;
   }

   private UserNode getCurrentNavigation() throws Exception
   {
      UserPortal userPortal = Util.getPortalRequestContext().getUserPortalConfig().getUserPortal();
      UserNavigation userNavigation = Util.getUIPortal().getUserNavigation();
      try 
      {
         UserNode rootNode = userPortal.getNode(userNavigation, navigationScope, NAVIGATION_FILTER_CONFIG, null);      
         return rootNode;
      } 
      catch (Exception ex)
      {
         log.error("Navigation has been deleted");
      }
      return null;
   }

   @Override
   public void serveResource(WebuiRequestContext context) throws Exception
   {      
      ResourceRequest req = context.getRequest();
      String nodeURI = req.getResourceID();
            
      JSONArray jsChilds = getChildrenAsJSON(nodeURI);
      if (jsChilds == null)
      {
         return;
      }
      
      MimeResponse res = context.getResponse(); 
      res.setContentType("text/json");
      res.getWriter().write(jsChilds.toString());
   }      
   

   public JSONArray getChildrenAsJSON(String nodeURI) throws Exception
   {
      WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();          
      
      Collection<UserNode> childs = null;      
      UserNode userNode = resolvePath(nodeURI);
      if (userNode != null)
      {
         childs = userNode.getChildren();
      }
      
      JSONArray jsChilds = new JSONArray();
      if (childs == null)
      {
         return null;
      }                  
      MimeResponse res = context.getResponse();
      for (UserNode child : childs)
      {
         jsChilds.put(toJSON(child, res));
      }
      return jsChilds;
   }

   private JSONObject toJSON(UserNode node, MimeResponse res) throws Exception
   {
      JSONObject json = new JSONObject();
      String nodeId = node.getId();

      json.put("label", node.getEncodedResolvedLabel());
      json.put("hasChild", node.getChildrenCount() > 0);

      UserNode selectedNode = Util.getUIPortal().getNavPath();
      json.put("isSelected", nodeId.equals(selectedNode.getId()));
      json.put("icon", node.getIcon());

      String resourceURL = resourceURL(node.getURI());
      json.put("getNodeURL", resourceURL);

      if (node.getPageRef() != null)
      {
         NavigationResource resource = new NavigationResource(node);
         NodeURL url = Util.getPortalRequestContext().createURL(NodeURL.TYPE, resource);
         url.setAjax(isUseAjax());
         json.put("actionLink", url.toString());
      }

      JSONArray childs = new JSONArray();
      for (UserNode child : node.getChildren())
      {
         childs.put(toJSON(child, res));
      }
      json.put("childs", childs);
      return json;
   }

   public void setScope(Scope scope)
   {
      this.navigationScope = scope;
   }   
}
