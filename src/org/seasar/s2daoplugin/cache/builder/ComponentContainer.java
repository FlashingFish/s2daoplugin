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

import java.util.LinkedList;
import java.util.List;

import org.seasar.kijimuna.core.dicon.model.IComponentElement;
import org.seasar.s2daoplugin.cache.DiconUtil;
import org.seasar.s2daoplugin.cache.model.ComponentElementWrapper;

public class ComponentContainer implements IComponentContainer {
	
	private ComponentDeployerFactory factory = new ComponentDeployerFactory(this);
	private RegistryBuilder builder;
	private ComponentQueue queue = new ComponentQueue();
	
	public ComponentContainer(RegistryBuilder builder) {
		this.builder = builder;
	}
	
	public void deploy(IComponentElement[] components) {
		prepare(components);
		while (!queue.isEmpty()) {
			IComponentElement component = queue.poll();
			IComponentDeployer deployer = factory.createComponentDeployer(component);
			deployer.deploy();
		}
	}
	
	public void addPreparedComponent(IComponentElement component) {
		queue.offer(component);
	}
	
	public IComponentElement[] getPreparedComponents() {
		return queue.toArray();
	}
	
	public void addComponent(IComponentElement component) {
		builder.addComponent(component);
	}
	
	private void prepare(IComponentElement[] components) {
		for (int i = 0; i < components.length; i++) {
			addPreparedComponent(new ComponentElementWrapper(components[i]));
		}
	}
	
	
	private static class ComponentQueue {
		
		private List queue = new LinkedList();
		
		public void offer(IComponentElement component) {
			if (component != null) {
				queue.add(component);
			}
		}
		
		public IComponentElement poll() {
			return !isEmpty() ? (IComponentElement) queue.remove(0) : null;
		}
		
		public boolean isEmpty() {
			return queue.isEmpty();
		}
		
		public IComponentElement[] toArray() {
			return DiconUtil.toComponentArray(queue);
		}
	}

}
