package com.wymd.cordova.db;

import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * 新消息冒泡，保存本地数据库
 * 
 * @author Administrator
 * 
 */
public class NewMessageRemindDb {

	private String TAG = "NewMessageRemindDb";
	public static final String CHAT_DBNAME = "chat_msg_ver3.db";
	public static final String NEW_MSG_REMIND_DBNAME = "new_msg_remind_info_ver3";
	private SQLiteDatabase db;

	// 卡包冒泡:1,消息冒泡:2
	// merchantid -商号id，type为1时-默认0
	final String creat_table_sql = "create table if not exists "
			+ NEW_MSG_REMIND_DBNAME
			+ " ( id INTEGER PRIMARY KEY AUTOINCREMENT,"
			+ "merchantId INTEGER, countType INTEGER, msgCount INTEGER" + ")";

	// 取消关注、进入该商号消息数更新
	final String update_msg_merchant_count_sql = "update "
			+ NEW_MSG_REMIND_DBNAME
			+ " set msgCount = ? where merchantId = ? and countType = 2";
	// 卡包消息数更新
	final String update_msg_card_count_sql = "update " + NEW_MSG_REMIND_DBNAME
			+ " set msgCount = ? where merchantId = 0 and countType = 1";

	public NewMessageRemindDb(Context context) {
		db = context.openOrCreateDatabase(CHAT_DBNAME, Context.MODE_PRIVATE,
				null);

		db.execSQL(creat_table_sql);
	}

	// 保存、更改消息数
	public void saveMsgRemind(JSONArray listMsgCount, boolean isCard,
			int msgCount) throws JSONException {
		db.execSQL(creat_table_sql);

		if (isCard) {
			if (isExistCard()) {

				db.execSQL(
						"update "
								+ NEW_MSG_REMIND_DBNAME
								+ " set msgCount = msgCount + ? where merchantId = 0 and countType = 1",
						new String[] { "" + msgCount });
			} else {

				db.execSQL("insert into " + NEW_MSG_REMIND_DBNAME
						+ " (merchantId, countType, msgCount)"
						+ " values(0, 1, ?)", new String[] { "" + msgCount });
			}

			// Log.d(TAG, "本地数据库中存储saveMsgRemind中isCard=" + isCard);
			return;
		}

		// Log.d(TAG, "本地数据库中存储saveMsgRemind中listMsgCount=" + listMsgCount);
		// 遍历商号消息集合，保存相关冒泡消息
		for (int i = 0; i < listMsgCount.length(); i++) {
			JSONObject lastObject = listMsgCount.getJSONObject(i);

			String merchantId = lastObject.getString("merchantId");
			int merchantMsgCount = lastObject.getInt("msgCount");

			String thisResult = isExistMerchant(merchantId);
			// Log.d(TAG, "本地数据库中存储saveMsgRemind中thisResult=" + thisResult);
			if (!thisResult.equals("false")) {
				int thisMsgCountDB = Integer.parseInt(thisResult);
				int lastThisMsgCount = 0;
				if (merchantMsgCount > thisMsgCountDB) {
					lastThisMsgCount = thisMsgCountDB
							+ (merchantMsgCount - thisMsgCountDB);
				} else if (merchantMsgCount <= thisMsgCountDB) {
					lastThisMsgCount = thisMsgCountDB;
				}

				// Log.d(TAG, "本地数据库中存储saveMsgRemind-isExist-lastThisMsgCount="
				// + lastThisMsgCount);
				db.execSQL(update_msg_merchant_count_sql, new Object[] {
						"" + lastThisMsgCount, merchantId });
			} else {
				// Log.d(TAG, "本地数据库中存储saveMsgRemind-Insert-merchantMsgCount="
				// + merchantMsgCount);

				db.execSQL("insert into " + NEW_MSG_REMIND_DBNAME
						+ " (merchantId, countType, msgCount)"
						+ " values(?, 2, ?)", new String[] { merchantId,
						"" + merchantMsgCount });

			}

		}

	}

	// 获取所有商号消息数
	public JSONArray getMerMsgRemind(List<Map<String, String>> merchantIdList)
			throws JSONException {
		db.execSQL(creat_table_sql);

		Cursor cursor = db
				.rawQuery(
						"SELECT * from "
								+ NEW_MSG_REMIND_DBNAME
								+ " WHERE merchantId != 0 and countType = 2 ORDER BY merchantId ASC",
						null);

		JSONArray sqlResult = new JSONArray();
		JSONObject json;
		while (cursor.moveToNext()) {
			json = new JSONObject();

			json.put("merchantId",
					cursor.getString(cursor.getColumnIndex("merchantId")));

			json.put("msgCount",
					cursor.getString(cursor.getColumnIndex("msgCount")));
			sqlResult.put(json);
		}

		JSONArray lastResult = new JSONArray();
		for (int i = 0; i < merchantIdList.size(); i++) {
			String merchantIdParent = merchantIdList.get(i).get("merchantId");

			boolean addFlg = false;
			for (int j = 0; j < sqlResult.length(); j++) {
				json = sqlResult.getJSONObject(j);
				String merchantIdChild = json.getString("merchantId");
				if (merchantIdParent.equals(merchantIdChild)) {
					lastResult.put(json);
					addFlg = true;
					break;
				} else {
					continue;
				}
			}

			// 如果没有找到商号-默认设置消息数为0
			if (!addFlg) {
				json = new JSONObject();
				json.put("merchantId", merchantIdParent);
				json.put("msgCount", "0");
				lastResult.put(json);
				continue;
			}

		}

		// Log.d(TAG, "本地数据库中存储lastResult=" + lastResult);
		cursor.close();
		return lastResult;
	}

