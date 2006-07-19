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
package org.seasar.s2daoplugin.cache.deployment.deployer;

import org.seasar.kijimuna.core.dicon.model.IComponentElement;
import org.seasar.s2daoplugin.cache.CacheConstants;
import org.seasar.s2daoplugin.cache.deployment.IDeploymentContainer;

public class ComponentDeployerFactory implements CacheConstants {

	private static final IComponentDeployer NULL_DEPLOYER = new IComponentDeployer() {
		public void deploy() {
		}
		public int getType() {
			return TYPE_UNKNOWN;
		}
	};
	
	private IDeploymentContainer container;
	
	public ComponentDeployerFactory(IDeploymentContainer container) {
		if (container == null) {
			throw new IllegalArgumentException();
		}
		this.container = container;
	}
	
	public IComponentDeployer createComponentDeployer(IComponentElement component) {
		if (component == null) {
			return NULL_DEPLOYER;
		}
		String className = component.getComponentClassName();
		if (FILESYSTEM_COMPONENT_AUTO_REGISTER.equals(className)) {
			return new FileSystemComponentAutoRegisterDeployer(container, component);
		} else if (ASPECT_AUTO_REGISTER.equals(className)) {
			return new AspectAutoRegisterDeployer(container, component);
		} else if (INTERFACE_ASPECT_AUTO_REGISTER.equals(className)) {
			return new InterfaceAspectAutoRegisterDeployer(container, component);
		} else if (JAR_COMPONENT_AUTO_REGISTER.equals(className)) {
			return new JarComponentAutoRegisterDeployer(container, component);
		} else if (COMPONENT_AUTO_REGISTER.equals(className)) {
			return new ComponentAutoRegisterDeployer(container, component);
		} else {
			return new DefaultComponentDeployer(container, component);
		}
	}

}
