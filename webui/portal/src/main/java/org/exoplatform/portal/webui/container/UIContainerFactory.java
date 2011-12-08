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

import org.exoplatform.webui.application.WebuiApplication;
import org.exoplatform.webui.application.WebuiRequestContext;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * @author <a href="hoang281283@gmail.com">Minh Hoang TO</a>
 * @date 12/6/11
 */
public abstract class UIContainerFactory
{

   public final static String DEFAULT_FACTORY = "Default";

   private static Map<String, UIContainerFactory> factories = new HashMap<String, UIContainerFactory>();

   static
   {
      factories.put(DEFAULT_FACTORY, new DefaultUIContainerFactory());

      ServiceLoader<UIContainerFactory> loader = ServiceLoader.load(UIContainerFactory.class);
      for(UIContainerFactory factory : loader)
      {
         factories.put(factory.getFactoryID(), factory);
      }
   }

   public static UIContainerFactory getFactoryInstance(String factoryID)
   {
      if(factoryID == null)
      {
         return factories.get(DEFAULT_FACTORY);
      }
      else
      {
         UIContainerFactory factory = factories.get(factoryID);
         if(factory != null)
         {
            return factory;
         }
         else
         {
            throw new RuntimeException("UIContainerFactory with factoryID = " + factoryID + " is not defined on classpath or not instantiable");
         }
      }
   }

   public abstract String getFactoryID();

   public abstract UIContainer createContainer(WebuiRequestContext requestContext) throws Exception;

   private static class DefaultUIContainerFactory extends UIContainerFactory
   {
      @Override
      public String getFactoryID()
      {
         return DEFAULT_FACTORY;
      }

      @Override
      public UIContainer createContainer(WebuiRequestContext requestContext) throws Exception
      {
         WebuiApplication app = (WebuiApplication)requestContext.getApplication();
         return app.createUIComponent(UIContainer.class, null, null, requestContext);
      }
   }
}
