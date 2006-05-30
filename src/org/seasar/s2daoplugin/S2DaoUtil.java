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

import java.util.StringTokenizer;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.seasar.s2daoplugin.cache.AutoRegisterCache;
import org.seasar.s2daoplugin.cache.CacheComposite;
import org.seasar.s2daoplugin.cache.CacheConstants;
import org.seasar.s2daoplugin.cache.ComponentCache;
import org.seasar.s2daoplugin.cache.DiconModelManager;
import org.seasar.s2daoplugin.cache.IComponentCache;
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
import org.seasar.s2daoplugin.util.StringUtil;

public class S2DaoUtil implements S2DaoConstants, CacheConstants {

	private static final String EXTENSION = ".sql";
	
	static {
		if (!CacheRegistry.isRegistered(S2DAO_CACHE_KEY)) {
			CacheRegistry.registerFactory(
					S2DAO_CACHE_KEY, new S2DaoComponentCacheFactory());
		}
	}
	
	public static String createBaseSqlFileName(IMethod method) {
		if (method == null) {
			return null;
		}
		IType type = method.getCompilationUnit().findPrimaryType();
		return type.getElementName() + "_" + method.getElementName();
	}
	
	public static String createSqlFileName(IMethod method) {
		if (method == null) {
			return null;
		}
		return createBaseSqlFileName(method) + EXTENSION;
	}
	
	public static boolean isValidSqlFileName(IFile file, String filename) {
		if (file == null || StringUtil.isEmpty(filename)) {
			return false;
		}
		for (int i = 0; i < DBMS_SUFFIXES.length; i++) {
			if (file.getName().equals(filename + DBMS_SUFFIXES[i] + EXTENSION)) {
				return true;
			}
		}
		return false;
	}
	
	public static String changeSuffix(String newSuffix, String filename) {
		if (newSuffix == null || StringUtil.isEmpty(filename)) {
			return null;
		}
		boolean extensionDeleted = false;
		if (hasSqlExtension(filename)) {
			filename = removeExtension(filename);
			extensionDeleted = true;
		}
		filename = removeSuffix(filename) + newSuffix;
		return extensionDeleted ? filename + EXTENSION : filename;
	}
	
	// FIXME: ‚à‚Á‚ÆŒµ–§‚É
	public static String[] splitSqlFileName(String filename) {
		if (StringUtil.isEmpty(filename)) {
			return new String[0];
		}
		StringTokenizer st = new StringTokenizer(filename, "_");
		if (st.countTokens() < 2) {
			return new String[0];
		}
		return new String[] {st.nextToken(), removeExtension(st.nextToken())};
	}
	
	private static String removeExtension(String filename) {
		int index = filename.lastIndexOf('.');
		return index != -1 ? filename.substring(0, index) : filename;
	}
	
	private static String removeSuffix(String filename) {
		boolean extensionDeleted = false;
		if (hasSqlExtension(filename)) {
			filename = removeExtension(filename);
			extensionDeleted = true;
		}
		for (int i = 0; i < DBMS_SUFFIXES.length; i++) {
			if (DBMS_SUFFIXES[i].equals(SUFFIX_DEFAULT)) {
				continue;
			}
			if (filename.endsWith(DBMS_SUFFIXES[i])) {
				filename = filename.substring(0, filename.lastIndexOf(DBMS_SUFFIXES[i]));
				break;
			}
		}
		return extensionDeleted ? filename + EXTENSION : filename;
	}
	
	private static boolean hasSqlExtension(String filename) {
		return filename.endsWith(EXTENSION);
	}
	
	public static synchronized IComponentCache getS2DaoComponentCache(IProject project) {
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
			SequentializedListenerChain chain = new SequentializedListenerChain();
			chain.addListener(new SqlMarkerUnmarkingListener());
			chain.addListener(cache);
			chain.addListener(new SqlMarkerMarkingListener());
			manager.addDiconChangeListener(S2DAO_CACHE_KEY, chain);
		}
		return cache;
	}
	
	public static synchronized void removeS2DaoComponentCache(IProject project) {
		CacheNature nature = CacheNature.getInstance(project);
		if (nature == null) {
			return;
		}
		CacheRegistry registry = nature.getCacheRegistry();
		registry.removeComponentCache(S2DAO_CACHE_KEY);
		DiconModelManager manager = nature.getDiconModelManager();
		manager.removeDiconChangeListener(S2DAO_CACHE_KEY);
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
