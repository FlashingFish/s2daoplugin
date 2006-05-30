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
import org.seasar.s2daoplugin.cache.AutoRegisterCache;
import org.seasar.s2daoplugin.cache.CacheComposite;
import org.seasar.s2daoplugin.cache.CacheConstants;
import org.seasar.s2daoplugin.cache.ComponentCache;
import org.seasar.s2daoplugin.cache.DiconModelManager;
import org.seasar.s2daoplugin.cache.IComponentCache;
import org.seasar.s2daoplugin.cache.IDiconChangeListener;
import org.seasar.s2daoplugin.cache.SequentializedListenerChain;
import org.seasar.s2daoplugin.cache.builder.CacheBuilderChain;
import org.seasar.s2daoplugin.cache.builder.ComponentCacheBuilder;
import org.seasar.s2daoplugin.cache.builder.ExtractionCacheBuilder;
import org.seasar.s2daoplugin.cache.builder.filter.AndFilterChain;
import org.seasar.s2daoplugin.cache.builder.filter.AspectFilter;
import org.seasar.s2daoplugin.cache.builder.filter.AutoRegisterAppiedFilter;
import org.seasar.s2daoplugin.cache.builder.filter.ClassNameFilter;
import org.seasar.s2daoplugin.cache.builder.filter.ExtractionFilter;
import org.seasar.s2daoplugin.cache.builder.filter.IComponentFilter;
import org.seasar.s2daoplugin.cache.builder.filter.InterceptorFilter;
import org.seasar.s2daoplugin.cache.builder.filter.PropertyFilter;
import org.seasar.s2daoplugin.cache.factory.CacheRegistry;
import org.seasar.s2daoplugin.cache.factory.IComponentCacheFactory;
import org.seasar.s2daoplugin.cache.project.CacheNature;
import org.seasar.s2daoplugin.sqlmarker.SqlMarkerMarkingListener;
import org.seasar.s2daoplugin.sqlmarker.SqlMarkerUnmarkingListener;
import org.seasar.s2daoplugin.util.ProjectUtil;

public class S2DaoNature implements IProjectNature, S2DaoConstants {

	private IProject project;
	
	static {
		if (!CacheRegistry.isRegistered(S2DAO_CACHE_KEY)) {
			CacheRegistry.registerFactory(
					S2DAO_CACHE_KEY, new S2DaoComponentCacheFactory());
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
		CacheRegistry registry = nature.getCacheRegistry();
		IComponentCache cache = registry.getComponentCache(S2DAO_CACHE_KEY);
		if (cache == null) {
			return null;
		}
		DiconModelManager manager = nature.getDiconModelManager();
		if (!manager.hasListener(S2DAO_CACHE_KEY)) {
			manager.addDiconChangeListener(S2DAO_CACHE_KEY, createListener(cache));
		}
		return cache;
	}
	
	public void removeComponentCache() {
		CacheNature nature = CacheNature.getInstance(project);
		if (nature == null) {
			return;
		}
		CacheRegistry registry = nature.getCacheRegistry();
		registry.removeComponentCache(S2DAO_CACHE_KEY);
		DiconModelManager manager = nature.getDiconModelManager();
		manager.removeDiconChangeListener(S2DAO_CACHE_KEY);
	}
	
	private IDiconChangeListener createListener(IComponentCache cache) {
		SequentializedListenerChain listener = new SequentializedListenerChain();
		listener.addListener(new SqlMarkerUnmarkingListener());
		listener.addListener(cache);
		listener.addListener(new SqlMarkerMarkingListener());
		return listener;
	}
	
	
	private static class S2DaoComponentCacheFactory implements IComponentCacheFactory {

		private IComponentFilter createAspectAutoRegisterFilter() {
			return new AndFilterChain()
					.addFilter(new ClassNameFilter(CacheConstants.ASPECT_AUTO_REGISTERS))
					.addFilter(new PropertyFilter("interceptor", new InterceptorFilter(new ClassNameFilter(S2DAO_INTERCEPTOR))));
		}

		public IComponentCache createComponentCache() {
			return new CacheComposite()
					.addComponentCache(new ComponentCache(new CacheBuilderChain()
							.addBuilder(new ComponentCacheBuilder(new AspectFilter(new InterceptorFilter(new ClassNameFilter(S2DAO_INTERCEPTOR)))))
							.addBuilder(new ExtractionCacheBuilder(new AutoRegisterAppiedFilter(new ExtractionFilter(createAspectAutoRegisterFilter()))))))
					.addComponentCache(new AutoRegisterCache(new ExtractionCacheBuilder(new AndFilterChain()
							.addFilter(new ExtractionFilter(new ClassNameFilter(CacheConstants.COMPONENT_AUTO_REGISTERS)))
							.addFilter(new ExtractionFilter(createAspectAutoRegisterFilter())))));
		}
	}

}
