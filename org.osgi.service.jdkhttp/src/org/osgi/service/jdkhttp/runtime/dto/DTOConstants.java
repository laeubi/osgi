/*******************************************************************************
 * Copyright (c) Contributors to the Eclipse Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0 
 *******************************************************************************/

package org.osgi.service.jdkhttp.runtime.dto;

/**
 * Defines standard constants for the DTOs.
 *
 * @author $Id$
 */
public final class DTOConstants {
	private DTOConstants() {
		// non-instantiable
	}

	/**
	 * Failure reason is unknown.
	 */
	public static final int	FAILURE_REASON_UNKNOWN						= 0;

	/**
	 * The service did not provide a valid
	 * {@link org.osgi.service.jdkhttp.whiteboard.JdkHttpWhiteboardConstants#JDK_HTTP_CONTEXT_PATH}
	 * or
	 * {@link org.osgi.service.jdkhttp.whiteboard.JdkHttpWhiteboardConstants#JDK_HTTP_FILTER_PATTERN}
	 * property.
	 */
	public static final int	FAILURE_REASON_INVALID_CONTEXT_PATH		= 1;

	/**
	 * Another service with a higher {@code service.ranking} is already
	 * registered at the same context path.
	 * <p>
	 * See {@link org.osgi.framework.ServiceReference#compareTo(Object)}.
	 */
	public static final int	FAILURE_REASON_SHADOWED_BY_OTHER_SERVICE	= 2;

	/**
	 * An exception occurred while initializing the handler, filter, or
	 * authenticator context.
	 */
	public static final int	FAILURE_REASON_EXCEPTION_ON_INIT			= 3;
}
