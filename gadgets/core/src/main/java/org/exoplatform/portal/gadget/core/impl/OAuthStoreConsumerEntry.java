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
package org.exoplatform.portal.gadget.core.impl;

import org.chromattic.api.annotations.PrimaryType;
import org.chromattic.api.annotations.Property;
import org.exoplatform.portal.gadget.core.OAuthStoreConsumer;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="kienna@exoplatform.com">Kien Nguyen</a>
 * @version $Revision$
 */
@PrimaryType(name = "ost:consumer")
public abstract class OAuthStoreConsumerEntry
{
   @Property(name = "keyName")
   public abstract String getKeyName();
   
   public abstract void setKeyName(String keyName);
   
   @Property(name = "consumerKey")
   public abstract String getConsumerKey();
   
   public abstract void setConsumerKey(String consumerKey);
   
   @Property(name = "consumerSecret")
   public abstract String getConsumerSecret();
   
   public abstract void setConsumerSecret(String consumerSecret);
   
   @Property(name = "keyType")
   public abstract String getKeyType();
   
   public abstract void setKeyType(String keyType);
   
   @Property(name = "callbackUrl")
   public abstract String getCallbackUrl();
   
   public abstract void setCallbackUrl(String callbackUrl);
   
   @Property(name = "gadgetUris")
   public abstract List<String> getGadgetUris();
   
   public abstract void setGadgetUris(List<String> gadgetUris);
   
   public void addGadgetUri(String gadgetUri)
   {
      List<String> uris = getGadgetUris();
      if (uris == null)
      {
         uris = new ArrayList<String>();
      }
      uris.add(gadgetUri);
      setGadgetUris(uris);
   }
   
   public void removeGadgetUri(String gadgetUri)
   {
      if (getGadgetUris() != null)
      {
         List<String> uris = getGadgetUris();
         uris.remove(gadgetUri);
         setGadgetUris(uris);
      }
   }
   
   public OAuthStoreConsumer toOAuthStoreConsumer()
   {
      return new OAuthStoreConsumer(this.getKeyName(), this.getConsumerKey(), this.getConsumerSecret(), this
         .getKeyType(), this.getCallbackUrl(), this.getGadgetUris());
   }

}
