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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IType;
import org.seasar.s2daoplugin.util.StringUtil;

public class CacheComposite extends AbstractCacheComposite {

	private List caches = new ArrayList();
	private IPath containerPath;
	
	public IType[] getAllAppliedTypes() {
		Set result = new HashSet();
		IComponentCache[] caches = getAllCaches();
		for (int i = 0; i < caches.length; i++) {
			add(result, caches[i].getAllAppliedTypes());
		}
		return (IType[]) result.toArray(new IType[result.size()]);
	}
	
	public void setContainerPath(IPath containerPath) {
		this.containerPath = containerPath;
	}
	
	public IPath getContainerPath() {
		return containerPath;
	}
	
	public boolean contains(IType type) {
		return type != null ? contains(type.getFullyQualifiedName()) : false;
	}

	public boolean contains(String fullyQualifiedClassName) {
		if (StringUtil.isEmpty(fullyQualifiedClassName)) {
			return false;
		}
		for (int i = 0; i < caches.size(); i++) {
			if (((IComponentCache) caches.get(i)).contains(fullyQualifiedClassName)) {
				return true;
			}
		}
		return false;
	}

	public void clearCache() {
		for (int i = 0; i < caches.size(); i++) {
			((IComponentCache) caches.get(i)).clearCache();
		}
		caches.clear();
	}

	public IComponentCache getComponentCache(IPath containerPath) {
		return this.containerPath.equals(containerPath) ? this : null; 
	}

	protected IComponentCache[] getAllCaches() {
		return (IComponentCache[]) caches.toArray(new IComponentCache[caches.size()]);
	}

	public CacheComposite addComponentCache(IComponentCache cache) {
		if (cache == null) {
			return this;
		}
		caches.add(cache);
		return this;
	}

}
