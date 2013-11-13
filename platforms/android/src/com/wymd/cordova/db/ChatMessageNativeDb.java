package com.wymd.cordova.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.wymd.cordova.utils.ChatTimeUtil;

/**
 * 互动消息，保存本地数据库 . sqlite默认使用UTF-8的编码方式
 * 
 * @author Administrator
 * 
 */
public class ChatMessageNativeDb {

	private String TAG = "ChatMessageNativeDb";
	public static final String CHAT_DBNAME = "chat_msg_ver3.db";
	public static final String CHAT_MSG_DBNAME = "chat_msg_info_ver3";
	// 保存互动消息其它零碎操作：加载了几条消息-超过18条不予记载
	// public static final String CHAT_MSG_OTHER_DBNAME_OLD =
	// "chat_msg_other_info";
	public static final String CHAT_MSG_OTHER_DBNAME = "chat_msg_other_infos_ver3";
	private SQLiteDatabase db;

	final String creat_table_sql = "create table if not exists "
			+ CHAT_MSG_DBNAME
			+ " (id INTEGER PRIMARY KEY AUTOINCREMENT,"
			+ "merchantId INTEGER, msgId INTEGER, time varchar, logoImgUrl varchar, ower varchar, serverInfo varchar, myType varchar,"
			+ "itemsItemId varchar, itemsItemId0 varchar, itemsItemId1 varchar, itemsItemId2 varchar, itemsItemId3 varchar, itemsItemId4 varchar,"
			+ "itemsTitle varchar, itemsTitle0 varchar, itemsTitle1 varchar, itemsTitle2 varchar, itemsTitle3 varchar, itemsTitle4 varchar,"
			+ "itemsLocation varchar, itemsLocation0 varchar, itemsLocation1 varchar, itemsLocation2 varchar, itemsLocation3 varchar, itemsLocation4 varchar,"
			+ "itemsTime varchar, itemsTime0 varchar, itemsTime1 varchar, itemsTime2 varchar, itemsTime3 varchar, itemsTime4 varchar,"
			+ "itemsImageUrl varchar, itemsImageUrl0 varchar, itemsImageUrl1 varchar, itemsImageUrl2 varchar, itemsImageUrl3 varchar, itemsImageUrl4 varchar,"
			+ "itemsLinkUrl varchar, itemsLinkUrl0 varchar, itemsLinkUrl1 varchar, itemsLinkUrl2 varchar, itemsLinkUrl3 varchar, itemsLinkUrl4 varchar,"
			+ "itemsContent varchar, itemsContent0 varchar, itemsContent1 varchar, itemsContent2 varchar, itemsContent3 varchar, itemsContent4 varchar,"
			+ "itemsType varchar, extInfoType varchar, extInfoContent varchar, extInfoActionName varchar, extInfoServerInfo varchar"
			+ ")";

	final String creat_table_other_sql = "create table if not exists "
			+ CHAT_MSG_OTHER_DBNAME + " (id INTEGER PRIMARY KEY AUTOINCREMENT,"
			+ "merchantId INTEGER, historyMsgCount INTEGER, time datetime"
			+ ")";

	// 共54字段
	final String insert_msg_sql = "insert into "
			+ CHAT_MSG_DBNAME
			+ " (merchantId, msgId, time, logoImgUrl, ower, serverInfo, myType,"
			+ "itemsItemId, itemsItemId0, itemsItemId1, itemsItemId2, itemsItemId3, itemsItemId4,"
			+ "itemsTitle, itemsTitle0, itemsTitle1, itemsTitle2, itemsTitle3, itemsTitle4,"
			+ "itemsLocation, itemsLocation0, itemsLocation1, itemsLocation2, itemsLocation3, itemsLocation4,"
			+ "itemsTime, itemsTime0, itemsTime1, itemsTime2, itemsTime3, itemsTime4,"
			+ "itemsImageUrl, itemsImageUrl0, itemsImageUrl1, itemsImageUrl2, itemsImageUrl3, itemsImageUrl4,"
			+ "itemsLinkUrl, itemsLinkUrl0, itemsLinkUrl1, itemsLinkUrl2, itemsLinkUrl3, itemsLinkUrl4,"
			+ "itemsContent, itemsContent0, itemsContent1, itemsContent2, itemsContent3, itemsContent4,"
			+ "itemsType, extInfoType, extInfoContent, extInfoActionName, extInfoServerInfo)"
			+ " values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

	final String insert_msg_other_sql = "insert into " + CHAT_MSG_OTHER_DBNAME
			+ " (merchantId, historyMsgCount, time)"
			+ " values(?,?, datetime('now','localtime'))";

