# DataStore

Step1 导入：

```
plugins  id 'com.google.protobuf'
android {
viewBinding{
   enable=true
  }
}
dependencies {
    implementation "androidx.datastore:datastore-preferences:1.0.0-rc01"
    implementation "androidx.datastore:datastore:1.0.0-rc01"
        //这个版本与protobuf 中一致
    implementation "com.google.protobuf:protobuf-java:3.14.0"
}
protobuf {
    protoc {
    //这个版本与dependencies 中一致
        artifact = "com.google.protobuf:protoc:3.14.0"
    }

    generateProtoTasks {
        all().each { task ->
            task.builtins {
                java {
                    option 'lite'
                }
            }
        }
    }
}	

项目根目录中
buildscript{
dependencies{
        classpath 'com.google.protobuf:protobuf-gradle-plugin:0.8.8'
}
}


```



Step2: 在项目中创建proto 文件

- `syntax` ：指定 protobuf 的版本，如果没有指定默认使用 proto2，**必须是.proto文件的除空行和注释内容之外的第一行**
- `option` ：表示一个可选字段
- 在一个 proto 文件中，可以定义多个 message
- `message` 中包含了 3 个字段：一个 string 类型(name)、一个整型类型(age)、一个 bool 类型(followAccount)。**注意** ：`=` 号后面都跟着一个字段编号
- 每个字段由三部分组成：字段类型 + 字段名称 + 字段编号，在 Java 中每个字段会被编译成 Java 对象，其他语言会被编译其他语言类型

```
syntax = "proto3";

option java_package = "com.example.datastore.User";
option java_multiple_files = true;

message UserPreferences {
      string name = 2;
      int32 age = 3;
      string phone = 4;
}
```

![img](https://pic1.zhimg.com/v2-5e55bba075013e240f41de57a5709534_r.jpg)

点击这里查看 [Encoding](https://link.zhihu.com/?target=https%3A//developers.google.com/protocol-buffers/docs/encoding%3Fhl%3Dzh-cn)

```
// 固定的，有proto2，但是proto3更好
syntax = "proto3";
// 格式：包名 + . + 文件名
option java_package = "com.example.datastore.User";
option java_multiple_files = true;
// 格式：字段类型 + 字段名称 + 字段编号 = 编号（唯一）
message UserPreferences {
      string name = 2;
      int32 age = 3;
      string phone = 4;
}
```

Step3: 编译：需要的字段添加好了后，ReBuild Project一下，可以看到自动生成的文件，如下：

Step4:定义一个实现 Serializer 的类

```
object UserSerializer : Serializer<UserPreferences> {
    override val defaultValue: UserPreferences
        get() = UserPreferences.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): UserPreferences {
        return UserPreferences.parseFrom(input)
    }

    override suspend fun writeTo(t: UserPreferences, output: OutputStream) {
        t.writeTo(output)
    }

}
```

Step5:创建数据帮助类

```
val Context.dataStroe by dataStore(
    fileName = "user.pd",
    serializer = UserSerializer
)

object UserHelp {

    suspend fun read(context: Context): Flow<UserPreferences> {
        return context.dataStroe.data.catch {
            if (it is IOException){
                it.printStackTrace()
                emit(UserPreferences.getDefaultInstance())
            }else{
                throw  it
            }
        }
    }

    suspend fun write(context: Context, age: Int, name: String, phont: String) {
        context.dataStroe.updateData {
            it.toBuilder().setAge(age).setName(name).setPhone(phont).build()
        }
    }

}
```

Step6:在使用中

```
class MainActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityMainBinding

    private val mHandle = object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            val it = msg.obj as UserPreferences
            val age = it.age
            val name = it.name
            val phone = it.phone
            viewBinding.tvContent.text = "$age$name$phone"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        viewBinding.btnOne.setOnClickListener {
            val str = viewBinding.etInput.text.toString()
            if (TextUtils.isEmpty(str)) {
                Toast.makeText(this@MainActivity, "数据是空的", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            GlobalScope.launch {
                UserHelp.write(this@MainActivity, 11, "小米", str)
            }
        }
        viewBinding.btnTwo.setOnClickListener {
            GlobalScope.launch {
                val read = UserHelp.read(this@MainActivity)
                read.collect {
                    val obtainMessage = mHandle.obtainMessage()
                    obtainMessage.obj = it
                    mHandle.sendMessage(obtainMessage)
                }
            }
        }
    }
}
```
