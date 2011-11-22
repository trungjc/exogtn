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
package org.exoplatform.portal.webui.application;

import org.exoplatform.application.registry.Application;
import org.exoplatform.portal.config.model.Properties;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.portal.UIPortalComponent;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIMaskWorkspace;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import java.util.UUID;

/**
 * Super class of UIPortlet and UIGadget
 *
 * @author <a href="hoang281283@gmail.com">Minh Hoang TO</a>
 * @date 8/25/11
 */
public abstract class UIWindow<S> extends UIPortalComponent
{

   private Properties properties;

   private boolean showInfoBar;

   private boolean showWindowState = true;

   private String description;

   private String icon;

   public static final String locationX = "locationX";

   public static final String locationY = "locationY";

   public static final String zIndex = "zIndex";

   public static final String appWidth = "appWidth";

   public static final String appHeight = "appHeight";

   public static final String appStatus = "appStatus";

   protected String storageId;

   protected String storageName;

   public UIWindow()
   {
      storageName = UUID.randomUUID().toString();
   }

   public void setStorageId(String storageId)
   {
      this.storageId = storageId;
   }

   public String getStorageId()
   {
      return storageId;
   }

   public void setStorageName(String storageName)
   {
      this.storageName = storageName;
   }

   public String getStorageName()
   {
      return storageName;
   }

   public Properties getProperties()
   {
      if (properties == null)
      { properties = new Properties(); }
      return properties;
   }

   public void setProperties(Properties properties)
   {
      this.properties = properties;
   }

   public boolean getShowWindowState()
   {
      return showWindowState;
   }

   public void setShowWindowState(Boolean b)
   {
      showWindowState = b;
   }

   public boolean getShowInfoBar()
   {
      return showInfoBar;
   }

   public void setShowInfoBar(Boolean b)
   {
      showInfoBar = b;
   }

   public String getDescription()
   {
      return description;
   }

   public void setDescription(String s)
   {
      description = s;
   }

   public String getIcon()
   {
      return icon;
   }

   public void setIcon(String s)
   {
      icon = s;
   }

   public abstract void initApplicationState(Application registryModel);

   public abstract Class<? extends UIWindowForm> getFormType();

   public boolean showCloseButton(WebuiRequestContext rcontext)
   {
      throw new UnsupportedOperationException("Please override this method");
   }

   public boolean showDragControl(WebuiRequestContext rcontext)
   {
      throw new UnsupportedOperationException("Please override this method");
   }

   public static class EditWindowActionListener extends EventListener<UIWindow>
   {
      @Override
      public void execute(Event<UIWindow> uiWindowEvent) throws Exception
      {
         UIPortal uiPortal = Util.getUIPortal();
         UIPortalApplication uiApp = uiPortal.getAncestorOfType(UIPortalApplication.class);
         UIMaskWorkspace uiMaskWS = uiApp.getChildById(UIPortalApplication.UI_MASK_WS_ID);
         UIWindow edittedWindow = uiWindowEvent.getSource();
         UIWindowForm editForm = (UIWindowForm)uiMaskWS.createUIComponent(edittedWindow.getFormType(), null, null);
         editForm.load(edittedWindow);
         uiMaskWS.setWindowSize(800, -1);
         uiWindowEvent.getRequestContext().addUIComponentToUpdateByAjax(uiMaskWS);
      }
   }
}