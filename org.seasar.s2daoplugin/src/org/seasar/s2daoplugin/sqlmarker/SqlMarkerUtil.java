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
package org.seasar.s2daoplugin.sqlmarker;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.seasar.kijimuna.core.dicon.model.IComponentElement;
import org.seasar.kijimuna.core.rtti.IRtti;
import org.seasar.s2daoplugin.S2DaoConstants;
import org.seasar.s2daoplugin.S2DaoPlugin;
import org.seasar.s2daoplugin.S2DaoSqlFinder;
import org.seasar.s2daoplugin.S2DaoUtil;
import org.seasar.s2daoplugin.cache.IComponentCache;

public class SqlMarkerUtil {

	private static S2DaoSqlFinder finder = new S2DaoSqlFinder();
	
	public static void remarkAll(IProject project) {
		IComponentCache cache = S2DaoUtil.getS2DaoComponentCache(project);
		if (cache == null) {
			return;
		}
		unmarkAll(project);
		IComponentElement[] components = cache.getAllComponents();
		for (int i = 0; i < components.length; i++) {
			IRtti rtti = (IRtti) components[i].getAdapter(IRtti.class);
			// TODO: AutoRegister‚Ìˆ—‚à“ü‚ê‚é
			if (rtti != null && !rtti.getType().isBinary()) {
				mark(rtti.getType());
			}
		}
	}
	
	public static void remark(IType type) {
		unmark(type);
		mark(type);
	}
	
	public static void mark(IType type) {
		if (type == null) {
			return;
		}
		IMethod[] methods = getMethodsHasSql(type);
		for (int i = 0; i < methods.length; i++) {
			mark(methods[i]);
		}
	}
	
	public static void mark(IMethod method) {
		if (method == null) {
			return;
		}
		createMarker(method);
	}
	
	public static void unmarkAll(IProject project) {
		if (project == null) {
			return;
		}
		try {
			project.deleteMarkers(S2DaoConstants.SQL_MARKER,
					false, IResource.DEPTH_INFINITE);
		} catch (CoreException e) {
			S2DaoPlugin.log(e);
		}
	}
	
	public static void unmark(IType type) {
		if (type == null) {
			return;
		}
		try {
			type.getResource().deleteMarkers(S2DaoConstants.SQL_MARKER,
					false, IResource.DEPTH_ZERO);
		} catch (CoreException e) {
			S2DaoPlugin.log(e);
		}
	}
	
	public static void unmark(IMethod method) {
		if (method == null) {
			return;
		}
		try {
			IMarker[] markers = method.getResource().findMarkers(
					S2DaoConstants.SQL_MARKER, false, IResource.DEPTH_ZERO);
			for (int i = 0; i < markers.length; i++) {
				int start = getStart(method);
				int end = start + getLength(method);
				if (start == getStart(markers[i]) && end == getEnd(markers[i])) {
					markers[i].delete();
					return;
				}
			}
		} catch (CoreException e) {
			S2DaoPlugin.log(e);
		}
	}
	
	private static IMethod[] getMethodsHasSql(IType type) {
		IMethod[] methods;
		try {
			methods = type.getMethods();
		} catch (JavaModelException e) {
			return new IMethod[0];
		}
		List result = new LinkedList();
		for (int i = 0; i < methods.length; i++) {
			if (finder.findSqlFiles(methods[i]).length != 0) {
				result.add(methods[i]);
			}
		}
		return (IMethod[]) result.toArray(new IMethod[result.size()]);
	}
	
	private static IMarker createMarker(IMethod method) {
		try {
			IMarker marker = method.getResource().createMarker(S2DaoConstants.SQL_MARKER);
			marker.setAttributes(createMarkerAttributes(method));
			return marker;
		} catch (CoreException e) {
			return null;
		}
	}
	
	private static Map createMarkerAttributes(IMethod method) throws JavaModelException {
		int start = getStart(method);
		int end = start + getLength(method);
		Map map = new HashMap();
		map.put(IMarker.CHAR_START, new Integer(start));
		map.put(IMarker.CHAR_END, new Integer(end));
		return map;
	}
	
	private static int getStart(IMethod method) throws JavaModelException {
		return method.getSourceRange().getOffset();
	}
	
	private static int getLength(IMethod method) throws JavaModelException {
		return method.getSourceRange().getLength();
	}

	private static int getStart(IMarker marker) {
		return marker.getAttribute(IMarker.CHAR_START, -1);
	}
	
	private static int getEnd(IMarker marker) {
		return marker.getAttribute(IMarker.CHAR_END, -1);
	}

}
