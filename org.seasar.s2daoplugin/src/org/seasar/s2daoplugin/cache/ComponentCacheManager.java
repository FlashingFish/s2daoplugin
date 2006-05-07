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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.seasar.s2daoplugin.util.StringUtil;

public class ComponentCacheManager {

	private static Map managerMap = new HashMap();
	private Map componentCacheMap = new HashMap();
	
	public static ComponentCacheManager getInstance(IProject project) {
		if (project == null) {
			return null;
		}
		if (managerMap.containsKey(project)) {
			return (ComponentCacheManager) managerMap.get(project);
		} else {
			synchronized (managerMap) {
				if (managerMap.containsKey(project)) {
					return (ComponentCacheManager) managerMap.get(project);
				}
				ComponentCacheManager manager = new ComponentCacheManager();
				managerMap.put(project, manager);
				return manager;
			}
		}
	}
	
	public void addComponentCache(String key, IComponentCache cache) {
		if (StringUtil.isEmpty(key) || cache == null) {
			return;
		}
		componentCacheMap.put(key, cache);
	}
	
	public IComponentCache getComponentCache(String key) {
		return (IComponentCache) componentCacheMap.get(key);
	}
	
	public void removeComponentCache(String key) {
		componentCacheMap.remove(key);
	}

}
