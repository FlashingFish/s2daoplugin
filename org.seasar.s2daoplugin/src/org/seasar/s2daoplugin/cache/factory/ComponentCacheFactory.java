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
package org.seasar.s2daoplugin.cache.factory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.seasar.s2daoplugin.cache.IComponentCache;
import org.seasar.s2daoplugin.util.StringUtil;

public class ComponentCacheFactory {

	private static Map factoryMap = new HashMap();
	private static FactoryDef factoryDef = new FactoryDef();
	
	private Map componentCacheMap = new HashMap();
	
	private ComponentCacheFactory() {
	}
	
	public static ComponentCacheFactory getInstance(IProject project) {
		if (project == null) {
			return null;
		}
		if (factoryMap.containsKey(project)) {
			return (ComponentCacheFactory) factoryMap.get(project);
		} else {
			synchronized (factoryMap) {
				if (factoryMap.containsKey(project)) {
					return (ComponentCacheFactory) factoryMap.get(project);
				}
				ComponentCacheFactory factory = new ComponentCacheFactory();
				factoryMap.put(project, factory);
				return factory;
			}
		}
	}
	
	public static boolean isRegistered(String key) {
		return factoryDef.isRegistered(key);
	}
	
	public static void registerFactory(String key, IComponentCacheFactory factory) {
		factoryDef.register(key, factory);
	}
	
	public static void unregisterFactory(String key) {
		factoryDef.unregister(key);
	}
	
	public IComponentCache getComponentCache(String key) {
		if (StringUtil.isEmpty(key)) {
			return null;
		}
		if (componentCacheMap.containsKey(key)) {
			return (IComponentCache) componentCacheMap.get(key);
		}
		synchronized (componentCacheMap) {
			if (componentCacheMap.containsKey(key)) {
				return (IComponentCache) componentCacheMap.get(key);
			}
			IComponentCacheFactory factory = factoryDef.getFactory(key);
			if (factory == null) {
				return null;
			}
			IComponentCache cache = factory.createComponentCache();
			componentCacheMap.put(key, cache);
			return cache;
		}
	}
	
	public void removeComponentCache(String key) {
		componentCacheMap.remove(key);
	}
	
	
	private static class FactoryDef {
		
		private final Map factoryDefMap = new HashMap();
		
		public boolean isRegistered(String key) {
			return factoryDefMap.containsKey(key);
		}
		
		public void register(String key, IComponentCacheFactory factory) {
			if (StringUtil.isEmpty(key) || factory == null) {
				return;
			}
			factoryDefMap.put(key, factory);
		}
		
		public void unregister(String key) {
			if (factoryDefMap.remove(key) == null) {
				return;
			}
			synchronized (factoryMap) {
				for (Iterator it = factoryMap.values().iterator(); it.hasNext();) {
					ComponentCacheFactory factory = (ComponentCacheFactory) it.next();
					factory.removeComponentCache(key);
				}
			}
		}
		
		public IComponentCacheFactory getFactory(String key) {
			return (IComponentCacheFactory) factoryDefMap.get(key);
		}
	}

}
