/*
 * Copyright (C) 2011 eXo Platform SAS.
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
package org.exoplatform.portal.webui.container;

import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import java.util.List;

/**
 * @author <a href="hoang281283@gmail.com">Minh Hoang TO</a>
 * @date 11/15/11
 */
@ComponentConfig(
   template = "system:/groovy/portal/webui/container/UIDashboardColumnContainer.gtmpl",
events={@EventConfig(listeners = UIDashboardColumnContainer.SetShowSelectContainerActionListener.class)})
public class UIDashboardColumnContainer extends UIContainer
{
   public UIDashboardColumnContainer() throws Exception
   {
      super();
   }

   public int getNumberOfDashboardColumns()
   {
      int count = 0;
      List<UIComponent> children = this.getChildren();
      for(UIComponent child : children)
      {
         if(child instanceof UIDashboardColumn)
         {
            count++;
         }
      }
      return Math.max(count, 1);
   }

   public String getDashboardContainerID()
   {
      UIDashboardLayout db = getParent();
      UIDashboardLayoutContainer dbContainer = db.getParent();

      return dbContainer.getId();
   }

   public boolean hasUIGadget()
   {
      return true;
   }

   public static class SetShowSelectContainerActionListener extends EventListener<UIDashboardColumnContainer>
   {
      public final void execute(final Event<UIDashboardColumnContainer> event) throws Exception
      {
         UIDashboardColumnContainer dbColumnContainer = event.getSource();
         UIDashboardLayout uiDashboard = dbColumnContainer.getParent();
         if (!uiDashboard.canEdit())
         {
            return;
         }
         PortalRequestContext pcontext = (PortalRequestContext)event.getRequestContext();
         //boolean isShow = Boolean.parseBoolean(pcontext.getRequestParameter("isShow"));
         //uiDashboard.setShowSelectPopup(isShow);
         //String windowId = uiDashboard.getChild(UIDashboardColumnContainer.class).getWindowId();
         //UIDashboardLayoutContainer window = uiDashboard.getParent();
         //String windowId = window.getId();
         uiDashboard.setShowSelectPopup(true);
         pcontext.addUIComponentToUpdateByAjax(uiDashboard);
         pcontext.ignoreAJAXUpdateOnPortlets(true);
         /*
         if (isShow)
         {
            event.getRequestContext().getJavascriptManager().addCustomizedOnLoadScript(
               "eXo.webui.UIDashboardLayout.onLoad('" + windowId + "'," + uiDashboard.canEdit() + ");");
         }
         */
      }
   }

}
