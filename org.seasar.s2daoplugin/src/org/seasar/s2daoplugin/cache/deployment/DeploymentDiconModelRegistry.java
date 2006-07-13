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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.seasar.kijimuna.core.dicon.model.IContainerElement;
import org.seasar.s2daoplugin.cache.AbstractListenerHoldableCache;

public class DeploymentDiconModelRegistry extends AbstractListenerHoldableCache
		implements IDeploymentDiconModelRegistry {

	private Map containerMap = new HashMap();
	
	public DeploymentDiconModelRegistry() {
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
	
	public void typeChanged() {
		for (Iterator it = containerMap.values().iterator(); it.hasNext();) {
			IDeploymentContainer container = (IDeploymentContainer) it.next();
			if (container.hasComponentAuto()) {
				diconUpdated(container.getOriginalContainer(),
						container.getOriginalContainer());
			}
		}
		finishChanged();
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
