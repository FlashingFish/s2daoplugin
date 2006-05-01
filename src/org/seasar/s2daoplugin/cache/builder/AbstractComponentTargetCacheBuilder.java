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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.seasar.kijimuna.core.dicon.model.IComponentElement;
import org.seasar.kijimuna.core.dicon.model.IContainerElement;
import org.seasar.s2daoplugin.cache.DiconModelManager;
import org.seasar.s2daoplugin.cache.DiconUtil;

public abstract class AbstractComponentTargetCacheBuilder extends AbstractCacheBuilder {

	private ComponentHolderChain holderChain = new ComponentHolderChain();
	private List updatedComponents = new LinkedList();
	
	public void initialize() {
		holderChain.setDiconModelManager(getManager());
		holderChain.initialize();
	}
	
	public void build(IComponentElement[] components) {
		updatedComponents.add(components);
		holderChain.build(components);
	}
	
	public void clear(IComponentElement[] components) {
		holderChain.clear(components);
		removeComponents(components);
	}
	
	public void finishBuild() {
		buildCache();
		holderChain.finishBuild();
		updatedComponents.clear();
	}
	
	protected abstract List findAdditionalComponents(IComponentElement[] components);
	
	protected void addComponentHolder(IComponentHolder holder) {
		holderChain.addComponentHolder(holder);
	}
	
	protected IComponentHolder createHolder(String[] targetClassNames) {
		IComponentHolder holder = new ComponentHolder(targetClassNames);
		addComponentHolder(holder);
		return holder;
	}
	
	private void removeComponents(IComponentElement[] components) {
		for (int i = 0; i < components.length; i++) {
			removeComponent(components[i]);
		}
	}
	
	private void buildCache() {
		if (holderChain.isEmpty()) {
			return;
		}
		List candidates = getAdditionalCandidates();
		for (int i = 0; i < candidates.size(); i++) {
			List components = findAdditionalComponents((IComponentElement[]) candidates.get(i));
			for (int j = 0; j < components.size(); j++) {
				addComponent((IComponentElement) components.get(j));
			}
		}
	}
	
	private List getAdditionalCandidates() {
		List result = new LinkedList();
		if (!holderChain.isComponentChanged()) {
			result.addAll(updatedComponents);
		} else {
			IContainerElement[] containers = getManager().getAllContainers();
			for (int i = 0; i < containers.length; i++) {
				result.add(DiconUtil.getComponents(containers[i]));
			}
		}
		return result;
	}
	
	
	protected class ComponentHolderChain implements IComponentHolder {
		
		private List holders = new ArrayList();
		
		public void addComponentHolder(IComponentHolder holder) {
			if (holder == null) {
				return;
			}
			holders.add(holder);
		}

		public void setDiconModelManager(DiconModelManager manager) {
			for (int i = 0; i < holders.size(); i++) {
				IComponentHolder holder = (IComponentHolder) holders.get(i);
				holder.setDiconModelManager(manager);
			}
		}

		public IComponentElement[] getComponents() {
			List result = new ArrayList();
			for (int i = 0; i < holders.size(); i++) {
				IComponentHolder holder = (IComponentHolder) holders.get(i);
				result.addAll(Arrays.asList(holder.getComponents()));
			}
			return (IComponentElement[]) result
					.toArray(new IComponentElement[result.size()]);
		}

		public boolean isComponentChanged() {
			for (int i = 0; i < holders.size(); i++) {
				IComponentHolder holder = (IComponentHolder) holders.get(i);
				if (holder.isComponentChanged()) {
					return true;
				}
			}
			return false;
		}

		public boolean hasComponent(IComponentElement component) {
			for (int i = 0; i < holders.size(); i++) {
				IComponentHolder holder = (IComponentHolder) holders.get(i);
				if (holder.hasComponent(component)) {
					return true;
				}
			}
			return false;
		}

		public boolean isEmpty() {
			for (int i = 0; i < holders.size(); i++) {
				IComponentHolder holder = (IComponentHolder) holders.get(i);
				if (holder.isEmpty()) {
					return true;
				}
			}
			return false;
		}
		
		public void initialize() {
			for (int i = 0; i < holders.size(); i++) {
				IComponentHolder holder = (IComponentHolder) holders.get(i);
				holder.initialize();
			}
		}

		public void build(IComponentElement[] components) {
			for (int i = 0; i < holders.size(); i++) {
				IComponentHolder holder = (IComponentHolder) holders.get(i);
				holder.build(components);
			}
		}

		public void clear(IComponentElement[] components) {
			for (int i = 0; i < holders.size(); i++) {
				IComponentHolder holder = (IComponentHolder) holders.get(i);
				holder.clear(components);
			}
		}

		public void finishBuild() {
			for (int i = 0; i < holders.size(); i++) {
				IComponentHolder holder = (IComponentHolder) holders.get(i);
				holder.finishBuild();
			}
		}
	}

}
