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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.exoplatform.application.registry.Application;
import org.exoplatform.application.registry.ApplicationCategory;
import org.exoplatform.application.registry.ApplicationRegistryService;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;

/**
 * @author <a href="hoang281283@gmail.com">Minh Hoang TO</a>
 * @date 11/15/11
 */
@ComponentConfig(template = "system:/groovy/portal/webui/container/UIDashboardContentList.gtmpl", lifecycle = UIFormLifecycle.class)
public class UIDashboardContentList extends UIContainer
{

   private List<ApplicationCategory> categories;

   public UIDashboardContentList() throws Exception
   {
      addChild(UIAddGadgetForm.class, null, null);
   }

   public final List<ApplicationCategory> getCategories() throws Exception
   {
      if (categories != null)
      {
         return categories;
      }
      
      ApplicationRegistryService service = getApplicationComponent(ApplicationRegistryService.class);
      UserACL acl = getApplicationComponent(UserACL.class);

      List<ApplicationCategory> listCategories = new ArrayList<ApplicationCategory>();
      List<ApplicationCategory> appCateIte = service.getApplicationCategories();
      for (ApplicationCategory cate : appCateIte)
      {
         for(String p : cate.getAccessPermissions())
         {
            if(acl.hasPermission(p))
            {
               List<Application> listGadgets = cate.getApplications();
               if (listGadgets != null && listGadgets.size() > 0)
               {
                  listCategories.add(cate);
                  break;
               }               
            }
         }
      }

      Collections.sort(listCategories, new Comparator<ApplicationCategory>()
      {
         public int compare(ApplicationCategory cate1, ApplicationCategory cate2)
         {
            String ds1 = cate1.getDisplayName() == null ? "" : cate1.getDisplayName();
            String ds2 = cate2.getDisplayName() == null ? "" : cate2.getDisplayName();
            return ds1.compareToIgnoreCase(ds2);
         }
      });
      categories = listCategories;
      return categories;
   }

   public List<Application> getAppsOfCategory(final ApplicationCategory appCategory) throws Exception
   {
      UserACL acl = getApplicationComponent(UserACL.class);
      List<Application> apps = new ArrayList<Application>();
      
      List<Application> applications = appCategory.getApplications();
      for (Application app : applications)
      {
         for(String p : app.getAccessPermissions())
         {
            if(acl.hasPermission(p))
            {
               apps.add(app);
               break;
            }
         }
      }

      Collections.sort(apps, new Comparator<Application>()
      {
         public int compare(Application app1, Application app2)
         {
            String ds1 = app1.getDisplayName() == null ? "" : app2.getDisplayName();
            String ds2 = app1.getDisplayName() == null ? "" : app2.getDisplayName();
            return ds1.compareToIgnoreCase(ds2);
         }
      });
      return apps;
   }
}
