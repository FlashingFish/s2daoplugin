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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.seasar.s2daoplugin.cache.AutoRegisterCache;
import org.seasar.s2daoplugin.cache.CacheConstants;
import org.seasar.s2daoplugin.cache.ComponentCache;
import org.seasar.s2daoplugin.cache.ComponentCacheGroup;
import org.seasar.s2daoplugin.cache.DiconModelManager;
import org.seasar.s2daoplugin.cache.IComponentCache;
import org.seasar.s2daoplugin.cache.builder.AspectedComponentCacheBuilder;
import org.seasar.s2daoplugin.cache.builder.AutoAspectedComponentCacheBuilder;
import org.seasar.s2daoplugin.cache.builder.CacheBuilderChain;
import org.seasar.s2daoplugin.cache.builder.ComponentCacheBuilder;
import org.seasar.s2daoplugin.util.StringUtil;

public class S2DaoUtil implements S2DaoConstants, CacheConstants {

	private static final String EXTENSION = ".sql";
	
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
	
	public static String changeSuffix(String suffix, String filename) {
		if (suffix == null || StringUtil.isEmpty(filename)) {
			return null;
		}
		for (int i = 0; i < DBMS_SUFFIXES.length; i++) {
			if (DBMS_SUFFIXES[i].equals(SUFFIX_DEFAULT)) {
				continue;
			}
			if (filename.endsWith(DBMS_SUFFIXES[i] + EXTENSION)) {
				return filename.replaceAll(DBMS_SUFFIXES[i] + EXTENSION, suffix + EXTENSION);
			}
		}
		if (filename.endsWith(EXTENSION)) {
			return filename.replaceAll(EXTENSION, suffix + EXTENSION);
		} else {
			return filename + suffix + EXTENSION;
		}
	}
	
	public static synchronized IComponentCache getS2DaoComponentCache(IProject project) {
		DiconModelManager manager = DiconModelManager.getInstance(project);
		if (manager != null) {
			IComponentCache cache = manager.getComponentCache(S2DAO_COMPONENT_CACHE_KEY);
			return cache != null ? cache :
				manager.addComponentCache(S2DAO_COMPONENT_CACHE_KEY, createS2DaoComponentCache());
		}
		return null;
	}
	
	private static IComponentCache createS2DaoComponentCache() {
		return new ComponentCacheGroup()
				.addComponentCache(new ComponentCache(new CacheBuilderChain()
						.addBuilder(new AspectedComponentCacheBuilder(S2DAO_INTERCEPTOR))
						.addBuilder(new AutoAspectedComponentCacheBuilder(S2DAO_INTERCEPTOR))))
				.addComponentCache(new AutoRegisterCache(new ComponentCacheBuilder(COMPONENT_AUTO_REGISTERS)))
				.addComponentCache(new AutoRegisterCache(new ComponentCacheBuilder(ASPECT_AUTO_REGISTERS)));
	}

}
