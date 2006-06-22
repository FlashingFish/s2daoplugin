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
package org.seasar.s2daoplugin.cache;

import java.util.Collection;
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
import org.seasar.s2daoplugin.cache.builder.RegistryBuilder;

public class VirtualDiconModelRegistry implements IVirtualDiconModelRegistry {

	private static final IComponentElement[] EMPTY_COMPONENTS = new IComponentElement[0];
	
	private RegistryBuilder builder;
	private DiconModelManager manager;
	private Map componentMap = new HashMap();
	private AffectedComponents affectedComponents = new AffectedComponents();
	
	public VirtualDiconModelRegistry() {
		this(new RegistryBuilder());
	}
	
	public VirtualDiconModelRegistry(RegistryBuilder builder) {
		if (builder == null) {
			throw new IllegalArgumentException();
		}
		builder.setRegistry(this);
		this.builder = builder;
	}
	
	public void addComponent(IComponentElement component) {
//		System.out.println("add: " + component);
		addComponentMap(component.getStorage().getFullPath(), component);
	}
	
	public void removeComponent(IContainerElement container) {
//		System.out.println("remove: " + container.getStorage().getFullPath());
		removeComponentMap(container.getStorage().getFullPath());
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
//		for (Iterator it = componentMap.values().iterator(); it.hasNext();) {
//			Set components = (Set) it.next();
//			for (Iterator jt = components.iterator(); jt.hasNext();) {
//				IComponentElement component = (IComponentElement) jt.next();
//				List aspects = component.getAspectList();
//				for (int i = 0; i < aspects.size(); i++) {
//					IAspectElement aspect = (IAspectElement) aspects.get(i);
//					if (i == 0) System.out.println(component);
//					System.out.println("  " + DiconUtil.getAvailableComponent(aspect));
//				}
//			}
//		}
		// TODO: ここでリスナ（おそらくIComponentCache）をコールバック
		affectedComponents.fireEvent();
	}
	
	public void addListener(String key, IVirtualDiconChangeListener listener) {
		affectedComponents.addListener(key, listener);
		listener.setManager(getManager());
		fireInitialEvent(listener);
	}
	
	private void fireInitialEvent(IVirtualDiconChangeListener listener) {
		listener.initialize();
		for (Iterator it = componentMap.values().iterator(); it.hasNext();) {
			listener.diconAdded(toComponentArray((Set) it.next()));
		}
		listener.finishChanged();
	}
	
	private IComponentElement[] getComponents(IContainerElement container) {
		return toComponentArray(getComponentSet(container));
	}
	
	private Set getComponentSet(IElement element) {
		return (Set) componentMap.get(element.getStorage().getFullPath());
	}
	
	private IComponentElement[] toComponentArray(Collection collection) {
		return collection != null ? (IComponentElement[]) collection.toArray(
				new IComponentElement[collection.size()]) : EMPTY_COMPONENTS;
	}
	
	public boolean hasDiconChangeListener(String key) {
		return affectedComponents.hasListener(key);
	}
	
	private void addComponentMap(IPath containerPath, IComponentElement component) {
		if (componentMap.containsKey(containerPath)) {
			Set components = (Set) componentMap.get(containerPath);
			components.add(component);
		} else {
			Set components = new HashSet();
			components.add(component);
			componentMap.put(containerPath, components);
		}
	}
	
	private void removeComponentMap(IPath containerPath) {
		Set components = (Set) componentMap.get(containerPath);
		if (components == null) {
			return;
		}
		components.clear();
		componentMap.remove(containerPath);
	}

	
	private class AffectedComponents {

		private Map listeners = new HashMap();
		private List containers = new LinkedList(); 
		
		public boolean hasListener(String key) {
			return listeners.containsKey(key);
		}
		
		public void addListener(String key, IVirtualDiconChangeListener listener) {
			listeners.put(key, listener);
		}
		
		public void addAddedComponents(final IComponentElement[] components) {
			containers.add(new EventFirer() {
				public void process(IVirtualDiconChangeListener listener) {
					listener.diconAdded(components);
				}
			});
		}
		
		public void addUpdatedComponents(final IComponentElement[] olds,
				final IComponentElement[] youngs) {
			containers.add(new EventFirer() {
				public void process(IVirtualDiconChangeListener listener) {
					listener.diconUpdated(olds, youngs);
				}
			});
		}
		
		public void addRemovedComponents(final IComponentElement[] components) {
			containers.add(new EventFirer() {
				public void process(IVirtualDiconChangeListener listener) {
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
					((IVirtualDiconChangeListener) it.next()).finishChanged();
				}
			} finally {
				containers.clear();
			}
		}
		
		
		private abstract class EventFirer {
			
			public void fire() {
				for (Iterator it = listeners.values().iterator(); it.hasNext();) {
					process((IVirtualDiconChangeListener) it.next());
				}
			}
			
			protected abstract void process(IVirtualDiconChangeListener listener);
		}
	}

}
