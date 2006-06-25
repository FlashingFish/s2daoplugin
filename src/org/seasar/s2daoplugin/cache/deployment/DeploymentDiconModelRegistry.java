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
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IPath;
import org.seasar.kijimuna.core.dicon.model.IAspectElement;
import org.seasar.kijimuna.core.dicon.model.IComponentElement;
import org.seasar.kijimuna.core.dicon.model.IContainerElement;
import org.seasar.kijimuna.core.parser.IElement;
import org.seasar.s2daoplugin.cache.DiconModelManager;
import org.seasar.s2daoplugin.cache.util.DiconUtil;

public class DeploymentDiconModelRegistry implements IDeploymentDiconModelRegistry {

	private DeploymentBuilder builder;
	private DiconModelManager manager;
	private Map componentMap = new HashMap();
	private AffectedComponents affectedComponents = new AffectedComponents();
	
	public DeploymentDiconModelRegistry() {
		this(new DeploymentBuilder());
	}
	
	public DeploymentDiconModelRegistry(DeploymentBuilder builder) {
		if (builder == null) {
			throw new IllegalArgumentException();
		}
		builder.setRegistry(this);
		this.builder = builder;
	}
	
	public void addComponent(IComponentElement component) {
		addComponentMap(component.getStorage().getFullPath(), component);
	}
	
	public void removeComponent(IContainerElement container) {
		removeComponentMap(container.getStorage().getFullPath());
	}
	
	public void setManager(DiconModelManager manager) {
		this.manager = manager;
	}

	public DiconModelManager getManager() {
		return manager;
	}

	public void initialize() {
		builder.initialize();
	}

	public void diconAdded(IContainerElement container) {
		builder.build(container);
		affectedComponents.addAddedComponents(getComponents(container));
	}

	public void diconUpdated(IContainerElement old, IContainerElement young) {
		IComponentElement[] olds = getComponents(old);
		builder.clear(old);
		builder.build(young);
		affectedComponents.addUpdatedComponents(olds, getComponents(young));
	}

	public void diconRemoved(IContainerElement container) {
		affectedComponents.addRemovedComponents(getComponents(container));
		builder.clear(container);
	}

	public void finishChanged() {
		System.out.print("\n");
		for (Iterator it = componentMap.values().iterator(); it.hasNext();) {
			Set components = (Set) it.next();
			for (Iterator jt = components.iterator(); jt.hasNext();) {
				IComponentElement component = (IComponentElement) jt.next();
				List aspects = component.getAspectList();
				for (int i = 0; i < aspects.size(); i++) {
					IAspectElement aspect = (IAspectElement) aspects.get(i);
					if (i == 0) System.out.println(component);
					System.out.println("  " + DiconUtil.getChildComponent(aspect));
				}
			}
		}
		builder.finishBuild();
		affectedComponents.fireEvent();
	}
	
	public boolean hasListener(String key) {
		return affectedComponents.hasListener(key);
	}
	
	public void addListener(String key, IDeploymentChangeListener listener) {
		affectedComponents.addListener(key, listener);
		listener.setManager(getManager());
		fireInitialEvent(listener);
	}
	
	public void removeListener(String key) {
		affectedComponents.removeListener(key);
	}
	
	private void fireInitialEvent(IDeploymentChangeListener listener) {
		listener.initialize();
		for (Iterator it = componentMap.values().iterator(); it.hasNext();) {
			listener.diconAdded(DiconUtil.toComponentArray((Set) it.next()));
		}
		listener.finishChanged();
	}
	
	private void addComponentMap(IPath containerPath, IComponentElement component) {
		if (componentMap.containsKey(containerPath)) {
			Set components = getComponentSet(containerPath);
			components.add(component);
		} else {
			Set components = new HashSet();
			components.add(component);
			componentMap.put(containerPath, components);
		}
	}
	
	private void removeComponentMap(IPath containerPath) {
		Set components = getComponentSet(containerPath);
		if (components == null) {
			return;
		}
		components.clear();
		componentMap.remove(containerPath);
	}
	
	private IComponentElement[] getComponents(IContainerElement container) {
		return DiconUtil.toComponentArray(getComponentSet(container));
	}
	
	private Set getComponentSet(IElement element) {
		return getComponentSet(element.getStorage().getFullPath());
	}
	
	private Set getComponentSet(IPath contaienrPath) {
		return (Set) componentMap.get(contaienrPath);
	}

	
	private class AffectedComponents {

		private Map listeners = new HashMap();
		private List containers = new LinkedList(); 
		
		public boolean hasListener(String key) {
			return listeners.containsKey(key);
		}
		
		public void addListener(String key, IDeploymentChangeListener listener) {
			listeners.put(key, listener);
		}
		
		public void removeListener(String key) {
			listeners.remove(key);
		}
		
		public void addAddedComponents(final IComponentElement[] components) {
			containers.add(new EventFirer() {
				public void process(IDeploymentChangeListener listener) {
					listener.diconAdded(components);
				}
			});
		}
		
		public void addUpdatedComponents(final IComponentElement[] olds,
				final IComponentElement[] youngs) {
			containers.add(new EventFirer() {
				public void process(IDeploymentChangeListener listener) {
					listener.diconUpdated(olds, youngs);
				}
			});
		}
		
		public void addRemovedComponents(final IComponentElement[] components) {
			containers.add(new EventFirer() {
				public void process(IDeploymentChangeListener listener) {
					listener.diconRemoved(components);
				}
			});
		}
		
		public void fireEvent() {
			try {
				for (int i = 0; i < containers.size(); i++) {
					((EventFirer) containers.get(i)).fire();
				}
				for (Iterator it = listeners.values().iterator(); it.hasNext();) {
					((IDeploymentChangeListener) it.next()).finishChanged();
				}
			} finally {
				containers.clear();
			}
		}
		
		
		private abstract class EventFirer {
			
			public void fire() {
				for (Iterator it = listeners.values().iterator(); it.hasNext();) {
					process((IDeploymentChangeListener) it.next());
				}
			}
			
			protected abstract void process(IDeploymentChangeListener listener);
		}
	}

}
