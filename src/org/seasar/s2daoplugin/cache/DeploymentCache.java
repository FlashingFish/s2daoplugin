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

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IType;
import org.seasar.kijimuna.core.dicon.model.IComponentElement;
import org.seasar.kijimuna.core.dicon.model.IContainerElement;
import org.seasar.kijimuna.core.rtti.IRtti;
import org.seasar.s2daoplugin.cache.builder.ICacheBuilder;
import org.seasar.s2daoplugin.util.ArrayUtil;

public class DeploymentCache implements IComponentCache {

	private ICacheBuilder builder;
	private DiconModelManager manager;
	private Map componentByFqcn = new HashMap();
	private Map fqcnByComponent = new HashMap();
	
	public DeploymentCache(ICacheBuilder builder) {
		if (builder == null) {
			throw new IllegalArgumentException();
		}
		builder.setComponentCache(this);
		this.builder = builder;
	}

	public void setManager(DiconModelManager manager) {
		this.manager = manager;
	}

	public DiconModelManager getManager() {
		return manager;
	}
	
//	public void addDeployment(IDeployment deployment) {
//		if (deployment == null) {
//			return;
//		}
//		IRtti[] keys = getAllTypes(deployment.getComponent());
//		addDeploymentMap(keys, deployment);
//	}

//	public void removeDeployment(IDeployment deployment) {
//		if (deployment == null) {
//			return;
//		}
//		Set fqcns = (Set) fqcnByComponent.get(deployment);
//		if (fqcns == null) {
//			return;
//		}
//		for (Iterator it = fqcns.iterator(); it.hasNext();) {
//			removeDeploymentByFqcn((String) it.next(), deployment);
//		}
//		fqcnByComponent.remove(deployment);
//	}
//	
//	private void removeDeploymentByFqcn(String fqcn, IDeployment deployment) {
//		Set deployments = (Set) componentByFqcn.get(fqcn);
//		if (deployments == null) {
//			return;
//		}
//		deployments.remove(deployment);
//		if (deployments.isEmpty()) {
//			componentByFqcn.remove(fqcn);
//		}
//	}

	public void initialize() {
		builder.initialize();
	}

	public void diconAdded(IContainerElement container) {
		builder.build(DiconUtil.getComponents(container));
	}

	public void diconUpdated(IContainerElement old, IContainerElement young) {
		builder.clear(DiconUtil.getComponents(old));
		builder.build(DiconUtil.getComponents(young));
	}

	public void diconRemoved(IContainerElement container) {
		builder.clear(DiconUtil.getComponents(container));
	}

	public void finishChanged() {
		builder.finishBuild();
	}
	
	protected IRtti getRtti(String fullyQualifiedClassName) {
		return getManager().getRtti(fullyQualifiedClassName);
	}

	
	
	
	
	
	public IComponentElement[] getComponents(IType type) {
		return null;
	}

	public IComponentElement[] getComponents(String fullyQualifiedClassName) {
		return null;
	}

	public IComponentElement[] getAllComponents() {
		return null;
	}

	public void setContainerPath(IPath containerPath) {
	}

	public IPath getContainerPath() {
		return null;
	}

	public IType[] getAllAppliedTypes() {
		return null;
	}

	public boolean contains(IType type) {
		return false;
	}

	public boolean contains(String fullyQualifiedClassName) {
		return false;
	}

	public void addComponent(IComponentElement component) {
		if (component == null) {
			return;
		}
		IRtti[] keys = getAllTypes(component);
		addComponentMap(keys, component);
	}
	
	private IRtti[] getAllTypes(IComponentElement component) {
		IRtti[] classes = RttiUtil.getAllClasses(
				getRtti(component.getComponentClassName()));
		IRtti[] keys = null;
		for (int i = 0; i < classes.length; i++) {
			keys = (IRtti[]) ArrayUtil.add(keys, classes[i].getInterfaces());
		}
		return keys;
	}
	
	private void addComponentMap(IRtti[] keys, IComponentElement component) {
		String[] fqcns = new String[keys.length];
		for (int i = 0; i < keys.length; i++) {
			fqcns[i] = keys[i].getQualifiedName();
			addComponentByFqcn(fqcns[i], component);
		}
		addFqcnByComponent(component, fqcns);
	}
	
	private void addComponentByFqcn(String fqcn, IComponentElement component) {
		if (componentByFqcn.containsKey(fqcn)) {
			Set deployments = (Set) componentByFqcn.get(fqcn);
			deployments.add(component);
		} else {
			Set deployments = new HashSet();
			deployments.add(component);
			componentByFqcn.put(fqcn, deployments);
		}
	}
	
	private void addFqcnByComponent(IComponentElement component, String[] fqcns) {
		fqcnByComponent.put(component, new HashSet(Arrays.asList(fqcns)));
	}
	
	public void removeComponent(IComponentElement component) {
		if (component == null) {
			return;
		}
		Set fqcns = (Set) fqcnByComponent.get(component);
		if (fqcns == null) {
			return;
		}
		for (Iterator it = fqcns.iterator(); it.hasNext();) {
			removeDeploymentByFqcn((String) it.next(), component);
		}
		fqcnByComponent.remove(component);
	}
	
	private void removeDeploymentByFqcn(String fqcn, IComponentElement component) {
		Set deployments = (Set) componentByFqcn.get(fqcn);
		if (deployments == null) {
			return;
		}
		deployments.remove(component);
		if (deployments.isEmpty()) {
			componentByFqcn.remove(fqcn);
		}
	}

	public void clearCache() {
	}

	public IComponentCache getComponentCache(IPath containerPath) {
		return null;
	}

}
