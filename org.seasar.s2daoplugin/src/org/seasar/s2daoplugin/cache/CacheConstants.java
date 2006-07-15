/*
 * Copyright 2004-2006 the Seasar Foundation and the Others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.seasar.s2daoplugin.cache;

public interface CacheConstants {

	String INTERCEPTOR_CHAIN = "org.seasar.framework.aop.interceptors.InterceptorChain";
	String COMPONENT_AUTO_REGISTER = "org.seasar.framework.container.autoregister.ComponentAutoRegister";
	String FILESYSTEM_COMPONENT_AUTO_REGISTER = "org.seasar.framework.container.autoregister.FileSystemComponentAutoRegister";
	String JAR_COMPONENT_AUTO_REGISTER = "org.seasar.framework.container.autoregister.JarComponentAutoRegister";
	String[] COMPONENT_AUTO_REGISTERS = new String[] {
			COMPONENT_AUTO_REGISTER,
			FILESYSTEM_COMPONENT_AUTO_REGISTER,
			JAR_COMPONENT_AUTO_REGISTER,
	};
	String ASPECT_AUTO_REGISTER = "org.seasar.framework.container.autoregister.AspectAutoRegister";
	String INTERFACE_ASPECT_AUTO_REGISTER = "org.seasar.framework.container.autoregister.InterfaceAspectAutoRegister";
	String[] ASPECT_AUTO_REGISTERS = new String[] {
			ASPECT_AUTO_REGISTER,
			INTERFACE_ASPECT_AUTO_REGISTER,
	};

	String ID_CACHE_NATURE = "org.seasar.s2daoplugin.cachenature";
	String ID_CACHE_BUILDER = "org.seasar.s2daoplugin.cachebuilder";

}
