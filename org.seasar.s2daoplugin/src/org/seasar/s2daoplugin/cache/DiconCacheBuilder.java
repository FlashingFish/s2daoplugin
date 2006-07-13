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

import org.eclipse.core.resources.IProject;
import org.seasar.kijimuna.core.dicon.DiconNature;
import org.seasar.kijimuna.core.dicon.ModelManager;

public class DiconCacheBuilder {

	private IRawDiconCache cache;
	private boolean modelInitialized;
	private IProject project;
	
	public DiconCacheBuilder(IRawDiconCache cache) {
		this.cache = cache;
	}
	
	public void setProject(IProject project) {
		this.project = project;
	}
	
	public synchronized void buildCache() {
		DiconNature nature = DiconNature.getInstance(project);
		if (nature != null) {
			ModelManager model = nature.getModel();
			if (model == null) {
				return;
			}
			// kijimuna OFF -> ONでモデルが空の時を回避するため。
			// だが完璧ではなく、ビルドされる前にdiconエディタを起動されると
			// そのdiconだけビルドされてしまうのでdirtyになってしまう。
			if (!modelInitialized && !model.isDirty()) {
				model.init(null);
			}
			modelInitialized = true;
			cache.buildModel(model.getContainers(null));
		} else {
			cache.clearModel();
		}
	}

}
