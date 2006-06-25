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
package org.seasar.s2daoplugin.cache.deployment;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.seasar.kijimuna.core.dicon.model.IComponentElement;
import org.seasar.kijimuna.core.dicon.model.IContainerElement;
import org.seasar.s2daoplugin.cache.deployment.deployer.ComponentDeployerFactory;
import org.seasar.s2daoplugin.cache.deployment.deployer.IComponentDeployer;
import org.seasar.s2daoplugin.cache.deployment.model.ComponentElementWrapper;
import org.seasar.s2daoplugin.cache.deployment.model.ContainerElementWrapper;
import org.seasar.s2daoplugin.cache.util.DiconUtil;

public class DeploymentContainer implements IDeploymentContainer {

	private static final Comparator comparator = new LineNumberComparator();
	
	private ComponentDeployerFactory factory = new ComponentDeployerFactory(this);
	private DeploymentBuilder builder;
	private ComponentQueue queue = new ComponentQueue();
	private List deployedComponents = new LinkedList();
	
	public DeploymentContainer(DeploymentBuilder builder) {
		this.builder = builder;
	}
	
	public void deploy(IContainerElement container) {
		prepare(container);
		while (!queue.isEmpty()) {
			IComponentElement component = queue.poll();
			IComponentDeployer deployer = factory.createComponentDeployer(component);
			deployer.deploy();
		}
		deployContainer(container);
	}
	
	public void addPreparedComponent(IComponentElement component) {
		queue.offer(component);
	}
	
	public IComponentElement[] getPreparedComponents() {
		return queue.toArray();
	}
	
	public void addComponent(IComponentElement component) {
		deployedComponents.add(component);
	}
	
	private void prepare(IContainerElement container) {
		deployedComponents.clear();
		queue.clear();
		List components = container.getComponentList();
		Collections.sort(components, comparator);
		for (int i = 0; i < components.size(); i++) {
			addPreparedComponent(new ComponentElementWrapper(
					(IComponentElement) components.get(i)));
		}
	}
	
	private void deployContainer(IContainerElement container) {
		IContainerElement newContainer = new ContainerElementWrapper(container);
		while (!deployedComponents.isEmpty()) {
			newContainer.addChild((IComponentElement) deployedComponents.remove(0));
		}
		builder.addContainer(newContainer);
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
		
		public void clear() {
			queue.clear();
		}
		
		public IComponentElement[] toArray() {
			return DiconUtil.toComponentArray(queue);
		}
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