	final String update_msg_other_sql = "update "
			+ CHAT_MSG_OTHER_DBNAME
			+ " set historyMsgCount=?, time=datetime('now','localtime') where merchantId=?";

	public ChatMessageNativeDb(Context context) {
		db = context.openOrCreateDatabase(CHAT_DBNAME, Context.MODE_PRIVATE,
				null);

		db.execSQL(creat_table_sql);
	}

	// 保存消息集合-判断是否存在-最新一条-获取历史消息时，不在判断保存
	public void saveMsgList(String merchantId,
			List<Map<String, String>> chatListItems) {
		db.execSQL(creat_table_sql);

		Cursor cursor;
		int count = 0;
		cursor = db.rawQuery("SELECT count(*) count from " + CHAT_MSG_DBNAME
				+ " WHERE merchantId = ?", new String[] { merchantId });
		if (cursor.moveToFirst()) {
			count = cursor.getInt(cursor.getColumnIndex("count"));
		}
		Log.d(TAG, "本地数据库中存储的数据数count=" + count);

		if (count > 66) {
			return;
		}

		// 只做保存操作、不做更新、修改操作
		for (int i = 0; i < chatListItems.size(); i++) {
			Map<String, String> map = chatListItems.get(i);
			String msgId = map.get("msgId");
			// 不存在就进行插入操作
			if (!msgIsExist(merchantId, msgId)) {
				insertMsgSql(merchantId, msgId, map);
			}
		}

		// Log.d(TAG, "本地数据库中存储saveMsgList=" + chatListItems);
	}

