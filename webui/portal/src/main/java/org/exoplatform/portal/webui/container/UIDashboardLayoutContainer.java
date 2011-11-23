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

import java.util.List;

import org.exoplatform.application.registry.Application;
import org.exoplatform.application.registry.ApplicationRegistryService;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.TransientApplicationState;
import org.exoplatform.portal.pom.spi.gadget.Gadget;
import org.exoplatform.portal.webui.application.UIGadget;
import org.exoplatform.portal.webui.page.UIPage;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.util.PortalDataMapper;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * @author <a href="hoang281283@gmail.com">Minh Hoang TO</a>
 * @date 11/14/11
 */
@ComponentConfig(template = "system:/groovy/portal/webui/container/UIDashboardLayoutContainer.gtmpl", events = {
   @EventConfig(listeners = UIDashboardLayoutContainer.SetShowSelectContainerActionListener.class),
   @EventConfig(listeners = UIDashboardLayoutContainer.MoveGadgetActionListener.class),
   @EventConfig(listeners = UIDashboardLayoutContainer.AddNewGadgetActionListener.class)})
public class UIDashboardLayoutContainer extends UIContainer
{

   public final static String DASHBOARD_LAYOUT_CONTAINER = "DashboardLayoutContainer";

   public static String GADGET_POPUP_ID = "UIAddGadgetPopup";

   public UIDashboardLayoutContainer() throws Exception
   {
      UIPopupWindow popup = addChild(UIPopupWindow.class, null, GADGET_POPUP_ID + "-" + hashCode());
      popup.setUIComponent(createUIComponent(UIDashboardContentList.class, null, null));
   }

   @Override
   public void processRender(WebuiRequestContext context) throws Exception
   {
      UIPortalApplication uiApp = Util.getUIPortalApplication();
      if (uiApp.getModeState() != UIPortalApplication.NORMAL_MODE)
      {
         getChild(UIPopupWindow.class).setShow(false);
      }
      super.processRender(context);
   }

   public static class AddNewGadgetActionListener extends EventListener<UIDashboardLayoutContainer>
   {
      @Override
      public final void execute(final Event<UIDashboardLayoutContainer> event) throws Exception
      {
         WebuiRequestContext context = event.getRequestContext();
         UIDashboardLayoutContainer uiDashboard = event.getSource();
         if (!uiDashboard.hasPermission())
         {
            return;
         }
         String col = context.getRequestParameter("columnId");
         int position = Integer.parseInt(context.getRequestParameter("position"));
         String objectId = context.getRequestParameter(UIComponent.OBJECTID);

         ApplicationRegistryService service = uiDashboard.getApplicationComponent(ApplicationRegistryService.class);
         Application application = service.getApplication(objectId);
         if (application == null)
         {
            UIApplication uiApplication = context.getUIApplication();
            uiApplication.addMessage(new ApplicationMessage("UIDashboard.msg.ApplicationNotExisted", null));
            // context.setAttribute(UIDashboard.APP_NOT_EXIST, true);
            return;
         }
                  
         UIGadget uiGadget = event.getSource().createUIComponent(context, UIGadget.class, null, null);         
         if (application.getDisplayName() != null)
         {
            uiGadget.setTitle(application.getDisplayName());
         }
         else if (application.getApplicationName() != null)
         {
            uiGadget.setTitle(application.getApplicationName());
         }
         uiGadget.setDescription(application.getDescription());
         List<String> accessPersList = application.getAccessPermissions();
         String[] accessPers = accessPersList.toArray(new String[accessPersList.size()]);
         for (String accessPer : accessPers)
         {
            if (accessPer.equals(""))
               accessPers = null;
         }
         if (accessPers == null || accessPers.length == 0)
            accessPers = new String[]{UserACL.EVERYONE};
         uiGadget.setAccessPermissions(accessPers);
         
         uiGadget.setState(new TransientApplicationState<Gadget>(application.getApplicationName()));

         UIColumnContainer column = uiDashboard.getChildById(col);
         column.getChildren().add(position, uiGadget);
         uiGadget.setParent(column);

         // Save
         DataStorage storage = uiDashboard.getApplicationComponent(DataStorage.class);
         UIPortal uiPortal = Util.getUIPortal();
         UIPage uiPage = uiPortal.findFirstComponentOfType(UIPage.class);
         Page page = PortalDataMapper.toPageModel(uiPage);
         storage.save(page);
         
         // Synchronize model object with UIPage object, that seems  redundant but in fact
         // mandatory to have consequent edit actions (on the same page) work properly
         page = storage.getPage(page.getPageId());
         uiPage.getChildren().clear();
         PortalDataMapper.toUIPage(uiPage, page);
         
         // Update UIPage cache on UIPortal
         uiPortal.setUIPage(page.getId(), uiPage);
         uiPortal.refreshUIPage();

         uiDashboard = uiPage.findComponentById(uiDashboard.getId());
         uiDashboard.getChild(UIPopupWindow.class).setShow(true);
         context.addUIComponentToUpdateByAjax(uiDashboard);
      }
   }

   public static class MoveGadgetActionListener extends EventListener<UIDashboardLayoutContainer>
   {
      @Override
      public final void execute(final Event<UIDashboardLayoutContainer> event) throws Exception
      {
         WebuiRequestContext context = event.getRequestContext();
         UIDashboardLayoutContainer uiDashboard = event.getSource();
         if (!uiDashboard.hasPermission())
         {
            return;
         }
         String col = context.getRequestParameter("columnId");
         int position = Integer.parseInt(context.getRequestParameter("position"));
         String objectId = context.getRequestParameter(UIComponent.OBJECTID);

         UIColumnContainer newColumn = uiDashboard.getChildById(col);
         UIGadget uiGadget = uiDashboard.findComponentById(objectId);
         UIColumnContainer oldColumn = uiGadget.getParent();

         // Move
         oldColumn.removeChildById(objectId);
         newColumn.getChildren().add(position, uiGadget);
         uiGadget.setParent(newColumn);

         // Save
         DataStorage storage = uiDashboard.getApplicationComponent(DataStorage.class);
         UIPortal uiPortal = Util.getUIPortal();
         UIPage uiPage = uiPortal.findFirstComponentOfType(UIPage.class);
         storage.save(PortalDataMapper.toPageModel(uiPage));

         // if (context.getAttribute(SAVE_FAIL) != null)
         // {
         // return;
         // }
         Util.getPortalRequestContext().setResponseComplete(true);
      }
   }

   public static class SetShowSelectContainerActionListener extends EventListener<UIDashboardLayoutContainer>
   {
      public final void execute(final Event<UIDashboardLayoutContainer> event) throws Exception
      {
         UIDashboardLayoutContainer uiDashboard = (UIDashboardLayoutContainer)event.getSource();
         UIPopupWindow popupWindow = uiDashboard.getChild(UIPopupWindow.class);
         popupWindow.setShow(!popupWindow.isShow());

         WebuiRequestContext context = event.getRequestContext();
         if (popupWindow.isShow())
         {
            context.addUIComponentToUpdateByAjax(uiDashboard.getChild(UIPopupWindow.class));
         }
         else
         {
            context.setResponseComplete(true);
         }

         context.getJavascriptManager().addCustomizedOnLoadScript(
            "eXo.webui.UIDashboard.onLoad('" + uiDashboard.getId() + "'," + uiDashboard.hasPermission() + ");");
      }
   }
}
