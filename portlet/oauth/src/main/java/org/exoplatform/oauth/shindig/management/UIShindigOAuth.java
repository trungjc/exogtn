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

package org.exoplatform.oauth.shindig.management;

import org.apache.shindig.gadgets.oauth.BasicOAuthStoreConsumerIndex;
import org.apache.shindig.gadgets.oauth.BasicOAuthStoreConsumerKeyAndSecret;
import org.apache.shindig.gadgets.oauth.BasicOAuthStoreConsumerKeyAndSecret.KeyType;
import org.apache.shindig.protocol.DataServiceServlet;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.gadget.core.ExoOAuthDataService;
import org.exoplatform.portal.gadget.core.ExoOAuthStore;
import org.juzu.Action;
import org.juzu.Path;
import org.juzu.Response;
import org.juzu.View;
import org.w3c.dom.Element;

import java.io.IOException;
import java.util.Map;

import javax.inject.Inject;
import javax.portlet.GenericPortlet;
import javax.portlet.MimeResponse;
import javax.portlet.PortletException;
import javax.portlet.RenderMode;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

/**
 * @author <a href="kienna@exoplatform.com">Kien Nguyen</a>
 * @version $Revision$
 */
public class UIShindigOAuth
{
   @Inject
   @Path("oauthlist.gtmpl")
   org.exoplatform.oauth.shindig.management.templates.oauthlist oauthList;
   
   @Inject
   @Path("newentry.gtmpl")
   org.exoplatform.oauth.shindig.management.templates.newentry newEntry;
   
   @Inject
   OAuthStoreEntry oauthStoreEntry;
   
   @Inject
   String message;
   
   @View
   public void index()
   {
      ExoOAuthDataService dataService =
         (ExoOAuthDataService)PortalContainer.getInstance().getComponentInstanceOfType(ExoOAuthDataService.class);
      Map<BasicOAuthStoreConsumerIndex, BasicOAuthStoreConsumerKeyAndSecret> allOSConsumers =
         dataService.getAllOAuthStoreConsumers();
      oauthList.allOSConsumers(allOSConsumers).render();
   }
   
   @View
   public void addNewEntry()
   {
      newEntry.oauthStoreEntry(oauthStoreEntry).message(message).render();
   }
   
   @Action
   public Response deleteEntry(String gadgetUri, String serviceName)
   {
      ExoOAuthDataService dataService =
         (ExoOAuthDataService)PortalContainer.getInstance().getComponentInstanceOfType(ExoOAuthDataService.class);
      dataService.removeOAuthStoreConsumer(gadgetUri, serviceName);
      return UIShindigOAuth_.index();
   }
   
   @Action
   public Response showAddNewEntry()
   {
      return UIShindigOAuth_.addNewEntry();
   }
   
   @Action
   public Response submitNewEntry(String gadgetUri, String serviceName, String consumerKey, String consumerSecret, String keyType)
   {
      if (gadgetUri == "" || serviceName == "" || consumerKey == "" || consumerSecret == "" || keyType == "")
      {
         message = "You must fill all fields";
         oauthStoreEntry = null;
         return UIShindigOAuth_.addNewEntry();
      }
      BasicOAuthStoreConsumerIndex key = new BasicOAuthStoreConsumerIndex();
      key.setGadgetUri(gadgetUri);
      key.setServiceName(serviceName);

      KeyType type = KeyType.HMAC_SYMMETRIC;
      if (keyType.equals("RSA_PRIVATE")) {
        type = KeyType.RSA_PRIVATE;
        consumerSecret.replaceAll("-----[A-Z ]*-----", "").replace("\n", "");
      }
      
      BasicOAuthStoreConsumerKeyAndSecret value =
         new BasicOAuthStoreConsumerKeyAndSecret(consumerKey, consumerSecret, type, null, null);

      ExoOAuthDataService dataService =
         (ExoOAuthDataService)PortalContainer.getInstance().getComponentInstanceOfType(ExoOAuthDataService.class);
      dataService.addOAuthStoreConsumer(key, value);
      
      return UIShindigOAuth_.index();
   }
}