	// 获取消息集合-进入历史记录保存18条
	public List<Map<String, String>> getMsgList(String merchantId,
			int listItemsSize, String lastMsgId) {
		db.execSQL(creat_table_sql);

		List<Map<String, String>> list = new ArrayList<Map<String, String>>();

		Log.d(TAG, "本地数据库merchantId=" + merchantId + "&listItemsSize="
				+ listItemsSize + "&lastMsgId=" + lastMsgId);
		// 检查最后一条信息的时间，超过半小时，清空数据库，重新保存
		if (getLastMsgTime(merchantId)) {
			clearMsgDB();
			return list;
		}

		Cursor cursor;
		int count = 0;
		cursor = db.rawQuery("SELECT count(*) count from " + CHAT_MSG_DBNAME
				+ " WHERE merchantId = ?", new String[] { merchantId });
		if (cursor.moveToFirst()) {
			count = cursor.getInt(cursor.getColumnIndex("count"));
		}
		Log.d(TAG, "本地数据库中存储的数据数count=" + count);

		// 剩最后三条时，不在从数据库取，返回0条
		if (count - 3 < listItemsSize) {
			return list;
		}

		if (lastMsgId.equals("0")) {

			cursor = db.rawQuery("SELECT * from " + CHAT_MSG_DBNAME
					+ " WHERE merchantId = ? ORDER BY msgId DESC LIMIT 10",
					new String[] { merchantId });

		} else {

			cursor = db
					.rawQuery(
							"SELECT * from "
									+ CHAT_MSG_DBNAME
									+ " WHERE merchantId = ? and msgId < ? ORDER BY msgId DESC LIMIT 6",
							new String[] { merchantId, lastMsgId });
		}

		Map<String, String> map;
		while (cursor.moveToNext()) {
			map = new HashMap<String, String>();

			map.put("msgId", cursor.getString(cursor.getColumnIndex("msgId")));
			map.put("time", cursor.getString(cursor.getColumnIndex("time")));
			map.put("logoImgUrl",
					cursor.getString(cursor.getColumnIndex("logoImgUrl")));
			map.put("serverInfo",
					cursor.getString(cursor.getColumnIndex("serverInfo")));
			map.put("ower", cursor.getString(cursor.getColumnIndex("ower")));
			map.put("myType", cursor.getString(cursor.getColumnIndex("myType")));

			map.put("itemsItemId",
					cursor.getString(cursor.getColumnIndex("itemsItemId")));
			map.put("itemsItemId0",
					cursor.getString(cursor.getColumnIndex("itemsItemId0")));
			map.put("itemsItemId1",
					cursor.getString(cursor.getColumnIndex("itemsItemId1")));
			map.put("itemsItemId2",
					cursor.getString(cursor.getColumnIndex("itemsItemId2")));
			map.put("itemsItemId3",
					cursor.getString(cursor.getColumnIndex("itemsItemId3")));
			map.put("itemsItemId4",
					cursor.getString(cursor.getColumnIndex("itemsItemId4")));

			map.put("itemsTitle",
					cursor.getString(cursor.getColumnIndex("itemsTitle")));
			map.put("itemsTitle0",
					cursor.getString(cursor.getColumnIndex("itemsTitle0")));
			map.put("itemsTitle1",
					cursor.getString(cursor.getColumnIndex("itemsTitle1")));
			map.put("itemsTitle2",
					cursor.getString(cursor.getColumnIndex("itemsTitle2")));
			map.put("itemsTitle3",
					cursor.getString(cursor.getColumnIndex("itemsTitle3")));
			map.put("itemsTitle4",
					cursor.getString(cursor.getColumnIndex("itemsTitle4")));

			map.put("itemsLocation",
					cursor.getString(cursor.getColumnIndex("itemsLocation")));
			map.put("itemsLocation0",
					cursor.getString(cursor.getColumnIndex("itemsLocation0")));
			map.put("itemsLocation1",
					cursor.getString(cursor.getColumnIndex("itemsLocation1")));
			map.put("itemsLocation2",
					cursor.getString(cursor.getColumnIndex("itemsLocation2")));
			map.put("itemsLocation3",
					cursor.getString(cursor.getColumnIndex("itemsLocation3")));
			map.put("itemsLocation4",
					cursor.getString(cursor.getColumnIndex("itemsLocation4")));

			map.put("itemsTime",
					cursor.getString(cursor.getColumnIndex("itemsTime")));
			map.put("itemsTime0",
					cursor.getString(cursor.getColumnIndex("itemsTime0")));
			map.put("itemsTime1",
					cursor.getString(cursor.getColumnIndex("itemsTime1")));
			map.put("itemsTime2",
					cursor.getString(cursor.getColumnIndex("itemsTime2")));
			map.put("itemsTime3",
					cursor.getString(cursor.getColumnIndex("itemsTime3")));
			map.put("itemsTime4",
					cursor.getString(cursor.getColumnIndex("itemsTime4")));

			map.put("itemsImageUrl",
					cursor.getString(cursor.getColumnIndex("itemsImageUrl")));
			map.put("itemsImageUrl0",
					cursor.getString(cursor.getColumnIndex("itemsImageUrl0")));
			map.put("itemsImageUrl1",
					cursor.getString(cursor.getColumnIndex("itemsImageUrl1")));
			map.put("itemsImageUrl2",
					cursor.getString(cursor.getColumnIndex("itemsImageUrl2")));
			map.put("itemsImageUrl3",
					cursor.getString(cursor.getColumnIndex("itemsImageUrl3")));
			map.put("itemsImageUrl4",
					cursor.getString(cursor.getColumnIndex("itemsImageUrl4")));

			map.put("itemsLinkUrl",
					cursor.getString(cursor.getColumnIndex("itemsLinkUrl")));
			map.put("itemsLinkUrl0",
					cursor.getString(cursor.getColumnIndex("itemsLinkUrl0")));
			map.put("itemsLinkUrl1",
					cursor.getString(cursor.getColumnIndex("itemsLinkUrl1")));
			map.put("itemsLinkUrl2",
					cursor.getString(cursor.getColumnIndex("itemsLinkUrl2")));
			map.put("itemsLinkUrl3",
					cursor.getString(cursor.getColumnIndex("itemsLinkUrl3")));
			map.put("itemsLinkUrl4",
					cursor.getString(cursor.getColumnIndex("itemsLinkUrl4")));

			map.put("itemsContent",
					cursor.getString(cursor.getColumnIndex("itemsContent")));
			map.put("itemsContent0",
					cursor.getString(cursor.getColumnIndex("itemsContent0")));
			map.put("itemsContent1",
					cursor.getString(cursor.getColumnIndex("itemsContent1")));
			map.put("itemsContent2",
					cursor.getString(cursor.getColumnIndex("itemsContent2")));
			map.put("itemsContent3",
					cursor.getString(cursor.getColumnIndex("itemsContent3")));
			map.put("itemsContent4",
					cursor.getString(cursor.getColumnIndex("itemsContent4")));

			map.put("itemsType",
					cursor.getString(cursor.getColumnIndex("itemsType")));
			map.put("extInfoType",
					cursor.getString(cursor.getColumnIndex("extInfoType")));
			map.put("extInfoContent",
					cursor.getString(cursor.getColumnIndex("extInfoContent")));
			map.put("extInfoActionName", cursor.getString(cursor
					.getColumnIndex("extInfoActionName")));
			map.put("extInfoServerInfo", cursor.getString(cursor
					.getColumnIndex("extInfoServerInfo")));

			list.add(0, map);
		}

		Log.d(TAG, "本地数据库中获取getMsgList.size()=" + list.size());
		cursor.close();
		return list;
	}

