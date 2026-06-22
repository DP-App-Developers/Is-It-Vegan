package com.isitveganapp.data.local;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.isitveganapp.data.model.Ingredient;
import com.isitveganapp.data.model.VeganStatus;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class IngredientDao_Impl implements IngredientDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<Ingredient> __insertionAdapterOfIngredient;

  private final VeganStatusConverter __veganStatusConverter = new VeganStatusConverter();

  private final SharedSQLiteStatement __preparedStmtOfDeleteAll;

  public IngredientDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfIngredient = new EntityInsertionAdapter<Ingredient>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR IGNORE INTO `ingredients` (`id`,`display_name`,`normalized_name`,`aliases`,`vegan_status`,`reason`,`category`) VALUES (nullif(?, 0),?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Ingredient entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getDisplayName());
        statement.bindString(3, entity.getNormalizedName());
        statement.bindString(4, entity.getAliases());
        final String _tmp = __veganStatusConverter.fromVeganStatus(entity.getVeganStatus());
        statement.bindString(5, _tmp);
        statement.bindString(6, entity.getReason());
        statement.bindString(7, entity.getCategory());
      }
    };
    this.__preparedStmtOfDeleteAll = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM ingredients";
        return _query;
      }
    };
  }

  @Override
  public Object insertAll(final List<Ingredient> ingredients,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfIngredient.insert(ingredients);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteAll(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteAll.acquire();
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteAll.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object findByNormalizedName(final String name,
      final Continuation<? super Ingredient> $completion) {
    final String _sql = "SELECT * FROM ingredients WHERE normalized_name = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, name);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Ingredient>() {
      @Override
      @Nullable
      public Ingredient call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfDisplayName = CursorUtil.getColumnIndexOrThrow(_cursor, "display_name");
          final int _cursorIndexOfNormalizedName = CursorUtil.getColumnIndexOrThrow(_cursor, "normalized_name");
          final int _cursorIndexOfAliases = CursorUtil.getColumnIndexOrThrow(_cursor, "aliases");
          final int _cursorIndexOfVeganStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "vegan_status");
          final int _cursorIndexOfReason = CursorUtil.getColumnIndexOrThrow(_cursor, "reason");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final Ingredient _result;
          if (_cursor.moveToFirst()) {
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpDisplayName;
            _tmpDisplayName = _cursor.getString(_cursorIndexOfDisplayName);
            final String _tmpNormalizedName;
            _tmpNormalizedName = _cursor.getString(_cursorIndexOfNormalizedName);
            final String _tmpAliases;
            _tmpAliases = _cursor.getString(_cursorIndexOfAliases);
            final VeganStatus _tmpVeganStatus;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfVeganStatus);
            _tmpVeganStatus = __veganStatusConverter.toVeganStatus(_tmp);
            final String _tmpReason;
            _tmpReason = _cursor.getString(_cursorIndexOfReason);
            final String _tmpCategory;
            _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            _result = new Ingredient(_tmpId,_tmpDisplayName,_tmpNormalizedName,_tmpAliases,_tmpVeganStatus,_tmpReason,_tmpCategory);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object findByAlias(final String alias,
      final Continuation<? super Ingredient> $completion) {
    final String _sql = "SELECT * FROM ingredients WHERE ('|' || aliases || '|') LIKE ('%|' || ? || '|%') LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, alias);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Ingredient>() {
      @Override
      @Nullable
      public Ingredient call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfDisplayName = CursorUtil.getColumnIndexOrThrow(_cursor, "display_name");
          final int _cursorIndexOfNormalizedName = CursorUtil.getColumnIndexOrThrow(_cursor, "normalized_name");
          final int _cursorIndexOfAliases = CursorUtil.getColumnIndexOrThrow(_cursor, "aliases");
          final int _cursorIndexOfVeganStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "vegan_status");
          final int _cursorIndexOfReason = CursorUtil.getColumnIndexOrThrow(_cursor, "reason");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final Ingredient _result;
          if (_cursor.moveToFirst()) {
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpDisplayName;
            _tmpDisplayName = _cursor.getString(_cursorIndexOfDisplayName);
            final String _tmpNormalizedName;
            _tmpNormalizedName = _cursor.getString(_cursorIndexOfNormalizedName);
            final String _tmpAliases;
            _tmpAliases = _cursor.getString(_cursorIndexOfAliases);
            final VeganStatus _tmpVeganStatus;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfVeganStatus);
            _tmpVeganStatus = __veganStatusConverter.toVeganStatus(_tmp);
            final String _tmpReason;
            _tmpReason = _cursor.getString(_cursorIndexOfReason);
            final String _tmpCategory;
            _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            _result = new Ingredient(_tmpId,_tmpDisplayName,_tmpNormalizedName,_tmpAliases,_tmpVeganStatus,_tmpReason,_tmpCategory);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object searchByPrefix(final String prefix,
      final Continuation<? super List<Ingredient>> $completion) {
    final String _sql = "SELECT * FROM ingredients WHERE normalized_name LIKE ? || '%' LIMIT 10";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, prefix);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<Ingredient>>() {
      @Override
      @NonNull
      public List<Ingredient> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfDisplayName = CursorUtil.getColumnIndexOrThrow(_cursor, "display_name");
          final int _cursorIndexOfNormalizedName = CursorUtil.getColumnIndexOrThrow(_cursor, "normalized_name");
          final int _cursorIndexOfAliases = CursorUtil.getColumnIndexOrThrow(_cursor, "aliases");
          final int _cursorIndexOfVeganStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "vegan_status");
          final int _cursorIndexOfReason = CursorUtil.getColumnIndexOrThrow(_cursor, "reason");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final List<Ingredient> _result = new ArrayList<Ingredient>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Ingredient _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpDisplayName;
            _tmpDisplayName = _cursor.getString(_cursorIndexOfDisplayName);
            final String _tmpNormalizedName;
            _tmpNormalizedName = _cursor.getString(_cursorIndexOfNormalizedName);
            final String _tmpAliases;
            _tmpAliases = _cursor.getString(_cursorIndexOfAliases);
            final VeganStatus _tmpVeganStatus;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfVeganStatus);
            _tmpVeganStatus = __veganStatusConverter.toVeganStatus(_tmp);
            final String _tmpReason;
            _tmpReason = _cursor.getString(_cursorIndexOfReason);
            final String _tmpCategory;
            _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            _item = new Ingredient(_tmpId,_tmpDisplayName,_tmpNormalizedName,_tmpAliases,_tmpVeganStatus,_tmpReason,_tmpCategory);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object count(final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM ingredients";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