	// 获取卡包消息数
	public int getCardMsgRemind() {
		db.execSQL(creat_table_sql);

		int userCardCount = 0;
		Cursor cursor = db.rawQuery("SELECT msgCount userCardCount FROM "
				+ NEW_MSG_REMIND_DBNAME
				+ " WHERE merchantId = 0 and countType = 1", null);

		while (cursor.moveToNext()) {
			userCardCount = cursor.getInt(cursor
					.getColumnIndex("userCardCount"));
		}

		// Log.d(TAG, "本地数据库中存储getCardMsgRemind=" + userCardCount
		// + "&cursor.getCount()" + cursor.getCount());
		cursor.close();
		return userCardCount;
	}

	// 获取新消息总数
	public int getCountMsgRemind() {
		db.execSQL(creat_table_sql);

		int userPushCount = 0;
		Cursor cursor = db.rawQuery("SELECT sum(msgCount) userPushCount FROM "
				+ NEW_MSG_REMIND_DBNAME
				+ " WHERE merchantId != 0 and countType = 2", null);

		if (cursor != null) {
			if (cursor.moveToFirst()) {
				do {
					userPushCount = cursor.getInt(0);
				} while (cursor.moveToNext());
			}
		}

		// while (cursor.moveToNext()) {
		// userPushCount = cursor.getInt(cursor
		// .getColumnIndex("userPushCount"));
		// }

		// Log.d(TAG, "本地数据库中存储getCountMsgRemind=" + userPushCount
		// + "&cursor.getCount()" + cursor.getCount());
		cursor.close();
		return userPushCount;

	}

	// 取消关注、进入该商号消息数置0
	public void thisMerchantMsgRemindDel(String merchantId, boolean isCard,
			int msgCount) {
		db.execSQL(creat_table_sql);
		// db.delete(NEW_MSG_REMIND_DBNAME, "userId=?", new String[] { userId
		// });
		if (null == merchantId) {
			return;
		}

		if (isCard) {

			if (isExistCard()) {
				db.execSQL(update_msg_card_count_sql, new String[] { ""
						+ msgCount });
			} else {
				db.execSQL("insert into " + NEW_MSG_REMIND_DBNAME
						+ " (merchantId, countType, msgCount)"
						+ " values(0, 1, ?)", new String[] { "" + msgCount });
			}

		} else {

			if (!isExistMerchant(merchantId).equals("false")) {
				db.execSQL(update_msg_merchant_count_sql, new Object[] {
						"" + msgCount, merchantId });

				// Log.d(TAG, "本地数据库中存储thisMerchantMsgRemindDel-isExist="
				// + msgCount);
			} else {
				db.execSQL("insert into " + NEW_MSG_REMIND_DBNAME
						+ " (merchantId, countType, msgCount)"
						+ " values(?, 2, ?)", new String[] { merchantId,
						"" + msgCount });

				// Log.d(TAG, "本地数据库中存储thisMerchantMsgRemindDel-Insert="
				// + msgCount);
			}

		}

	}

	// 卡包冒泡是否存在
	private boolean isExistCard() {
		Cursor cursor = db.rawQuery("SELECT * FROM " + NEW_MSG_REMIND_DBNAME
				+ " WHERE merchantId = 0 and countType = 1", null);

		return cursor.moveToFirst();
	}

	// 商号冒泡是否存在
	private String isExistMerchant(String merchantId) {
		Cursor cursor = db.rawQuery("SELECT * FROM " + NEW_MSG_REMIND_DBNAME
				+ " WHERE merchantId = ? and countType = 2",
				new String[] { merchantId });

		if (cursor.moveToFirst()) {
			int msgCount = cursor.getInt(cursor.getColumnIndex("msgCount"));
			return "" + msgCount;
		} else {
			return "false";
		}

	}

	// 清空该商号获取过的历史消息数
	public void clearMsgCountInfo() {
		db.execSQL(creat_table_sql);

		db.execSQL("DELETE FROM " + NEW_MSG_REMIND_DBNAME);
	}

	public void close() {
		if (db != null) {
			db.close();
		}
	}

}
