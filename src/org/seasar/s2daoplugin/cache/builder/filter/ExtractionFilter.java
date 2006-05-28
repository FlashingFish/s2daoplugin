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

import java.util.HashSet;
import java.util.Set;

import org.seasar.kijimuna.core.dicon.model.IComponentElement;
import org.seasar.s2daoplugin.cache.builder.IComponentFilter;

public class ExtractionFilter extends AbstractComponentFilter implements
		IExtractionComponentFilter {

	private IComponentFilter filter;
	private Set extractedComponents = new HashSet();
	
	public ExtractionFilter(IComponentFilter filter) {
		if (filter == null) {
			throw new IllegalArgumentException();
		}
		this.filter = filter;
	}
	
	public boolean isPassable(IComponentElement component) {
		return filter.isPassable(component);
	}
	
	public boolean addComponentIfNecessary(IComponentElement component) {
		if (isPassable(component)) {
			extractedComponents.add(component);
			return true;
		}
		return false;
	}
	
	public void clearComponents() {
		extractedComponents.clear();
	}
	
	public IComponentElement[] getComponents() {
		return (IComponentElement[]) extractedComponents
				.toArray(new IComponentElement[extractedComponents.size()]);
	}
	
	protected void onManagerSet() {
		filter.setManager(getManager());
	}

}