	// 判断是否存在
	private boolean msgIsExist(String merchantId, String msgId) {
		db.execSQL(creat_table_sql);

		Cursor c = db.rawQuery("SELECT * FROM " + CHAT_MSG_DBNAME
				+ " WHERE merchantId = ? and msgId = ?", new String[] {
				merchantId, msgId });

		return c.moveToFirst();
	}

	// 清除消息、清空表-消息保存时间
	public void clearMsgDB() {
		db.execSQL(creat_table_sql);

		db.execSQL("DELETE FROM " + CHAT_MSG_DBNAME);
	}

	// 获取最后一条信息的时间-检查最后一条信息的时间，超过半小时，返回true，else-false
	private boolean getLastMsgTime(String merchantId) {
		db.execSQL(creat_table_other_sql);

		String lastMsgTime = "";

		// String lastMsgId = "";
		// Cursor cursor = db.rawQuery("SELECT * FROM " + CHAT_MSG_DBNAME
		// + " WHERE merchantId = ? ORDER BY msgId ASC LIMIT 1",
		// new String[] { merchantId });
		//
		// if (cursor.moveToFirst()) {
		// lastMsgTime = cursor.getString(cursor.getColumnIndex("time"));
		// lastMsgId = cursor.getString(cursor.getColumnIndex("msgId"));
		// }

		if (msgOtherIsExist(merchantId)) {
			lastMsgTime = getMsgOtherInfo(merchantId);
		} else {
			db.execSQL(insert_msg_other_sql, new Object[] { merchantId, 0 });
			lastMsgTime = getMsgOtherInfo(merchantId);
		}

		if (ChatTimeUtil.getNetTimeForCurrentTime(lastMsgTime)) {

			saveMsgOtherInfo(merchantId, 0);
			return true;
		} else {

			saveMsgOtherInfo(merchantId, 0);
			return false;
		}
		// Log.d(TAG, "本地数据库中存储getCardMsgRemind=" + userCardCount
		// + "&cursor.getCount()" + cursor.getCount());
	}

	// 保存该商号获取过的历史消息数-用于下次进入加载同样的条数，超过18不记录
	public void saveMsgOtherInfo(String merchantId, int getHistoryMsgCount) {
		db.execSQL(creat_table_other_sql);

		// 判断是否存在
		if (msgOtherIsExist(merchantId)) {
			db.execSQL(update_msg_other_sql, new Object[] { getHistoryMsgCount,
					merchantId });
		} else {
			db.execSQL(insert_msg_other_sql, new Object[] { merchantId,
					getHistoryMsgCount });
		}
	}

	// 获取该商号获取过的历史消息数
	public String getMsgOtherInfo(String merchantId) {
		db.execSQL(creat_table_other_sql);

		// Cursor cursor = db.rawQuery("select historyMsgCount from "
		// + CHAT_MSG_OTHER_DBNAME + " where merchantId=?",
		// new String[] { merchantId });
		//
		// int historyMsgCount = 0;
		// if (cursor.moveToFirst() && cursor.getCount() > 0
		// && !cursor.isAfterLast()) {
		// historyMsgCount = cursor.getInt(cursor
		// .getColumnIndex("historyMsgCount"));
		// }

		// if (historyMsgCount == 0) {
		// historyMsgCount = 6;
		// } else if (historyMsgCount > 18) {
		// historyMsgCount = 18;
		// }

		Cursor cursor = db.rawQuery("select time from " + CHAT_MSG_OTHER_DBNAME
				+ " where merchantId=?", new String[] { merchantId });

		String getTime = "";
		if (cursor.moveToFirst() && cursor.getCount() > 0
				&& !cursor.isAfterLast()) {
			getTime = cursor.getString(cursor.getColumnIndex("time"));
		}

		cursor.close();
		return getTime;
	}

	private boolean msgOtherIsExist(String merchantId) {
		db.execSQL(creat_table_other_sql);

		Cursor c = db.rawQuery("SELECT * FROM " + CHAT_MSG_OTHER_DBNAME
				+ " WHERE merchantId = ?", new String[] { merchantId });
		return c.moveToFirst();
	}

	// 清空该商号获取过的历史消息数
	public void clearMsgOtherInfo() {
		db.execSQL(creat_table_other_sql);

		db.execSQL("DELETE FROM " + CHAT_MSG_OTHER_DBNAME);

	}

