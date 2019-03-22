/**
 * Project: mybatis-3
 * Source file: CachedSqlLogger.java
 * Create At 2019年03月22日 22:51
 * Create By 龚云
 */
package org.apache.ibatis.logging.jdbc;

import org.apache.ibatis.logging.Log;

/**
 * @author 龚云
 */
public class CachedSqlLogger extends BaseJdbcLogger {

    public CachedSqlLogger(Log log, int queryStack) {
        super(log, queryStack);
    }

    public void debugCachedSql(String sql) {
        debug(" Cached Sql: " + removeBreakingWhitespace(sql), true);
    }

}
