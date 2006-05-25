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

// ANDCache
// FIXME: AutoRegisterÇÃèáèòÇîªíËÇ∑ÇÈ
public class AutoRegisterCacheComposite extends AbstractCacheComposite {

	private List caches = new ArrayList();
	
	public IType[] getAllAppliedTypes() {
		Set result = new HashSet();
		Set temp = new HashSet();
		IComponentCache[] caches = getAllCaches();
		for (int i = 0; i < caches.length; i++) {
			if (i == 0) {
				add(result, caches[i].getAllAppliedTypes());
				continue;
			}
			IType[] types = caches[i].getAllAppliedTypes();
			for (int j = 0; j < types.length; j++) {
				if (result.contains(types[j])) {
					temp.add(types[j]);
				}
			}
			result.clear();
			result.addAll(temp);
			temp.clear();
		}
		return (IType[]) result.toArray(new IType[result.size()]);
	}
	
	public void setContainerPath(IPath containerPath) {
		// do nothing
	}

	public IPath getContainerPath() {
		return null;
	}

	public boolean contains(IType type) {
		return type != null ? contains(type.getFullyQualifiedName()) : false;
	}

	public boolean contains(String fullyQualifiedClassName) {
		if (StringUtil.isEmpty(fullyQualifiedClassName)) {
			return false;
		}
		IComponentCache[] caches = getAllCaches();
		for (int i = 0; i < caches.length; i++) {
			if (!caches[i].contains(fullyQualifiedClassName)) {
				return false;
			}
		}
		return true;
	}

	public void clearCache() {
		caches.clear();
	}

	public IComponentCache getComponentCache(IPath containerPath) {
		return null;
	}
	
	public AutoRegisterCacheComposite addAutoRegisterCache(AutoRegisterCache cache) {
		if (cache == null) {
			return this;
		}
		caches.add(cache);
		return this;
	}

	protected IComponentCache[] getAllCaches() {
		return (IComponentCache[]) caches.toArray(new IComponentCache[caches.size()]);
	}

}
