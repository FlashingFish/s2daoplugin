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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.seasar.kijimuna.core.dicon.model.IComponentElement;
import org.seasar.kijimuna.core.dicon.model.IContainerElement;
import org.seasar.s2daoplugin.cache.DiconUtil;
import org.seasar.s2daoplugin.cache.IVirtualDiconModelRegistry;

public class RegistryBuilder {

	private static final Comparator comparator = new LineNumberComparator();
	
	private IVirtualDiconModelRegistry registry;
	private IComponentContainer cc = new ComponentContainer(this);
	
	public void setRegistry(IVirtualDiconModelRegistry registry) {
		this.registry = registry;
	}
	
	public void initialize() {
	}

	public void build(IContainerElement container) {
		List components = container.getComponentList();
		Collections.sort(components, comparator);
		cc.deploy(DiconUtil.toComponentArray(components));
	}

	public void clear(IContainerElement container) {
		registry.removeComponent(container);
	}

	public void finishBuild() {
	}
	
	public void addComponent(IComponentElement component) {
		registry.addComponent(component);
	}


	private static class LineNumberComparator implements Comparator {

		public int compare(Object o1, Object o2) {
			if (o1 instanceof IComponentElement &&
					o2 instanceof IComponentElement) {
				return doCompare((IComponentElement) o1, (IComponentElement) o2);
			}
			throw new ClassCastException();
		}
		
		private int doCompare(IComponentElement c1, IComponentElement c2) {
			if (c1.getStartLine() == c2.getStartLine()) {
				return 0;
			}
			return c1.getStartLine() < c2.getStartLine() ? -1 : 1;
		}
		
	}

}
