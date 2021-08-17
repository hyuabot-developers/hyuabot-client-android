package app.kobuggi.hyuabot.function

import android.content.Context
import android.content.SharedPreferences
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import androidx.preference.PreferenceManager
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class DatabaseHelper (context: Context){
    private val mContext = context
    private val mDatabaseName = "information.db"
    private var mDatabasePath :String = context.getDatabasePath(mDatabaseName).path
    private val versionPreferencesKey = "1"
    private val assetDbVersionFileName = "app_database.db.version"


    init{

        initialize()
    }

    private fun initialize(){
        // 설정 관리자
        val prefs = PreferenceManager.getDefaultSharedPreferences(mContext)

        // Local 데이터베이스가 있는지 확인하고, 분기를 나눔.
        if(!existsDatabaseLoaded(mContext)){
            // assets 디비 파일이 있는지 체크하고, 있을 시에 복사함
            try {
                if (existsAssetsDatabase(mContext)) {
                    // 오래된 버전의 기기인 경우 databases 디렉토리를 생성 안 하는 버그가 있다. 이 경우를 대비해서 디렉토리를 미리 생성
                    File(mDatabasePath).also{
                            file -> file.parentFile!!.mkdirs()
                    }.createNewFile()
                    copyDatabaseFromAssets(mContext)
                    val assetDbVersion = getAssetDbVersion(mContext)
                    setAppDbVersionPreference(prefs, assetDbVersion)
                }
            } catch (e: Exception){
                debug("[open] Assets 에서 복사 실패 ",e)
            }

        } else {
            // 앱을 실행하거나, 업데이트 했을 때 등의 동작.
            val localDbVersion = getAppDbVersionPreference(prefs)
            val assetDbVersion = getAssetDbVersion(mContext)

            if(localDbVersion < assetDbVersion){
                try {
                    // assets 디비 파일이 있는지 체크하고, 있을 시에 복사함
                    if(existsAssetsDatabase(mContext)) {
                        copyDatabaseFromAssets(mContext)
                        setAppDbVersionPreference(prefs,assetDbVersion)
                    }
                } catch (e: Exception) {
                    debug("[open] Assets 에서 복사 실패  ",e)
                }
            }
        }
    }

    /**
     * 데이터베이스 경로를 리턴.
     */
    fun getDatabasePath() : String{
        return mDatabasePath
    }

    @Suppress("unused")
    private fun getAppDbVersionPreference() : Int{
        val prefs = PreferenceManager.getDefaultSharedPreferences(mContext)
        return getAppDbVersionPreference(prefs)
    }

    @Suppress("unused")
    private fun getAppDbVersionPreference(prefs : SharedPreferences) : Int{
        return prefs.getInt(versionPreferencesKey,0)
    }

    private fun setAppDbVersionPreference(prefs : SharedPreferences, version:Int){
        val editor = prefs.edit()
        editor.putInt(versionPreferencesKey,version)
        editor.apply()
    }

    private fun getAssetDbVersion(context:Context):Int{
        return try {
            Integer.parseInt(readTextFromAssets(context, mDatabaseName))
        } catch (e: Exception) {
            0
        }
    }

    @Suppress("unused")
    private fun getVersionFromDatabaseFile(file: File): Int{
        // 파일의 존재 유무를 먼저 체크한다. 그런데 이미 file 개체로 넘어왔으니 있을 것 같은데..
        return if(file.exists())
        {
            val db: SQLiteDatabase = SQLiteDatabase.openOrCreateDatabase(file,null)
            val version = db.version
            db.close()
            version
        } else {
            0
        }
    }

    private fun existsDatabaseLoaded(context:Context): Boolean{
        val dbFile = context.getDatabasePath(mDatabaseName)
        return dbFile.exists()
    }

    private fun existsAssetsDatabase(context:Context):Boolean {
        return existsAssetFile(context, ".", mDatabaseName)
    }

    @Throws(IOException::class)
    private fun copyDatabaseFromAssets(context:Context){
        copyFromAssets(context, mDatabaseName, mDatabasePath)
    }

    private fun existsAssetFile(context:Context,path:String,fileName:String):Boolean {
        return context.assets.list(path)!!.toList().contains(fileName)
    }

    private fun readTextFromAssets(context:Context, assetURI:String) : String{
        return context.assets.open(assetURI).bufferedReader().use {
            it.readText()
        }
    }

    @Throws(IOException::class)
    private fun copyFromAssets(context:Context, assetURI : String, toPath:String){
        FileOutputStream(toPath).use { out ->
            context.assets.open(assetURI).use {
                it.copyTo(out)
            }
        }
    }

    @Suppress("unused")
    private fun debug(msg: String, msg2 : Any = "") {
        @Suppress("ConstantConditionIf")
        if (isDebug) {
            Log.d(TAG, "$msg $msg2")
        }
    }

    companion object {
        private const val TAG = "[AppDatabaseAssetsHandler]"
        private const val isDebug = false
    }
}