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
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.IType;
import org.seasar.kijimuna.core.dicon.model.IComponentElement;
import org.seasar.s2daoplugin.cache.builder.ICacheBuilder;
import org.seasar.s2daoplugin.cache.model.IAutoRegisterElement;

public class AutoRegisterCache extends AbstractComponentCache {

	private Set autoRegisters = new HashSet();
	
	public AutoRegisterCache(ICacheBuilder builder) {
		super(builder);
	}

	public IComponentElement[] getComponents(IType type) {
		if (type == null) {
			return CacheConstants.EMPTY_COMPONENTS;
		}
		List components = new ArrayList();
		for (Iterator it = autoRegisters.iterator(); it.hasNext();) {
			IAutoRegisterElement ar = (IAutoRegisterElement) it.next();
			if (isApplied(ar, type)) {
				components.add(ar);
			}
		}
		return (IComponentElement[]) components
				.toArray(new IComponentElement[components.size()]);
	}

	public IComponentElement[] getAllComponents() {
		return (IComponentElement[]) autoRegisters
				.toArray(new IComponentElement[autoRegisters.size()]);
	}

	public boolean contains(IType type) {
		if (type == null) {
			return false;
		}
		for (Iterator it = autoRegisters.iterator(); it.hasNext();) {
			IAutoRegisterElement ar = (IAutoRegisterElement) it.next();
			if (isApplied(ar, type)) {
				return true;
			}
		}
		return false;
	}
	
	public void addComponent(IComponentElement component) {
		if (!(component instanceof IAutoRegisterElement)) {
			return;
		}
		autoRegisters.add(component);
	}

	public void removeComponent(IComponentElement component) {
		autoRegisters.remove(component);
	}

	public void clearCache() {
		autoRegisters.clear();
	}
	
	private boolean isApplied(IAutoRegisterElement autoRegister, IType type) {
		return autoRegister.isApplied(
				type.getPackageFragment().getElementName(),
				type.getElementName());
	}

}
