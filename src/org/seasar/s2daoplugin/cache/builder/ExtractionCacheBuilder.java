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
package org.seasar.s2daoplugin.cache.builder;

import org.seasar.kijimuna.core.dicon.model.IComponentElement;
import org.seasar.s2daoplugin.cache.builder.filter.IComponentFilter;
import org.seasar.s2daoplugin.cache.builder.filter.IExtractionComponentFilter;

public class ExtractionCacheBuilder extends AbstractCacheBuilder {

	public ExtractionCacheBuilder(IExtractionComponentFilter filter) {
		super(filter);
	}
	
	public void build(IComponentElement[] components) {
		for (int i = 0; i < components.length; i++) {
			getExtractionFilter().addComponentIfNecessary(components[i]);
		}
	}
	
	public void finishBuild() {
		addComponents(getExtractionFilter().getComponents());
		getExtractionFilter().clearComponents();
	}
	
	private IExtractionComponentFilter getExtractionFilter() {
		IComponentFilter filter = getFilter();
		if (filter instanceof IExtractionComponentFilter) {
			return (IExtractionComponentFilter) filter;
		}
		throw new IllegalStateException();
	}

}
