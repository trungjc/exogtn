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

import org.exoplatform.portal.webui.application.UIGadget;
import org.exoplatform.portal.webui.application.UIPortlet;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIComponent;
import java.util.Random;

/**
 * @author <a href="hoang281283@gmail.com">Minh Hoang TO</a>
 * @date 11/14/11
 */
@ComponentConfig(template = "system:/groovy/portal/webui/container/UIDashboardLayoutContainer.gtmpl")
public class UIDashboardLayoutContainer extends UIContainer
{

   public final static String DASHBOARD_LAYOUT_CONTAINER = "DashboardLayoutContainer";

   public UIDashboardLayoutContainer() throws Exception
   {
      super();
   }

   public boolean canEdit()
   {
      return true;
   }

   @Override
   public void addChild(UIComponent uicomponent)
   {
      if(uicomponent instanceof UIPortlet || uicomponent instanceof UIGadget)
      {
         UIDashboardLayout dbLayout = getChild(UIDashboardLayout.class);
         UIDashboardColumnContainer columnContainer = dbLayout.getChild(UIDashboardColumnContainer.class);
         int index = new Random().nextInt(columnContainer.getChildren().size());

         UIDashboardColumn dashboardColumn = columnContainer.getChild(index);
         dashboardColumn.addChild(uicomponent);
      }
      else if(uicomponent instanceof UIDashboardLayout)
      {
         super.addChild(uicomponent);
      }
      else
      {
         //do nothing
      }
   }
}
