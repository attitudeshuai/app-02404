/**
 * 数据访问层（DAO）：所有数据库操作必须防止 SQL 注入。
 *
 * <h2>规范</h2>
 * <ul>
 *   <li>仅使用 {@link java.sql.PreparedStatement}，SQL 中占位符使用 {@code ?}。</li>
 *   <li>所有动态值通过 {@code setObject}/{@code setString}/{@code setLong}/{@code setInt} 等绑定，禁止将用户输入或任意字符串拼接到 SQL 中（包括 {@code +}、{@code StringBuilder}、{@code String.format} 等）。</li>
 *   <li>动态条件（如可选筛选）通过固定子句 {@code AND col = ?} 与参数列表实现，不拼接列名或值。</li>
 * </ul>
 */
package com.hospital.dao;
