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
import org.seasar.s2daoplugin.cache.deployment.IComponentContainer;

public class ComponentDeployerFactory implements CacheConstants {

	private static final IComponentDeployer NULL_DEPLOYER = new IComponentDeployer() {
		public void deploy() {
		}
	};
	
	private IComponentContainer container;
	
	public ComponentDeployerFactory(IComponentContainer container) {
		if (container == null) {
			throw new IllegalArgumentException();
		}
		this.container = container;
	}
	
	public IComponentDeployer createComponentDeployer(IComponentElement component) {
		if (component == null) {
			return NULL_DEPLOYER;
		}
		if (FILESYSTEM_COMPONENT_AUTO_REGISTER.equals(component.getComponentClassName())){
			return new FileSystemComponentAutoRegisterDeployer(container, component);
		} else if (ASPECT_AUTO_REGISTER.equals(component.getComponentClassName())) {
			return new AspectAutoRegisterDeployer(container, component);
		} else {
			return new DefaultComponentDeployer(container, component);
		}
	}

}
