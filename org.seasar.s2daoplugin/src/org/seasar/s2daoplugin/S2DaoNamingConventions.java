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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.seasar.s2daoplugin.util.ArrayUtil;
import org.seasar.s2daoplugin.util.JavaProjectUtil;
import org.seasar.s2daoplugin.util.StringUtil;

public class S2DaoNamingConventions implements S2DaoConstants {

	private static final String SQL_EXTENSION = ".sql";
	private static final char SEPARATOR = '_';
	private static final String[][] EMPTY_RESOLVE = new String[0][0]; 

	public static String createSqlFileName(IMethod method) {
		return createBaseSqlFileName(method) + SQL_EXTENSION;
	}
	
	public static String createBaseSqlFileName(IMethod method) {
		if (method == null) {
			return null;
		}
		IType type = method.getDeclaringType();
		return type.getElementName() + SEPARATOR + method.getElementName();
	}
	
	public static boolean isValidSqlFileName(IFile file, String filename) {
		if (file == null || StringUtil.isEmpty(filename)) {
			return false;
		}
		for (int i = 0; i < DBMS_SUFFIXES.length; i++) {
			if (file.getName().equals(filename + DBMS_SUFFIXES[i] +
					SQL_EXTENSION)) {
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
		if (hasExtension(filename)) {
			filename = removeExtension(filename);
			extensionDeleted = true;
		}
		filename = removeSuffix(filename) + newSuffix;
		return extensionDeleted ? filename + SQL_EXTENSION : filename;
	}
	
	/**
	 * SQLファイル名から、DAOの名称と成り得るパターンを全て解決します。
	 * 返却される配列は、最も可能性の高い名称から順に並んでいます。
	 * 
	 * @return {{"PackageName", "DaoTypeName", "MethodName"}, ...}
	 */
	public static String[][] resolveDao(IFile sql) {
		if (sql == null || !sql.getName().toLowerCase().endsWith(SQL_EXTENSION) ||
				!JavaProjectUtil.isInSourceFolder(sql)) {
			return EMPTY_RESOLVE;
		}
		IJavaElement element = JavaCore.create(sql.getParent());
		String packageName = element instanceof IPackageFragment ?
				element.getElementName() : "";
		List ret = new ArrayList();
		String name = sql.getName();
		if (hasSuffix(name)) {
			ret.addAll(createDaoNames(packageName, removeSuffix(removeExtension(
					name))));
		}
		ret.addAll(createDaoNames(packageName, removeExtension(name)));
		String[][] result = new String[ret.size()][];
		for (int i = 0; i < ret.size(); i++) {
			result[i] = (String[]) ret.get(i);
		}
		return result;
	}
	
	private static List createDaoNames(String packageName, String target) {
		List ret = new ArrayList();
		String[] pack = new String[] {packageName};
		for (int i = 0; contains(target, SEPARATOR); i++) {
			String[] typeMethod = target.split(String.valueOf(SEPARATOR), 2);
			typeMethod[0] = i == 0 ? typeMethod[0] :
				((String[]) ret.get(i - 1))[1] + SEPARATOR + typeMethod[0];
			ret.add(ArrayUtil.add(pack, typeMethod));
			target = typeMethod[1];
		}
		return ret;
	}
	
	private static boolean contains(String value, char c) {
		for (int i = 0; i < value.length(); i++) {
			if (c == value.charAt(i)) {
				return true;
			}
		}
		return false;
	}
	
	private static String removeExtension(String value) {
		String extension = getExtension(value);
		return extension == null ? value :
			value.substring(0, value.lastIndexOf(extension) - 1);
	}
	
	private static boolean hasExtension(String value) {
		return getExtension(value) != null;
	}
	
	private static String getExtension(String value) {
		int index = value.lastIndexOf('.');
		return index != -1 ? value.substring(index + 1, value.length()) : null;
	}
	
	private static String removeSuffix(String value) {
		String suffix = getSuffix(value);
		return suffix == null ? value :
			value.substring(0, value.lastIndexOf(suffix));
	}
	
	private static boolean hasSuffix(String value) {
		return getSuffix(value) != null;
	}
	
	private static String getSuffix(String value) {
		String valueWithoutExt = removeExtension(value);
		for (int i = 0; i < DBMS_SUFFIXES.length; i++) {
			if (SUFFIX_DEFAULT.equals(DBMS_SUFFIXES[i])) {
				continue;
			}
			if (valueWithoutExt.endsWith(DBMS_SUFFIXES[i])) {
				return DBMS_SUFFIXES[i];
			}
		}
		return null;
	}

}
