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

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IType;
import org.seasar.kijimuna.core.dicon.model.IComponentElement;
import org.seasar.kijimuna.core.dicon.model.IContainerElement;
import org.seasar.s2daoplugin.cache.AbstractListenerHoldableCache;
import org.seasar.s2daoplugin.cache.IComponentChangeListener;
import org.seasar.s2daoplugin.cache.ITypeChangeEvent;
import org.seasar.s2daoplugin.cache.deployment.model.ComponentElementWrapper;
import org.seasar.s2daoplugin.cache.deployment.model.ContainerElementWrapper;

public class DeploymentDiconCache extends AbstractListenerHoldableCache
		implements IDeploymentDiconCache {

	private Map containerMap = new HashMap();
	private Map componentChangeListenerMap = new HashMap();
	
	public DeploymentDiconCache() {
	}

	public IContainerElement getContainer(IPath path) {
		IDeploymentContainer container = getDeploymentContainer(path);
		return container != null ? container.getDeployedContainer() : null;
	}
	
	public void initialize() {
	}

	public void diconAdded(IContainerElement container) {
		addContainer(container);
		getAffectedContainers().addAddedContainer(getContainer(container));
	}

	public void diconUpdated(IContainerElement old, IContainerElement young) {
		IContainerElement removing = getContainer(old);
		addContainer(young);
		getAffectedContainers().addUpdatedContainer(removing, getContainer(young));
	}

	public void diconRemoved(IContainerElement container) {
		getAffectedContainers().addRemovedContainer(getContainer(container));
		removeContainer(container);
	}

	public void finishChanged() {
		getAffectedContainers().fireEvents();
	}
	
	public void addComponentChangeListener(String key,
			IComponentChangeListener listener) {
		componentChangeListenerMap.put(key, listener);
	}
	
	public void removeComponentChangeListener(String key) {
		componentChangeListenerMap.remove(key);
	}
	
	public boolean hasComponentChangeListener(String key) {
		return componentChangeListenerMap.containsKey(key);
	}
	
	public void typeChanged(ITypeChangeEvent[] events) {
		if (rebuilt()) {
			return;
		}
		for (int i = 0; i < events.length; i++) {
			if (events[i].getEventType() == ITypeChangeEvent.TYPE_ADDED) {
				processTypeAdded(events[i].getType());
			} else if (events[i].getEventType() == ITypeChangeEvent.TYPE_REMOVED) {
				processTypeRemoved(events[i].getFullyQualifiedClassName());
			}
		}
	}
	
	// TODO: AutoRegisterを持っており、追加/削除のあったITypeがClassPatternにマッチ
	// するかどうかで判断すればムダがない。が、すでにdicon自体の変更でビルドされている
	// なら、再度ビルドしてもムダなだけ…
	private void processTypeAdded(IType type) {
		Set ret = new HashSet();
		for (Iterator it = containerMap.values().iterator(); it.hasNext();) {
			IDeploymentContainer container = (IDeploymentContainer) it.next();
			if (container.hasComponentAuto()) {
				ret.addAll(Arrays.asList(container.deployType(type)));
			}
		}
		for (Iterator it = componentChangeListenerMap.values().iterator();
				it.hasNext();) {
			IComponentChangeListener listener = (IComponentChangeListener)
					it.next();
			for (Iterator jt = ret.iterator(); jt.hasNext();) {
				listener.componentAdded((IComponentElement) jt.next());
			}
		}
	}
	
	private void processTypeRemoved(String fullyQualifiedClassName) {
		Set ret = new HashSet();
		for (Iterator it = containerMap.values().iterator(); it.hasNext();) {
			IDeploymentContainer container = (IDeploymentContainer) it.next();
			if (!container.hasComponentAuto()) {
				continue;
			}
			IContainerElement c = container.getDeployedContainer();
			List components = c.getComponentList();
			for (int i = 0; i < components.size(); i++) {
				IComponentElement component = (IComponentElement) components.get(i);
				if (!(component instanceof ComponentElementWrapper) &&
						fullyQualifiedClassName.equals(component
								.getComponentClassName())) {
					ret.add(component);
					((ContainerElementWrapper) c).removeChild(component);
				}
			}
		}
		for (Iterator it = componentChangeListenerMap.values().iterator();
				it.hasNext();) {
			IComponentChangeListener listener = (IComponentChangeListener)
					it.next();
			for (Iterator jt = ret.iterator(); jt.hasNext();) {
				listener.componentRemoved((IComponentElement) jt.next());
			}
		}
	}
	
	protected IContainerElement[] getInitialTargetContainers() {
		IContainerElement[] containers = new IContainerElement[containerMap.size()];
		int i = 0;
		for (Iterator it = containerMap.values().iterator(); it.hasNext();) {
			IDeploymentContainer deployment = (IDeploymentContainer) it.next();
			containers[i++] = deployment.getDeployedContainer();
		}
		return containers;
	}

	private boolean rebuilt() {
		boolean built = false;
		for (Iterator it = containerMap.values().iterator(); it.hasNext();) {
			IDeploymentContainer container = (IDeploymentContainer) it.next();
			if (container.needsToBuild()) {
				diconUpdated(container.getOriginalContainer(),
						container.getOriginalContainer());
				built = true;
			}
		}
		if (built) {
			finishChanged();
		}
		return built;
	}
	
	private void addContainer(IContainerElement container) {
		addContainerMap(container.getStorage().getFullPath(), container);
	}
	
	private void removeContainer(IContainerElement container) {
		removeContainerMap(container.getStorage().getFullPath());
	}
	
	private void addContainerMap(IPath containerPath, IContainerElement container) {
		IDeploymentContainer deployment = getDeploymentContainer(containerPath);
		if (deployment == null) {
			deployment = new DeploymentContainer();
		}
		deployment.setOriginalContainer(container);
		deployment.deploy();
		containerMap.put(containerPath, deployment);
	}
	
	private void removeContainerMap(IPath containerPath) {
		containerMap.remove(containerPath);
	}
	
	private IContainerElement getContainer(IContainerElement container) {
		IDeploymentContainer deployment = getDeploymentContainer(
				container.getStorage().getFullPath());
		return deployment != null ? deployment.getDeployedContainer() : null;
	}
	
	private IDeploymentContainer getDeploymentContainer(IPath containerPath) {
		return (IDeploymentContainer) containerMap.get(containerPath);
	}

}
