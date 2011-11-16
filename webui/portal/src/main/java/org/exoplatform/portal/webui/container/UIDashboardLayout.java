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

package org.exoplatform.portal.webui.container;

import org.exoplatform.portal.webui.application.UIGadget;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

@ComponentConfigs({@ComponentConfig(template = "system:/groovy/portal/webui/container/UIDashboardLayout.gtmpl", events = {
   /*@EventConfig(listeners = UIDashboardColumnContainer.MoveGadgetActionListener.class),*/
   /*@EventConfig(listeners = UIDashboardColumnContainer.AddNewGadgetActionListener.class), */
   /*@EventConfig(listeners = UIDashboardColumnContainer.DeleteGadgetActionListener.class),*/
   @EventConfig(listeners = UIDashboardLayout.MinimizeGadgetActionListener.class),
   @EventConfig(listeners = UIDashboardLayout.MaximizeGadgetActionListener.class)})})
public class UIDashboardLayout extends UIContainer
{

   public static String GADGET_POPUP_ID = "UIAddGadgetPopup";

   public static String APP_NOT_EXIST = "APP_NOT_EXIT";

   private boolean isShowSelectPopup = false;

   private String aggregatorId;

   private UIGadget maximizedGadget;

   public UIDashboardLayout() throws Exception
   {
      UIPopupWindow popup = addChild(UIPopupWindow.class, null, GADGET_POPUP_ID + "-" + hashCode());
      popup.setUIComponent(createUIComponent(UIDashboardContentList.class, null, null));
   }

   @Override
   public void processRender(WebuiRequestContext context) throws Exception
   {
      UIGadget uiGadget = this.getMaximizedGadget();
      if (uiGadget != null)
      {
         if (context.getAttribute(APP_NOT_EXIST) != null ||
                  context.getAttribute(UIGadget.SAVE_PREF_FAIL) != null)
         {
            this.setMaximizedGadget(null);
         }
      }

      super.processRender(context);
   }

   public void setColumns(int num) throws Exception
   {
      //getChild(UIDashboardColumnContainer.class).setColumns(num);
   }

   public void setContainerTemplate(String template)
   {
      //getChild(UIDashboardColumnContainer.class).setContainerTemplate(template);
   }

   //TODO: Remove this method
   public boolean canEdit()
   {
      return true;
   }

   public boolean isShowSelectPopup()
   {
      return isShowSelectPopup;
   }

   public void setShowSelectPopup(final boolean value)
   {
      this.isShowSelectPopup = value;
      getChild(UIPopupWindow.class).setShow(value);
   }

   public String getAggregatorId()
   {
      return aggregatorId;
   }

   public void setAggregatorId(String aggregatorId)
   {
      this.aggregatorId = aggregatorId;
   }

   public UIGadget getMaximizedGadget()
   {
      return maximizedGadget;
   }

   public void setMaximizedGadget(UIGadget gadget)
   {
      maximizedGadget = gadget;
   }

   public static class MinimizeGadgetActionListener extends EventListener<UIDashboardLayout>
   {
      public final void execute(final Event<UIDashboardLayout> event) throws Exception
      {
         /*
         WebuiRequestContext context = event.getRequestContext();
         UIDashboardLayout uiDashboard = event.getSource();
         String objectId = context.getRequestParameter(OBJECTID);
         String minimized = context.getRequestParameter("minimized");

         UIDashboardColumnContainer uiDashboardCont = uiDashboard.getChild(UIDashboardColumnContainer.class);
         UIGadget uiGadget = uiDashboard.getChild(UIDashboardColumnContainer.class).getUIGadget(objectId);
         if (uiGadget.isLossData())
         {
            UIPortalApplication uiApp = Util.getUIPortalApplication();
            uiApp.addMessage(new ApplicationMessage("UIDashboard.msg.ApplicationNotExisted", null));
            context.setAttribute(APP_NOT_EXIST, true);
            context.addUIComponentToUpdateByAjax(uiDashboard);
         }
         else
         {
            uiGadget.getProperties().setProperty("minimized", minimized);
            uiDashboardCont.save();
            if (context.getAttribute(UIDashboardColumnContainer.SAVE_FAIL) != null)
            {
               return;
            }
            Util.getPortalRequestContext().setResponseComplete(true);
         }
         */
      }
   }

   public static class MaximizeGadgetActionListener extends EventListener<UIDashboardLayout>
   {
      public final void execute(final Event<UIDashboardLayout> event) throws Exception
      {
         /*
         WebuiRequestContext context = event.getRequestContext();
         UIDashboardLayout uiDashboard = event.getSource();
         String objectId = context.getRequestParameter(OBJECTID);
         String maximize = context.getRequestParameter("maximize");
         UIDashboardColumnContainer uiDashboardCont = uiDashboard.getChild(UIDashboardColumnContainer.class);
         UIGadget uiGadget = uiDashboardCont.getUIGadget(objectId);
         if (uiGadget == null || uiGadget.isLossData())
         {
            UIPortalApplication uiApp = Util.getUIPortalApplication();
            uiApp.addMessage(new ApplicationMessage("UIDashboard.msg.ApplicationNotExisted", null));
            context.setAttribute(APP_NOT_EXIST, true);
            context.addUIComponentToUpdateByAjax(uiDashboard);
            return;
         }

         // TODO nguyenanhkien2a@gmail.comá
         // We need to expand unminimized state of uiGadget to view all body of
         // gadget, not just a title with no content
         uiGadget.getProperties().setProperty("minimized", "false");
         uiDashboardCont.save();

         if (maximize.equals("maximize")
            && context.getAttribute(UIDashboardColumnContainer.SAVE_FAIL) == null)
         {
            uiGadget.setView(UIGadget.CANVAS_VIEW);
            uiDashboard.setMaximizedGadget(uiGadget);
         }
         else
         {
            uiGadget.setView(UIGadget.HOME_VIEW);
            uiDashboard.setMaximizedGadget(null);
         }
         */
      }
   }
}
