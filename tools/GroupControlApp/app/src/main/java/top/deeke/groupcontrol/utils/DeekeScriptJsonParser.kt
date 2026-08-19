package top.deeke.groupcontrol.utils

import org.json.JSONArray
import org.json.JSONObject

class DeekeScriptJsonParser {
    
    data class DeekeScriptMethod(
        val title: String,
        val jsFile: String,
        val icon: String? = null,
        val settingPage: JSONObject? = null,
        val hidden: Boolean = false,
        val runType: String? = null,
        val packageName: String? = null,
        val columns: Int? = null,
        val autoOpen: Boolean? = null
    )
    
    /**
     * 解析deekeScript.json文件，提取所有methods对象中的title和jsFile
     */
    fun parseDeekeScriptJson(jsonString: String): List<DeekeScriptMethod> {
        val methods = mutableListOf<DeekeScriptMethod>()
        
        try {
            val jsonObject = JSONObject(jsonString)
            println("JSON解析开始")
            
            // 递归遍历JSON对象，查找所有methods数组
            findMethodsInJson(jsonObject, methods)
            
        } catch (e: Exception) {
            println("JSON解析出错: ${e.message}")
            e.printStackTrace()
        }
        
        println("总共解析到${methods.size}个methods")
        return methods
    }
    
    /**
     * 递归遍历JSON对象，查找所有methods数组
     */
    private fun findMethodsInJson(jsonObject: Any, methods: MutableList<DeekeScriptMethod>) {
        when (jsonObject) {
            is JSONObject -> {
                // 检查当前对象是否有methods数组
                if (jsonObject.has("methods")) {
                    val methodsArray = jsonObject.getJSONArray("methods")
                    println("找到methods数组，包含${methodsArray.length()}个methods")
                    for (i in 0 until methodsArray.length()) {
                        val method = methodsArray.getJSONObject(i)
                        val parsedMethod = parseMethod(method)
                        println("解析method: ${parsedMethod.title} -> ${parsedMethod.jsFile}")
                        methods.add(parsedMethod)
                    }
                }
                
                // 递归遍历所有键值对
                val keys = jsonObject.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val value = jsonObject.get(key)
                    findMethodsInJson(value, methods)
                }
            }
            is JSONArray -> {
                // 遍历数组中的每个元素
                for (i in 0 until jsonObject.length()) {
                    findMethodsInJson(jsonObject.get(i), methods)
                }
            }
        }
    }
    
    /**
     * 解析单个method对象
     */
    private fun parseMethod(method: JSONObject): DeekeScriptMethod {
        return DeekeScriptMethod(
            title = method.optString("title", ""),
            jsFile = method.optString("jsFile", ""),
            icon = if (method.has("icon")) method.getString("icon") else null,
            settingPage = method.optJSONObject("settingPage"),
            hidden = method.optBoolean("hidden", false),
            runType = if (method.has("runType")) method.getString("runType") else null,
            packageName = if (method.has("packageName")) method.getString("packageName") else null,
            columns = if (method.has("columns")) method.getInt("columns") else null,
            autoOpen = if (method.has("autoOpen")) method.getBoolean("autoOpen") else null
        )
    }
    
    
    /**
     * 将DeekeScriptMethod转换为JSON字符串
     */
    fun DeekeScriptMethod.toJsonString(): String {
        val json = JSONObject()
        json.put("title", title)
        json.put("jsFile", jsFile)
        icon?.let { json.put("icon", it) }
        settingPage?.let { json.put("settingPage", it) }
        json.put("hidden", hidden)
        runType?.let { json.put("runType", it) }
        packageName?.let { json.put("packageName", it) }
        columns?.let { json.put("columns", it) }
        autoOpen?.let { json.put("autoOpen", it) }
        return json.toString()
    }
}
