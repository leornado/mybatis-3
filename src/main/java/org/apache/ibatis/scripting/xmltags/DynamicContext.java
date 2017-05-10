/*
 *    Copyright 2009-2012 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.scripting.xmltags;

import ognl.OgnlException;
import ognl.OgnlRuntime;
import ognl.PropertyAccessor;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author Clinton Begin
 */
public class DynamicContext {

    public DynamicContext(Configuration configuration, Object parameterObject) {
        if (parameterObject != null && !(parameterObject instanceof Map)) {
            MetaObject metaObject = configuration.newMetaObject(parameterObject);
            bindings = new ContextMap(metaObject);
        } else {
            bindings = new ContextMap(null);
        }

        Properties variables = configuration.getVariables();
        if (variables != null) {
            for (Map.Entry<Object, Object> entry : variables.entrySet()) {
                Object key = entry.getKey();
                if (key == null) continue;

                if (parameterObject instanceof Map) {
                    if (!((Map) parameterObject).containsKey(String.valueOf(key)))
                        ((Map) parameterObject).put(String.valueOf(key), entry.getValue());
                } else {
                    if (!bindings.containsKey(String.valueOf(key)))
                        bindings.put(String.valueOf(key), entry.getValue());
                }
            }
        }
        bindings.put(PARAMETER_OBJECT_KEY, parameterObject);
        bindings.put(DATABASE_ID_KEY, configuration.getDatabaseId());
        bindings.put(VARIABLES_KEY, configuration.getVariables());
    }

    public void appendSql(String sql) {
        sqlBuilder.append(sql);
        sqlBuilder.append(" ");
    }

    public void bind(String name, Object value) {
        bindings.put(name, value);
    }

    public Map<String, Object> getBindings() {
        return bindings;
    }

    public String getSql() {
        return sqlBuilder.toString().trim();
    }

    public int getUniqueNumber() {
        return uniqueNumber++;
    }

    private int uniqueNumber = 0;

    public static final String DATABASE_ID_KEY = "_databaseId";

    public static final String PARAMETER_OBJECT_KEY = "_parameter";

    public static final String VARIABLES_KEY = "_variables";

    private final ContextMap bindings;

    private final StringBuilder sqlBuilder = new StringBuilder();

    static {
        OgnlRuntime.setPropertyAccessor(ContextMap.class, new ContextAccessor());
    }

    static class ContextAccessor implements PropertyAccessor {

        public Object getProperty(Map context, Object target, Object name)
                throws OgnlException {
            Map map = (Map) target;

            Object result = map.get(name);
            if (result != null) {
                return result;
            }

            Object parameterObject = map.get(PARAMETER_OBJECT_KEY);
            Object variables = map.get(VARIABLES_KEY);
            if (parameterObject instanceof Map) {
                Map m = (Map) parameterObject;
                if (!m.containsKey(name) && !(variables instanceof Map) && !((Map) variables).containsKey(m))
                    return null;

                result = m.get(name);
                if (result != null) return result;
            }

            if (variables instanceof Map) {
                return ((Map) variables).get(name);
            }

            return null;
        }

        public void setProperty(Map context, Object target, Object name, Object value)
                throws OgnlException {
            Map map = (Map) target;
            map.put(name, value);
        }

    }

    static class ContextMap extends HashMap<String, Object> {

        public ContextMap(MetaObject parameterMetaObject) {
            this.parameterMetaObject = parameterMetaObject;
        }

        @Override
        public Object get(Object key) {
            String strKey = (String) key;
            if (super.containsKey(strKey)) {
                return super.get(strKey);
            }

            if (parameterMetaObject != null) {
                Object object = parameterMetaObject.getValue(strKey);
                // issue #61 do not modify the context when reading
//        if (object != null) { 
//          super.put(strKey, object);
//        }

                return object;
            }

            return null;
        }

        @Override
        public Object put(String key, Object value) {
            return super.put(key, value);
        }

        private MetaObject parameterMetaObject;

        private static final long serialVersionUID = 2977601501966151582L;

    }

}