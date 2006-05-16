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

public class CacheComposite extends AbstractCacheComposite {

	private List caches = new ArrayList();
	
	public IType[] getAllAppliedTypes() {
		Set result = new HashSet();
		IComponentCache[] caches = getAllCaches();
		for (int i = 0; i < caches.length; i++) {
			result.addAll(Arrays.asList(caches[i].getAllAppliedTypes()));
		}
		return (IType[]) result.toArray(new IType[result.size()]);
	}
	
	public boolean contains(IType type) {
		IComponentCache[] caches = getAllCaches();
		for (int i = 0; i < caches.length; i++) {
			if (caches[i].contains(type)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean contains(IType type, IPath containerPath) {
		IComponentCache[] caches = getAllCaches();
		for (int i = 0; i < caches.length; i++) {
			if (caches[i].contains(type, containerPath)) {
				return true;
			}
		}
		return false;
	}
	
	public CacheComposite addComponentCache(IComponentCache cache) {
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
