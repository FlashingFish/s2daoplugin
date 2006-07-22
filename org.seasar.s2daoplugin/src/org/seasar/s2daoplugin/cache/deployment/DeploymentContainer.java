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
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.IType;
import org.seasar.kijimuna.core.dicon.model.IComponentElement;
import org.seasar.kijimuna.core.dicon.model.IContainerElement;
import org.seasar.s2daoplugin.cache.deployment.deployer.ComponentDeployerFactory;
import org.seasar.s2daoplugin.cache.deployment.deployer.ITypeDeployable;
import org.seasar.s2daoplugin.cache.deployment.deployer.IComponentDeployer;
import org.seasar.s2daoplugin.cache.deployment.model.ComponentElementWrapper;
import org.seasar.s2daoplugin.cache.deployment.model.ContainerElementWrapper;
import org.seasar.s2daoplugin.cache.util.DiconUtil;

public class DeploymentContainer implements IDeploymentContainer {

	private static final Comparator comparator = new LineNumberComparator();
	
	private ComponentDeployerFactory factory = new ComponentDeployerFactory(this);
	private IContainerElement originalContainer;
	private IContainerElement deployedContainer;
	private ComponentQueue preparedQueue = new ComponentQueue();
	private ComponentQueue deployedQueue = new ComponentQueue();
	private boolean hasComponentAuto;
	
	private Set autoRegisterDeployers = new LinkedHashSet();
	
	public void setOriginalContainer(IContainerElement originalContainer) {
		this.originalContainer = originalContainer;
	}

	public IContainerElement getOriginalContainer() {
		return originalContainer;
	}
	
	public boolean needsToBuild() {
		for (Iterator it = autoRegisterDeployers.iterator(); it.hasNext();) {
			if (((IComponentDeployer) it.next()).setUp()) {
				return true;
			}
		}
		return false;
	}
	
	public void deploy() {
		prepare();
		doDeploy();
		createDeployedContainer();
	}
	
	public IComponentElement[] deployType(IType type) {
		for (Iterator it = autoRegisterDeployers.iterator(); it.hasNext();) {
			IComponentDeployer deployer = (IComponentDeployer) it.next();
			if (deployer instanceof ITypeDeployable) {
				((ITypeDeployable) deployer).deployType(type);
			} else {
				deployer.deploy();
			}
		}
		IComponentElement[] added = preparedQueue.toArray();
		doDeploy();
		updateDeployedContainer();
		return added;
	}
	
	public void addPreparedComponent(IComponentElement component) {
		preparedQueue.offer(component);
	}
	
	public IComponentElement[] getPreparedComponents() {
		return preparedQueue.toArray();
	}
	
	public void addComponent(IComponentElement component) {
		deployedQueue.offer(component);
	}
	
	public IContainerElement getDeployedContainer() {
		return deployedContainer;
	}
	
	public boolean hasComponentAuto() {
		return hasComponentAuto;
	}
	
	private void prepare() {
		clear();
		List components = originalContainer.getComponentList();
		Collections.sort(components, comparator);
		for (int i = 0; i < components.size(); i++) {
			addPreparedComponent(new ComponentElementWrapper((IComponentElement)
					components.get(i)));
		}
	}

	private void doDeploy() {
		while (!preparedQueue.isEmpty()) {
			IComponentElement component = preparedQueue.poll();
			IComponentDeployer deployer = factory.createComponentDeployer(component);
			deployer.deploy();
			switch (deployer.getType()) {
			case IComponentDeployer.TYPE_COMPONENT_AUTO:
				hasComponentAuto = true;
			case IComponentDeployer.TYPE_COMPONENT_TARGET_AUTO:
				autoRegisterDeployers.add(deployer);
				break;
			}
		}
	}
	
	private void clear() {
		preparedQueue.clear();
		deployedQueue.clear();
		autoRegisterDeployers.clear();
		deployedContainer = null;
		hasComponentAuto = false;
	}
	
	private void createDeployedContainer() {
		IContainerElement newContainer = new ContainerElementWrapper(
				originalContainer);
		addComponentToContainer(newContainer);
		deployedContainer = newContainer;
	}
	
	private void updateDeployedContainer() {
		addComponentToContainer(deployedContainer);
	}
	
	private void addComponentToContainer(IContainerElement container) {
		while (!deployedQueue.isEmpty()) {
			container.addChild(deployedQueue.poll());
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
			return c1.getStartLine() == c2.getStartLine() ? 0 :
				c1.getStartLine() < c2.getStartLine() ? -1 : 1;
		}
		
	}

}