	public void close() {
		if (db != null) {
			db.close();
		}
	}

	private void insertMsgSql(String merchantId, String msgId,
			Map<String, String> map) {
		db.execSQL(creat_table_sql);

		db.execSQL(
				insert_msg_sql,
				new Object[] {
						merchantId,
						msgId,
						map.get("time"),
						map.get("logoImgUrl"),
						map.get("ower"),
						map.get("serverInfo"),
						map.get("myType"),
						map.containsKey("itemsItemId") ? map.get("itemsItemId")
								: "",
						map.containsKey("itemsItemId0") ? map
								.get("itemsItemId0") : "",
						map.containsKey("itemsItemId1") ? map
								.get("itemsItemId1") : "",
						map.containsKey("itemsItemId2") ? map
								.get("itemsItemId2") : "",
						map.containsKey("itemsItemId3") ? map
								.get("itemsItemId3") : "",
						map.containsKey("itemsItemId4") ? map
								.get("itemsItemId4") : "",
						map.containsKey("itemsTitle") ? map.get("itemsTitle")
								: "",
						map.containsKey("itemsTitle0") ? map.get("itemsTitle0")
								: "",
						map.containsKey("itemsTitle1") ? map.get("itemsTitle1")
								: "",
						map.containsKey("itemsTitle2") ? map.get("itemsTitle2")
								: "",
						map.containsKey("itemsTitle3") ? map.get("itemsTitle3")
								: "",
						map.containsKey("itemsTitle4") ? map.get("itemsTitle4")
								: "",
						map.containsKey("itemsLocation") ? map
								.get("itemsLocation") : "",
						map.containsKey("itemsLocation0") ? map
								.get("itemsLocation0") : "",
						map.containsKey("itemsLocation1") ? map
								.get("itemsLocation1") : "",
						map.containsKey("itemsLocation2") ? map
								.get("itemsLocation2") : "",
						map.containsKey("itemsLocation3") ? map
								.get("itemsLocation3") : "",
						map.containsKey("itemsLocation4") ? map
								.get("itemsLocation4") : "",
						map.containsKey("itemsTime") ? map.get("itemsTime")
								: "",
						map.containsKey("itemsTime0") ? map.get("itemsTime0")
								: "",
						map.containsKey("itemsTime1") ? map.get("itemsTime1")
								: "",
						map.containsKey("itemsTime2") ? map.get("itemsTime2")
								: "",
						map.containsKey("itemsTime3") ? map.get("itemsTime3")
								: "",
						map.containsKey("itemsTime4") ? map.get("itemsTime4")
								: "",
						map.containsKey("itemsImageUrl") ? map
								.get("itemsImageUrl") : "",
						map.containsKey("itemsImageUrl0") ? map
								.get("itemsImageUrl0") : "",
						map.containsKey("itemsImageUrl1") ? map
								.get("itemsImageUrl1") : "",
						map.containsKey("itemsImageUrl2") ? map
								.get("itemsImageUrl2") : "",
						map.containsKey("itemsImageUrl3") ? map
								.get("itemsImageUrl3") : "",
						map.containsKey("itemsImageUrl4") ? map
								.get("itemsImageUrl4") : "",
						map.containsKey("itemsLinkUrl") ? map
								.get("itemsLinkUrl") : "",
						map.containsKey("itemsLinkUrl0") ? map
								.get("itemsLinkUrl0") : "",
						map.containsKey("itemsLinkUrl1") ? map
								.get("itemsLinkUrl1") : "",
						map.containsKey("itemsLinkUrl2") ? map
								.get("itemsLinkUrl2") : "",
						map.containsKey("itemsLinkUrl3") ? map
								.get("itemsLinkUrl3") : "",
						map.containsKey("itemsLinkUrl4") ? map
								.get("itemsLinkUrl4") : "",
						map.containsKey("itemsContent") ? map
								.get("itemsContent") : "",
						map.containsKey("itemsContent0") ? map
								.get("itemsContent0") : "",
						map.containsKey("itemsContent1") ? map
								.get("itemsContent1") : "",
						map.containsKey("itemsContent2") ? map
								.get("itemsContent2") : "",
						map.containsKey("itemsContent3") ? map
								.get("itemsContent3") : "",
						map.containsKey("itemsContent4") ? map
								.get("itemsContent4") : "",
						map.get("itemsType"), map.get("extInfoType"),
						map.get("extInfoContent"),
						map.get("extInfoActionName"),
						map.get("extInfoServerInfo") });

	}

}
