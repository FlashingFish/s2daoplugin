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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.seasar.kijimuna.core.dicon.model.IContainerElement;
import org.seasar.s2daoplugin.cache.DiconModelManager;
import org.seasar.s2daoplugin.cache.IDiconChangeListener;

public class DeploymentDiconModelRegistry implements IDeploymentDiconModelRegistry {

	private DiconModelManager manager;
	private Map containerMap = new HashMap();
	private AffectedContainers affectedContainers = new AffectedContainers();
	
	public DeploymentDiconModelRegistry() {
	}
	
	public void setManager(DiconModelManager manager) {
		this.manager = manager;
	}

	public DiconModelManager getManager() {
		return manager;
	}

	public void initialize() {
	}

	public void diconAdded(IContainerElement container) {
		addContainer(container);
		affectedContainers.addAddedContainer(getContainer(container));
	}

	public void diconUpdated(IContainerElement old, IContainerElement young) {
		IContainerElement removing = getContainer(old);
		addContainer(young);
		affectedContainers.addUpdatedContainer(removing, getContainer(young));
	}

	public void diconRemoved(IContainerElement container) {
		affectedContainers.addRemovedContainer(getContainer(container));
		removeContainer(container);
	}

	public void finishChanged() {
		affectedContainers.fireEvents();
	}
	
	public boolean hasListener(String key) {
		return affectedContainers.hasListener(key);
	}
	
	public void addListener(String key, IDiconChangeListener listener) {
		affectedContainers.addListener(key, listener);
		listener.setManager(getManager());
		fireInitialEvent(listener);
	}
	
	public void removeListener(String key) {
		affectedContainers.removeListener(key);
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
	
	private void fireInitialEvent(IDiconChangeListener listener) {
		listener.initialize();
		for (Iterator it = containerMap.values().iterator(); it.hasNext();) {
			IDeploymentContainer container = (IDeploymentContainer) it.next();
			listener.diconAdded(container.getDeployedContainer());
		}
		listener.finishChanged();
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


	private class AffectedContainers {

		private Map listeners = new HashMap();
		private List containers = new LinkedList(); 
		
		public boolean hasListener(String key) {
			return listeners.containsKey(key);
		}
		
		public void addListener(String key, IDiconChangeListener listener) {
			listeners.put(key, listener);
		}
		
		public void removeListener(String key) {
			listeners.remove(key);
		}
		
		public void addAddedContainer(final IContainerElement container) {
			containers.add(new EventFirer() {
				public void process(IDiconChangeListener listener) {
					listener.diconAdded(container);
				}
			});
		}
		
		public void addUpdatedContainer(final IContainerElement old,
				final IContainerElement young) {
			containers.add(new EventFirer() {
				public void process(IDiconChangeListener listener) {
					listener.diconUpdated(old, young);
				}
			});
		}
		
		public void addRemovedContainer(final IContainerElement container) {
			containers.add(new EventFirer() {
				public void process(IDiconChangeListener listener) {
					listener.diconRemoved(container);
				}
			});
		}
		
		public void fireEvents() {
			try {
				for (int i = 0; i < containers.size(); i++) {
					((EventFirer) containers.get(i)).fire();
				}
				for (Iterator it = listeners.values().iterator(); it.hasNext();) {
					((IDiconChangeListener) it.next()).finishChanged();
				}
			} finally {
				containers.clear();
			}
		}
		
		
		private abstract class EventFirer {
			
			public void fire() {
				for (Iterator it = listeners.values().iterator(); it.hasNext();) {
					process((IDiconChangeListener) it.next());
				}
			}
			
			protected abstract void process(IDiconChangeListener listener);
		}
	}

}
