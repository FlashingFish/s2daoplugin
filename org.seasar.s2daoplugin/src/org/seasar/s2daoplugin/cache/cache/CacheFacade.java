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
package org.seasar.s2daoplugin.cache.cache;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IType;
import org.seasar.kijimuna.core.dicon.model.IComponentElement;
import org.seasar.kijimuna.core.dicon.model.IContainerElement;
import org.seasar.s2daoplugin.cache.cache.factory.IComponentCacheFactory;
import org.seasar.s2daoplugin.cache.util.DiconUtil;
import org.seasar.s2daoplugin.util.StringUtil;

public class CacheFacade extends AbstractCache {

	private Map cacheByContainerPath = new HashMap();
	private IComponentCacheFactory factory;
	
	public CacheFacade(IComponentCacheFactory factory) {
		if (factory == null) {
			throw new IllegalArgumentException();
		}
		this.factory = factory;
	}
	
	public IComponentElement[] getComponents(IType type) {
		return type != null ?
				getComponents(type.getFullyQualifiedName()) : EMPTY_COMPONENTS;
	}

	public IComponentElement[] getComponents(String fullyQualifiedClassName) {
		if (StringUtil.isEmpty(fullyQualifiedClassName)) {
			return EMPTY_COMPONENTS;
		}
		Set result = new HashSet();
		for (Iterator it = cacheByContainerPath.values().iterator(); it.hasNext();) {
			IComponentCache cache = (IComponentCache) it.next();
			result.addAll(Arrays.asList(cache.getComponents(fullyQualifiedClassName)));
		}
		return DiconUtil.toComponentArray(result);
	}

	public IComponentElement[] getAllComponents() {
		Set result = new HashSet();
		for (Iterator it = cacheByContainerPath.values().iterator(); it.hasNext();) {
			IComponentCache cache = (IComponentCache) it.next();
			result.addAll(Arrays.asList(cache.getAllComponents()));
		}
		return DiconUtil.toComponentArray(result);
	}

	public void setContainerPath(IPath containerPath) {
		// do nothing
	}
	
	public IPath getContainerPath() {
		return null;
	}

	public IType[] getAllAppliedTypes() {
		Set result = new HashSet();
		for (Iterator it = cacheByContainerPath.values().iterator(); it.hasNext();) {
			IComponentCache cache = (IComponentCache) it.next();
			result.addAll(Arrays.asList(cache.getAllAppliedTypes()));
		}
		return (IType[]) result.toArray(new IType[result.size()]);
	}

	public boolean contains(IType type) {
		return type != null ? contains(type.getFullyQualifiedName()) : false;
	}
	
	public boolean contains(String fullyQualifiedClassName) {
		if (StringUtil.isEmpty(fullyQualifiedClassName)) {
			return false;
		}
		for (Iterator it = cacheByContainerPath.values().iterator(); it.hasNext();) {
			if (((IComponentCache) it.next()).contains(fullyQualifiedClassName)) {
				return true;
			}
		}
		return false;
	}

	public void addComponent(IComponentElement component) {
		throw new UnsupportedOperationException();
	}

	public void removeComponent(IComponentElement component) {
		throw new UnsupportedOperationException();
	}

	public void clearCache() {
		for (Iterator it = cacheByContainerPath.values().iterator(); it.hasNext();) {
			((IComponentCache) it.next()).clearCache();
		}
		cacheByContainerPath.clear();
	}

	public IComponentCache getComponentCache(IPath containerPath) {
		return (IComponentCache) cacheByContainerPath.get(containerPath);
	}
	
	public void initialize() {
		for (Iterator it = cacheByContainerPath.values().iterator(); it.hasNext();) {
			((IComponentCache) it.next()).initialize();
		}
	}
	
	public void diconAdded(IContainerElement container) {
		IPath path = container.getStorage().getFullPath();
		IComponentCache cache = factory.createComponentCache();
		// atomic begin
		cache.setManager(getManager());
		cache.setContainerPath(path);
		cache.initialize();
		cache.diconAdded(container);
		// atomic end
		cacheByContainerPath.put(path, cache);
	}

	public void diconUpdated(IContainerElement old, IContainerElement young) {
		IPath path = old.getStorage().getFullPath();
		IComponentCache cache = (IComponentCache) cacheByContainerPath.get(path);
		cache.diconUpdated(old, young);
	}

	public void diconRemoved(IContainerElement container) {
		IPath path = container.getStorage().getFullPath();
		IComponentCache cache = (IComponentCache) cacheByContainerPath.get(path);
		if (cache == null) {
			return;
		}
		cache.diconRemoved(container);
		cacheByContainerPath.remove(path);
	}

	public void finishChanged() {
		for (Iterator it = cacheByContainerPath.values().iterator(); it.hasNext();) {
			((IComponentCache) it.next()).finishChanged();
		}
	}

}
