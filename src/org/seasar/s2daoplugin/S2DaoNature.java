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
package org.seasar.s2daoplugin;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
import org.seasar.s2daoplugin.cache.CacheNature;
import org.seasar.s2daoplugin.cache.cache.ComponentCache;
import org.seasar.s2daoplugin.cache.cache.IComponentCache;
import org.seasar.s2daoplugin.cache.cache.factory.CacheRegistry;
import org.seasar.s2daoplugin.cache.cache.factory.IComponentCacheFactory;
import org.seasar.s2daoplugin.cache.cache.filter.AspectFilter;
import org.seasar.s2daoplugin.cache.deployment.IDeploymentChangeListener;
import org.seasar.s2daoplugin.cache.deployment.IDeploymentDiconModelRegistry;
import org.seasar.s2daoplugin.cache.deployment.SequentializedListenerChain;
import org.seasar.s2daoplugin.sqlmarker.SqlMarkerMarkingListener;
import org.seasar.s2daoplugin.sqlmarker.SqlMarkerUnmarkingListener;
import org.seasar.s2daoplugin.util.ProjectUtil;

public class S2DaoNature implements IProjectNature, S2DaoConstants {

	private IProject project;
	
	static {
		if (!CacheRegistry.isRegistered(S2DAO_CACHE_KEY)) {
			CacheRegistry.registerFactory(
					S2DAO_CACHE_KEY, new IComponentCacheFactory() {
						public IComponentCache createComponentCache() {
							return new ComponentCache(new AspectFilter(S2DAO_INTERCEPTOR));
						}
					});
		}
	}
	
	public static S2DaoNature getInstance(IProject project) {
		IProjectNature nature = null;
		try {
			nature = ProjectUtil.getNature(project, ID_S2DAO_NATURE);
		} catch (CoreException e) {
			S2DaoPlugin.log(e);
		}
		return nature instanceof S2DaoNature ? (S2DaoNature) nature : null;
	}
	
	public void configure() throws CoreException {
		ProjectUtil.addBuilder(getProject(), S2DaoConstants.ID_SQL_MARKER_BUILDER);
	}

	public void deconfigure() throws CoreException {
		removeComponentCache();
		ProjectUtil.removeBuilder(getProject(), S2DaoConstants.ID_SQL_MARKER_BUILDER);
	}

	public IProject getProject() {
		return project;
	}

	public void setProject(IProject project) {
		this.project = project;
	}
	
	public IComponentCache getComponentCache() {
		CacheNature nature = CacheNature.getInstance(project);
		if (nature == null) {
			return null;
		}
		IComponentCache cache = nature.getCacheRegistry().getComponentCache(
				S2DAO_CACHE_KEY);
		if (cache == null) {
			return null;
		}
		addListenerIfNecessary(nature, cache);
		return cache;
	}
	
	private void addListenerIfNecessary(CacheNature nature, IComponentCache cache) {
		IDeploymentDiconModelRegistry registry = nature.getDeploymentModelRegistry();
		if (!registry.hasListener(S2DAO_CACHE_KEY)) {
			registry.addListener(S2DAO_CACHE_KEY, createListener(cache));
		}
	}
	
	public void removeComponentCache() {
		CacheNature nature = CacheNature.getInstance(project);
		if (nature == null) {
			return;
		}
		CacheRegistry registry = nature.getCacheRegistry();
		registry.removeComponentCache(S2DAO_CACHE_KEY);
		IDeploymentDiconModelRegistry modelRegistry = nature.getDeploymentModelRegistry();
		modelRegistry.removeListener(S2DAO_CACHE_KEY);
	}
	
	private IDeploymentChangeListener createListener(IComponentCache cache) {
		SequentializedListenerChain listener = new SequentializedListenerChain();
		listener.addListener(new SqlMarkerUnmarkingListener());
		listener.addListener(cache);
		listener.addListener(new SqlMarkerMarkingListener());
		return listener;
	}

}
