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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IType;
import org.seasar.kijimuna.core.dicon.model.IComponentElement;
import org.seasar.s2daoplugin.cache.builder.AutoRegisterUtil;
import org.seasar.s2daoplugin.cache.model.IAutoRegisterElement;

public class AutoRegisterCacheComposite extends AbstractCacheComposite {

	private List componentAutoCaches = new ArrayList();
	private List componentTargetAutoCaches = new ArrayList();
	
	public IComponentElement[] getComponents(IType type, IPath containerPath) {
		if (type == null || containerPath == null) {
			return CacheConstants.EMPTY_COMPONENTS;
		}
		Set result = new HashSet();
		for (int i = 0; i < componentAutoCaches.size(); i++) {
			IComponentCache cache = (IComponentCache) componentAutoCaches.get(i);
			IComponentElement[] components = cache.getComponents(type, containerPath);
			if (components.length == 0) {
				continue;
			}
			IComponentElement[] componentTargets =
				getComponentTargetsByContainer(type, containerPath);
			if (componentTargets.length == 0) {
				continue;
			}
			result.addAll(Arrays.asList(components));
			result.addAll(Arrays.asList(componentTargets));
		}
		return (IComponentElement[]) result.toArray(new IComponentElement[result.size()]);
	}
	
	public IType[] getAllAppliedTypes() {
		Set result = new HashSet();
		for (int i = 0; i < componentAutoCaches.size(); i++) {
			IComponentCache cache = (IComponentCache) componentAutoCaches.get(i);
			IComponentElement[] components = cache.getAllComponents();
			for (int j = 0; j < components.length; j++) {
				List appliedTypes = AutoRegisterUtil.getAppliedTypes(
						(IAutoRegisterElement) components[j]);
				for (int k = 0; k < appliedTypes.size(); k++) {
					IPath path = components[j].getStorage().getFullPath();
					if (contains((IType) appliedTypes.get(k), path)) {
						result.add(appliedTypes.get(k));
					}
				}
			}
		}
		return (IType[]) result.toArray(new IType[result.size()]);
	}
	
	public boolean contains(IType type) {
		if (type == null) {
			return false;
		}
		for (int i = 0; i < componentAutoCaches.size(); i++) {
			IComponentCache cache = (IComponentCache) componentAutoCaches.get(i);
			IPath[] paths = cache.getAllContainerPaths();
			if (containsByContainers(type, paths, cache)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean contains(IType type, IPath containerPath) {
		if (containerPath == null || type == null) {
			return false;
		}
		for (int i = 0; i < componentAutoCaches.size(); i++) {
			IComponentCache cache = (IComponentCache) componentAutoCaches.get(i);
			if (containsByContainers(type, new IPath[] {containerPath}, cache)) {
				return true;
			}
		}
		return false;
	}
	
	public AutoRegisterCacheComposite addComponentAutoRegisterCache(
			AutoRegisterCache cache) {
		if (cache == null) {
			return this;
		}
		componentAutoCaches.add(cache);
		return this;
	}
	
	public AutoRegisterCacheComposite addComponentTargetAutoRegisterCache(
			AutoRegisterCache cache) {
		if (cache == null) {
			return this;
		}
		componentTargetAutoCaches.add(cache);
		return this;
	}
	
	protected IComponentCache[] getAllCaches() {
		List caches = new ArrayList(componentAutoCaches.size() +
				componentTargetAutoCaches.size());
		caches.addAll(componentAutoCaches);
		caches.addAll(componentTargetAutoCaches);
		return (IComponentCache[]) caches.toArray(new IComponentCache[caches.size()]);
	}
	
	private boolean containsByContainers(IType type, IPath[] paths, IComponentCache cache) {
		for (int i = 0; i < paths.length; i++) {
			if (!cache.contains(type, paths[i])) {
				continue;
			}
			int lowest = getLowestComponentLineNumber(cache.getComponents(type, paths[i]));
			if (containsComponentTargetAuto(type, paths[i], lowest)) {
				return true;
			}
		}
		return false;
	}
	
	private boolean containsComponentTargetAuto(IType type, IPath containerPath, int lowest) {
		for (int i = 0; i < componentTargetAutoCaches.size(); i++) {
			IComponentCache cache = (IComponentCache) componentTargetAutoCaches.get(i);
			if (!cache.contains(type, containerPath)) {
				return false;
			}
			if (lowest > getLowestComponentLineNumber(
					cache.getComponents(type, containerPath))) {
				return false;
			}
		}
		return true;
	}
	
	private int getLowestComponentLineNumber(IComponentElement[] components) {
		int lowest = Integer.MAX_VALUE;
		for (int i = 0; i < components.length; i++) {
			int start = components[i].getStartLine();
			lowest = lowest > start ? start : lowest;
		}
		return lowest;
	}
	
	private IComponentElement[] getComponentTargetsByContainer(IType type,
			IPath containerPath) {
		Set result = new HashSet();
		for (int i = 0; i < componentTargetAutoCaches.size(); i++) {
			IComponentCache cache = (IComponentCache) componentTargetAutoCaches.get(i);
			IComponentElement[] components = cache.getComponents(type, containerPath);
			if (components.length == 0) {
				return CacheConstants.EMPTY_COMPONENTS;
			}
			result.addAll(Arrays.asList(components));
		}
		return (IComponentElement[]) result.toArray(new IComponentElement[result.size()]);
	}

}
