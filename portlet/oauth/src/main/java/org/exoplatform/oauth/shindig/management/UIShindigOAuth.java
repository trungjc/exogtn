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

import org.apache.shindig.protocol.DataServiceServlet;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.gadget.core.OAuthStoreConsumer;
import org.exoplatform.portal.gadget.core.OAuthStoreConsumerService;
import org.exoplatform.portal.gadget.core.ExoOAuthStore;
import org.juzu.Action;
import org.juzu.Path;
import org.juzu.Response;
import org.juzu.View;
import org.juzu.impl.compiler.BaseProcessor;
import org.juzu.impl.utils.Logger;
import org.w3c.dom.Element;

import java.io.IOException;
import java.util.List;
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
   private final Logger log = BaseProcessor.getLogger(UIShindigOAuth.class);
   
   @Inject
   @Path("oauthlist.gtmpl")
   org.exoplatform.oauth.shindig.management.templates.oauthlist oauthList;
   
   @Inject
   @Path("newconsumer.gtmpl")
   org.exoplatform.oauth.shindig.management.templates.newconsumer newConsumer;
   
   @Inject
   @Path("newmapping.gtmpl")
   org.exoplatform.oauth.shindig.management.templates.newmapping newMapping;
   
   @Inject
   @Path("consumerdetail.gtmpl")
   org.exoplatform.oauth.shindig.management.templates.consumerdetail consumerDetail;
   
   @Inject
   Session session;
   
   @Inject
   String message;
   
   @View
   public void index()
   {
      OAuthStoreConsumerService store =
         (OAuthStoreConsumerService)PortalContainer.getInstance().getComponentInstanceOfType(OAuthStoreConsumerService.class);
      List<OAuthStoreConsumer> allConsumers = store.getAllConsumers();
      oauthList.allConsumers(allConsumers).render();
   }
   
   @View
   public void addNewConsumer()
   {
      newConsumer.session(session).message(message).render();
   }
   
   @View
   public void addMapping()
   {
      OAuthStoreConsumerService store =
         (OAuthStoreConsumerService)PortalContainer.getInstance().getComponentInstanceOfType(OAuthStoreConsumerService.class);
      List<OAuthStoreConsumer> allConsumers = store.getAllConsumers();
      newMapping.allConsumers(allConsumers).render();
   }
   
   @View
   public void consumerDetail()
   {
      consumerDetail.consumer(session.getConsumer()).render();
   }
   
   @Action
   public Response deleteConsumer(String keyName)
   {
      OAuthStoreConsumerService dataService =
         (OAuthStoreConsumerService)PortalContainer.getInstance().getComponentInstanceOfType(OAuthStoreConsumerService.class);
      dataService.removeConsumer(keyName);
      return UIShindigOAuth_.index();
   }
   
   @Action
   public Response showAddNewConsumer()
   {
      return UIShindigOAuth_.addNewConsumer();
   }
   
   @Action
   public Response showAddMapping()
   {
      return UIShindigOAuth_.addMapping();
   }
   
   @Action
   public Response showConsumerDetail(String keyName)
   {
      OAuthStoreConsumerService dataService =
         (OAuthStoreConsumerService)PortalContainer.getInstance().getComponentInstanceOfType(OAuthStoreConsumerService.class);
      session.setConsumer(dataService.getConsumer(keyName));
      return UIShindigOAuth_.consumerDetail();
   }
   
   @Action
   public Response deleteMapping(String keyName, String gadgetUri)
   {
      OAuthStoreConsumerService dataService =
         (OAuthStoreConsumerService)PortalContainer.getInstance().getComponentInstanceOfType(OAuthStoreConsumerService.class);
      dataService.removeMappingKeyAndGadget(keyName, gadgetUri);
      session.setConsumer(dataService.getConsumer(keyName));
      return UIShindigOAuth_.consumerDetail();
   }
   
   @Action
   public Response submitNewConsumer(String keyName, String consumerKey, String consumerSecret, String keyType)
   {
      if (keyName == "" || consumerKey == "" || consumerSecret == "" || keyType == "")
      {
         message = "You must fill all fields";
         session = null;
         return UIShindigOAuth_.addNewConsumer();
      }

      if (keyType.equals("RSA_PRIVATE")) {
        consumerSecret.replaceAll("-----[A-Z ]*-----", "").replace("\n", "");
      }
      
      OAuthStoreConsumerService dataService =
         (OAuthStoreConsumerService)PortalContainer.getInstance().getComponentInstanceOfType(OAuthStoreConsumerService.class);
      OAuthStoreConsumer consumer = new OAuthStoreConsumer(keyName, consumerKey, consumerSecret, keyType, null);
      try
      {
         dataService.storeConsumer(consumer);
      }
      catch (Exception e)
      {
         log.log("Can not store consumer with key name: " + consumer.getKeyName() + e.getMessage());
      }
      
      return UIShindigOAuth_.index();
   }
   
   @Action
   public Response submitNewMapping(String gadgetUri, String keyName)
   {
      if (gadgetUri == "" || keyName == "")
      {
         message = "Gadget Uri is not null";
         return UIShindigOAuth_.addMapping();
      }
      
      //TODO: check if mapping is added before, show message
      
      OAuthStoreConsumerService store =
         (OAuthStoreConsumerService)PortalContainer.getInstance().getComponentInstanceOfType(OAuthStoreConsumerService.class);
      try
      {
         store.addMappingKeyAndGadget(keyName, gadgetUri);
      }
      catch (Exception e)
      {
         log.log("Can not add map key:" + keyName + " and gadget uri:" + gadgetUri + e.getMessage());
      }
      return UIShindigOAuth_.index();
   }
}
