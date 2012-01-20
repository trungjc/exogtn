package org.exoplatform.portal.gadget.core;

import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.portal.AbstractPortalTest;

import java.util.List;

/**
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

/**
 * @author <a href="kienna@exoplatform.com">Kien Nguyen</a>
 * @version $Revision$
 */
@ConfiguredBy({
   @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.test.jcr-configuration.xml"),
   @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.identity-configuration.xml"),
   @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.portal-configuration.xml"),
   @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.gadget-core.test-configuration.xml")
})
public class TestOAuthStoreConsumerService extends AbstractPortalTest
{
   private OAuthStoreConsumerService service;
   
   @Override
   protected void setUp() throws Exception
   {
      super.setUp();
      
      service = (OAuthStoreConsumerService) getContainer().getComponentInstanceOfType(OAuthStoreConsumerService.class);

      begin();
   }
   
   @Override
   protected void tearDown() throws Exception
   {
      end();
      super.tearDown();
   }
   
   public void testStoreConsumer() throws Exception
   {
      OAuthStoreConsumer c =
         new OAuthStoreConsumer("testKey", "testKey", "testKey123456", "HMAC_SYMMETRIC",
            "http://test.com/eXogadgetServer/oauth/callback");
      service.storeConsumer(c);
      OAuthStoreConsumer c1 = service.getConsumer("testKey");
      
      assertNotNull(c1);
      assertEquals("testKey", c1.getKeyName());
      assertEquals("testKey", c1.getConsumerKey());
      assertEquals("testKey123456", c1.getConsumerSecret());
      assertEquals("HMAC_SYMMETRIC", c1.getKeyType());
      assertEquals("http://test.com/eXogadgetServer/oauth/callback", c1.getCallbackUrl());
      
      service.removeConsumer("testKey");
      assertNull(service.getConsumer("testKey"));
   }
   
   public void testDuplicatedConsumer() throws Exception
   {
      OAuthStoreConsumer c =
         new OAuthStoreConsumer("testKey", "testKey", "testKey123456", "HMAC_SYMMETRIC",
            "http://test.com/eXogadgetServer/oauth/callback");
      service.storeConsumer(c);
      OAuthStoreConsumer c1 = service.getConsumer("testKey");
      
      assertNotNull(c1);
      assertEquals("testKey", c1.getKeyName());
      assertEquals("testKey", c1.getConsumerKey());
      assertEquals("testKey123456", c1.getConsumerSecret());
      assertEquals("HMAC_SYMMETRIC", c1.getKeyType());
      assertEquals("http://test.com/eXogadgetServer/oauth/callback", c1.getCallbackUrl());
      
      try
      {
         service.storeConsumer(c);
      }
      catch (OAuthStoreException e)
      {
         assertEquals(OAuthStoreError.DUPLICATION_DATA, e.getError());
      }
      
      try
      {
         service.storeConsumer(c);
      }
      catch (OAuthStoreException e)
      {
         assertEquals(OAuthStoreError.DUPLICATION_DATA, e.getError());
      }
          
      service.removeConsumer("testKey");
      assertNull(service.getConsumer("testKey"));
   }
   
   public void testGetAllConsumers() throws Exception
   {
      OAuthStoreConsumer c =
         new OAuthStoreConsumer("testKey", "testKey", "testKey123456", "HMAC_SYMMETRIC",
            "http://test.com/eXogadgetServer/oauth/callback");
      OAuthStoreConsumer c1 =
         new OAuthStoreConsumer("testKey1", "testKey1", "testKey123456", "HMAC_SYMMETRIC",
            "http://test.com/eXogadgetServer/oauth/callback1");
      OAuthStoreConsumer c2 =
         new OAuthStoreConsumer("testKey2", "testKey2", "testKey123456", "HMAC_SYMMETRIC",
            "http://test.com/eXogadgetServer/oauth/callback2");
      service.storeConsumer(c);
      service.storeConsumer(c1);
      service.storeConsumer(c2);
      
      List<OAuthStoreConsumer> consumers = service.getAllConsumers();
      assertEquals(3, consumers.size());
      
      service.removeConsumer(c.getKeyName());
      service.removeConsumer(c1.getKeyName());
      service.removeConsumer(c2.getKeyName());
      consumers = service.getAllConsumers();
      assertEquals(0, consumers.size());
   }
   
   public void testAddMappingKeyAndGadget()
   {
      OAuthStoreConsumer c =
         new OAuthStoreConsumer("testKey", "testKey", "testKey123456", "HMAC_SYMMETRIC",
            "http://test.com/eXogadgetServer/oauth/callback");
      service.storeConsumer(c);
      OAuthStoreConsumer c1 = service.getConsumer("testKey");
      
      assertNotNull(c1);
      assertEquals("testKey", c1.getKeyName());
      assertEquals("testKey", c1.getConsumerKey());
      assertEquals("testKey123456", c1.getConsumerSecret());
      assertEquals("HMAC_SYMMETRIC", c1.getKeyType());
      assertEquals("http://test.com/eXogadgetServer/oauth/callback", c1.getCallbackUrl());
      
      service.addMappingKeyAndGadget("testKey", "http://test.com/teset.xml");
      
      OAuthStoreConsumer c2 = service.findMappingKeyAndGadget("testKey", "http://test.com/teset.xml");
      assertNotNull(c2);
      assertEquals("testKey", c2.getKeyName());
      assertEquals(true, c2.getGadgetUris().contains("http://test.com/teset.xml"));
      
      service.removeConsumer("testKey");
      assertNull(service.getConsumer("testKey"));
   }
}
