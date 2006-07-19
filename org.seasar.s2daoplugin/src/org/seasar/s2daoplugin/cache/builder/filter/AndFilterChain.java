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
package org.seasar.s2daoplugin.cache.builder.filter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.seasar.kijimuna.core.dicon.model.IComponentElement;
import org.seasar.s2daoplugin.cache.DiconModelManager;

public class AndFilterChain implements IExtractionComponentFilter {

	private Set filters = new HashSet();
	
	public void setManager(DiconModelManager manager) {
		for (Iterator it = filters.iterator(); it.hasNext();) {
			((IComponentFilter) it.next()).setManager(manager);
		}
	}

	public boolean isPassable(IComponentElement component) {
		for (Iterator it = filters.iterator(); it.hasNext();) {
			if (!((IComponentFilter) it.next()).isPassable(component)) {
				return false;
			}
		}
		return true;
	}
	
	public boolean addComponentIfNecessary(IComponentElement component) {
		boolean result = false;
		for (Iterator it = filters.iterator(); it.hasNext();) {
			IComponentFilter filter = (IComponentFilter) it.next();
			if (filter instanceof IExtractionComponentFilter) {
				((IExtractionComponentFilter) filter).addComponentIfNecessary(component);
				result = true;
			}
		}
		return result;
	}
	
	public void clearComponents() {
		for (Iterator it = filters.iterator(); it.hasNext();) {
			IComponentFilter filter = (IComponentFilter) it.next();
			if (filter instanceof IExtractionComponentFilter) {
				((IExtractionComponentFilter) filter).clearComponents();
			}
		}
	}
	
	public IComponentElement[] getComponents() {
		Set result = new HashSet();
		for (Iterator it = filters.iterator(); it.hasNext();) {
			IComponentFilter filter = (IComponentFilter) it.next();
			if (filter instanceof IExtractionComponentFilter) {
				IComponentElement[] comps = ((IExtractionComponentFilter) filter).getComponents();
				if (comps.length == 0) {
					return new IComponentElement[0];
				}
				result.addAll(Arrays.asList(comps));
			}
		}
		return (IComponentElement[]) result.toArray(new IComponentElement[result.size()]);
	}
	
	public AndFilterChain addFilter(IComponentFilter filter) {
		if (filter != null) {
			filters.add(filter);
		}
		return this;
	}

}
