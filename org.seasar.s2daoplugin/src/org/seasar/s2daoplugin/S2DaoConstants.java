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

public interface S2DaoConstants {

	String ID_PLUGIN = "org.seasar.s2daoplugin";
	String ID_S2DAO_NATURE = ID_PLUGIN + ".s2daonature";
	String ID_SQL_MARKER_BUILDER = ID_PLUGIN + ".sqlmarkerbuilder";
	String ID_SQL_MARKER = ID_PLUGIN + ".sqlmarker";

	String SUFFIX_DEFAULT = "";
	String SUFFIX_ORACLE = "_oracle";
	String SUFFIX_DB2 = "_db2";
	String SUFFIX_MSSQL = "_mssql";
	String SUFFIX_MYSQL = "_mysql";
	String SUFFIX_POSTGRESQL = "_postgre";
	String SUFFIX_FIREBIRD = "_firebird";
	String SUFFIX_HSQLDB = "_hsql";
	String SUFFIX_DERBY = "_derby";
	String SUFFIX_MAXDB = "_maxdb";
	
	String[] DBMS_SUFFIXES = new String[] {
			SUFFIX_DEFAULT, SUFFIX_ORACLE, SUFFIX_DB2, SUFFIX_MSSQL,
			SUFFIX_MYSQL, SUFFIX_POSTGRESQL, SUFFIX_FIREBIRD, SUFFIX_HSQLDB,
			SUFFIX_DERBY, SUFFIX_MAXDB,
	};

	String S2DAO_CACHE_KEY = ID_PLUGIN + ".s2dao";
	
	String S2DAO_INTERCEPTOR = "org.seasar.dao.interceptors.S2DaoInterceptor";
	String S2DAO_PAGER_INTERCEPTOR = "org.seasar.dao.pager.PagerS2DaoInterceptorWrapper";

}
