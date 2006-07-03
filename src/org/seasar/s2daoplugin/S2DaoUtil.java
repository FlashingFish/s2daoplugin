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

import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.seasar.kijimuna.core.dicon.model.IAspectElement;
import org.seasar.kijimuna.core.dicon.model.IComponentElement;
import org.seasar.kijimuna.core.dicon.model.IDiconElement;
import org.seasar.kijimuna.core.rtti.IRtti;
import org.seasar.s2daoplugin.cache.CacheConstants;
import org.seasar.s2daoplugin.cache.cache.IComponentCache;
import org.seasar.s2daoplugin.cache.util.AspectUtil;
import org.seasar.s2daoplugin.cache.util.FlagsUtil;
import org.seasar.s2daoplugin.util.StringUtil;

public class S2DaoUtil implements S2DaoConstants, CacheConstants {

	private static final String EXTENSION = ".sql";
	
	public static String createBaseSqlFileName(IMethod method) {
		if (method == null) {
			return null;
		}
		IType type = method.getDeclaringType();
		return type.getElementName() + "_" + method.getElementName();
	}
	
	public static String createSqlFileName(IMethod method) {
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
	
	public static IComponentCache getS2DaoComponentCache(IProject project) {
		S2DaoNature nature = S2DaoNature.getInstance(project);
		return nature != null ? nature.getComponentCache() : null;
	}
	
	public static boolean isS2DaoInterceptorAppliedMethod(IMethod method) {
		if (method == null || !isOnApplieableType(method)) {
			return false;
		}
		IComponentCache cache = getS2DaoComponentCache(method.getJavaProject().getProject());
		if (cache == null) {
			return false;
		}
		IComponentElement[] components = cache.getComponents(method.getDeclaringType());
		for (int i = 0; i < components.length; i++) {
			List aspects = components[i].getAspectList();
			for (int j = 0; j < aspects.size(); j++) {
				IAspectElement aspect = (IAspectElement) aspects.get(j);
				if (!AspectUtil.hasInterceptor(aspect, getS2DaoInterceptorType(aspect))) {
					continue;
				}
				if (AspectUtil.isApplied(aspect, method)) {
					return true;
				}
			}
		}
		return false;
	}
	
	private static boolean isOnApplieableType(IMethod method) {
		IType type = method.getDeclaringType();
		return FlagsUtil.isInterface(type) || FlagsUtil.isAbstract(method);
	}
	
	private static IType getS2DaoInterceptorType(IDiconElement element) {
		IRtti rtti = element.getRttiLoader().loadRtti(S2DAO_INTERCEPTOR);
		return rtti != null ? rtti.getType() : null;
	}

}
