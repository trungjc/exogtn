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
package org.exoplatform.commons.chromattic;

import org.exoplatform.commons.utils.Safe;

import javax.jcr.Credentials;

/**
 * @author <a href="trongtt@gmail.com">Trong Tran</a>
 * @version $Revision$
 */
public class SessionContextKey
{
   private String domainName;

   private Credentials credentials;

   public SessionContextKey(String domainName, Credentials credentials)
   {
      this.domainName = domainName;
      this.credentials = credentials;
   }

   @Override
   public int hashCode()
   {
      return (credentials == null) ? domainName.hashCode() : domainName.hashCode() ^ credentials.hashCode();
   }

   @Override
   public boolean equals(Object obj)
   {
      if (obj == null || !(obj instanceof SessionContextKey))
      {
         return false;
      }

      SessionContextKey temp = (SessionContextKey)obj;
      return (Safe.equals(domainName, temp.domainName) && Safe.equals(credentials, temp.credentials));
   }
}
