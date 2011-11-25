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
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.model.ApplicationType;
import org.exoplatform.portal.config.model.Container;
import org.exoplatform.portal.webui.application.UIGadget;
import org.exoplatform.portal.webui.application.UIPortlet;
import org.exoplatform.portal.webui.application.UIWindow;
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
   @EventConfig(listeners = UIDashboardLayoutContainer.MoveWindowActionListener.class),
   @EventConfig(listeners = UIDashboardLayoutContainer.AddNewWindowActionListener.class)})
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

   public static class AddNewWindowActionListener extends EventListener<UIDashboardLayoutContainer>
   {
      @Override
      public final void execute(final Event<UIDashboardLayoutContainer> event) throws Exception
      {
         PortalRequestContext context = (PortalRequestContext)event.getRequestContext();
         UIDashboardLayoutContainer uiDashboard = event.getSource();
         if (!uiDashboard.hasPermission())
         {
            return;
         }
         
         context.ignoreAJAXUpdateOnPortlets(true);

         String col = context.getRequestParameter("columnId");
         int position = Integer.parseInt(context.getRequestParameter("position"));
         String objectId = context.getRequestParameter(UIComponent.OBJECTID);

         ApplicationRegistryService service = uiDashboard.getApplicationComponent(ApplicationRegistryService.class);
         Application application = service.getApplication(objectId);
         if (application == null)
         {
            UIApplication uiApplication = context.getUIApplication();
            uiApplication.addMessage(new ApplicationMessage("UIDashboard.msg.ApplicationNotExisted", null));
            context.addUIComponentToUpdateByAjax(uiDashboard);            
            return;
         }
                  
         Class<? extends UIWindow> clazz = UIPortlet.class;
         if(application.getType() == ApplicationType.GADGET)
         {
            clazz = UIGadget.class;
         }
         UIWindow uiWindow = event.getSource().createUIComponent(clazz, null, null);
         toUIWindow(uiWindow, application);

         boolean staleData = false;
         UIColumnContainer column = uiDashboard.getChildById(col);
         if (column == null || column.getChildren().size() < position)
         {
            staleData = true;
         } 
         else 
         {
            column.getChildren().add(position, uiWindow);
            uiWindow.setParent(column);
            
            // Save
            DataStorage storage = uiDashboard.getApplicationComponent(DataStorage.class);
            Container updatedColumn = null;
            try
            {
               updatedColumn = storage.saveContainer((Container)PortalDataMapper.buildModelObject(column));
            }
            catch (Exception ex)
            {
               staleData = true;
            }

            if (updatedColumn != null)
            {
               column.getChildren().clear();
               PortalDataMapper.toUIContainer(column, updatedColumn);
            }
         }

         if (staleData)
         {
            context.getUIApplication().addMessage(
               new ApplicationMessage("UIDashboard.msg.StaleData", null, ApplicationMessage.ERROR));
            context.addUIComponentToUpdateByAjax(uiDashboard);
         }
         else
         {                                                               
            context.addUIComponentToUpdateByAjax(column);         
            context.getJavascriptManager().addCustomizedOnLoadScript("eXo.webui.UIDashboard.onLoad('" + 
                              uiDashboard.getId() + "', " + uiDashboard.hasPermission() + ");");
         }
      }

      private void toUIWindow(UIWindow uiWindow, Application application)
      {         
         uiWindow.setShowInfoBar(true);
         
         if (application.getDisplayName() != null)
         {
            uiWindow.setTitle(application.getDisplayName());
         }
         else if (application.getApplicationName() != null)
         {
            uiWindow.setTitle(application.getApplicationName());
         }
         uiWindow.setDescription(application.getDescription());
         List<String> accessPersList = application.getAccessPermissions();
         String[] accessPers = accessPersList.toArray(new String[accessPersList.size()]);
         for (String accessPer : accessPers)
         {
            if (accessPer.equals(""))
               accessPers = null;
         }
         if (accessPers == null || accessPers.length == 0)
            accessPers = new String[]{UserACL.EVERYONE};
         uiWindow.setAccessPermissions(accessPers);
         
         uiWindow.initApplicationState(application);
      }
   }

   public static class MoveWindowActionListener extends EventListener<UIDashboardLayoutContainer>
   {
      @Override
      public final void execute(final Event<UIDashboardLayoutContainer> event) throws Exception
      {
         PortalRequestContext context = (PortalRequestContext)event.getRequestContext();
         UIDashboardLayoutContainer uiDashboard = event.getSource();
         if (!uiDashboard.hasPermission())
         {
            return;
         }
         String col = context.getRequestParameter("columnId");
         int position = Integer.parseInt(context.getRequestParameter("position"));
         String objectId = context.getRequestParameter(UIComponent.OBJECTID);

         boolean staleData = false;

         UIColumnContainer newColumn = uiDashboard.getChildById(col);
         UIWindow uiwindow = uiDashboard.findComponentById(objectId);
         if (uiwindow == null || newColumn == null)
         {
            staleData = true;
         } 
         else
         {
            UIColumnContainer oldColumn = uiwindow.getParent();
            
            // Move
            oldColumn.removeChildById(objectId);
            newColumn.getChildren().add(position, uiwindow);
            uiwindow.setParent(newColumn);
            
            // Save
            DataStorage storage = uiDashboard.getApplicationComponent(DataStorage.class);
            UIPortal uiPortal = Util.getUIPortal();
            UIPage uiPage = uiPortal.findFirstComponentOfType(UIPage.class);
            
            try
            {
               storage.save(PortalDataMapper.toPageModel(uiPage));            
            } 
            catch (Exception ex)
            {
              staleData = true;  
            }            
         }
            
         if (staleData)
         {
            context.getUIApplication().addMessage(
               new ApplicationMessage("UIDashboard.msg.StaleData", null, ApplicationMessage.ERROR));
            context.ignoreAJAXUpdateOnPortlets(true);
            context.addUIComponentToUpdateByAjax(uiDashboard);
         }
         else
         {
            context.setResponseComplete(true);            
         }
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
