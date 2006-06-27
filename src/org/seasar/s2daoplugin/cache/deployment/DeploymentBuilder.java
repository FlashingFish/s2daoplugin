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

import org.seasar.kijimuna.core.dicon.model.IContainerElement;

public class DeploymentBuilder {

	private IDeploymentDiconModelRegistry registry;
	private IDeploymentContainer dc = new DeploymentContainer(this);
	
	public void setRegistry(IDeploymentDiconModelRegistry registry) {
		this.registry = registry;
	}
	
	public void initialize() {
	}

	public void build(IContainerElement container) {
		dc.deploy(container);
	}

	public void clear(IContainerElement container) {
		registry.removeContainer(container);
	}

	public void finishBuild() {
	}
	
	public void addContainer(IContainerElement container) {
		registry.addContainer(container);
	}

}