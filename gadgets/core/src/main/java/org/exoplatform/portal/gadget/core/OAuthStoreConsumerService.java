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
package org.exoplatform.portal.gadget.core;

import java.util.List;

/**
 * Manage information of all consumer, configuration mappings between consumer and gadget uri. 
 * This information will be used by Shindig or applications to authorize with OAuth provider
 * 
 * @author <a href="kienna@exoplatform.com">Kien Nguyen</a>
 * @version $Revision$
 */
public interface OAuthStoreConsumerService
{
   /**
    * Get a consumer with name
    * @param keyName
    * @return consumer
    */
   public OAuthStoreConsumer getConsumer(String keyName);

   /**
    * Store consumer into storage
    * A consumer is identified by its keyName
    * @param consumer
    * @throws OAuthStoreException when keyName is duplication to another consumer
    *  @see OAuthStoreError
    */
   public void storeConsumer(OAuthStoreConsumer consumer) throws OAuthStoreException;

   /**
    * Remove consumer with name
    * when consumer is removed, all configuration that mapped to this consumer will be removed
    * @param name
    */
   public void removeConsumer(String keyName);
   
   /**
    * Get all consumers that stored in storage
    * @return list of consumers
    */
   public List<OAuthStoreConsumer> getAllConsumers();
   
   /**
    * Add new mapping configuration of a consumer and gadget uri
    * A consumer can be mapped to many gadget uris
    * @param keyName name of an existing consumer
    * @param gadgetUri
    * @throws OAuthStoreException if keyName indicates non-existing consumer or gadgetUri is not URL format standard
    * @see OAuthStoreError
    */
   public void addMappingKeyAndGadget(String keyName, String gadgetUri) throws OAuthStoreException;

   /**
    * Find a mapping configuration of a consumer and gadget uri
    * A consumer can be mapped to many gadget uris
    * @param keyName
    * @param gadgetUri
    * @return consumer or null if not found any mapping configuration
    */
   public OAuthStoreConsumer findMappingKeyAndGadget(String keyName, String gadgetUri);
   
   /**
    * Remove configuration of consumer and gadget uri
    * A consumer can be mapped to many gadget uris
    * @param keyName
    * @param gadgetUri
    */
   public void removeMappingKeyAndGadget(String keyName, String gadgetUri);
}
