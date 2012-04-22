/**
 * Copyright (C) 2012 eXo Platform SAS.
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

import javax.portlet.MimeResponse;
import javax.portlet.ResourceRequest;

import java.util.ArrayList;
import java.util.LinkedList;
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
import org.exoplatform.portal.webui.navigation.TreeNode;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.web.url.navigation.NavigationResource;
import org.exoplatform.web.url.navigation.NodeURL;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.ResourceServingComponent;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author <a href="mailto:phuong.vu@exoplatform.com">Vu Viet Phuong</a>
 * @version $Id$
 * 
 */
@ComponentConfig(template = "system:/groovy/webui/core/UISitemapTree.gtmpl", events = {
   @EventConfig(listeners = UISiteMapTree.CollapseNodeActionListener.class)})
public class UISiteMapTree extends UIComponent implements ResourceServingComponent
{
   private boolean useAJAX = true;

   private boolean showUserNavigation = true;

   private TreeNode treeNode_;

   private final UserNodeFilterConfig NAVIGATION_FILTER_CONFIG;

   private Scope navigationScope;

   private Log log = ExoLogger.getExoLogger(UISiteMapTree.class);

   public UISiteMapTree()
   {
      UserNodeFilterConfig.Builder filterConfigBuilder = UserNodeFilterConfig.builder();
      filterConfigBuilder.withReadWriteCheck().withVisibility(Visibility.DISPLAYED, Visibility.TEMPORAL);
      filterConfigBuilder.withTemporalCheck();
      NAVIGATION_FILTER_CONFIG = filterConfigBuilder.build();
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
   
   public void loadTreeNodes() throws Exception
   {
      treeNode_ = new TreeNode();

      UserPortal userPortal = Util.getPortalRequestContext().getUserPortalConfig().getUserPortal();
      List<UserNavigation> listNavigations = userPortal.getNavigations();

      List<UserNode> childNodes = new LinkedList<UserNode>();
      for (UserNavigation nav : rearrangeNavigations(listNavigations))
      {
         if (!showUserNavigation && nav.getKey().getType().equals(SiteType.USER))
         {
            continue;
         }
         try
         {
            UserNode rootNode = userPortal.getNode(nav, navigationScope, NAVIGATION_FILTER_CONFIG, null);
            if (rootNode != null)
            {
               childNodes.addAll(rootNode.getChildren());
            }
         }
         catch (Exception ex)
         {
            log.error(ex.getMessage(), ex);
         }
      }
      treeNode_.setChildren(childNodes);
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
               // Node has been deleted
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

   /**
    * 
    * @param listNavigation
    * @return
    */
   private List<UserNavigation> rearrangeNavigations(List<UserNavigation> listNavigation)
   {
      List<UserNavigation> returnNavs = new ArrayList<UserNavigation>();

      List<UserNavigation> portalNavs = new ArrayList<UserNavigation>();
      List<UserNavigation> groupNavs = new ArrayList<UserNavigation>();
      List<UserNavigation> userNavs = new ArrayList<UserNavigation>();

      for (UserNavigation nav : listNavigation)
      {
         SiteType siteType = nav.getKey().getType();
         switch (siteType)
         {
            case PORTAL :
               portalNavs.add(nav);
               break;
            case GROUP :
               groupNavs.add(nav);
               break;
            case USER :
               userNavs.add(nav);
               break;
         }
      }

      returnNavs.addAll(portalNavs);
      returnNavs.addAll(groupNavs);
      returnNavs.addAll(userNavs);

      return returnNavs;
   }

   public TreeNode getTreeNodes()
   {
      return treeNode_;
   }

   public void setScope(Scope scope)
   {
      this.navigationScope = scope;
   }

   @Override
   public void serveResource(WebuiRequestContext context) throws Exception
   {      
      ResourceRequest req = context.getRequest();
      String nodeID = req.getResourceID();
            
      JSONArray jsChilds = getChildrenAsJSON(nodeID);
      if (jsChilds == null)
      {
         return;
      }
      
      MimeResponse res = context.getResponse(); 
      res.setContentType("text/json");
      res.getWriter().write(jsChilds.toString());
   }

   private JSONArray getChildrenAsJSON(String nodeID) throws Exception
   {            
      WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();   
      List<TreeNode> childs = null;
      
      TreeNode tnode = getTreeNodes().findNodes(nodeID);              
      if (tnode != null) 
      {
         UserNode userNode = updateNode(tnode.getNode());
         if (userNode != null)
         {
            tnode.setExpanded(true);     
            tnode.setChildren(userNode.getChildren());          
            childs = tnode.getChildren();
         }
      }
      
      JSONArray jsChilds = new JSONArray();
      if (childs == null)
      {
         return null;
      }                  
      MimeResponse res = context.getResponse();
      for (TreeNode child : childs)
      {
         jsChilds.put(toJSON(child, res));
      }
      return jsChilds;
   }

   private JSONObject toJSON(TreeNode tnode, MimeResponse res) throws Exception
   {
      JSONObject json = new JSONObject();
      UserNode node = tnode.getNode();
      String nodeId = node.getId();
      
      json.put("label", node.getEncodedResolvedLabel());      
      json.put("hasChild", tnode.hasChild());            
      json.put("isExpanded", tnode.isExpanded());
      json.put("collapseURL", url("CollapseNode", nodeId));  
      
      PortletRequestContext pcontext = WebuiRequestContext.<PortletRequestContext>getCurrentInstance();
      String rsURL = serveResourceURL(nodeId);      
      json.put("getNodeURL", rsURL);            
      
      if (node.getPageRef() != null)
      {
         NavigationResource resource = new NavigationResource(node);
         NodeURL url = pcontext.createURL(NodeURL.TYPE, resource);
         url.setAjax(isUseAjax());
         json.put("actionLink", url.toString());
      } 
      
      JSONArray childs = new JSONArray();
      for (TreeNode child : tnode.getChildren())
      {
         childs.put(toJSON(child, res));
      }      
      json.put("childs", childs);
      return json;
   }   
   
   static public class CollapseNodeActionListener extends EventListener<UISiteMapTree>
   {
      public void execute(Event<UISiteMapTree> event) throws Exception
      {
         // get URI
         String treePath = event.getRequestContext().getRequestParameter(OBJECTID);

         UISiteMapTree uiNavigation = event.getSource();
         TreeNode rootNode = uiNavigation.getTreeNodes();

         TreeNode collapseTree = rootNode.findNodes(treePath);
         if (collapseTree != null)
         {
            collapseTree.setExpanded(false);
         }

         Util.getPortalRequestContext().setResponseComplete(true);
      }
   }
}
