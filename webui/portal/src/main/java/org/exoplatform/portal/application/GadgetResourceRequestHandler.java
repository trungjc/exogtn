/*
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
package org.exoplatform.portal.application;

import org.exoplatform.application.gadget.GadgetRegistryService;
import org.exoplatform.application.gadget.impl.GadgetDefinition;
import org.exoplatform.application.gadget.impl.GadgetRegistryServiceImpl;
import org.exoplatform.application.gadget.impl.LocalGadgetData;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.web.ControllerContext;
import org.exoplatform.web.WebRequestHandler;
import org.exoplatform.web.controller.QualifiedName;

/**
 * @author <a href="hoang281283@gmail.com">Minh Hoang TO</a>
 * @date 7/24/12
 */
public class GadgetResourceRequestHandler extends WebRequestHandler
{
   private final QualifiedName GADGET_NAME_PARAM = QualifiedName.create("gtn", "gadgetname");

   private final QualifiedName GADGET_RESOURCE_PATH_PARAM = QualifiedName.create("gtn", "gadgetresourcepath");

   private GadgetRegistryServiceImpl gadgetRegistry;

   public GadgetResourceRequestHandler(GadgetRegistryService gadgetRegistry) throws Exception
   {
      this.gadgetRegistry = (GadgetRegistryServiceImpl)gadgetRegistry;
   }

   @Override
   public String getHandlerName()
   {
      return "gadgetResource";
   }

   @Override
   public boolean execute(ControllerContext context) throws Exception
   {
      String gadgetName = context.getParameter(GADGET_NAME_PARAM);
      String resourcePath = context.getParameter(GADGET_RESOURCE_PATH_PARAM);

      if(gadgetName == null)
      {
         return false;
      }
      else
      {
         GadgetDefinition def = gadgetRegistry.getRegistry().getGadget(gadgetName);
         if(def != null && def.isLocal())
         {
            LocalGadgetData data = (LocalGadgetData)def.getData();
            String redirectURL = "/" + PortalContainer.getCurrentRestContextName() + "/" + gadgetRegistry.getJCRGadgetResourcesDir(data);
            redirectURL += (resourcePath != null) ? resourcePath : data.getFileName();

            context.getResponse().sendRedirect(redirectURL);
            return true;
         }
         return false;
      }
   }

   @Override
   protected boolean getRequiresLifeCycle()
   {
      return true;
   }
}
