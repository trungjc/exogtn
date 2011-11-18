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

import org.exoplatform.application.registry.Application;
import org.exoplatform.application.registry.ApplicationRegistryService;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.model.ApplicationState;
import org.exoplatform.portal.config.model.ApplicationType;
import org.exoplatform.portal.config.model.Container;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.TransientApplicationState;
import org.exoplatform.portal.pom.spi.gadget.Gadget;
import org.exoplatform.portal.webui.application.PortletState;
import org.exoplatform.portal.webui.application.UIGadget;
import org.exoplatform.portal.webui.application.UIPortlet;
import org.exoplatform.portal.webui.page.UIPage;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.util.PortalDataMapper;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import java.util.List;
import java.util.Random;

/**
 * @author <a href="hoang281283@gmail.com">Minh Hoang TO</a>
 * @date 11/15/11
 */
@ComponentConfig(template = "system:/groovy/portal/webui/container/UIDashboardContentList.gtmpl",
events = {@EventConfig(listeners = UIDashboardContentList.AddContentActionListener.class)})
public class UIDashboardContentList extends org.exoplatform.webui.core.UIContainer
{

   //TODO: Don't cache application list like this
   private List<Application> applications;

   public UIDashboardContentList() throws Exception
   {
      super();

      ApplicationRegistryService appRegistry = getApplicationComponent(ApplicationRegistryService.class);
      applications = appRegistry.getAllApplications();
   }

   private Application getApplicationByName(String name)
   {
      for(Application app : applications)
      {
         if(name.equals(app.getApplicationName()))
         {
            return app;
         }
      }
      return null;
   }

   public static class AddContentActionListener extends EventListener<UIDashboardContentList>
   {
      @Override
      public void execute(Event<UIDashboardContentList> uiContentListEvent) throws Exception
      {
         UIDashboardContentList contentList = uiContentListEvent.getSource();
         PortalRequestContext prContext = (PortalRequestContext)uiContentListEvent.getRequestContext();

         String appName = prContext.getRequestParameter(UIComponent.OBJECTID);
         Application app = contentList.getApplicationByName(appName);

         if (app == null)
         {
            System.out.println("Application not found");
            return;
         }


         UIDashboardLayout dashboardLayout = contentList.getAncestorOfType(UIDashboardLayout.class);
         UIDashboardColumnContainer columnContainer = dashboardLayout.getChild(UIDashboardColumnContainer.class);

         int numberOfColumns = columnContainer.getChildren().size();
         int randomIndex = new Random().nextInt(numberOfColumns);

         UIDashboardColumn dbColumn = columnContainer.getChild(randomIndex);
         UIComponent appWindow = buildAppWindow(dbColumn, app, appName);
         dbColumn.addChild(appWindow);

         DataStorage dataStorage = dashboardLayout.getApplicationComponent(DataStorage.class);
         try
         {
            Container updatedModel = dataStorage.save((Container)PortalDataMapper.buildModelObject(dbColumn));
            dbColumn.getChildren().clear();
            PortalDataMapper.toUIContainer(dbColumn, updatedModel);
         }
         catch (Exception ex)
         {
            //TODO: Delete the appWindow
         }
      }

      private UIComponent buildAppWindow(UIDashboardColumn dbColumn, Application app, String appName) throws Exception
      {
         if (app.getType() == ApplicationType.PORTLET || app.getType() == ApplicationType.WSRP_PORTLET)
         {
            UIPortlet uiPortlet = dbColumn.createUIComponent(UIPortlet.class, null, null);
            uiPortlet.setDescription(app.getDescription());
            List<String> accessPersList = app.getAccessPermissions();
            String[] accessPers = accessPersList.toArray(new String[accessPersList.size()]);
            for (String accessPer : accessPers)
            {
               if (accessPer.equals(""))
               { accessPers = null; }
            }
            if (accessPers == null || accessPers.length == 0)
            { accessPers = new String[]{UserACL.EVERYONE}; }
            uiPortlet.setAccessPermissions(accessPers);

            ApplicationState state = new TransientApplicationState<Object>(app.getContentId());
            uiPortlet.setState(new PortletState(state, app.getType()));

            return uiPortlet;
         }
         else if (app.getType() == ApplicationType.GADGET)
         {
            UIGadget uiGadget = dbColumn.createUIComponent(UIGadget.class, null, null);
            uiGadget.setState(new TransientApplicationState<Gadget>(appName));

            return uiGadget;
         }
         else
         {
            throw new IllegalArgumentException("We supports only portlet or gadget");
         }
      }
   }
}
