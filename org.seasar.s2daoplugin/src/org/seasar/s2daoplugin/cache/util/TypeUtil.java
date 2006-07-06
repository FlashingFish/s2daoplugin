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
package org.seasar.s2daoplugin.cache.util;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

public class TypeUtil {

	public static final IType[] EMPTY_TYPES = new IType[0];
	private static final IMethod[] EMPTY_METHODS = new IMethod[0];
	
	public static IType[] findSuperInterfaces(IType type) {
		if (type == null) {
			return EMPTY_TYPES;
		}
		String[] interfaceNames;
		try {
			interfaceNames = type.getSuperInterfaceNames();
		} catch (JavaModelException e) {
			return EMPTY_TYPES;
		}
		Set result = new HashSet();
		for (int i = 0; i < interfaceNames.length; i++) {
			String[][] resolved = null;
			try {
				resolved = type.resolveType(interfaceNames[i]);
			} catch (JavaModelException ignore) {
			}
			if (resolved == null) {
				continue;
			}
			for (int j = 0; j < resolved.length; j++) {
				IType found = findType(type.getJavaProject(),
						resolved[j][0], resolved[j][1]);
				if (found != null) {
					result.add(found);
				}
			}
		}
		return (IType[]) result.toArray(new IType[result.size()]);
	}
	
	public static IMethod[] getMethods(IType type) {
		try {
			return type != null ? type.getMethods() : EMPTY_METHODS;
		} catch (JavaModelException e) {
			return EMPTY_METHODS;
		}
	}
	
	private static IType findType(IJavaProject project, String packageName,
			String typeName) {
		try {
			return project.findType(packageName, typeName);
		} catch (JavaModelException e) {
			return null;
		}
	}

}
