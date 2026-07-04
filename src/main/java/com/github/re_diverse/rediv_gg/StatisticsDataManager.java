package com.github.re_diverse.rediv_gg;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

public class StatisticsDataManager {

	public record StatisticsData(int statId, String statKey, long count) { }

	public static boolean createTable() {
		try {
			Connection con = ReDiv_GG.dataSource.getConnection();
			try {
				StringBuilder sql = new StringBuilder();
				sql.append(" CREATE TABLE IF NOT EXISTS StatisticsData ( ");
				sql.append("     stat_id  smallint       NOT NULL     ");
				sql.append("   , stat_key text           NOT NULL     ");
				sql.append("   , count    int            NOT NULL     ");
				sql.append("   , time     timestamp      NULL         ");
				sql.append(" )                                        ");
				PreparedStatement stmt = con.prepareStatement(sql.toString());
				stmt.executeUpdate();
				stmt.close();
				con.close();
				return true;
			} catch (Throwable throwable) {
				if (con != null)
					try {
						con.close();
					} catch (Throwable throwable1) {
						throwable.addSuppressed(throwable1);
					}
				throw throwable;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public List<StatisticsData> reDiverseGG(String sqlFunc, @Nullable Timestamp from, @Nullable Timestamp to, @Nullable String statKey) {
		try {
			Connection con = ReDiv_GG.dataSource.getConnection();
			try {
				String[] funcWhiteList = {"SUM", "AVG", "MIN", "MAX", "COUNT"};
				boolean isValidFunc = false;
				for(String func : funcWhiteList) {
					if(func.equalsIgnoreCase(sqlFunc)) {
						isValidFunc = true;
						break;
					}
				}
				if(!isValidFunc) {
					throw new IllegalArgumentException("Invalid SQL function: " + sqlFunc);
				}
				if(from == null) from = new Timestamp(0L);
				if(to == null) to = new Timestamp(System.currentTimeMillis());
				String sql = "SELECT stat_id             " +
							 "     , stat_key            " +
							 "     , " + sqlFunc + "(count) AS count " +
							 "FROM   StatisticsData      " +
							 "WHERE  time >= ?           " +
							 "AND    time <= ?           ";
				if(statKey != null) {
					sql += "AND stat_key = ?";
				}
				sql += "GROUP BY stat_id, stat_key";
				PreparedStatement stmtStat = con.prepareStatement(sql);
				stmtStat.setTimestamp(1, from);
				stmtStat.setTimestamp(2, to);
				if(statKey != null) {
					stmtStat.setString(3, statKey);
				}
				ResultSet result = stmtStat.executeQuery();
				int statId = 0;
				String statKeyResult = null;
				long count = 0L;
				List<StatisticsData> dataList = new ArrayList<>();
				while (result.next()) {
					statId = result.getInt("stat_id");
					statKeyResult = result.getString("stat_key");
					count = result.getLong("count");
					dataList.add(new StatisticsData(statId, statKeyResult, count));
				}
				result.close();
				stmtStat.close();
				con.close();
				return dataList;
			} catch (Throwable throwable) {
				if (con != null)
					try {
						con.close();
					} catch (Throwable throwable1) {
						throwable.addSuppressed(throwable1);
					}
				throw throwable;
			}
		} catch (SQLException ex) {
			ex.printStackTrace();
			return null;
		}
	}

	public void appendLog(StatisticsData data) {
		ReDiv_GG.instance.getLogger().info("saving StatisticsData");
		try(Connection con = ReDiv_GG.dataSource.getConnection()) {
			try {
				con.setAutoCommit(true);
				StringBuilder sql = new StringBuilder();
				sql.append(" INSERT INTO StatisticsData ( ");
				sql.append("         stat_id  ");
				sql.append("       , stat_key ");
				sql.append("       , count    ");
				sql.append("       , time     ");
				sql.append(" ) VALUES (       ");
				sql.append("        ?         ");
				sql.append("      , ?         ");
				sql.append("      , ?         ");
				sql.append("      , ?         ");
				sql.append(" )                ");
				PreparedStatement stmtIns = con.prepareStatement(sql.toString());
				stmtIns.setInt(1, data.statId);
				stmtIns.setString(2, data.statKey);
				stmtIns.setLong(3, data.count);
				stmtIns.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
				stmtIns.executeUpdate();
				stmtIns.close();
				con.close();
			} catch (Throwable throwable) {
				if (con != null)
					try {
						con.close();
					} catch (Throwable throwable1) {
						throwable.addSuppressed(throwable1);
					}
				throw throwable;
			}
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}
}
