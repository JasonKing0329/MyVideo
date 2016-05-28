package com.king.app.video.data.personal;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.king.app.video.model.VideoData;
import com.king.app.video.model.VideoOrder;

import android.util.Log;

public class PersonalSqldroidDAO implements PersonalDataService {

	private final String TAG = "PersonalSqldroidDAO";
	public PersonalSqldroidDAO() {
		try {
			Class.forName("org.sqldroid.SqldroidDriver").newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	private Connection connect(String dbFile) {
		try {
			//Connection connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile);
			Connection connection = DriverManager.getConnection("jdbc:sqldroid:" + dbFile);
			return connection;
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	@Override
	public VideoPersonalData queryPersonalData(String id) {
		Log.d(TAG, "queryPersonalData " + id);
		Connection connection = connect(DatabaseInfor.DB_PATH);

		VideoPersonalData data = null;
		Statement statement = null;
		try {
			statement = connection.createStatement();
			ResultSet set = statement.executeQuery("SELECT * FROM video WHERE v_id = '" + id + "'");
			if (set.next()) {
				data = new VideoPersonalData();
				data.setId(id);
				data.setLastPlayPosition(set.getInt(2));
				data.setScore(set.getInt(3));
				data.setPath(set.getString(4));
				data.setFlag(set.getString(5));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return data;
	}

	@Override
	public List<VideoPersonalData> queryAllPersonalData() {
		Log.d(TAG, "queryAllPersonalData");
		Connection connection = connect(DatabaseInfor.DB_PATH);
		Statement statement = null;
		List<VideoPersonalData> list = null;
		try {
			statement = connection.createStatement();
			ResultSet set = statement.executeQuery("SELECT * FROM video");
			while (set.next()) {
				if (list == null) {
					list = new ArrayList<VideoPersonalData>();
				}
				VideoPersonalData data = new VideoPersonalData();
				data.setId(set.getString(1));
				data.setLastPlayPosition(set.getInt(2));
				data.setScore(set.getInt(3));
				data.setPath(set.getString(4));
				data.setFlag(set.getString(5));
				list.add(data);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return list;
	}

	@Override
	public boolean updatePersonalData(VideoPersonalData item) {
		Log.d(TAG, "updatePersonalData " + item.getPath());
		Connection connection = connect(DatabaseInfor.DB_PATH);

		StringBuffer buffer = new StringBuffer("UPDATE ");
		buffer.append(" video SET v_position= ").append(item.getLastPlayPosition())
				.append(", v_score= ").append(item.getScore())
				.append(", v_path= '").append(item.getPath())
				.append("', v_flag= '").append(item.getFlag())
				.append("' WHERE v_id = '").append(item.getId()).append("'");
		Statement statement = null;
		try {
			statement = connection.createStatement();
			statement.executeUpdate(buffer.toString());
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	@Override
	public boolean deletePersonalData(VideoPersonalData item) {
		Log.d(TAG, "deletePersonalData " + item.getPath());
		Connection connection = connect(DatabaseInfor.DB_PATH);

		String sql = "DELETE FROM video WHERE v_id = '" + item.getId() + "'";
		Statement statement = null;
		try {
			statement = connection.createStatement();
			statement.executeUpdate(sql);

			/**
			 * 删除一个video记录，同时要从从default列表中删除
			 */
			StringBuffer buffer = new StringBuffer("UPDATE ");
			buffer.append(" video_order SET vo_total = vo_total - 1 ")
					.append(" WHERE vo_id = ").append(0);
			statement.executeUpdate(buffer.toString());

			/**
			 * 为所在order列表减去1
			 */
			sql = "SELECT vol_order_id FROM video_order_list WHERE vol_video_id = '" + item.getId() + "'";
			ResultSet set = statement.executeQuery(sql);
			List<Integer> idList = new ArrayList<Integer>();
			while (set.next()) {
				idList.add(set.getInt(1));
			}
			set.close();
			for (int i = 0; i < idList.size(); i ++) {
				buffer = new StringBuffer("UPDATE ");
				buffer.append(" video_order SET vo_total = vo_total - 1 ")
						.append(" WHERE vo_id = ").append(idList.get(i));
				statement.executeUpdate(buffer.toString());
			}

			/**
			 * 最后从所在列表中删除
			 */
			sql = "DELETE FROM video_order_list WHERE vol_video_id = '" + item.getId() + "'";
			statement.executeUpdate(sql);
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	@Override
	public boolean addPersonalData(VideoPersonalData item) {
		Log.d(TAG, "addPersonalData " + item.getPath());
		Connection connection = connect(DatabaseInfor.DB_PATH);

		String sql = "INSERT INTO video (v_id, v_position, v_score, v_path, v_flag)" +
				" VALUES(?,?,?,?,?)";
		PreparedStatement stmt = null;
		Statement statement = null;
		try {
			stmt = connection.prepareStatement(sql);
			stmt.setString(1, item.getId());
			stmt.setInt(2, item.getLastPlayPosition());
			stmt.setInt(3, item.getScore());
			stmt.setString(4, item.getPath());
			stmt.setString(5, item.getFlag());

			stmt.executeUpdate();
			stmt.close();

			/**
			 * 每增加一条记录，要在default列表中增加统计
			 */
			statement = connection.createStatement();
			StringBuffer buffer = new StringBuffer("UPDATE ");
			buffer.append(" video_order SET vo_total = vo_total + 1 ")
					.append(" WHERE vo_id = ").append(0);
			statement.executeUpdate(buffer.toString());
			statement.close();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} finally {
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		return false;
	}
	@Override
	public List<VideoOrder> queryAllVideoOrders() {
		Log.d(TAG, "queryAllVideoOrders");
		Connection connection = connect(DatabaseInfor.DB_PATH);
		Statement statement = null;
		List<VideoOrder> list = null;
		try {
			statement = connection.createStatement();
			ResultSet set = statement.executeQuery("SELECT * FROM video_order");
			while (set.next()) {
				if (list == null) {
					list = new ArrayList<VideoOrder>();
				}
				VideoOrder data = new VideoOrder();
				data.setId(set.getInt(1));
				data.setName(set.getString(2));
				data.setTotal(set.getInt(3));
				data.setFlag(set.getString(4));
				list.add(data);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return list;
	}
	@Override
	public boolean updateVideoOrder(VideoOrder order) {
		Log.d(TAG, "updateVideoOrder " + order.getName());
		Connection connection = connect(DatabaseInfor.DB_PATH);

		StringBuffer buffer = new StringBuffer("UPDATE ");
		buffer.append(" video_order SET vo_name= '").append(order.getName())
				.append("', vo_total= ").append(order.getTotal())
				.append(", vo_flag= '").append(order.getFlag())
				.append("' WHERE vo_id = ").append(order.getId());
		Statement statement = null;
		try {
			statement = connection.createStatement();
			statement.executeUpdate(buffer.toString());
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	@Override
	public boolean deleteVideoOrder(VideoOrder order) {
		Log.d(TAG, "deleteVideoOrder " + order.getName());
		Connection connection = connect(DatabaseInfor.DB_PATH);

		String sql = "DELETE FROM video_order WHERE vo_id = " + order.getId();
		Statement statement = null;
		try {
			statement = connection.createStatement();
			statement.executeUpdate(sql);

			/**
			 * 删除一个order记录，同时要删掉order list
			 */
			sql = "DELETE FROM video_order_list WHERE vol_order_id = " + order.getId();
			statement.executeUpdate(sql);
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	@Override
	public boolean addVideoOrder(VideoOrder order) {
		Log.d(TAG, "addVideoOrder " + order.getName());
		Connection connection = connect(DatabaseInfor.DB_PATH);

		PreparedStatement stmt = null;
		Statement statement = null;
		try {
			//name should be unique
			String sql = "SELECT * FROM video_order WHERE vo_name = '" + order.getName() + "'";
			statement = connection.createStatement();
			ResultSet set = statement.executeQuery(sql);
			if (set.next()) {
				statement.close();
				return false;
			}
			statement.close();

			sql = "INSERT INTO video_order (vo_name, vo_total, vo_flag)" +
					" VALUES(?,?,?)";
			stmt = connection.prepareStatement(sql);
			stmt.setString(1, order.getName());
			stmt.setInt(2, order.getTotal());
			stmt.setString(3, order.getFlag());

			stmt.executeUpdate();
			stmt.close();

			/**
			 * 添加完成后将ID赋值，以便添加后的delete/update操作
			 */
			statement = connection.createStatement();
			set = statement.executeQuery("SELECT vo_id FROM video_order WHERE vo_name='" + order.getName() + "'");
			if (set.next()) {
				order.setId(set.getInt(1));
			}
			statement.close();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} finally {
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		return false;
	}
	@Override
	public boolean addVideoToOrder(VideoData videoData, VideoOrder order) {
		Log.d(TAG, "addVideoToOrder " + videoData.getPath() + " to " + order.getName());
		Connection connection = connect(DatabaseInfor.DB_PATH);

		//prevent add repeat data
		PreparedStatement stmt = null;
		Statement statement = null;
		try {
			//prevent add repeat data
			String sql = "SELECT * FROM video_order_list WHERE vol_video_id = '"
					+ videoData.getId() + "' AND vol_order_id = " + order.getId();
			statement = connection.createStatement();
			ResultSet set = statement.executeQuery(sql);
			if (set.next()) {
				statement.close();
				return false;
			}

			sql = "INSERT INTO video_order_list (vol_video_id, vol_order_id)" +
					" VALUES(?,?)";
			stmt = connection.prepareStatement(sql);
			stmt.setString(1, videoData.getId());
			stmt.setInt(2, order.getId());

			stmt.executeUpdate();
			stmt.close();

			/**
			 * 相应列表统计加1
			 */
			statement = connection.createStatement();
			StringBuffer buffer = new StringBuffer("UPDATE ");
			buffer.append(" video_order SET vo_total = vo_total + 1 ")
					.append(" WHERE vo_id = ").append(order.getId());
			statement.executeUpdate(buffer.toString());
			statement.close();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} finally {
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		return false;
	}
	@Override
	public boolean deleteVideoFromOrder(VideoData videoData, VideoOrder order) {
		Log.d(TAG, "deleteVideoFromOrder " + videoData.getPath() + " from " + order.getName());
		Connection connection = connect(DatabaseInfor.DB_PATH);

		String sql = "DELETE FROM video_order_list WHERE vol_video_id = '" + videoData.getId()
				+ "' AND vol_order_id = " + order.getId();
		Statement statement = null;
		try {
			statement = connection.createStatement();
			statement.executeUpdate(sql);

			/**
			 * 相应列表统计减1
			 */
			statement = connection.createStatement();
			StringBuffer buffer = new StringBuffer("UPDATE ");
			buffer.append(" video_order SET vo_total = vo_total - 1 ")
					.append(" WHERE vo_id = ").append(order.getId());
			statement.executeUpdate(buffer.toString());
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	@Override
	public List<VideoPersonalData> queryVideoFromOrder(VideoOrder order) {
		Log.d(TAG, "queryVideoFromOrder " + order.getName());
		Connection connection = connect(DatabaseInfor.DB_PATH);
		Statement statement = null;
		List<VideoPersonalData> list = null;
		try {
			statement = connection.createStatement();
			ResultSet set = statement.executeQuery("SELECT v.* FROM video v INNER JOIN video_order_list vol" +
					" WHERE v.v_id = vol.vol_video_id AND vol.vol_order_id = " + order.getId());
			while (set.next()) {
				if (list == null) {
					list = new ArrayList<VideoPersonalData>();
				}
				VideoPersonalData data = new VideoPersonalData();
				data.setId(set.getString(1));
				data.setLastPlayPosition(set.getInt(2));
				data.setScore(set.getInt(3));
				data.setPath(set.getString(4));
				data.setFlag(set.getString(5));
				list.add(data);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return list;
	}
	@Override
	public boolean addVideosToOrder(List<VideoData> list, VideoOrder order) {
		Log.d(TAG, "addVideosToOrder size=" + list.size() + " to " + order.getName());
		Connection connection = connect(DatabaseInfor.DB_PATH);

		try {

			String insertSql = "INSERT INTO video_order_list (vol_video_id, vol_order_id)" +
					" VALUES(?,?)";
			PreparedStatement stmt = null;
			stmt = connection.prepareStatement(insertSql);

			String querySql = "SELECT * FROM video_order_list WHERE vol_video_id = ? AND vol_order_id = ?";
			PreparedStatement statement = connection.prepareStatement(querySql);
			int count = 0;
			for (int i = 0; i < list.size(); i ++) {
				//prevent add repeat data
				statement.setString(1, list.get(i).getId());
				statement.setInt(2, order.getId());
				ResultSet set = statement.executeQuery();
				if (set.next()) {
					continue;
				}

				stmt.setString(1, list.get(i).getId());
				stmt.setInt(2, order.getId());
				stmt.executeUpdate();
				count ++;
			}
			stmt.close();
			statement.close();

			/**
			 * 相应列表统计加1
			 */
			String updateSql = "UPDATE video_order SET vo_total = vo_total + " + count + " WHERE vo_id = ?";
			statement = connection.prepareStatement(updateSql);
			statement.setInt(1, order.getId());
			statement.executeUpdate();
			statement.close();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} finally {
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		return false;
	}
	@Override
	public void deleteVideosFromOrder(List<VideoData> list, VideoOrder order) {
		Log.d(TAG, "deleteVideosFromOrder size=" + list.size() + " to " + order.getName());
		Connection connection = connect(DatabaseInfor.DB_PATH);

		String sql = "DELETE FROM video_order_list WHERE vol_video_id = ? AND vol_order_id = ?";
		PreparedStatement statement = null;
		try {
			statement = connection.prepareStatement(sql);
			for (int i = 0; i < list.size(); i ++) {
				statement.setString(1, list.get(i).getId());
				statement.setInt(2, order.getId());
				statement.executeUpdate();
			}
			statement.executeUpdate();

			/**
			 * 相应列表统计减1
			 */
			sql = "UPDATE video_order SET vo_total = vo_total - " + list.size() + " WHERE vo_id = ?";
			statement = connection.prepareStatement(sql);
			statement.setInt(1, order.getId());
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

}
